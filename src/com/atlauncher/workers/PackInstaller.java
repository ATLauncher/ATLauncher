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

import javax.swing.SwingWorker;

import com.atlauncher.data.Downloader;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class PackInstaller extends SwingWorker<Boolean, Void> {

    String instanceName;
    Pack pack;
    String version;
    String minecraftVersion;
    String jarOrder;
    int percent = 0; // Percent done installing

    public PackInstaller(String instanceName, Pack pack, String version, String minecraftVersion) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public String getInstanceSafeName() {
        return this.instanceName.replaceAll("[^A-Za-z0-9]", "");
    }

    public File getRootDirectory() {
        return new File(LauncherFrame.settings.getInstancesDir(), getInstanceSafeName());
    }

    public File getMinecraftDirectory() {
        return new File(getRootDirectory(), ".minecraft");
    }

    public File getModsDirectory() {
        return new File(getMinecraftDirectory(), "mods");
    }

    public File getCoreModsDirectory() {
        return new File(getMinecraftDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getMinecraftDirectory(), "jarmods");
    }

    public File getBinDirectory() {
        return new File(getMinecraftDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public void addToJarOrder(String file) {
        if (jarOrder == null) {
            jarOrder = file;
        } else {
            jarOrder += "," + file;
        }
    }

    private void makeDirectories() {
        File[] directories = { getRootDirectory(), getMinecraftDirectory(), getModsDirectory(),
                getCoreModsDirectory(), getJarModsDirectory(), getBinDirectory(),
                getNativesDirectory() };
        for (File directory : directories) {
            directory.mkdir();
        }
    }

    private void downloadMojangStuff() {
        File nativesFile = null;
        String nativesRoot = null;
        String nativesURL = null;
        if (Utils.isWindows()) {
            nativesFile = new File(LauncherFrame.settings.getJarsDir(), "windows_natives.jar");
            nativesRoot = "windowsnatives";
            nativesURL = "windows_natives";
        } else if (Utils.isMac()) {
            nativesFile = new File(LauncherFrame.settings.getJarsDir(), "macosx_natives.jar");
            nativesRoot = "macosxnatives";
            nativesURL = "macosx_natives";
        } else {
            nativesFile = new File(LauncherFrame.settings.getJarsDir(), "linux_natives.jar");
            nativesRoot = "linuxnatives";
            nativesURL = "linux_natives";
        }
        File[] files = {
                new File(LauncherFrame.settings.getJarsDir(), minecraftVersion.replace(".", "_")
                        + "_minecraft.jar"),
                new File(LauncherFrame.settings.getJarsDir(), "lwjgl.jar"),
                new File(LauncherFrame.settings.getJarsDir(), "lwjgl_util.jar"),
                new File(LauncherFrame.settings.getJarsDir(), "jinput.jar"), nativesFile };
        String[] hashes = {
                LauncherFrame.settings.getMinecraftHash("minecraft", minecraftVersion, "client"),
                LauncherFrame.settings.getMinecraftHash("lwjgl", "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash("lwjglutil", "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash("jinput", "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash(nativesRoot, "mojang", "client") };
        String[] urls = {
                "http://assets.minecraft.net/" + minecraftVersion.replace(".", "_")
                        + "/minecraft.jar", "http://s3.amazonaws.com/MinecraftDownload/lwjgl.jar",
                "http://s3.amazonaws.com/MinecraftDownload/lwjgl_util.jar",
                "http://s3.amazonaws.com/MinecraftDownload/jinput.jar",
                "http://s3.amazonaws.com/MinecraftDownload/" + nativesURL + ".jar" };
        for (int i = 0; i < 5; i++) {
            while (!Utils.getMD5(files[i]).equalsIgnoreCase(hashes[i])) {
                firePropertyChange("doing", null, "Downloading " + files[i].getName());
                new Downloader(urls[i], files[i].getAbsolutePath(), this).runNoReturn();
            }
            if (i == 0) {
                Utils.copyFile(files[i], getMinecraftJar(), true);
            } else if (i == 4) {
                Utils.unzip(files[i], getNativesDirectory());
            } else {
                Utils.copyFile(files[i], getBinDirectory());
            }
        }
    }

    protected Boolean doInBackground() throws Exception {
        ArrayList<Mod> mods = this.pack.getMods(this.version);
        addPercent(0);
        makeDirectories();
        addPercent(5);
        downloadMojangStuff();
        addPercent(5);
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
                mod.install(this);
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
