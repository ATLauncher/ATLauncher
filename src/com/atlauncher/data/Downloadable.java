/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class Downloadable {

    private String beforeURL;
    private String url;
    private File file;
    private String md5;
    private int size;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;
    private boolean isATLauncherDownload;
    private int attempts = 0;

    public Downloadable(String url, File file, String md5, int size,
            InstanceInstaller instanceInstaller, boolean isATLauncherDownload) {
        if (isATLauncherDownload) {
            this.url = App.settings.getFileURL(url);
        } else {
            this.url = url;
        }
        this.beforeURL = url;
        this.file = file;
        this.md5 = md5;
        this.size = size;
        this.instanceInstaller = instanceInstaller;
        this.isATLauncherDownload = isATLauncherDownload;
    }

    public Downloadable(String url, File file, String md5, InstanceInstaller instanceInstaller,
            boolean isATLauncherDownload) {
        this(url, file, md5, -1, instanceInstaller, isATLauncherDownload);
    }

    public Downloadable(String url, boolean isATLauncherDownload) {
        this(url, null, null, -1, null, isATLauncherDownload);
    }

    public String getMD5FromURL() throws IOException {
        String etag = null;
        etag = getConnection().getHeaderField("ETag");

        if (etag == null) {
            etag = getConnection().getHeaderField("ATLauncher-MD5");
        }

        if (etag == null) {
            return "-";
        }

        if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }

        return etag;
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
            if (Utils.getMD5(this.file).equalsIgnoreCase(getMD5())) {
                return false;
            }
        }
        return true;
    }

    public String getMD5() {
        if (this.md5 == null) {
            try {
                this.md5 = getMD5FromURL();
            } catch (IOException e) {
                App.settings.logStackTrace(e);
                this.md5 = "-";
                this.connection = null;
            }
        }
        return this.md5;
    }

    public File getFile() {
        return this.file;
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
        if (this.connection == null) {
            try {
                this.connection = (HttpURLConnection) new URL(this.url).openConnection();
                this.connection.setUseCaches(false);
                this.connection.setDefaultUseCaches(false);
                this.connection.setConnectTimeout(5000);
                this.connection.setRequestProperty("Accept-Encoding", "gzip");
                this.connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
                this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
                this.connection.setRequestProperty("Expires", "0");
                this.connection.setRequestProperty("Pragma", "no-cache");
                this.connection.connect();
                if (this.connection.getResponseCode() / 100 != 2) {
                    throw new IOException(this.url + " returned response code "
                            + this.connection.getResponseCode());
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
                if (this.isATLauncherDownload) {
                    if (App.settings.getNextServer()) {
                        this.url = App.settings.getFileURL(this.beforeURL);
                        this.connection = null;
                        return getConnection();
                    } else {
                        App.settings.log("Failed to download " + this.beforeURL
                                + " from all ATLauncher servers. Cancelling install!",
                                LogMessageType.error, false);
                        if (this.instanceInstaller != null) {
                            instanceInstaller.cancel(true);
                        }
                    }
                }
            }
            App.settings.clearTriedServers(); // Okay downloaded it so clear the servers used
        }
        return this.connection;
    }

    private void downloadFile(boolean downloadAsLibrary) {
        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return;
            }
        }
        try {
            InputStream in = null;
            if (isGziped()) {
                in = new GZIPInputStream(getConnection().getInputStream());
            } else {
                in = getConnection().getInputStream();
            }
            FileOutputStream writer = new FileOutputStream(this.file);
            byte[] buffer = new byte[2048];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                buffer = new byte[2048];
                if (this.instanceInstaller != null && downloadAsLibrary && getFilesize() != 0) {
                    this.instanceInstaller.addDownloadedBytes(bytesRead);
                }
            }
            writer.close();
            in.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
            if (isGziped()) {
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
            App.settings.logStackTrace(e);
        }
        this.connection.disconnect();
        return response.toString();
    }

    public void download(boolean downloadAsLibrary) {
        if (this.file == null) {
            App.settings.log("Cannot download " + this.url + " to file as one wasn't specified!",
                    LogMessageType.error, false);
            return;
        }
        if (instanceInstaller != null) {
            if (instanceInstaller.isCancelled()) {
                return;
            }
        }
        if (!this.file.canWrite()) {
            Utils.delete(this.file);
        }
        // Create the directory structure
        new File(this.file.getAbsolutePath().substring(0,
                this.file.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
        if (getMD5().equalsIgnoreCase("-")) {
            downloadFile(downloadAsLibrary); // Only download the file once since we have no MD5 to
                                             // check
        } else {
            String fileMD5;
            boolean done = false;
            while (attempts <= 3) {
                attempts++;
                if (this.file.exists()) {
                    fileMD5 = Utils.getMD5(this.file);
                } else {
                    fileMD5 = "0";
                }
                if (fileMD5.equalsIgnoreCase(getMD5())) {
                    done = true;
                    break; // MD5 matches, file is good
                }
                downloadFile(downloadAsLibrary); // Keep downloading file until it matches MD5
            }
            if (!done) {
                if (this.isATLauncherDownload) {
                    if (App.settings.getNextServer()) {
                        App.settings.log("Error downloading " + this.file.getName() + " from "
                                + this.url + ". Trying another server!", LogMessageType.warning,
                                false);
                        this.url = App.settings.getFileURL(this.beforeURL);
                        this.connection = null;
                        download(downloadAsLibrary); // Redownload the file
                    } else {
                        App.settings.log("Failed to download file " + this.file.getName()
                                + " from all ATLauncher servers. Cancelling install!",
                                LogMessageType.error, false);
                        if (this.instanceInstaller != null) {
                            instanceInstaller.cancel(true);
                        }
                    }
                } else {
                    App.settings.log("Error downloading " + this.file.getName() + " from "
                            + this.url + ". Cancelling install!", LogMessageType.error, false);
                    if (this.instanceInstaller != null) {
                        instanceInstaller.cancel(true);
                    }
                }
            }
            App.settings.clearTriedServers(); // Okay downloaded it so clear the servers used
        }
        this.connection.disconnect();
    }
}
