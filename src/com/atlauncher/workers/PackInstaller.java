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

import java.util.ArrayList;

import javax.swing.SwingWorker;

import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Version;

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
        for(Mod mod : mods){
            firePropertyChange("doing", null, "Downloading " + mod);
            Thread.sleep(300);
        }
        Thread.sleep(1000);
        firePropertyChange("progress", null, 50);
        for(Mod mod : mods){
            firePropertyChange("doing", null, "Installing " + mod);
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
