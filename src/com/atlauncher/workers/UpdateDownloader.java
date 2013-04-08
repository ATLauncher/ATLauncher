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

import javax.swing.SwingWorker;

public class UpdateDownloader extends
        SwingWorker<Void, UpdateDownloaderResults> {

    @Override
    protected Void doInBackground() throws Exception {
        publish(UpdateDownloaderResults.checking);
        Thread.sleep(3000);
        publish(UpdateDownloaderResults.downloading);
        int i;
        for (i = 1; i <= 10; i++) {
            if (i == 4) {
                publish(UpdateDownloaderResults.serverNotReachable);
                return null;
            }
            Thread.sleep(1000);
        }
        publish(UpdateDownloaderResults.complete);
        return null;
    }

}
