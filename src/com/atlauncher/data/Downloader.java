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

/**
 * Class to download files and optionally show a progress dialog
 * 
 * @author Ryan
 */
public class Downloader {

    private String url; // URL to download
    private String destination; // Destination to save file to
    private boolean showDialog; // If to show dialog or not
    private DownloadWorker worker; // The download worker process
    private String response; // The response from the worker process

    public Downloader(String url, String destination, boolean showDialog) {
        this.url = url;
        this.destination = destination;
        this.showDialog = showDialog;
        this.worker = new DownloadWorker(url, destination);
        this.worker.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("response" == evt.getPropertyName()) {
                    String res = (String) evt.getNewValue();
                    response = res;
                }
            }
        });
    }

    public Downloader(String url, boolean showDialog) {
        this(url, null, showDialog); // Default to not save the file
    }

    public Downloader(String url) {
        this(url, null, false); // Default to not show the dialog
    }

    public void setURL(String url) {
        this.url = url;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setShowDialog(boolean showDialog) {
        this.showDialog = showDialog;
    }

    public String run() {
        if (worker != null) {
            worker.execute(); // Run the worker process
        }
        while(!worker.isDone()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return this.response;
    }

    public void runNoReturn() {
        if (worker != null) {
            worker.execute(); // Run the worker process
        }
        while(!worker.isDone()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
