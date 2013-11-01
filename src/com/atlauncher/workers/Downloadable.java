/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.workers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.atlauncher.App;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.utils.Utils;

public class Downloadable implements Runnable {

    private String url;
    private File file;
    private String md5;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;
    private int attempts = 0;

    public Downloadable(String url, File file, String md5, InstanceInstaller instanceInstaller) {
        this.url = url;
        this.file = file;
        this.md5 = md5;
        this.instanceInstaller = instanceInstaller;
    }

    public Downloadable(String url, File file, String md5) {
        this(url, file, md5, null);
    }

    public Downloadable(String url, File file) {
        this(url, file, null, null);
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

    private HttpURLConnection getConnection() {
        if (this.connection == null) {
            try {
                this.connection = (HttpURLConnection) new URL(this.url).openConnection();
                this.connection.setUseCaches(false);
                this.connection.setDefaultUseCaches(false);
                this.connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
                this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
                this.connection.setRequestProperty("Expires", "0");
                this.connection.setRequestProperty("Pragma", "no-cache");
                this.connection.connect();
            } catch (IOException e) {
                App.settings.log("Cannot make a connection to " + this.url, LogMessageType.error,
                        false);
                App.settings.logStackTrace(e);
            }
        }
        return this.connection;
    }

    public void downloadFile() {
        if (instanceInstaller.isCancelled()) {
            return;
        }
        try {
            InputStream in = null;
            in = getConnection().getInputStream();
            FileOutputStream writer = new FileOutputStream(this.file);
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                buffer = new byte[1024];
            }
            writer.close();
            in.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

    @Override
    public void run() {
        if (instanceInstaller.isCancelled()) {
            return;
        }
        instanceInstaller.setDoingResources("Downloading " + file.getName());
        if (this.md5 == null) {
            try {
                this.md5 = getMD5FromURL();
            } catch (IOException e) {
                App.settings.logStackTrace(e);
                this.md5 = "-";
                this.connection = null;
            }
        }
        // Create the directory structure
        new File(file.getAbsolutePath().substring(0,
                file.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
        if (this.md5.equalsIgnoreCase("-")) {
            downloadFile(); // Only download the file once since we have no MD5 to check
        } else {
            String fileMD5;
            if (this.file.exists()) {
                fileMD5 = Utils.getMD5(this.file);
            } else {
                fileMD5 = "0";
            }
            while (!fileMD5.equalsIgnoreCase(this.md5) && attempts <= 3) {
                attempts++;
                downloadFile(); // Keep downloading file until it matches MD5, up to 3 times
                if (this.file.exists()) {
                    fileMD5 = Utils.getMD5(this.file);
                } else {
                    fileMD5 = "0";
                }
            }
        }
    }
}
