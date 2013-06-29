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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.SwingWorker;

import com.atlauncher.data.Downloader;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.ModsChooser;
import com.atlauncher.gui.Utils;

public class PackInstaller extends SwingWorker<Boolean, Void> {

    private String instanceName;
    private Pack pack;
    private String version;
    private boolean useLatestLWJGL;
    private String minecraftVersion;
    private String jarOrder;
    private int percent = 0; // Percent done installing
    private ArrayList<Mod> allMods;

    public PackInstaller(String instanceName, Pack pack, String version, boolean useLatestLWJGL) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        if (this.version.equalsIgnoreCase("Dev Version")) {
            this.version = "dev";
        }
        this.useLatestLWJGL = useLatestLWJGL;
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
            jarOrder = file + "," + jarOrder;
        }
    }

    public Mod getModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.getName().equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }

    public ArrayList<Mod> getLinkedMods(Mod mod) {
        ArrayList<Mod> linkedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (modd.getLinked().equalsIgnoreCase(mod.getName())) {
                linkedMods.add(modd);
            }
        }
        return linkedMods;
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
            nativesFile = new File(LauncherFrame.settings.getJarsDir(),
                    ((this.useLatestLWJGL) ? "latest_" : "") + "windows_natives.jar");
            nativesRoot = "windowsnatives";
            nativesURL = "windows_natives";
        } else if (Utils.isMac()) {
            nativesFile = new File(LauncherFrame.settings.getJarsDir(),
                    ((this.useLatestLWJGL) ? "latest_" : "") + "macosx_natives.jar");
            nativesRoot = "macosxnatives";
            nativesURL = "macosx_natives";
        } else {
            nativesFile = new File(LauncherFrame.settings.getJarsDir(),
                    ((this.useLatestLWJGL) ? "latest_" : "") + "linux_natives.jar");
            nativesRoot = "linuxnatives";
            nativesURL = "linux_natives";
        }
        File[] files = {
                new File(LauncherFrame.settings.getJarsDir(), minecraftVersion.replace(".", "_")
                        + "_minecraft.jar"),
                new File(LauncherFrame.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_"
                        : "") + "lwjgl.jar"),
                new File(LauncherFrame.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_"
                        : "") + "lwjgl_util.jar"),
                new File(LauncherFrame.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_"
                        : "") + "jinput.jar"), nativesFile };
        String[] hashes = {
                LauncherFrame.settings.getMinecraftHash("minecraft", minecraftVersion, "client"),
                LauncherFrame.settings.getMinecraftHash("lwjgl", (this.useLatestLWJGL) ? "latest"
                        : "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash("lwjglutil",
                        (this.useLatestLWJGL) ? "latest" : "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash("jinput", (this.useLatestLWJGL) ? "latest"
                        : "mojang", "client"),
                LauncherFrame.settings.getMinecraftHash(nativesRoot,
                        (this.useLatestLWJGL) ? "latest" : "mojang", "client") };
        String[] urls = {
                "http://assets.minecraft.net/" + minecraftVersion.replace(".", "_")
                        + "/minecraft.jar",
                (this.useLatestLWJGL) ? LauncherFrame.settings
                        .getFileURL("launcher/lwjgl/latest_lwjgl.jar")
                        : "http://s3.amazonaws.com/MinecraftDownload/lwjgl.jar",
                (this.useLatestLWJGL) ? LauncherFrame.settings
                        .getFileURL("launcher/lwjgl/latest_lwjgl_util.jar")
                        : "http://s3.amazonaws.com/MinecraftDownload/lwjgl_util.jar",
                (this.useLatestLWJGL) ? LauncherFrame.settings
                        .getFileURL("launcher/lwjgl/latest_jinput.jar")
                        : "http://s3.amazonaws.com/MinecraftDownload/jinput.jar",
                (this.useLatestLWJGL) ? LauncherFrame.settings.getFileURL("launcher/lwjgl/latest_"
                        + nativesURL + ".jar") : "http://s3.amazonaws.com/MinecraftDownload/"
                        + nativesURL + ".jar" };
        for (int i = 0; i < 5; i++) {
            addPercent(5);
            while (!Utils.getMD5(files[i]).equalsIgnoreCase(hashes[i])) {
                firePropertyChange("doing", null, "Downloading " + files[i].getName());
                new Downloader(urls[i], files[i].getAbsolutePath(), this).runNoReturn();
            }
            if (i == 0) {
                Utils.copyFile(files[i], getMinecraftJar(), true);
            } else if (i == 4) {
                Utils.unzip(files[i], getNativesDirectory());
                Utils.delete(new File(getNativesDirectory(), "META-INF"));
            } else {
                if (useLatestLWJGL) {
                    Utils.copyFile(files[i], new File(getBinDirectory(), files[i].getName()
                            .replace("latest_", "")), true);
                } else {
                    Utils.copyFile(files[i], getBinDirectory());
                }
            }
        }
    }

    public void deleteMetaInf() {
        File inputFile = getMinecraftJar();
        File outputTmpFile = new File(LauncherFrame.settings.getTempDir(), pack.getSafeName()
                + "-minecraft.jar");
        try {
            JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
            JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
            JarEntry entry;

            while ((entry = input.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    continue;
                }
                output.putNextEntry(entry);
                byte buffer[] = new byte[1024];
                int amo;
                while ((amo = input.read(buffer, 0, 1024)) != -1) {
                    output.write(buffer, 0, amo);
                }
                output.closeEntry();
            }

            input.close();
            output.close();

            inputFile.delete();
            outputTmpFile.renameTo(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configurePack() {
        firePropertyChange("doing", null, "Extracting Configs");
        File configs = new File(LauncherFrame.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version + "/Configs.zip";
        String configsURL = LauncherFrame.settings.getFileURL(path); // The zip on the server
        new Downloader(configsURL, configs.getAbsolutePath(), this).runNoReturn();
        Utils.unzip(configs, getMinecraftDirectory());
        configs.delete();
    }

    public ArrayList<Mod> getMods() {
        return this.allMods;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    protected Boolean doInBackground() throws Exception {
        this.allMods = this.pack.getMods(this.version);
        this.minecraftVersion = this.pack.getMinecraftVersion(this.version);
        if (this.minecraftVersion == null) {
            this.cancel(true);
        }
        ModsChooser modsChooser = new ModsChooser(this);
        modsChooser.setVisible(true);
        if (modsChooser.wasClosed()) {
            this.cancel(true);
        }
        ArrayList<Mod> mods = modsChooser.getSelectedMods();
        addPercent(0);
        makeDirectories();
        downloadMojangStuff();
        deleteMetaInf();
        if (mods.size() != 0) {
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
        }else{
            addPercent(80);
        }
        configurePack();
        addPercent(5);
        firePropertyChange("doing", null, "Finished");
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
