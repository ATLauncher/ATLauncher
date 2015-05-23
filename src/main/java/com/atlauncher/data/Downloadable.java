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
import com.atlauncher.LogManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Downloadable {
    private String beforeURL;
    private String url;
    private Path path;
    private Path oldPath;
    private String hash;
    private int size;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;
    private boolean isATLauncherDownload;
    private Path copyTo;
    private boolean actuallyCopy;
    private int attempts = 0;
    private List<Server> servers;
    private Server server;

    public Downloadable(String url, Path path, String hash, int size, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload, Path copyTo, boolean actuallyCopy) {
        if (isATLauncherDownload) {
            this.servers = new ArrayList<Server>(App.settings.getServers());
            this.server = this.servers.get(0);
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
        this.beforeURL = url;
        this.path = path;
        this.hash = hash;
        this.size = size;
        this.instanceInstaller = instanceInstaller;
        this.isATLauncherDownload = isATLauncherDownload;
        this.copyTo = copyTo;
        this.actuallyCopy = actuallyCopy;
    }

    public Downloadable(String url, Path path, String hash, int size, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload) {
        this(url, path, hash, size, instanceInstaller, isATLauncherDownload, null, false);
    }

    public Downloadable(String url, Path path, String hash, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload) {
        this(url, path, hash, -1, instanceInstaller, isATLauncherDownload, null, false);
    }

    public Downloadable(String url, Path path) {
        this(url, path, null, -1, null, false, null, false);
    }

    public Downloadable(String url, boolean isATLauncherDownload) {
        this(url, null, null, -1, null, isATLauncherDownload, null, false);
    }

    public String getFilename() {
        if (this.copyTo == null) {
            return this.path.getFileName().toString();
        }

        return this.copyTo.getFileName().toString();
    }

    public boolean isMD5() {
        return hash == null || hash.length() != 40;
    }

    public String getHashFromURL() throws IOException {
        String etag = null;
        etag = getConnection().getHeaderField("ETag");

        if (etag == null) {
            etag = getConnection().getHeaderField(Constants.LAUNCHER_NAME + "-MD5");
        }

        if (etag == null) {
            return "-";
        }

        if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }

        if (etag.matches("[A-Za-z0-9]{32}")) {
            return etag;
        } else {
            return "-";
        }
    }

    public int getFilesize() {
        if (this.size == -1) {
            int size = getConnection().getContentLength();
            if (size == -1) {
                this.size = 0;
            } else {
                this.size = size;
            }
        }
        return this.size;
    }

    public boolean needToDownload() {
        if (this.path == null) {
            return true;
        }

        if (Files.exists(this.path)) {
            if (isMD5()) {
                return !Utils.getMD5(this.path).equalsIgnoreCase(getHash());
            } else {
                return !Utils.getSHA1(this.path).equalsIgnoreCase(getHash());
            }
        }

        return true;
    }

    public void copyFile() {
        if (this.copyTo != null && this.actuallyCopy) {
            if (Files.exists(this.copyTo)) {
                FileUtils.delete(this.copyTo);
            }

            FileUtils.createDirectory(this.copyTo.getParent());
            FileUtils.copyFile(this.path, this.copyTo, true);
        }
    }

    public String getHash() {
        if (this.hash == null || this.hash.isEmpty()) {
            try {
                this.hash = getHashFromURL();
            } catch (IOException e) {
                App.settings.logStackTrace(e);
                this.hash = "-";
                this.connection = null;
            }
        }
        return this.hash;
    }

    public Path getPath() {
        return this.path;
    }

    public boolean isGziped() {
        if (getConnection().getContentEncoding() == null) {
            return false;
        } else if (getConnection().getContentEncoding().equalsIgnoreCase("gzip")) {
            return true;
        } else {
            return false;
        }
    }

    private HttpURLConnection getConnection() {
        if (this.instanceInstaller != null) {
            if (this.instanceInstaller.isCancelled()) {
                return null;
            }
        }
        if (this.connection == null) {
            LogManager.debug("Opening connection to " + this.url, 3);
            try {
                if (App.settings.getEnableProxy()) {
                    this.connection = (HttpURLConnection) new URL(this.url).openConnection(App.settings.getProxy());
                } else {
                    this.connection = (HttpURLConnection) new URL(this.url).openConnection();
                }
                this.connection.setUseCaches(false);
                this.connection.setDefaultUseCaches(false);
                if (App.useGzipForDownloads) {
                    this.connection.setRequestProperty("Accept-Encoding", "gzip");
                }
                this.connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
                this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
                this.connection.setRequestProperty("Expires", "0");
                this.connection.setRequestProperty("Pragma", "no-cache");
                this.connection.connect();

                if (this.connection.getResponseCode() / 100 != 2) {
                    throw new IOException(this.url + " returned response code " + this.connection.getResponseCode() +
                            (this.connection.getResponseMessage() != null ? " with message of " + this.connection
                                    .getResponseMessage() : ""));
                }
                LogManager.debug("Connection opened to " + this.url, 3);
            } catch (IOException e) {
                LogManager.debug("Exception when opening connection to " + this.url, 3);
                App.settings.logStackTrace(e);
                if (this.isATLauncherDownload) {
                    if (getNextServer()) {
                        this.url = server.getFileURL(this.beforeURL);
                        this.connection = null;
                        return getConnection();
                    } else {
                        LogManager.error("Failed to download " + this.beforeURL + " from all " + Constants
                                .LAUNCHER_NAME + " servers. " +
                                "Cancelling install!");
                        if (this.instanceInstaller != null) {
                            instanceInstaller.cancel(true);
                        }
                    }
                }
            }
        }
        return this.connection;
    }

    private void downloadFile(boolean downloadAsLibrary) {
        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return;
            }
        }
        InputStream in = null;
        FileOutputStream writer = null;
        try {
            if (isGziped() && App.useGzipForDownloads) {
                in = new GZIPInputStream(getConnection().getInputStream());
            } else {
                in = getConnection().getInputStream();
            }
            writer = new FileOutputStream(this.path.toFile());
            byte[] buffer = new byte[2048];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                buffer = new byte[2048];
                if (this.instanceInstaller != null && downloadAsLibrary && getFilesize() != 0) {
                    this.instanceInstaller.addDownloadedBytes(bytesRead);
                }
            }
        } catch (SocketException e) {
            LogManager.error("Failed to download " + this.url + " due to SocketException!");
            // Connection reset. Close connection and try again
            App.settings.logStackTrace(e);
            this.connection.disconnect();
            this.connection = null;
            if (this.oldPath != null && Files.exists(this.oldPath)) {
                FileUtils.moveFile(this.oldPath, this.path, true);
            }
        } catch (IOException e) {
            LogManager.error("Failed to download " + this.url + " due to IOException!");
            App.settings.logStackTrace(e);
            if (this.oldPath != null && Files.exists(this.oldPath)) {
                FileUtils.moveFile(this.oldPath, this.path, true);
            }
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                App.settings.logStackTrace(e1);
            }
        }
    }

    public String getContents() {
        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return null;
            }
        }
        StringBuilder response = null;
        try {
            InputStream in = null;
            if (isGziped() && App.useGzipForDownloads) {
                in = new GZIPInputStream(getConnection().getInputStream());
            } else {
                in = getConnection().getInputStream();
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            response = new StringBuilder();
            String inputLine;

            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        } catch (IOException e) {
            LogManager.error("Failed to get contents of " + this.url + " due to IOException!");
            App.settings.logStackTrace(e);
            return null;
        }
        this.connection.disconnect();
        return response.toString();
    }

    public void download(boolean downloadAsLibrary) {
        download(downloadAsLibrary, false);
    }

    public void download(boolean downloadAsLibrary, boolean force) {
        this.attempts = 0;
        if (this.connection != null) {
            this.connection.disconnect();
            this.connection = null;
        }

        if (this.path == null) {
            LogManager.error("Cannot download " + this.url + " to path as one wasn't specified!");
            return;
        }

        if (Files.exists(this.path)) {
            this.oldPath = this.path.resolveSibling(this.path.getFileName().toString() + ".bak");
            FileUtils.moveFile(this.path, this.oldPath, true);
        }

        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return;
            }
        }

        if (Files.exists(this.path) && Files.isRegularFile(this.path)) {
            FileUtils.delete(this.path);
        }

        // Create the directory structure if the parent doesn't exist
        if (!Files.exists(this.path.getParent())) {
            FileUtils.createDirectory(this.path.getParent());
        }

        if (getHash().equalsIgnoreCase("-")) {
            downloadFile(downloadAsLibrary); // Only download the path once since we have no MD5 to check
        } else {
            String fileHash = "0";
            boolean done = false;

            while (attempts <= 3) {
                attempts++;

                if (Files.exists(this.path)) {
                    if (isMD5()) {
                        fileHash = Utils.getMD5(this.path);
                    } else {
                        fileHash = Utils.getSHA1(this.path);
                    }
                } else {
                    fileHash = "0";
                }

                if (fileHash.equalsIgnoreCase(getHash())) {
                    done = true;
                    break; // Hash matches, path is good
                }

                if (this.connection != null) {
                    this.connection.disconnect();
                    this.connection = null;
                }

                if (Files.exists(this.path)) {
                    FileUtils.delete(this.path); // Delete path since it doesn't match MD5
                }

                if (attempts != 1 && downloadAsLibrary) {
                    this.instanceInstaller.addTotalDownloadedBytes(this.size);
                }

                downloadFile(downloadAsLibrary); // Keep downloading path until it matches MD5
            }
            if (!done) {
                if (this.isATLauncherDownload) {
                    if (getNextServer()) {
                        LogManager.warn("Error downloading " + this.path.getFileName() + " from " + this.url + ". " +
                                "Expected hash of " + getHash() + " but got " + fileHash + " instead. Trying another " +
                                "server!");
                        this.url = server.getFileURL(this.beforeURL);
                        if (downloadAsLibrary) {
                            this.instanceInstaller.addTotalDownloadedBytes(this.size);
                        }
                        download(downloadAsLibrary); // Redownload the path
                    } else {
                        FileUtils.copyFile(this.path, FileSystem.FAILED_DOWNLOADS);
                        LogManager.error("Failed to download path " + this.path.getFileName() + " from all " +
                                Constants.LAUNCHER_NAME + "servers. Copied to FailedDownloads folder and Cancelling " +
                                "install!");
                        if (this.instanceInstaller != null) {
                            instanceInstaller.cancel(true);
                        }
                    }
                } else {
                    FileUtils.copyFile(this.path, FileSystem.FAILED_DOWNLOADS);
                    LogManager.error("Error downloading " + this.path.getFileName() + " from " + this.url + ". " +
                            "Expected  hash of " + getHash() + " but got " + fileHash + " instead. Copied to " +
                            "FailedDownloads folder and cancelling install!");
                    if (this.instanceInstaller != null) {
                        instanceInstaller.cancel(true);
                    }
                }
            } else if (this.copyTo != null && this.actuallyCopy) {
                String fileHash2;
                if (Files.exists(this.copyTo)) {
                    if (isMD5()) {
                        fileHash2 = Utils.getMD5(this.path);
                    } else {
                        fileHash2 = Utils.getSHA1(this.path);
                    }
                } else {
                    fileHash2 = "0";
                }
                if (!fileHash2.equalsIgnoreCase(getHash())) {
                    if (Files.exists(this.copyTo)) {
                        FileUtils.delete(this.copyTo);
                    }

                    FileUtils.createDirectory(this.copyTo.getParent());

                    FileUtils.copyFile(this.path, this.copyTo, true);
                }
            }
            App.settings.clearTriedServers(); // Okay downloaded it so clear the servers used
        }

        if (this.oldPath != null && Files.exists(this.oldPath)) {
            FileUtils.delete(this.oldPath);
        }

        if (this.connection != null) {
            this.connection.disconnect();
        }
    }

    public boolean getNextServer() {
        for (Server server : this.servers) {
            if (this.server != server) {
                LogManager.warn("Server " + this.server.getName() + " Not Available! Switching To " + server.getName());
                this.servers.remove(this.server);
                this.server = server; // Setup next available server
                return true;
            }
        }
        return false;
    }

    public int getResponseCode() {
        try {
            return getConnection().getResponseCode();
        } catch (IOException e) {
            App.settings.logStackTrace("IOException when getting response code for the url " + this.url, e);
            return -1;
        }
    }
}
