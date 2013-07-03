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
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.ModsChooser;
import com.atlauncher.gui.Utils;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {

    private String instanceName;
    private Pack pack;
    private String version;
    private boolean useLatestLWJGL;
    private boolean isReinstall;
    private String minecraftVersion;
    private String jarOrder;
    private boolean newLaunchMethod;
    private String librariesNeeded = null;
    private String nativesNeeded = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private int percent = 0; // Percent done installing
    private ArrayList<Mod> allMods;
    private int totalResources = 0; // Total number of Resources to download for Minecraft >=1.6
    private int doneResources = 0; // Total number of Resources downloaded for Minecraft >=1.6

    public InstanceInstaller(String instanceName, Pack pack, String version,
            boolean useLatestLWJGL, boolean isReinstall) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        if (this.version.equalsIgnoreCase("Dev Version")) {
            this.version = "dev";
        }
        this.useLatestLWJGL = useLatestLWJGL;
        this.isReinstall = isReinstall;
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
        if (isReinstall) {
            // We're reinstalling so delete these folders
            Utils.delete(getBinDirectory());
            Utils.delete(getModsDirectory());
            Utils.delete(getCoreModsDirectory());
            Utils.delete(getJarModsDirectory());
        }
        File[] directories = { getRootDirectory(), getMinecraftDirectory(), getModsDirectory(),
                getCoreModsDirectory(), getJarModsDirectory(), getBinDirectory(),
                getNativesDirectory() };
        for (File directory : directories) {
            directory.mkdir();
        }
    }

    private void downloadMojangStuffNew() {
        firePropertyChange("doing", null, "Downloading Resources (May Take A While)");
        firePropertyChange("subprogressint", null, null);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        ArrayList<Downloadable> downloads = getNeededResources();
        totalResources = downloads.size();

        for (Downloadable download : downloads) {
            executor.execute(download);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        if (!isCancelled()) {
            firePropertyChange("doing", null, "Organising Libraries");
            firePropertyChange("subprogress", null, 0);
            String[] libraries = librariesNeeded.split(",");
            for (String libraryFile : libraries) {
                Utils.copyFile(new File(LauncherFrame.settings.getLibrariesDir(), libraryFile),
                        getBinDirectory());
            }
            String[] natives = nativesNeeded.split(",");
            for (String nativeFile : natives) {
                Utils.unzip(new File(LauncherFrame.settings.getLibrariesDir(), nativeFile),
                        getNativesDirectory());
            }
            Utils.delete(new File(getNativesDirectory(), "META-INF"));
            Utils.copyFile(new File(LauncherFrame.settings.getJarsDir(), this.minecraftVersion
                    + ".jar"), new File(getBinDirectory(), "minecraft.jar"), true);
        }
    }

    private ArrayList<Downloadable> getNeededResources() {
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>(); // All the files

        // Read in the resources needed

        try {
            URL resourceUrl = new URL("https://s3.amazonaws.com/Minecraft.Resources/");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(resourceUrl.openStream());
            NodeList nodeLst = doc.getElementsByTagName("Contents");

            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);

                if (node.getNodeType() == 1) {
                    Element element = (Element) node;
                    String key = element.getElementsByTagName("Key").item(0).getChildNodes()
                            .item(0).getNodeValue();
                    String etag = element.getElementsByTagName("ETag") != null ? element
                            .getElementsByTagName("ETag").item(0).getChildNodes().item(0)
                            .getNodeValue() : "-";
                    etag = getEtag(etag);
                    long size = Long.parseLong(element.getElementsByTagName("Size").item(0)
                            .getChildNodes().item(0).getNodeValue());

                    if (size > 0L) {
                        File file;
                        String filename;
                        if (key.contains("/")) {
                            filename = key.substring(key.lastIndexOf('/') + 1, key.length());
                            File directory = new File(LauncherFrame.settings.getResourcesDir(),
                                    key.substring(0, key.lastIndexOf('/')));
                            file = new File(directory, filename);
                        } else {
                            file = new File(LauncherFrame.settings.getResourcesDir(), key);
                            filename = file.getName();
                        }
                        if (!Utils.getMD5(file).equalsIgnoreCase(etag))
                            downloads.add(new Downloadable(
                                    "https://s3.amazonaws.com/Minecraft.Resources/" + key, file,
                                    etag, this));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now read in the library jars needed

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(Utils
                    .urlToString("https://s3.amazonaws.com/Minecraft.Download/versions/"
                            + this.minecraftVersion + "/" + this.minecraftVersion + ".json"));
            JSONObject jsonObject = (JSONObject) obj;
            this.minecraftArguments = (String) jsonObject.get("minecraftArguments");
            this.mainClass = (String) jsonObject.get("mainClass");
            JSONArray msg = (JSONArray) jsonObject.get("libraries");
            Iterator<JSONObject> iterator = msg.iterator();
            while (iterator.hasNext()) {
                JSONObject object = iterator.next();
                String[] parts = ((String) object.get("name")).split(":");
                String dir = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2];
                String filename;
                if (object.containsKey("natives")) {
                    JSONObject nativesObject = (JSONObject) object.get("natives");
                    String nativesName;
                    if (Utils.isWindows()) {
                        nativesName = (String) nativesObject.get("windows");
                    } else if (Utils.isMac()) {
                        nativesName = (String) nativesObject.get("osx");
                    } else {
                        nativesName = (String) nativesObject.get("linux");
                    }
                    filename = parts[1] + "-" + parts[2] + "-" + nativesName + ".jar";
                    if (nativesNeeded == null) {
                        this.nativesNeeded = filename;
                    } else {
                        this.nativesNeeded += "," + filename;
                    }
                } else {
                    filename = parts[1] + "-" + parts[2] + ".jar";
                    if (librariesNeeded == null) {
                        this.librariesNeeded = filename;
                    } else {
                        this.librariesNeeded += "," + filename;
                    }
                }
                String url = "https://s3.amazonaws.com/Minecraft.Download/libraries/" + dir + "/"
                        + filename;
                File file = new File(LauncherFrame.settings.getLibrariesDir(), filename);
                downloads.add(new Downloadable(url, file, null, this));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        downloads.add(new Downloadable("https://s3.amazonaws.com/Minecraft.Download/versions/"
                + this.minecraftVersion + "/" + this.minecraftVersion + ".jar", new File(
                LauncherFrame.settings.getJarsDir(), this.minecraftVersion + ".jar"), null, this));
        return downloads;
    }

    public static String getEtag(String etag) {
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
    }

    private void downloadMojangStuffOld() {
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
                new com.atlauncher.data.Downloader(urls[i], files[i].getAbsolutePath(), this).run();
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
        new com.atlauncher.data.Downloader(configsURL, configs.getAbsolutePath(), this).run();
        Utils.unzip(configs, getMinecraftDirectory());
        configs.delete();
    }

    public ArrayList<Mod> getMods() {
        return this.allMods;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean isNewLaunchMethod() {
        return this.newLaunchMethod;
    }

    public String getLibrariesNeeded() {
        return this.librariesNeeded;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    protected Boolean doInBackground() throws Exception {
        this.allMods = this.pack.getMods(this.version);
        this.minecraftVersion = this.pack.getMinecraftVersion(this.version);
        if (this.minecraftVersion == null) {
            this.cancel(true);
        }
        ArrayList<Mod> mods = new ArrayList<Mod>();
        if (allMods.size() != 0) {
            ModsChooser modsChooser = new ModsChooser(this);
            modsChooser.setVisible(true);
            if (modsChooser.wasClosed()) {
                this.cancel(true);
            }
            mods = modsChooser.getSelectedMods();
        }
        addPercent(0);
        makeDirectories();
        if (pack.isNewInstallMethod(this.version)) {
            this.newLaunchMethod = true;
            downloadMojangStuffNew();
        } else {
            this.newLaunchMethod = false;
            downloadMojangStuffOld();
        }
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
        } else {
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

    public void setDoing(String doing) {
        firePropertyChange("doing", null, doing);
        doneResources++;
        int progress = (100 * doneResources) / totalResources;
        firePropertyChange("subprogress", null, progress);
    }

}
