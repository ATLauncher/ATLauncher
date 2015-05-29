/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.managers.LogManager;
import com.atlauncher.Network;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class Downloadable {
    public static final int MAX_ATTEMPTS = 3;
    public static final CacheControl CACHE_CONTROL = new CacheControl.Builder().noStore().noCache().maxAge(0,
            TimeUnit.MILLISECONDS).build();

    public final String URL;
    public final int size;
    public final Path to;
    public final Path copyTo;
    public final boolean atlauncher;
    public final boolean copy;
    public final String filename;

    private final InstanceInstaller installer;
    private final List<Server> servers = new LinkedList<>(App.settings.getServers());

    private String url;
    private String hash;
    private Server server;
    private Response response;

    public Downloadable(String url, boolean atlauncher) {
        this(url, null, null, null, null, -1, atlauncher, false, null);
    }

    public Downloadable(String url, Path to) {
        this(url, null, to, null, null, -1, false, false, null);
    }

    public Downloadable(String url, Path output, boolean atlauncher) {
        this(url, null, output, null, null, -1, atlauncher, false, null);
    }

    public Downloadable(String url, String hash, Path to, int size, boolean atlauncher, InstanceInstaller installer) {
        this(url, hash, to, null, null, size, atlauncher, false, installer);
    }

    public Downloadable(String url, String hash, Path to, String filename, int size, boolean atlauncher,
                        InstanceInstaller installer) {
        this(url, hash, to, filename, null, size, atlauncher, false, installer);
    }

    public Downloadable(String url, String hash, Path to, String filename, Path copyTo, int size, boolean atlauncher,
                        boolean copy, InstanceInstaller installer) {
        this.atlauncher = atlauncher;
        this.URL = url;
        this.copy = copy;
        this.copyTo = copyTo;
        this.filename = filename;

        if (this.atlauncher) {
            this.server = App.settings.getServers().get(0);
            for (Server server : this.servers) {
                if (server.getName().equals(App.settings.getServer().getName())) {
                    this.server = server;
                    break;
                }
            }
            this.url = this.server.getFileURL(url);
        } else {
            this.url = url;
        }

        this.installer = installer;
        this.hash = hash;
        this.size = size;
        this.to = to;
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

    public <T> T fromJson(Class<T> tClass) throws IOException {
        if (this.installer != null) {
            if (this.installer.isCancelled()) {
                return null;
            }
        }

        this.execute();
        return Gsons.DEFAULT.fromJson(this.response.body().charStream(), tClass);
    }

    public <T> T fromJson(Type type) throws IOException {
        if (this.installer != null) {
            if (this.installer.isCancelled()) {
                return null;
            }
        }

        this.execute();
        return Gsons.DEFAULT.fromJson(this.response.body().charStream(), type);
    }

    private boolean md5() {
        return this.hash == null || this.hash.length() != 40;
    }

    private String getHashFromURL() throws IOException {
        this.execute();
        String etag = this.response.header("ETag");
        if (etag == null) {
            etag = this.response.header(Constants.LAUNCHER_NAME + "-MD5");
        }

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

    public int getFilesize() {
        return this.size;
    }

    public boolean needToDownload() {
        if (this.to == null) {
            return true;
        }

        if (Files.exists(this.to)) {
            if (this.md5()) {
                return !Hashing.md5(this.to).toString().equalsIgnoreCase(this.getHash());
            } else {
                return !Hashing.sha1(this.to).toString().equalsIgnoreCase(this.getHash());
            }
        }

        return true;
    }

    private boolean getNextServer() {
        for (Server server : this.servers) {
            if (this.server != server) {
                LogManager.warn("Server " + this.server.getName() + " Not Available");
                this.servers.remove(this.server);
                this.server = server;
                return true;
            }
        }

        return false;
    }

    private void execute() throws IOException {
        LogManager.debug("Opening connection to " + this.url, 3);

        Request.Builder builder = new Request.Builder().url(this.url).addHeader("User-Agent", App.settings
                .getUserAgent()).addHeader("Expires", "0").cacheControl(CACHE_CONTROL);

        this.response = (this.installer != null ? Network.PROGRESS_CLIENT : Network.CLIENT).newCall(builder.build())
                .execute();

        if (!this.response.isSuccessful()) {
            throw new IOException(this.url + " request wasn't successful: " + this.response);
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
            String fileHash = "0";
            if (Files.exists(this.to)) {
                if (this.md5()) {
                    fileHash = Hashing.md5(this.to).toString();
                } else {
                    fileHash = Hashing.sha1(this.to).toString();
                }
            }

            if (fileHash.equalsIgnoreCase(this.getHash())) {
                return true;
            }

            if (Files.exists(this.to)) {
                FileUtils.delete(this.to);
            }

            this.downloadDirect();
        }
        return this.downloadRec(attempt + 1);
    }

    public void copy() {
        if (this.copyTo != null && this.copy) {
            if (Files.exists(this.copyTo)) {
                FileUtils.delete(this.copyTo);
            }

            FileUtils.createDirectory(this.copyTo.getParent());
            FileUtils.copyFile(this.to, this.copyTo);
        }
    }

    public void download() throws IOException {
        this.execute();

        Path oldPath = null;
        if (Files.exists(this.to)) {
            oldPath = this.to.resolveSibling(this.to.getFileName().toString() + ".bak");
            FileUtils.moveFile(this.to, oldPath, true);
        }

        if (this.installer != null) {
            if (this.installer.isCancelled()) {
                return;
            }
        }

        if (Files.exists(this.to) && Files.isRegularFile(this.to)) {
            FileUtils.delete(this.to);
        }

        if (!Files.exists(this.to.getParent())) {
            FileUtils.createDirectory(this.to.getParent());
        }

        String expectedHash = this.getHash().trim();
        if (expectedHash.equalsIgnoreCase("-")) {
            this.downloadDirect();
        } else {
            boolean finished = this.downloadRec(1);
            if (!finished) {
                if (this.atlauncher) {
                    if (this.getNextServer()) {
                        LogManager.warn("Error downloading " + this.to.getFileName() + " from " + this.url + ". " +
                                "Expected hash of " + expectedHash + " but got " + this.hash + " instead. Trying " +
                                "another server!");
                        this.url = this.server.getFileURL(this.URL);
                    } else {
                        FileUtils.copyFile(this.to, FileSystem.FAILED_DOWNLOADS);
                        LogManager.error("Failed to download file " + this.to.getFileName() + " from all " +
                                Constants.LAUNCHER_NAME +
                                "servers. Copied to FailedDownloads Folder. Cancelling install!");
                        if (this.installer != null) {
                            this.installer.cancel(true);
                        }
                    }
                } else {
                    FileUtils.copyFile(this.to, FileSystem.FAILED_DOWNLOADS);
                    LogManager.error("Error downloading " + this.to.getFileName() + " from " + this.url + ". Expected" +
                            " hash of " + expectedHash + " but got " + this.hash + " instead. Copied to " +
                            "FailedDownloads folder & cancelling install!");
                    if (this.installer != null) {
                        this.installer.cancel(true);
                    }
                }
            } else if (this.copyTo != null && this.copy) {
                String fileHash2 = "0";
                if (Files.exists(this.copyTo)) {
                    if (this.md5()) {
                        fileHash2 = Hashing.md5(this.to).toString();
                    } else {
                        fileHash2 = Hashing.sha1(this.to).toString();
                    }
                }

                if (!fileHash2.trim().equalsIgnoreCase(expectedHash)) {
                    if (Files.exists(this.copyTo)) {
                        FileUtils.delete(this.copyTo);
                    }

                    FileUtils.createDirectory(this.copyTo.getParent());
                    FileUtils.copyFile(this.to, this.copyTo, true);
                }
            }

            App.settings.clearTriedServers();
        }

        if (oldPath != null && Files.exists(oldPath)) {
            FileUtils.delete(oldPath);
        }
    }

    @Override
    public String toString() {
        try {
            this.execute();
            return this.response.body().string();
        } catch (IOException e) {
            return null;
        }
    }
}