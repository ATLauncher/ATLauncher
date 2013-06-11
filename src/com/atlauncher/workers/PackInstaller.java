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
    int percent = 0; // Percent done installing

    public PackInstaller(Pack pack, Version version, String instanceName) {
        this.pack = pack;
        this.version = version;
        this.instanceName = instanceName;
    }

    protected Boolean doInBackground() throws Exception {
        ArrayList<Mod> mods = this.pack.getMods(this.version);
        System.out.println("Installing " + pack.getName() + " version " + version);
        addPercent(0);
        int amountPer = 40 / mods.size();
        for (Mod mod : mods) {
            if (!isCancelled()) {
                firePropertyChange("doing", null, "Downloading " + mod.getName());
                addPercent(amountPer);
                mod.download(this);
            }
        }
        for (Mod mod : mods) {
            if (!isCancelled()) {
                firePropertyChange("doing", null, "Installing " + mod.getName());
                addPercent(amountPer);
                mod.install();
            }
        }
        firePropertyChange("progress", null, 75);
        firePropertyChange("doing", null, "Configuring Pack");
        addPercent(5);
        firePropertyChange("progress", null, 100);
        firePropertyChange("doing", null, "Finished");
        addPercent(5);
        return true;
    }

    private void addPercent(int percent) {
        this.percent = this.percent + percent;
        if (this.percent > 100) {
            this.percent = 100;
        }
        firePropertyChange("progress", null, this.percent);
    }

    public void setSubPercent(int percent) {
        firePropertyChange("subprogress", null, percent);
    }

}
