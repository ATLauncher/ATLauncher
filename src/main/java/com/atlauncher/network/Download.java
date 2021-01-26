/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.Gson;

import org.zeroturnaround.zip.ZipUtil;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class Download {
    public static final int MAX_ATTEMPTS = 3;

    // pre request
    String url;
    private String friendlyFileName;
    public Path to;
    public Path unzipTo;
    public Path extractedTo;
    public Path copyTo;
    private boolean ignoreFailures = false;
    private String hash;
    private Long fingerprint = null;
    public long size = -1L;
    public InstanceInstaller instanceInstaller;
    private OkHttpClient httpClient = Network.CLIENT;
    private RequestBody post = null;
    private CacheControl cacheControl = null;
    private Map<String, String> headers = new HashMap<String, String>();

    // generated on/after request
    public Response response;

    public Download() {

    }

    public static Download build() {
        return new Download();
    }

    public boolean exists() {
        try {
            if (this.response == null) {
                this.execute();
            }

            return this.response.isSuccessful();
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            if (this.response != null) {
                this.response.close();
                this.response = null;
            }
        }

        return false;
    }

    public String asString() {
        try {
            this.execute();

            return this.response.body().string();
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            if (this.response != null) {
                this.response.close();
                this.response = null;
            }
        }

        return null;
    }

    public <T> T asClassWithThrow(Class<T> tClass, Gson gson) throws IOException {
        if (this.to != null) {
            if (this.needToDownload()) {
                this.downloadFile();
            }

            try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(this.to))) {
                return gson.fromJson(isr, tClass);
            }
        }

        this.execute();

        return gson.fromJson(this.response.body().charStream(), tClass);
    }

    public <T> T asClass(Class<T> tClass, Gson gson) {
        try {
            return asClassWithThrow(tClass, gson);
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            if (this.response != null) {
                this.response.close();
                this.response = null;
            }
        }

        return null;
    }

    public <T> T asClass(Class<T> tClass) {
        return asClass(tClass, Gsons.MINECRAFT);
    }

    public <T> T asClassWithThrow(Class<T> tClass) throws IOException {
        return asClassWithThrow(tClass, Gsons.MINECRAFT);
    }

    public <T> T asType(Type tClass, Gson gson) {
        try {
            if (this.to != null) {
                if (this.needToDownload()) {
                    this.downloadFile();
                }

                try (InputStreamReader isr = new InputStreamReader(Files.newInputStream(this.to))) {
                    return gson.fromJson(isr, tClass);
                }
            }

            this.execute();

            return gson.fromJson(this.response.body().charStream(), tClass);
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            if (this.response != null) {
                this.response.close();
                this.response = null;
            }
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

    public Download post(RequestBody post) {
        this.post = post;

        return this;
    }

    public Download unzipTo(Path unzipTo) {
        this.unzipTo = unzipTo;

        return this;
    }

    public Download ignoreFailures() {
        this.ignoreFailures = true;

        return this;
    }

    public Download hash(String hash) {
        this.hash = hash;

        return this;
    }

    public Download fingerprint(long fingerprint) {
        this.fingerprint = fingerprint;

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

    public Download header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public Download cached() {
        return cached(null);
    }

    public Download cached(CacheControl cacheControl) {
        this.httpClient = Network.CACHED_CLIENT;
        this.cacheControl = cacheControl;
        return this;
    }

    public Download withInstanceInstaller(InstanceInstaller instanceInstaller) {
        this.instanceInstaller = instanceInstaller;
        return this;
    }

    public Download withHttpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }

    public Download withFriendlyFileName(String friendlyFileName) {
        this.friendlyFileName = friendlyFileName;
        return this;
    }

    private void execute() throws IOException {
        // connection is already open, so close it first
        if (this.response != null) {
            this.response.close();
        }

        Request.Builder builder = new Request.Builder().url(this.url);

        if (this.post != null) {
            builder.post(this.post);
        }

        if (this.headers.size() != 0) {
            builder.headers(Headers.of(this.headers));
        }

        if (this.cacheControl != null) {
            builder.cacheControl(this.cacheControl);
        }

        this.response = httpClient.newCall(builder.build()).execute();

        if (this.response == null || (!this.ignoreFailures && !this.response.isSuccessful())) {
            throw new DownloadException(this);
        }
    }

    public int code() {
        try {
            if (this.response == null) {
                this.execute();
            }
            return this.response.code();
        } catch (Exception e) {
            LogManager.logStackTrace(e);

            if (this.response != null) {
                this.response.close();
                this.response = null;
            }

            return -1;
        }
    }

    private boolean md5() {
        return this.hash != null && this.hash.length() == 32;
    }

    private boolean sha512() {
        return this.hash != null && this.hash.length() == 128;
    }

    public int getResponseCode() throws IOException {
        if (this.response == null) {
            this.execute();
        }

        return this.response.code();
    }

    private String getHashFromURL() throws IOException {
        if (this.response == null) {
            this.execute();
        }

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
                if (this.response == null) {
                    this.execute();
                }
                long size = Long.parseLong(this.response.header("Content-Length"));

                if (size == -1L) {
                    this.size = 0L;
                } else {
                    this.size = size;
                }
            }
        } catch (Exception ignored) {
            if (this.response != null) {
                this.response.close();
                this.response = null;
            }
            return -1;
        }

        return this.size;
    }

    public boolean needToDownload() {
        if (this.to == null) {
            return true;
        }

        if (Files.exists(this.to)) {
            // if we're ignoring failures and the file is not 0 size, then we're fine
            if (this.ignoreFailures && this.to.toFile().length() != 0) {
                return false;
            }

            if (this.fingerprint != null) {
                try {
                    if (Hashing.murmur(this.to) == this.fingerprint) {
                        return false;
                    }
                } catch (IOException e) {
                    LogManager.error("Error getting murmur hash");
                    return false;
                }
            } else if (this.md5() && Hashing.md5(this.to).equals(Hashing.HashCode.fromString(this.getHash()))) {
                return false;
            } else if (this.sha512() && Hashing.sha512(this.to).equals(Hashing.HashCode.fromString(this.getHash()))) {
                return false;
            } else if (Hashing.sha1(this.to).equals(Hashing.HashCode.fromString(this.getHash()))) {
                return false;
            }

            // if no hash, but filesizes match, then no need to download
            if ((this.hash == null || this.hash.equals("-")) && this.to.toFile().length() == this.getFilesize()) {
                return false;
            }
        }

        return true;
    }

    private void downloadDirect() {
        if (size == -1L) {
            size = this.getFilesize();

            if (instanceInstaller != null && size > 0L) {
                instanceInstaller.addBytesToDownload(size);
            }
        }
        try (FileChannel fc = FileChannel.open(this.to, Utils.WRITE);
                ReadableByteChannel rbc = Channels.newChannel(this.response.body().byteStream())) {
            fc.transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to download file " + this.to, e, false);
        }
    }

    private boolean hashMatches() {
        if (Files.exists(this.to)) {
            if (this.fingerprint != null) {
                try {
                    return Hashing.murmur(this.to) == this.fingerprint;
                } catch (IOException e) {
                    LogManager.error("Error getting murmur hash");
                    return false;
                }
            } else if (this.md5()) {
                return Hashing.md5(this.to).equals(Hashing.HashCode.fromString(this.getHash()));
            } else if (this.sha512()) {
                return Hashing.sha512(this.to).equals(Hashing.HashCode.fromString(this.getHash()));
            } else {
                return Hashing.sha1(this.to).equals(Hashing.HashCode.fromString(this.getHash()));
            }
        }

        return false;
    }

    private boolean downloadRec(int attempt) {
        if (attempt > MAX_ATTEMPTS) {
            return false;
        }

        // if file exists, delete it
        if (Files.exists(this.to)) {
            FileUtils.delete(this.to);
        }

        // if already opened or not first attempt, open the connection
        if (this.response == null || attempt != 1) {
            try {
                this.execute();
            } catch (IOException e) {
                LogManager.logStackTrace(e);

                if (this.response != null) {
                    this.response.close();
                    this.response = null;
                }

                return false;
            }
        }

        // download the file to disk
        this.downloadDirect();

        // check if the hash matches (or they're ignored and file isn't 0 bytes)
        if ((this.ignoreFailures && this.to.toFile().length() != 0) || hashMatches()) {
            return true;
        }

        // if the hash doesn't match, attempt again
        LogManager.debug("Failed downloading " + this.url + " on attempt " + attempt);
        return this.downloadRec(attempt + 1);
    }

    public void copy() {
        if (this.copyTo != null) {
            if (Files.exists(this.copyTo)) {
                if (hashMatches()) {
                    return;
                }

                FileUtils.delete(this.copyTo);
            }

            if (!Files.isDirectory(this.copyTo.getParent())) {
                FileUtils.createDirectory(this.copyTo.getParent());
            }

            FileUtils.copyFile(this.to, this.copyTo, true);
        }
    }

    public void downloadFile() throws IOException {
        if (this.instanceInstaller != null && this.instanceInstaller.isCancelled()) {
            return;
        }

        if (!this.needToDownload()) {
            if (this.copyTo != null) {
                if (this.fingerprint != null) {
                    try {
                        if (Hashing.murmur(this.copyTo) != this.fingerprint) {
                            this.copy();
                        }
                    } catch (IOException ignored) {
                    }
                } else {
                    Hashing.HashCode fileHash = Hashing.HashCode.EMPTY;
                    if (Files.exists(this.copyTo)) {
                        if (this.md5()) {
                            fileHash = Hashing.md5(this.copyTo);
                        } else if (this.sha512()) {
                            fileHash = Hashing.sha512(this.copyTo);
                        } else {
                            fileHash = Hashing.sha1(this.copyTo);
                        }
                    }

                    if (!fileHash.equals(Hashing.HashCode.fromString(this.getHash()))) {
                        this.copy();
                    }
                }
            }

            runPostProcessors();
            return;
        }

        // open the connection if not already opened
        if (this.response == null) {
            try {
                this.execute();
            } catch (IOException e) {
                if (this.instanceInstaller != null) {
                    this.instanceInstaller.cancel(true);
                }

                throw e;
            }
        }

        Path oldPath = null;
        if (Files.exists(this.to)) {
            oldPath = this.to.resolveSibling(this.to.getFileName().toString() + ".bak");
            FileUtils.moveFile(this.to, oldPath, true);
        }

        if (Files.exists(this.to) && Files.isRegularFile(this.to)) {
            FileUtils.delete(this.to);
        }

        if (!Files.isDirectory(this.to.getParent())) {
            FileUtils.createDirectory(this.to.getParent());
        }

        Hashing.HashCode expected = null;

        if (this.fingerprint == null) {
            expected = Hashing.HashCode.fromString(this.getHash());
        }

        if ((this.ignoreFailures && this.to.toFile().length() != 0)
                || (expected != null && expected.equals(Hashing.HashCode.EMPTY))) {
            if (this.response.isSuccessful()) {
                this.downloadDirect();
            }
        } else {
            boolean downloaded = this.downloadRec(1);

            if (!downloaded) {
                if (this.response.header("content-type").contains("text/html")) {
                    LogManager.error(
                            "The response from this request was a HTML response. This is usually caused by an antivirus or firewall software intercepting and rewriting the response. The response is below.");

                    LogManager.error(new String(Files.readAllBytes(this.to)));
                }

                FileUtils.copyFile(this.to, FileSystem.FAILED_DOWNLOADS);
                if (fingerprint != null) {
                    LogManager.error("Error downloading " + this.to.getFileName() + " from " + this.url + ". Expected"
                            + " fingerprint of " + fingerprint.toString() + " (with size of " + this.size + ") but got "
                            + Hashing.murmur(this.to) + " (with size of "
                            + (Files.exists(this.to) ? Files.size(this.to) : 0)
                            + ") instead. Copied to FailedDownloads folder & cancelling install!");
                } else {
                    LogManager.error("Error downloading " + this.to.getFileName() + " from " + this.url + ". Expected"
                            + " hash of " + expected.toString() + " (with size of " + this.size + ") but got "
                            + (this.md5() ? Hashing.md5(this.to)
                                    : (this.sha512() ? Hashing.sha512(this.to) : Hashing.sha1(this.to)))
                            + " (with size of " + (Files.exists(this.to) ? Files.size(this.to) : 0)
                            + ") instead. Copied to FailedDownloads folder & cancelling install!");
                }
                if (this.instanceInstaller != null) {
                    this.instanceInstaller.cancel(true);
                }
            }

            if (downloaded && this.copyTo != null) {
                if (this.fingerprint != null) {
                    try {
                        if (Hashing.murmur(this.copyTo) != this.fingerprint) {
                            this.copy();
                        }
                    } catch (IOException ignored) {
                    }
                } else {
                    Hashing.HashCode fileHash2 = Hashing.HashCode.EMPTY;
                    if (Files.exists(this.copyTo)) {
                        if (this.md5()) {
                            fileHash2 = Hashing.md5(this.copyTo);
                        } else if (this.sha512()) {
                            fileHash2 = Hashing.sha512(this.copyTo);
                        } else {
                            fileHash2 = Hashing.sha1(this.copyTo);
                        }
                    }

                    if (!fileHash2.equals(expected)) {
                        this.copy();
                    }
                }
            }
        }

        runPostProcessors();

        if (oldPath != null && Files.exists(oldPath)) {
            FileUtils.delete(oldPath);
        }
    }

    private void runPostProcessors() {
        if (this.response != null) {
            this.response.close();
        }

        if (Files.exists(this.to) && this.unzipTo != null) {
            FileUtils.createDirectory(this.unzipTo);

            ZipUtil.unpack(this.to.toFile(), this.unzipTo.toFile());
        }
    }

    public String getPrintableFileName() {
        if (this.friendlyFileName != null) {
            return this.friendlyFileName;
        }

        return this.to.getFileName().toString();
    }

    public boolean equals(Object other) {
        if (other instanceof Download) {
            try {
                return Files.isSameFile(this.to, ((Download) other).to);
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.to.hashCode();
    }
}
