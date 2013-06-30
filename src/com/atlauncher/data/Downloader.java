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
package com.atlauncher.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.atlauncher.workers.DownloadWorker;
import com.atlauncher.workers.InstanceInstaller;

/**
 * Class to download files and optionally show a progress dialog
 * 
 * @author Ryan
 */
public class Downloader {

    private String url; // URL to download
    private String destination; // Destination to save file to
    private DownloadWorker worker; // The download worker process
    private String response; // The response from the worker process
    private InstanceInstaller installer;

    public Downloader(String url, String destination, InstanceInstaller installerr) {
        this.url = url;
        this.destination = destination;
        this.installer = (InstanceInstaller) installerr;
        this.worker = new DownloadWorker(url, destination);
        if (this.destination == null) {
            this.worker.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("response" == evt.getPropertyName()) {
                        response = (String) evt.getNewValue();
                    }
                }
            });
        }
        if (this.installer != null) {
            this.worker.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("progress" == evt.getPropertyName()) {
                        int progress = (Integer) evt.getNewValue();
                        if (progress > 100) {
                            progress = 100;
                        }
                        installer.setSubPercent(progress);
                    }
                }
            });
        }
    }

    public Downloader(String url, String destination) {
        this(url, destination, null); // Default to not show progress
    }

    public Downloader(String url) {
        this(url, null, null); // Default to not save the file
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String run() {
        if (worker != null) {
            worker.execute(); // Run the worker process
        }
        while (response == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                worker.cancel(true);
            }
        }
        return this.response;
    }

    public void runNoReturn() {
        if (worker != null) {
            worker.execute(); // Run the worker process
        }
        while (!worker.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                worker.cancel(true);
            }
        }
    }
}
