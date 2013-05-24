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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingWorker;

import com.atlauncher.data.Server;
import com.atlauncher.gui.LauncherFrame;

public class ServerTester extends SwingWorker<Void, String> {

    @Override
    protected Void doInBackground() {
        Server[] servers = LauncherFrame.settings.getServers();
        double[] responseTimes = new double[servers.length];
        int count = 0;
        for (Server server : servers) {
            double startTime = System.currentTimeMillis();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(
                        server.getFileURL("10MB.file")).openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    responseTimes[count] = 1000000.0;
                    LauncherFrame.console.log("Server " + server.getName()
                            + " isn't available!");
                    server.disableServer();
                } else {
                    double endTime = System.currentTimeMillis();
                    responseTimes[count] = endTime - startTime;
                    LauncherFrame.console.log("Server " + server.getName()
                            + " is available! (" + responseTimes[count] + ")");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
        int best = 0;
        double bestTime = 10000000.0;
        for(int i=0;i<responseTimes.length;i++){
            if(responseTimes[i]<bestTime){
                best = i;
                bestTime = responseTimes[i];
            }
        }
        LauncherFrame.console.log("The best connected server is " + servers[best].getName());
        return null;
    }

}
