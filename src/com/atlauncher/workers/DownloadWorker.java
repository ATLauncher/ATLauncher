/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.workers;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.SwingWorker;

import com.atlauncher.App;

public class DownloadWorker extends SwingWorker<Void, String> {

    private String url; // URL to download
    private String destination; // Destination to save the file
    private Boolean downloaded = false; // If it was downloaded

    public DownloadWorker(String url, String destination) {
        this.url = url; // Set the url
        this.destination = destination; // Set the destination
    }

    public Boolean wasDownloaded() {
        return this.downloaded;
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
            App.settings.getConsole().log("Cannot Connect To URL " + url, true);
            App.settings.getConsole().logStackTrace(e);
            cancel(true);
        }
        do {
            c.setRequestProperty("User-Agent", App.settings.getUserAgent());
            c.setConnectTimeout(5000);
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

    @Override
    protected Void doInBackground() throws Exception {
        if (this.destination == null) { // No destination received so download to string
            StringBuilder response = null;
            URL downloadURL = getRedirect(this.url);
            URLConnection connection = null;
            try {
                connection = downloadURL.openConnection();
            } catch (IOException e) {
                App.settings.getConsole().log("Cannot Connect To URL " + downloadURL, true);
                App.settings.getConsole().logStackTrace(e);
                cancel(true);
            }
            connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            firePropertyChange("response", null, response.toString());
        } else {
            InputStream in = null;
            URL downloadURL = getRedirect(this.url);
            URLConnection conn = null;
            try {
                conn = downloadURL.openConnection();
            } catch (IOException e) {
                App.settings.getConsole().log("Cannot Connect To URL " + downloadURL, true);
                App.settings.getConsole().logStackTrace(e);
                cancel(true);
            }
            conn.setRequestProperty("User-Agent", App.settings.getUserAgent());
            int size = conn.getContentLength();
            int downloaded = 0;
            in = conn.getInputStream();
            FileOutputStream writer = new FileOutputStream(this.destination);
            byte[] buffer = new byte[1024];
            long bytesCopied = 0;
            int bytesRead = 0;
            while ((bytesRead = in.read(buffer)) > 0) {
                writer.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                firePropertyChange("progress", null, (100 * downloaded) / size);
                buffer = new byte[1024];
            }
            writer.close();
            in.close();
        }
        this.downloaded = true;
        return null;
    }

}
