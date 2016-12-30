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
import com.atlauncher.LogManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class Downloadable {
    private String beforeURL;
    private String url;
    private File file;
    private File oldFile;
    private String hash;
    private int size;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;
    private boolean isATLauncherDownload;
    private File copyTo;
    private boolean actuallyCopy;
    private int attempts = 0;
    private List<Server> servers;
    private Server server;
    private boolean checkForNewness = false;

    public Downloadable(String url, File file, String hash, int size, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload, File copyTo, boolean actuallyCopy) {
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
        this.file = file;
        this.hash = hash;
        this.size = size;
        this.instanceInstaller = instanceInstaller;
        this.isATLauncherDownload = isATLauncherDownload;
        this.copyTo = copyTo;
        this.actuallyCopy = actuallyCopy;
    }

    public Downloadable(String url, File file, String hash, int size, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload) {
        this(url, file, hash, size, instanceInstaller, isATLauncherDownload, null, false);
    }

    public Downloadable(String url, File file, String hash, InstanceInstaller instanceInstaller, boolean
            isATLauncherDownload) {
        this(url, file, hash, -1, instanceInstaller, isATLauncherDownload, null, false);
    }

    public Downloadable(String url, File file) {
        this(url, file, null, -1, null, false, null, false);
    }

    public Downloadable(String url, boolean isATLauncherDownload) {
        this(url, null, null, -1, null, isATLauncherDownload, null, false);
    }

    public String getFilename() {
        if (this.copyTo == null) {
            return this.file.getName();
        }
        return this.copyTo.getName();
    }

    public void checkForNewness() {
        this.checkForNewness = true;
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
        if (this.file == null) {
            return true;
        }

        if (this.file.exists()) {
            if (this.checkForNewness) {
                if (this.getConnection() != null && this.getFile().length() == this.getFilesize()) {
                    return false;
                }
            }

            if (isMD5()) {
                if (Utils.getMD5(this.file).equalsIgnoreCase(getHash())) {
                    return false;
                }
            } else {
                if (Utils.getSHA1(this.file).equalsIgnoreCase(getHash())) {
                    return false;
                }
            }
        }

        return true;
    }

    public void copyFile() {
        if (this.copyTo != null && this.actuallyCopy) {
            if (this.copyTo.exists()) {
                Utils.delete(this.copyTo);
            }
            new File(this.copyTo.getAbsolutePath().substring(0, this.copyTo.getAbsolutePath().lastIndexOf(File
                    .separatorChar))).mkdirs();
            Utils.copyFile(this.file, this.copyTo, true);
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

    public File getFile() {
        return this.file;
    }

    public File getCopyToFile() {
        return this.copyTo;
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
                String url = this.url;
                Integer numberOfRedirects = 0;

                while (numberOfRedirects < 5)
                {
                    URL resourceUrl = new URL(url);

                    if (App.settings.getEnableProxy()) {
                        this.connection = (HttpURLConnection) resourceUrl.openConnection(App.settings.getProxy());
                    } else {
                        this.connection = (HttpURLConnection) resourceUrl.openConnection();
                    }
                    this.connection.setInstanceFollowRedirects(true);
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

                    // check for redirections
                    switch (this.connection.getResponseCode()) {
                        case HttpURLConnection.HTTP_MOVED_PERM:
                        case HttpURLConnection.HTTP_MOVED_TEMP:
                        case 307:
                            String location = this.connection.getHeaderField("Location");
                            URL base = new URL(url);
                            URL next = new URL(base, location);  // Deal with relative URLs
                            url = next.toExternalForm();
                            numberOfRedirects++;
                            continue;
                    }

                    break;
                }

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
                        LogManager.error("Failed to download " + this.beforeURL + " from all " + Constants.LAUNCHER_NAME + " servers. " +
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
            writer = new FileOutputStream(this.file);
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
            if (this.oldFile != null && this.oldFile.exists()) {
                Utils.moveFile(this.oldFile, this.file, true);
            }
        } catch (IOException e) {
            LogManager.error("Failed to download " + this.url + " due to IOException!");
            App.settings.logStackTrace(e);
            if (this.oldFile != null && this.oldFile.exists()) {
                Utils.moveFile(this.oldFile, this.file, true);
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
        if (this.file == null) {
            LogManager.error("Cannot download " + this.url + " to file as one wasn't specified!");
            return;
        }
        if (this.file.exists()) {
            this.oldFile = new File(this.file.getParent(), this.file.getName() + ".bak");
            Utils.moveFile(this.file, this.oldFile, true);
        }
        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return;
            }
        }
        if (!this.file.canWrite()) {
            Utils.delete(this.file);
        }
        if (this.file.exists() && this.file.isFile()) {
            Utils.delete(this.file);
        }
        // Create the directory structure
        new File(this.file.getAbsolutePath().substring(0, this.file.getAbsolutePath().lastIndexOf(File.separatorChar)
        )).mkdirs();
        if (getHash().equalsIgnoreCase("-")) {
            downloadFile(downloadAsLibrary); // Only download the file once since we have no MD5 to check
        } else {
            String fileHash = "0";
            boolean done = false;
            while (attempts <= 3) {
                attempts++;
                if (this.file.exists()) {
                    if (isMD5()) {
                        fileHash = Utils.getMD5(this.file);
                    } else {
                        fileHash = Utils.getSHA1(this.file);
                    }
                } else {
                    fileHash = "0";
                }
                if (App.skipHashChecking || fileHash.equalsIgnoreCase(getHash())) {
                    done = true;
                    break; // Hash matches, file is good
                }
                if (this.connection != null) {
                    this.connection.disconnect();
                    this.connection = null;
                }
                if (this.file.exists()) {
                    Utils.delete(this.file); // Delete file since it doesn't match MD5
                }
                if (attempts != 1 && downloadAsLibrary) {
                    this.instanceInstaller.addTotalDownloadedBytes(this.size);
                }
                downloadFile(downloadAsLibrary); // Keep downloading file until it matches MD5
            }
            if (!done) {
                if (this.isATLauncherDownload) {
                    if (getNextServer()) {
                        LogManager.warn("Error downloading " + this.file.getName() + " from " + this.url + ". " +
                                "Expected hash of " + getHash() + " but got " + fileHash + " instead. Trying another " +
                                "server!");
                        this.url = server.getFileURL(this.beforeURL);
                        if (downloadAsLibrary) {
                            this.instanceInstaller.addTotalDownloadedBytes(this.size);
                        }
                        download(downloadAsLibrary); // Redownload the file
                    } else {
                        Utils.copyFile(this.file, App.settings.getFailedDownloadsDir());
                        LogManager.error("Failed to download file " + this.file.getName() + " from all " + Constants.LAUNCHER_NAME +
                                "servers. Copied to FailedDownloads Folder. Cancelling install!");
                        if (this.instanceInstaller != null) {
                            instanceInstaller.cancel(true);
                        }
                    }
                } else {
                    Utils.copyFile(this.file, App.settings.getFailedDownloadsDir());
                    LogManager.error("Error downloading " + this.file.getName() + " from " + this.url + ". Expected " +
                            "hash of " + getHash() + " but got " + fileHash + " instead. Copied to FailedDownloads " +
                            "Folder. Cancelling install!");
                    if (this.instanceInstaller != null) {
                        instanceInstaller.cancel(true);
                    }
                }
            } else if (this.copyTo != null && this.actuallyCopy) {
                String fileHash2;
                if (this.copyTo.exists()) {
                    if (isMD5()) {
                        fileHash2 = Utils.getMD5(this.file);
                    } else {
                        fileHash2 = Utils.getSHA1(this.file);
                    }
                } else {
                    fileHash2 = "0";
                }
                if (!fileHash2.equalsIgnoreCase(getHash())) {
                    if (this.copyTo.exists()) {
                        Utils.delete(this.copyTo);
                    }
                    new File(this.copyTo.getAbsolutePath().substring(0, this.copyTo.getAbsolutePath().lastIndexOf
                            (File.separatorChar))).mkdirs();
                    Utils.copyFile(this.file, this.copyTo, true);
                }
            }
            App.settings.clearTriedServers(); // Okay downloaded it so clear the servers used
        }

        if (this.oldFile != null && this.oldFile.exists()) {
            Utils.delete(this.oldFile);
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
