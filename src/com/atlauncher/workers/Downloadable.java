/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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

import com.atlauncher.gui.Utils;

public class Downloadable implements Runnable {

    private String url;
    private File file;
    private String md5;
    private HttpURLConnection connection;

    public Downloadable(String url, File file, String md5) {
        this.url = url;
        this.file = file;
        this.md5 = md5;
    }

    public Downloadable(String url, File file) {
        this(url, file, null);
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
        URLConnection c = downloadURL.openConnection();
        do {
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

    public String getEtagFromURL(String url) {
        try {
            this.connection = (HttpURLConnection) new URL(url).openConnection();
            this.connection.setUseCaches(false);
            this.connection.setDefaultUseCaches(false);
            this.connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            this.connection.setRequestProperty("Expires", "0");
            this.connection.setRequestProperty("Pragma", "no-cache");
            this.connection.connect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String etag = this.connection.getHeaderField("ETag");
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
    }

    @Override
    public void run() {
        if (this.md5 == null) {
            this.md5 = getEtagFromURL(this.url);
        }
        // Create the directory structure
        new File(file.getAbsolutePath().substring(0,
                file.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
        while (!Utils.getMD5(this.file).equalsIgnoreCase(this.md5)) {
            try {
                InputStream in = null;
                URL downloadURL = getRedirect(this.url);
                if (this.connection == null) {
                    this.connection = (HttpURLConnection) downloadURL.openConnection();
                }
                int size = this.connection.getContentLength();
                int downloaded = 0;
                in = this.connection.getInputStream();
                FileOutputStream writer = new FileOutputStream(this.file);
                byte[] buffer = new byte[1024];
                long bytesCopied = 0;
                int bytesRead = 0;
                while ((bytesRead = in.read(buffer)) > 0) {
                    writer.write(buffer, 0, bytesRead);
                    downloaded += bytesRead;
                    buffer = new byte[1024];
                }
                writer.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
