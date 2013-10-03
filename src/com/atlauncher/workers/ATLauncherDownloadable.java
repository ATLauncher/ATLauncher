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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.atlauncher.App;
import com.atlauncher.data.Downloader;
import com.atlauncher.gui.Utils;

public class ATLauncherDownloadable implements Runnable {

    private String beforeURL;
    private String url;
    private File file;
    private String md5;
    private HttpURLConnection connection;
    private InstanceInstaller instanceInstaller;

    public ATLauncherDownloadable(String url, File file, String md5,
            InstanceInstaller instanceInstaller) {
        this.beforeURL = url;
        this.url = App.settings.getFileURL(url);
        this.file = file;
        this.md5 = md5;
        this.instanceInstaller = instanceInstaller;
    }

    public ATLauncherDownloadable(String url, File file, InstanceInstaller instanceInstaller) {
        this(url, file, null, instanceInstaller);
    }

    /**
     * Gets the redirected URL
     * 
     * @param url
     *            URL to check for redirections
     * @return The redirected URL
     * @throws IOException
     */
    public URL getRedirect(String url) throws IOException {
        boolean redir;
        int redirects = 0;
        InputStream in = null;
        URL target = null;
        URL downloadURL = new URL(url);
        URLConnection c = null;
        try {
            c = downloadURL.openConnection();
        } catch (IOException e) {
            App.settings.getConsole().log("Cannot Connect To URL " + downloadURL, true);
            App.settings.getConsole().logStackTrace(e);
            if (App.settings.disableServerGetNext()) {
                this.url = App.settings.getFileURL(this.beforeURL);
                return getRedirect(this.url);
            } else {
                App.settings.getConsole().log("Couldn't Get Redirected URL From " + this.url, true);
                instanceInstaller.cancel(true);
                return downloadURL;
            }
        }
        do {
            c.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306
                        && stat != HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    target = null;
                    if (loc != null) {
                        target = new URL(base, loc);
                    }
                    http.disconnect();
                    if (target == null
                            || !(target.getProtocol().equals("http") || target.getProtocol()
                                    .equals("https")) || redirects >= 5) {
                        throw new SecurityException("illegal URL redirect");
                    }
                    redir = true;
                    c = target.openConnection();
                    redirects++;
                }
            }
        } while (redir);
        if (target == null) {
            return downloadURL;
        } else {
            return target;
        }
    }

    public String getMD5FromURL(String url) {
        try {
            try {
                this.connection = (HttpURLConnection) new URL(url).openConnection();
            } catch (IOException e) {
                App.settings.getConsole().log("Cannot Connect To URL " + url, true);
                App.settings.getConsole().logStackTrace(e);
                if (App.settings.disableServerGetNext()) {
                    this.url = App.settings.getFileURL(this.beforeURL);
                    getMD5FromURL(this.url);
                } else {
                    App.settings.getConsole().log("Couldn't Get MD5 From " + this.url, true);
                    instanceInstaller.cancel(true);
                }
            }
            this.connection.setUseCaches(false);
            this.connection.setDefaultUseCaches(false);
            this.connection
                    .setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            this.connection.setRequestProperty("Expires", "0");
            this.connection.setRequestProperty("Pragma", "no-cache");
            this.connection.connect();
        } catch (MalformedURLException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        String etag = this.connection.getHeaderField("ATLauncher-MD5");
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
    }

    public void downloadFile() {
        try {
            InputStream in = null;
            URL downloadURL = getRedirect(this.url);
            if (this.connection == null) {
                try {
                    this.connection = (HttpURLConnection) downloadURL.openConnection();
                } catch (IOException e) {
                    App.settings.getConsole().log("Cannot Connect To URL " + downloadURL, true);
                    App.settings.getConsole().logStackTrace(e);
                    if (App.settings.disableServerGetNext()) {
                        this.url = App.settings.getFileURL(this.beforeURL);
                        downloadFile();
                    } else {
                        App.settings.getConsole().log("Couldn't Download File " + file.getName(),
                                true);
                        instanceInstaller.cancel(true);
                    }
                }
                this.connection
                        .setRequestProperty(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36");
            }
            in = this.connection.getInputStream();
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
            App.settings.getConsole().logStackTrace(e);
        }
    }

    @Override
    public void run() {
        if (instanceInstaller.isCancelled()) {
            return;
        }
        if (this.md5 == null) {
            this.md5 = getMD5FromURL(this.url);
        }
        // Create the directory structure
        new File(file.getAbsolutePath().substring(0,
                file.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
        if (this.md5.equalsIgnoreCase("-")) {
            downloadFile(); // Only download the file once since we have no MD5 to check
        } else {
            int tries = 0;
            while (!Utils.getMD5(this.file).equalsIgnoreCase(this.md5) && tries <= 3) {
                tries++;
                downloadFile(); // Keep downloading file until it matches MD5, up to 3 times
            }
        }
        instanceInstaller.setDownloadDone();
    }
}
