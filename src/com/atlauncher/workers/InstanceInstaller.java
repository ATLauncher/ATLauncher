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
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingWorker;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.App;
import com.atlauncher.data.Downloader;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.ModsChooser;
import com.atlauncher.gui.Utils;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {

    private String instanceName;
    private Pack pack;
    private String version;
    private boolean useLatestLWJGL;
    private boolean isReinstall;
    private boolean isServer;
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
    private Instance instance = null;
    private String[] modsInstalled;

    public InstanceInstaller(String instanceName, Pack pack, String version,
            boolean useLatestLWJGL, boolean isReinstall, boolean isServer) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        if (this.version.equalsIgnoreCase("Dev")) {
            this.version = "dev";
        }
        this.useLatestLWJGL = useLatestLWJGL;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public boolean isServer() {
        return this.isServer;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public String[] getModsInstalled() {
        return this.modsInstalled;
    }

    public String getInstanceSafeName() {
        return this.instanceName.replaceAll("[^A-Za-z0-9]", "");
    }

    public File getRootDirectory() {
        if (isServer) {
            return new File(App.settings.getServersDir(), pack.getSafeName() + "_"
                    + version.replaceAll("[^A-Za-z0-9]", ""));
        }
        return new File(App.settings.getInstancesDir(), getInstanceSafeName());
    }

    public File getTempJarDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.replaceAll("[^A-Za-z0-9]", "") + "_JarTemp");
    }

    public File getMinecraftDirectory() {
        if (isServer) {
            return getRootDirectory();
        }
        return new File(getRootDirectory(), ".minecraft");
    }

    public File getLibrariesDirectory() {
        return new File(getMinecraftDirectory(), "libraries");
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
        if (isServer) {
            if (isNewLaunchMethod()) {
                return new File(getRootDirectory(), "minecraft_server." + minecraftVersion + ".jar");
            } else {
                return new File(getRootDirectory(), "minecraft.jar");
            }
        }
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public void addToJarOrder(String file) {
        if (jarOrder == null) {
            jarOrder = file;
        } else {
            if (newLaunchMethod) {
                jarOrder = jarOrder + "," + file;
            } else {
                jarOrder = file + "," + jarOrder;
            }
        }
    }

    public boolean wasModInstalled(String mod) {
        if (isReinstall && instance != null) {
            return instance.wasModInstalled(mod);
        }
        return false;
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

    public ArrayList<Mod> getGroupedMods(Mod mod) {
        ArrayList<Mod> groupedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (modd.getGroup().equalsIgnoreCase(mod.getGroup())) {
                if (modd != mod) {
                    groupedMods.add(modd);
                }
            }
        }
        return groupedMods;
    }

    private void makeDirectories() {
        if (isReinstall) {
            // We're reinstalling so delete these folders
            Utils.delete(getBinDirectory());
            Utils.delete(getModsDirectory());
            Utils.delete(getCoreModsDirectory());
            Utils.delete(getJarModsDirectory());
        } else if (isServer) {
            // We're installing a server so delete the whole folder
            Utils.delete(getRootDirectory());
        }
        File[] directories;
        if (isServer) {
            directories = new File[] { getRootDirectory(), getModsDirectory(),
                    getCoreModsDirectory(), getLibrariesDirectory() };
        } else {
            directories = new File[] { getRootDirectory(), getMinecraftDirectory(),
                    getModsDirectory(), getCoreModsDirectory(), getJarModsDirectory(),
                    getBinDirectory(), getNativesDirectory() };
        }
        for (File directory : directories) {
            directory.mkdir();
        }
    }

    private void downloadMojangStuffNew() {
        firePropertyChange("doing", null,
                App.settings.getLocalizedString("instance.downloadingresources"));
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
            firePropertyChange("doing", null,
                    App.settings.getLocalizedString("instance.organisinglibraries"));
            firePropertyChange("subprogress", null, 0);
            if (!isServer) {
                String[] libraries = librariesNeeded.split(",");
                for (String libraryFile : libraries) {
                    Utils.copyFile(new File(App.settings.getLibrariesDir(), libraryFile),
                            getBinDirectory());
                }
                String[] natives = nativesNeeded.split(",");
                for (String nativeFile : natives) {
                    Utils.unzip(new File(App.settings.getLibrariesDir(), nativeFile),
                            getNativesDirectory());
                }
                Utils.delete(new File(getNativesDirectory(), "META-INF"));
            }
            if (isServer) {
                Utils.copyFile(new File(App.settings.getJarsDir(), "minecraft_server."
                        + this.minecraftVersion + ".jar"), getRootDirectory());
            } else {
                Utils.copyFile(new File(App.settings.getJarsDir(), this.minecraftVersion + ".jar"),
                        new File(getBinDirectory(), "minecraft.jar"), true);
            }
        }
    }

    private ArrayList<Downloadable> getNeededResources() {
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>(); // All the files

        // Read in the resources needed

        if (!isServer) {
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
                                File directory = new File(App.settings.getResourcesDir(),
                                        key.substring(0, key.lastIndexOf('/')));
                                file = new File(directory, filename);
                            } else {
                                file = new File(App.settings.getResourcesDir(), key);
                                filename = file.getName();
                            }
                            if (!Utils.getMD5(file).equalsIgnoreCase(etag))
                                downloads.add(new Downloadable(
                                        "https://s3.amazonaws.com/Minecraft.Resources/" + key,
                                        file, etag, this));
                        }
                    }
                }
            } catch (Exception e) {
                App.settings.getConsole().logStackTrace(e);
            }
        }
        // Now read in the library jars needed from the pack

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(version)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("library");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String url = element.getAttribute("url");
                    String file = element.getAttribute("file");
                    if (librariesNeeded == null) {
                        this.librariesNeeded = file;
                    } else {
                        this.librariesNeeded += "," + file;
                    }
                    File downloadTo = null;
                    if (isServer) {
                        if (!element.hasAttribute("server")) {
                            continue;
                        }
                        downloadTo = new File(new File(getLibrariesDirectory(), element
                                .getAttribute("server").substring(0,
                                        element.getAttribute("server").lastIndexOf('/'))), element
                                .getAttribute("server").substring(
                                        element.getAttribute("server").lastIndexOf('/'),
                                        element.getAttribute("server").length()));
                    } else {
                        downloadTo = new File(App.settings.getLibrariesDir(), file);
                    }
                    if (element.hasAttribute("md5")) {
                        downloads.add(new Downloadable(url, downloadTo,
                                element.getAttribute("md5"), this));
                    } else {
                        downloads.add(new Downloadable(url, downloadTo, "-", this));
                    }
                }
            }
        } catch (SAXException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }

        // Now read in the library jars needed from Mojang
        if (!isServer) {
            JSONParser parser = new JSONParser();

            try {
                Object obj = parser.parse(Utils
                        .urlToString("https://s3.amazonaws.com/Minecraft.Download/versions/"
                                + this.minecraftVersion + "/" + this.minecraftVersion + ".json"));
                JSONObject jsonObject = (JSONObject) obj;
                this.minecraftArguments = pack.getExtraArguments(version);
                String mc = pack.getMainClass(version);
                if (mc == null) {
                    this.mainClass = (String) jsonObject.get("mainClass");
                } else {
                    this.mainClass = mc;
                }
                JSONArray msg = (JSONArray) jsonObject.get("libraries");
                Iterator<JSONObject> iterator = msg.iterator();
                while (iterator.hasNext()) {
                    boolean shouldDownload = false;
                    JSONObject object = iterator.next();
                    String libraryName = (String) object.get("name");
                    String[] parts = ((String) object.get("name")).split(":");
                    String dir = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2];
                    String filename = null;
                    if (object.containsKey("rules")) {
                        JSONArray ruless = (JSONArray) object.get("rules");
                        Iterator<JSONObject> itt = ruless.iterator();
                        while (itt.hasNext()) {
                            JSONObject rules = itt.next();
                            if (((String) rules.get("action")).equalsIgnoreCase("allow")) {
                                if (rules.containsKey("os")) {
                                    JSONObject rule = (JSONObject) rules.get("os");
                                    if (((String) rule.get("name")).equalsIgnoreCase(Utils
                                            .getOSName())) {
                                        Pattern pattern = Pattern.compile((String) rule
                                                .get("version"));
                                        Matcher matcher = pattern.matcher(System
                                                .getProperty("os.version"));
                                        if (matcher.matches()) {
                                            shouldDownload = true;
                                        }
                                    }
                                } else {
                                    shouldDownload = true;
                                }
                            } else if (((String) rules.get("action")).equalsIgnoreCase("disallow")) {
                                if (rules.containsKey("os")) {
                                    JSONObject rule = (JSONObject) rules.get("os");
                                    if (((String) rule.get("name")).equalsIgnoreCase(Utils
                                            .getOSName())) {
                                        Pattern pattern = Pattern.compile((String) rule
                                                .get("version"));
                                        Matcher matcher = pattern.matcher(System
                                                .getProperty("os.version"));
                                        if (matcher.matches()) {
                                            shouldDownload = false;
                                        }
                                    }
                                }
                            } else {
                                shouldDownload = true;
                            }
                        }
                    } else {
                        shouldDownload = true;
                    }

                    if (shouldDownload) {
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
                        String url = "https://s3.amazonaws.com/Minecraft.Download/libraries/" + dir
                                + "/" + filename;
                        File file = new File(App.settings.getLibrariesDir(), filename);
                        downloads.add(new Downloadable(url, file, null, this));
                    }
                }

            } catch (ParseException e) {
                App.settings.getConsole().logStackTrace(e);
            }
        }

        if (isServer) {
            downloads.add(new Downloadable(
                    "https://s3.amazonaws.com/Minecraft.Download/versions/" + this.minecraftVersion
                            + "/minecraft_server." + this.minecraftVersion + ".jar", new File(
                            App.settings.getJarsDir(), "minecraft_server." + this.minecraftVersion
                                    + ".jar"), null, this));
        } else {
            downloads.add(new Downloadable("https://s3.amazonaws.com/Minecraft.Download/versions/"
                    + this.minecraftVersion + "/" + this.minecraftVersion + ".jar", new File(
                    App.settings.getJarsDir(), this.minecraftVersion + ".jar"), null, this));
        }
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
        if (!isServer) {
            File nativesFile = null;
            String nativesRoot = null;
            String nativesURL = null;
            if (Utils.isWindows()) {
                nativesFile = new File(App.settings.getJarsDir(),
                        ((this.useLatestLWJGL) ? "latest_" : "") + "windows_natives.jar");
                nativesRoot = "windowsnatives";
                nativesURL = "windows_natives";
            } else if (Utils.isMac()) {
                nativesFile = new File(App.settings.getJarsDir(),
                        ((this.useLatestLWJGL) ? "latest_" : "") + "macosx_natives.jar");
                nativesRoot = "macosxnatives";
                nativesURL = "macosx_natives";
            } else {
                nativesFile = new File(App.settings.getJarsDir(),
                        ((this.useLatestLWJGL) ? "latest_" : "") + "linux_natives.jar");
                nativesRoot = "linuxnatives";
                nativesURL = "linux_natives";
            }
            File[] files = {
                    new File(App.settings.getJarsDir(), minecraftVersion.replace(".", "_")
                            + "_minecraft.jar"),
                    new File(App.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_" : "")
                            + "lwjgl.jar"),
                    new File(App.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_" : "")
                            + "lwjgl_util.jar"),
                    new File(App.settings.getJarsDir(), ((this.useLatestLWJGL) ? "latest_" : "")
                            + "jinput.jar"), nativesFile };
            String[] hashes = {
                    App.settings.getMinecraftHash("minecraft", minecraftVersion, "client"),
                    App.settings.getMinecraftHash("lwjgl", (this.useLatestLWJGL) ? "latest"
                            : "mojang", "client"),
                    App.settings.getMinecraftHash("lwjglutil", (this.useLatestLWJGL) ? "latest"
                            : "mojang", "client"),
                    App.settings.getMinecraftHash("jinput", (this.useLatestLWJGL) ? "latest"
                            : "mojang", "client"),
                    App.settings.getMinecraftHash(nativesRoot, (this.useLatestLWJGL) ? "latest"
                            : "mojang", "client") };
            String[] urls = {
                    "http://assets.minecraft.net/" + minecraftVersion.replace(".", "_")
                            + "/minecraft.jar",
                    (this.useLatestLWJGL) ? App.settings
                            .getFileURL("launcher/lwjgl/latest_lwjgl.jar")
                            : "http://s3.amazonaws.com/MinecraftDownload/lwjgl.jar",
                    (this.useLatestLWJGL) ? App.settings
                            .getFileURL("launcher/lwjgl/latest_lwjgl_util.jar")
                            : "http://s3.amazonaws.com/MinecraftDownload/lwjgl_util.jar",
                    (this.useLatestLWJGL) ? App.settings
                            .getFileURL("launcher/lwjgl/latest_jinput.jar")
                            : "http://s3.amazonaws.com/MinecraftDownload/jinput.jar",
                    (this.useLatestLWJGL) ? App.settings.getFileURL("launcher/lwjgl/latest_"
                            + nativesURL + ".jar") : "http://s3.amazonaws.com/MinecraftDownload/"
                            + nativesURL + ".jar" };
            for (int i = 0; i < 5; i++) {
                addPercent(5);
                while (!Utils.getMD5(files[i]).equalsIgnoreCase(hashes[i])) {
                    firePropertyChange(
                            "doing",
                            null,
                            App.settings.getLocalizedString("common.downloading") + " "
                                    + files[i].getName());
                    new com.atlauncher.data.Downloader(urls[i], files[i].getAbsolutePath(), this)
                            .run();
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
        } else {
            String hash = App.settings.getMinecraftHash("minecraft", minecraftVersion, "server");
            File file = new File(App.settings.getJarsDir(), minecraftVersion.replace(".", "_")
                    + "_minecraft_server.jar");
            String url = "http://assets.minecraft.net/" + minecraftVersion.replace(".", "_")
                    + "/minecraft_server.jar";
            addPercent(25);
            while (!Utils.getMD5(file).equalsIgnoreCase(hash)) {
                firePropertyChange(
                        "doing",
                        null,
                        App.settings.getLocalizedString("common.downloading") + " "
                                + file.getName());
                new com.atlauncher.data.Downloader(url, file.getAbsolutePath(), this).run();
            }
            Utils.copyFile(file, getMinecraftJar(), true);
        }
    }

    public void deleteMetaInf() {
        File inputFile = getMinecraftJar();
        File outputTmpFile = new File(App.settings.getTempDir(), pack.getSafeName()
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
            App.settings.getConsole().logStackTrace(e);
        }
    }

    public void configurePack() {
        firePropertyChange("doing", null,
                App.settings.getLocalizedString("instance.extractingconfigs"));
        File configs = new File(App.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version + "/Configs.zip";
        String configsURL = App.settings.getFileURL(path); // The zip on the server
        new Downloader(configsURL, configs.getAbsolutePath(), this).run();
        Utils.unzip(configs, getMinecraftDirectory());
        configs.delete();
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getMinecraftDirectory());
        }
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
        this.allMods = this.pack.getMods(this.version, isServer);
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
        if (mods.size() != 0) {
            modsInstalled = new String[mods.size()];
            for (int i = 0; i < mods.size(); i++) {
                modsInstalled[i] = mods.get(i).getName();
            }
        } else {
            modsInstalled = new String[0];
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
        if (isServer) {
            firePropertyChange("doing", null,
                    App.settings.getLocalizedString("server.extractingjar"));
            firePropertyChange("subprogressint", null, 0);
            Utils.unzip(getMinecraftJar(), getTempJarDirectory());
        } else {
            if (!isNewLaunchMethod()) {
                deleteMetaInf();
            }
        }
        if (mods.size() != 0) {
            int amountPer = 40 / mods.size();
            for (Mod mod : mods) {
                if (!isCancelled()) {
                    firePropertyChange(
                            "doing",
                            null,
                            App.settings.getLocalizedString("common.downloading") + " "
                                    + mod.getName());
                    addPercent(amountPer);
                    mod.download(this);
                }
            }
            for (Mod mod : mods) {
                if (!isCancelled()) {
                    firePropertyChange(
                            "doing",
                            null,
                            App.settings.getLocalizedString("common.installing") + " "
                                    + mod.getName());
                    addPercent(amountPer);
                    mod.install(this);
                }
            }
        } else {
            addPercent(80);
        }
        if (isServer) {
            firePropertyChange("doing", null, App.settings.getLocalizedString("server.zippingjar"));
            firePropertyChange("subprogressint", null, 0);
            Utils.zip(getTempJarDirectory(), getMinecraftJar());
        }
        configurePack();
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
