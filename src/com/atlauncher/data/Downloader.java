/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.atlauncher.App;
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
    private boolean downloaded = false; // If the file was downloaded

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

    public boolean downloaded() {
        return downloaded;
    }

    public void run() {
        if (worker != null) {
            worker.execute(); // Run the worker process
        }
        while (!worker.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                App.settings.getConsole().logStackTrace(e);
                worker.cancel(true);
            }
        }
        downloaded = worker.wasDownloaded();
    }
}
