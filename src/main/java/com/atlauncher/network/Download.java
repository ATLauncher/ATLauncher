/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.NewInstanceInstaller;
import com.google.gson.Gson;

import org.zeroturnaround.zip.ZipUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class Download {
    public static final int MAX_ATTEMPTS = 3;

    // pre request
    private String url;
    private String friendlyFileName;
    public Path to;
    public Path extractedTo;
    public Path copyTo;
    private String hash;
    private List<String> checksums;
    public long size = -1L;
    private NewInstanceInstaller instanceInstaller;
    private OkHttpClient httpClient = Network.CLIENT;
    private boolean usesPackXz = false;

    // generated on/after request
    private Response response;

    public Download() {

    }

    public static Download build() {
        return new Download();
    }

    public String asString() {
        try {
            this.execute();

            return this.response.body().string();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public <T> T asClass(Class<T> tClass, Gson gson) {
        try {
            if (this.to != null) {
                this.downloadFile();
                return gson.fromJson(new InputStreamReader(Files.newInputStream(this.to)), tClass);
            }

            this.execute();

            return gson.fromJson(this.response.body().charStream(), tClass);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public <T> T asClass(Class<T> tClass) {
        return asClass(tClass, Gsons.MINECRAFT);
    }

    public <T> T asType(Type tClass, Gson gson) {
        try {
            if (this.to != null) {
                this.downloadFile();
                return gson.fromJson(new InputStreamReader(Files.newInputStream(this.to)), tClass);
            }

            this.execute();

            return gson.fromJson(this.response.body().charStream(), tClass);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public <T> T asType(Type tClass) {
        return asType(tClass, Gsons.MINECRAFT);
    }

    public Download downloadTo(Path to) {
        this.to = to;

        return this;
    }

    public Download hash(String hash) {
        this.hash = hash;

        return this;
    }

    public Download size(long size) {
        this.size = size;

        return this;
    }

    public Download copyTo(Path copyTo) {
        this.copyTo = copyTo;

        return this;
    }

    public Download setUrl(String url) {
        this.url = url;
        return this;
    }

    public Download withInstanceInstaller(NewInstanceInstaller instanceInstaller) {
        this.instanceInstaller = instanceInstaller;
        return this;
    }

    public Download withHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public Download usesPackXz(List<String> checksums) {
        this.usesPackXz = true;
        this.extractedTo = this.to;
        this.checksums = checksums;
        this.url = this.url + ".pack.xz";
        this.to = this.to.resolveSibling(this.to.getFileName().toString() + ".pack.xz");
        return this;
    }

    public Download withFriendlyFileName(String friendlyFileName) {
        this.friendlyFileName = friendlyFileName;
        return this;
    }

    private void execute() throws IOException {
        Request.Builder builder = new Request.Builder().url(this.url).addHeader("User-Agent", Network.USER_AGENT);

        this.response = httpClient.newCall(builder.build()).execute();

        if (this.response == null || !this.response.isSuccessful()) {
            throw new IOException(this.url + " request wasn't successful: " + this.response);
        }
    }

    public int code() {
        try {
            this.execute();
            return this.response.code();
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            return -1;
        }
    }

    private boolean md5() {
        return this.hash == null || this.hash.length() != 40;
    }

    private String getHashFromURL() throws IOException {
        this.execute();

        String etag = this.response.header("ETag");
        if (etag == null) {
            return "-";
        }

        if (etag.startsWith("\"") && etag.endsWith("\"")) {
            etag = etag.substring(1, etag.length() - 1);
        }

        return etag.matches("[A-Za-z0-9]{32}") ? etag : "-";
    }

    public String getHash() {
        if (this.hash == null || this.hash.isEmpty()) {
            try {
                this.hash = this.getHashFromURL();
            } catch (Exception e) {
                LogManager.logStackTrace(e);
                this.hash = "-";
            }
        }

        return this.hash;
    }

    public long getFilesize() {
        try {
            if (this.size == -1L) {
                this.execute();
                long size = Long.parseLong(this.response.header("Content-Length"));

                if (size == -1L) {
                    this.size = 0L;
                } else {
                    this.size = size;
                }
            }
        } catch (Exception ignored) {
            return -1;
        }

        return this.size;
    }

    public boolean needToDownload() {
        if (this.to == null) {
            return true;
        }

        if (this.usesPackXz && Files.exists(this.extractedTo)) {
            return this.checksumsMatch();
        }

        if (Files.exists(this.to)) {
            if (this.to.toFile().length() == this.getFilesize()) {
                return false;
            }

            if (this.md5()) {
                return !Hashing.md5(this.to).equals(Hashing.HashCode.fromString(this.getHash()));
            } else {
                return !Hashing.sha1(this.to).equals(Hashing.HashCode.fromString(this.getHash()));
            }
        }

        return true;
    }

    private boolean checksumsMatch() {
        try {
            if (!ZipUtil.containsEntry(this.extractedTo.toFile(), "checksums.sha1")) {
                return true;
            }

            return !this.checksums.contains(
                    Hashing.sha1(ZipUtil.unpackEntry(this.extractedTo.toFile(), "checksums.sha1")).toString());
        } catch (Exception e) {
            return true;
        }
    }

    private void downloadDirect() {
        try (FileChannel fc = FileChannel.open(this.to, Utils.WRITE);
                ReadableByteChannel rbc = Channels.newChannel(this.response.body().byteStream())) {
            fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    private boolean downloadRec(int attempt) {
        if (attempt <= MAX_ATTEMPTS) {
            Hashing.HashCode fileHash = Hashing.HashCode.EMPTY;
            if (Files.exists(this.to)) {
                if (this.md5()) {
                    fileHash = Hashing.md5(this.to);
                } else {
                    fileHash = Hashing.sha1(this.to);
                }
            }

            if (fileHash.equals(Hashing.HashCode.fromString(this.getHash()))) {
                return true;
            }

            if (Files.exists(this.to)) {
                FileUtils.delete(this.to);
            }

            this.downloadDirect();
        } else {
            return false;
        }

        try {
            this.execute();
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            return false;
        }

        return this.downloadRec(attempt + 1);
    }

    public void copy() {
        if (this.copyTo != null) {
            if (Files.exists(this.copyTo)) {
                FileUtils.delete(this.copyTo);
            }

            FileUtils.createDirectory(this.copyTo.getParent());
            FileUtils.copyFile(this.to, this.copyTo, true);
        }
    }

    public void downloadFile() throws IOException {
        if (!this.needToDownload()) {
            if (this.copyTo != null) {
                Hashing.HashCode fileHash = Hashing.HashCode.EMPTY;
                if (Files.exists(this.copyTo)) {
                    if (this.md5()) {
                        fileHash = Hashing.md5(this.to);
                    } else {
                        fileHash = Hashing.sha1(this.to);
                    }
                }

                if (!fileHash.equals(Hashing.HashCode.fromString(this.getHash()))) {
                    this.copy();
                }
            }
            return;
        }

        this.execute();

        Path oldPath = null;
        if (Files.exists(this.to)) {
            oldPath = this.to.resolveSibling(this.to.getFileName().toString() + ".bak");
            FileUtils.moveFile(this.to, oldPath, true);
        }

        if (this.instanceInstaller != null && this.instanceInstaller.isCancelled()) {
            return;
        }

        if (Files.exists(this.to) && Files.isRegularFile(this.to)) {
            FileUtils.delete(this.to);
        }

        if (!Files.exists(this.to.getParent())) {
            FileUtils.createDirectory(this.to.getParent());
        }

        Hashing.HashCode expected = Hashing.HashCode.fromString(this.getHash());
        if (expected.equals(Hashing.HashCode.EMPTY)) {
            this.downloadDirect();
        } else {
            boolean downloaded = this.downloadRec(1);

            if (!downloaded) {
                FileUtils.copyFile(this.to, FileSystem.FAILED_DOWNLOADS);
                LogManager.error("Error downloading " + this.to.getFileName() + " from " + this.url + ". Expected"
                        + " hash of " + expected.toString() + " but got " + this.getHash() + " instead. Copied to "
                        + "FailedDownloads folder & cancelling install!");
                if (this.instanceInstaller != null) {
                    this.instanceInstaller.cancel(true);
                }
            }

            if (downloaded && this.copyTo != null) {
                Hashing.HashCode fileHash2 = Hashing.HashCode.EMPTY;
                if (Files.exists(this.copyTo)) {
                    if (this.md5()) {
                        fileHash2 = Hashing.md5(this.to);
                    } else {
                        fileHash2 = Hashing.sha1(this.to);
                    }
                }

                if (!fileHash2.equals(expected)) {
                    this.copy();
                }
            }
        }

        if (this.usesPackXz) {
            Utils.unXZPackFile(this.to.toFile(), this.extractedTo.toFile());
        }

        if (oldPath != null && Files.exists(oldPath)) {
            FileUtils.delete(oldPath);
        }
    }

    public String getPrintableFileName() {
        if (this.friendlyFileName != null) {
            return this.friendlyFileName;
        }

        return this.to.getFileName().toString();
    }
}
