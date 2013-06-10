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
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.atlauncher.data.Downloader;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Version;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class PackInstaller extends SwingWorker<Boolean, Void> {

    Pack pack;
    Version version;
    String instanceName;

    public PackInstaller(Pack pack, Version version, String instanceName) {
        this.pack = pack;
        this.version = version;
        this.instanceName = instanceName;
    }

    protected Boolean doInBackground() throws Exception {
        ArrayList<Mod> mods = this.pack.getMods(this.version);
        System.out.println("Installing " + pack.getName() + " version " + version);
        firePropertyChange("progress", null, 25);
        for (Mod mod : mods) {
            firePropertyChange("doing", null, "Downloading " + mod.getName());
            File fileLocation = new File(LauncherFrame.settings.getDownloadsDir(), mod.getFile());
            if (mod.isDirectDownload()) {
                if (mod.getURL().contains("http://newfiles.atlauncher.com/")) {
                    new Downloader(LauncherFrame.settings.getFileURL(mod.getURL().replace(
                            "http://newfiles.atlauncher.com/", "")),
                            fileLocation.getAbsolutePath(), true).runNoReturn();
                } else {
                    new Downloader(mod.getURL(), fileLocation.getAbsolutePath(), true)
                            .runNoReturn();
                }
            } else {
                while (!fileLocation.exists()) {
                    Utils.openBrowser(mod.getURL());
                    String[] options = new String[] { "I've Downloaded This File" };
                    int retValue = JOptionPane
                            .showOptionDialog(
                                    LauncherFrame.settings.getParent(),
                                    "<html><center>Browser opened to download file "
                                            + mod.getName()
                                            + "<br/><br/>Please save this file to the following location<br/><br/>"
                                            + LauncherFrame.settings.getDownloadsDir()
                                                    .getAbsolutePath() + "</center></html>",
                                    "Downloading " + mod.getFile(), JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
                    if (retValue == JOptionPane.CLOSED_OPTION) {
                        cancel(true);
                        break;
                    }
                }
            }
            Thread.sleep(300);
        }
        Thread.sleep(1000);
        firePropertyChange("progress", null, 50);
        for (Mod mod : mods) {
            firePropertyChange("doing", null, "Installing " + mod.getName());
            mod.install();
            Thread.sleep(300);
        }
        Thread.sleep(1000);
        firePropertyChange("progress", null, 75);
        firePropertyChange("doing", null, "Configuring Pack");
        Thread.sleep(2000);
        firePropertyChange("progress", null, 100);
        firePropertyChange("doing", null, "Finished");
        Thread.sleep(250);
        return true;
    }

}
