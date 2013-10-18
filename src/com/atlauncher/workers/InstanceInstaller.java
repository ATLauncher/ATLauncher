/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
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
import com.atlauncher.data.DecompType;
import com.atlauncher.data.Download;
import com.atlauncher.data.Downloader;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Type;
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
    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved
    private boolean extractedTexturePack = false; // If there is an extracted texturepack
    private boolean extractedResourcePack = false; // If there is an extracted resourcepack
    private int permgen = 0;
    private int memory = 0;
    private String librariesNeeded = null;
    private String nativesNeeded = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private int percent = 0; // Percent done installing
    private ArrayList<Mod> allMods;
    private ArrayList<Mod> selectedMods;
    private int totalResources = 0; // Total number of Resources to download for Minecraft >=1.6
    private int doneResources = 0; // Total number of Resources downloaded for Minecraft >=1.6
    private int totalDownloads = 0; // Total number of mods to download
    private int doneDownloads = 0; // Total number of mods downloaded
    private Instance instance = null;
    private String[] modsInstalled;
    private ArrayList<File> serverLibraries;

    public InstanceInstaller(String instanceName, Pack pack, String version,
            String minecraftVersion, boolean useLatestLWJGL, boolean isReinstall, boolean isServer) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.useLatestLWJGL = useLatestLWJGL;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        if (isServer) {
            serverLibraries = new ArrayList<File>();
        }
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

    public File getTempDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.replaceAll("[^A-Za-z0-9]", ""));
    }

    public File getTempJarDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.replaceAll("[^A-Za-z0-9]", "") + "_JarTemp");
    }

    public File getTempTexturePackDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.replaceAll("[^A-Za-z0-9]", "") + "_TexturePackTemp");
    }

    public File getTempResourcePackDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.replaceAll("[^A-Za-z0-9]", "") + "_ResourcePackTemp");
    }

    public File getLibrariesDirectory() {
        return new File(getRootDirectory(), "libraries");
    }

    public File getTexturePacksDirectory() {
        return new File(getRootDirectory(), "texturepacks");
    }

    public File getShaderPacksDirectory() {
        return new File(getRootDirectory(), "shaderpacks");
    }

    public File getResourcePacksDirectory() {
        return new File(getRootDirectory(), "resourcepacks");
    }

    public File getConfigDirectory() {
        return new File(getRootDirectory(), "config");
    }

    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public File getMinecraftJar() {
        if (isServer) {
            if (isNewLaunchMethod()) {
                return new File(getRootDirectory(), "minecraft_server." + minecraftVersion + ".jar");
            } else {
                return new File(getRootDirectory(), "minecraft_server.jar");
            }
        }
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public void setTexturePackExtracted() {
        this.extractedTexturePack = true;
    }

    public void setResourcePackExtracted() {
        this.extractedResourcePack = true;
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

    public ArrayList<Mod> getModsDependancies(Mod mod) {
        ArrayList<Mod> dependsMods = new ArrayList<Mod>();
        for (String name : mod.getDependancies()) {
            inner: {
                for (Mod modd : allMods) {
                    if (modd.getName().equalsIgnoreCase(name)) {
                        dependsMods.add(modd);
                        break inner;
                    }
                }
            }
        }
        return dependsMods;
    }

    public ArrayList<Mod> dependedMods(Mod mod) {
        ArrayList<Mod> dependedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (!modd.hasDepends()) {
                continue;
            }
            if (modd.isADependancy(mod)) {
                dependedMods.add(modd);
            }
        }
        return dependedMods;
    }

    public boolean hasADependancy(Mod mod) {
        for (Mod modd : allMods) {
            if (!modd.hasDepends()) {
                continue;
            }
            if (modd.isADependancy(mod)) {
                return true;
            }
        }
        return false;
    }

    private void makeDirectories() {
        if (isReinstall || isServer) {
            // We're reinstalling or installing a server so delete these folders
            Utils.delete(getBinDirectory());
            Utils.delete(getConfigDirectory());
            Utils.delete(getModsDirectory());
            Utils.delete(getCoreModsDirectory());
            if (isReinstall) {
                Utils.delete(getJarModsDirectory()); // Only delete if it's not a server
                Utils.delete(new File(getTexturePacksDirectory(), "TexturePack.zip"));
                Utils.delete(new File(getResourcePacksDirectory(), "ResourcePack.zip"));
            } else {
                Utils.delete(getLibrariesDirectory()); // Only delete if it's a server
            }
        }
        File[] directories;
        if (isServer) {
            directories = new File[] { getRootDirectory(), getModsDirectory(),
                    getLibrariesDirectory() };
        } else {
            directories = new File[] { getRootDirectory(), getModsDirectory(),
                    getJarModsDirectory(), getBinDirectory(), getNativesDirectory() };
        }
        for (File directory : directories) {
            directory.mkdir();
        }
        if (!isNewLaunchMethod()) {
            getCoreModsDirectory().mkdir();
        }
        getTempDirectory().mkdir();
    }

    private ArrayList<ATLauncherDownloadable> getDownloadableMods() {
        ArrayList<ATLauncherDownloadable> mods = new ArrayList<ATLauncherDownloadable>();

        for (Mod mod : this.selectedMods) {
            if (mod.isServerDownload()) {
                ATLauncherDownloadable downloadable;
                if (mod.hasMD5()) {
                    downloadable = new ATLauncherDownloadable(mod.getURL(), new File(
                            App.settings.getDownloadsDir(), mod.getFile()), mod.getMD5(), this);
                } else {
                    downloadable = new ATLauncherDownloadable(mod.getURL(), new File(
                            App.settings.getDownloadsDir(), mod.getFile()), this);
                }
                mods.add(downloadable);
            }
        }

        return mods;
    }

    private boolean hasOptionalMods() {
        for (Mod mod : allMods) {
            if (mod.isOptional()) {
                return true;
            }
        }
        return false;
    }

    private void downloadMods(ArrayList<Mod> mods) {
        fireSubProgressUnknown();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        ArrayList<ATLauncherDownloadable> downloads = getDownloadableMods();
        totalDownloads = downloads.size();

        for (ATLauncherDownloadable download : downloads) {
            executor.execute(download);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        for (Mod mod : mods) {
            if (!downloads.contains(mod) && !isCancelled()) {
                fireTask(App.settings.getLocalizedString("common.downloading") + " "
                        + mod.getFile());
                mod.download(this);
            }
        }
    }

    private void installMods(ArrayList<Mod> mods) {
        for (Mod mod : mods) {
            if (!isCancelled()) {
                fireTask(App.settings.getLocalizedString("common.installing") + " " + mod.getName());
                addPercent(mods.size() / 40);
                mod.install(this);
            }
        }
    }

    public boolean hasRecommendedMods() {
        for (Mod mod : allMods) {
            if (!mod.isRecommeneded()) {
                return true; // One of the mods is marked as not recommended, so return true
            }
        }
        return false; // No non recommended mods found
    }

    public boolean isOnlyRecommendedInGroup(Mod mod) {
        for (Mod modd : allMods) {
            if (modd == mod) {
                continue;
            }
            if (modd.getGroup().equalsIgnoreCase(mod.getGroup())) {
                if (modd.isRecommeneded()) {
                    return false; // Another mod is recommended. Don't check anything
                }
            }
        }
        return true; // No other recommended mods found in the group
    }

    private void downloadMojangStuffNew() {
        fireTask(App.settings.getLocalizedString("instance.downloadingresources"));
        fireSubProgressUnknown();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        ArrayList<MojangDownloadable> downloads = getNeededResources();
        totalResources = downloads.size();

        for (MojangDownloadable download : downloads) {
            executor.execute(download);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        if (!isCancelled()) {
            fireTask(App.settings.getLocalizedString("instance.organisinglibraries"));
            fireSubProgress(0);
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

    private ArrayList<MojangDownloadable> getNeededResources() {
        ArrayList<MojangDownloadable> downloads = new ArrayList<MojangDownloadable>(); // All the
                                                                                       // files

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
                                downloads.add(new MojangDownloadable(
                                        "https://s3.amazonaws.com/Minecraft.Resources/" + key,
                                        file, etag, this));
                        }
                    }
                }
            } catch (Exception e) {
                App.settings.getConsole().logStackTrace(e);
            }
        }

        // Now lets see if we have custom mainclass and extraarguments

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(version, false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("mainclass");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("depends")) {
                        boolean found = false;
                        for (Mod mod : selectedMods) {
                            if (element.getAttribute("depends").equalsIgnoreCase(mod.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                    NodeList nodeList1 = element.getChildNodes();
                    this.mainClass = nodeList1.item(0).getNodeValue();
                }
            }
        } catch (SAXException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(version, false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("extraarguments");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.hasAttribute("depends")) {
                        boolean found = false;
                        for (Mod mod : selectedMods) {
                            if (element.getAttribute("depends").equalsIgnoreCase(mod.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            break;
                        }
                    }
                    NodeList nodeList1 = element.getChildNodes();
                    this.minecraftArguments = nodeList1.item(0).getNodeValue();
                }
            }
        } catch (SAXException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }

        // Now read in the library jars needed from the pack

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(version, false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("library");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String url = element.getAttribute("url");
                    String file = element.getAttribute("file");
                    Download download = Download.direct;
                    if (element.hasAttribute("download")) {
                        download = Download.valueOf(element.getAttribute("download"));
                    }
                    String md5 = "-";
                    if (element.hasAttribute("md5")) {
                        md5 = element.getAttribute("md5");
                    }
                    if (element.hasAttribute("depends")) {
                        boolean found = false;
                        for (Mod mod : selectedMods) {
                            if (element.getAttribute("depends").equalsIgnoreCase(mod.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            continue;
                        }
                    }
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
                        serverLibraries.add(new File(new File(getLibrariesDirectory(), element
                                .getAttribute("server").substring(0,
                                        element.getAttribute("server").lastIndexOf('/'))), element
                                .getAttribute("server").substring(
                                        element.getAttribute("server").lastIndexOf('/'),
                                        element.getAttribute("server").length())));
                    }
                    downloadTo = new File(App.settings.getLibrariesDir(), file);
                    if (download == Download.server) {
                        downloads.add(new MojangDownloadable(App.settings.getFileURL(url),
                                downloadTo, md5, this));
                    } else {
                        downloads.add(new MojangDownloadable(url, downloadTo, md5, this));
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
                if (this.mainClass == null) {
                    this.mainClass = (String) jsonObject.get("mainClass");
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
                        downloads.add(new MojangDownloadable(url, file, null, this));
                    }
                }

            } catch (ParseException e) {
                App.settings.getConsole().logStackTrace(e);
            }
        }

        if (isServer) {
            downloads.add(new MojangDownloadable(
                    "https://s3.amazonaws.com/Minecraft.Download/versions/" + this.minecraftVersion
                            + "/minecraft_server." + this.minecraftVersion + ".jar", new File(
                            App.settings.getJarsDir(), "minecraft_server." + this.minecraftVersion
                                    + ".jar"), null, this));
        } else {
            downloads.add(new MojangDownloadable(
                    "https://s3.amazonaws.com/Minecraft.Download/versions/" + this.minecraftVersion
                            + "/" + this.minecraftVersion + ".jar", new File(App.settings
                            .getJarsDir(), this.minecraftVersion + ".jar"), null, this));
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
                    fireTask(App.settings.getLocalizedString("common.downloading") + " "
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
                fireTask(App.settings.getLocalizedString("common.downloading") + " "
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
        Boolean configsDownloaded = false; // If the configs were downloaded
        fireTask(App.settings.getLocalizedString("instance.extractingconfigs"));
        File configs = new File(App.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version + "/Configs.zip";
        String configsURL = App.settings.getFileURL(path); // The zip on the server
        Downloader configsDownloader = new Downloader(configsURL, configs.getAbsolutePath(), this);
        configsDownloader.run();
        configsDownloaded = configsDownloader.downloaded();
        while (!configsDownloaded) {
            if (App.settings.disableServerGetNext()) {
                configsURL = App.settings.getFileURL(path); // The zip on the server
                configsDownloader = new Downloader(configsURL, configs.getAbsolutePath(), this);
                configsDownloader.run();
                configsDownloaded = configsDownloader.downloaded();
            } else {
                App.settings.getConsole().log("Couldn't Download Configs For " + pack.getName(),
                        true);
                cancel(true);
            }
        }
        Utils.unzip(configs, getRootDirectory());
        configs.delete();
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getRootDirectory());
        }
    }

    public String getServerJar() {
        Mod forge = null; // The Forge Mod
        Mod mcpc = null; // The MCPC Mod
        for (Mod mod : selectedMods) {
            if (mod.getType() == Type.forge) {
                forge = mod;
            } else if (mod.getType() == Type.mcpc) {
                mcpc = mod;
            }
        }
        if (mcpc != null) {
            return mcpc.getFile();
        } else if (forge != null) {
            return forge.getFile();
        } else {
            if (isNewLaunchMethod()) {
                return "minecraft_server." + minecraftVersion + ".jar";
            } else {
                return "minecraft_server.jar";
            }
        }
    }

    public boolean hasJarMods() {
        for (Mod mod : selectedMods) {
            if (!mod.installOnServer() && isServer) {
                continue;
            }
            if (mod.getType() == Type.jar) {
                return true;
            } else if (mod.getType() == Type.decomp) {
                if (mod.getDecompType() == DecompType.jar) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Mod> getMods() {
        return this.allMods;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public int getPermGen() {
        return this.permgen;
    }

    public int getMemory() {
        return this.memory;
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

    public ArrayList<Mod> sortMods(ArrayList<Mod> original) {
        ArrayList<Mod> mods = new ArrayList<Mod>(original);

        for (Mod mod : original) {
            if (mod.isOptional()) {
                if (!mod.getLinked().isEmpty()) {
                    for (Mod mod1 : original) {
                        if (mod1.getName().equalsIgnoreCase(mod.getLinked())) {
                            mods.remove(mod);
                            int index = mods.indexOf(mod1) + 1;
                            mods.add(index, mod);
                        }
                    }

                }
            }
        }

        return mods;
    }

    protected Boolean doInBackground() throws Exception {
        this.allMods = sortMods(this.pack.getMods(this.version, isServer));
        this.permgen = this.pack.getPermGen(this.version);
        this.memory = this.pack.getMemory(this.version);
        if (this.minecraftVersion == null) {
            this.cancel(true);
        }
        if (pack.isNewInstallMethod(this.version)) {
            this.newLaunchMethod = true;
        } else {
            this.newLaunchMethod = false;
        }
        selectedMods = new ArrayList<Mod>();
        if (allMods.size() != 0 && hasOptionalMods()) {
            ModsChooser modsChooser = new ModsChooser(this);
            modsChooser.setVisible(true);
            if (modsChooser.wasClosed()) {
                this.cancel(true);
            }
            selectedMods = modsChooser.getSelectedMods();
        }
        if (!hasOptionalMods()) {
            selectedMods = allMods;
        }
        if (selectedMods.size() != 0) {
            modsInstalled = new String[selectedMods.size()];
            for (int i = 0; i < selectedMods.size(); i++) {
                modsInstalled[i] = selectedMods.get(i).getName();
            }
        } else {
            modsInstalled = new String[0];
        }
        File reis = new File(getModsDirectory(), "rei_minimap");
        if (reis.exists() && reis.isDirectory()) {
            if (Utils.copyDirectory(reis, getTempDirectory(), true)) {
                savedReis = true;
            }
        }
        File zans = new File(getModsDirectory(), "VoxelMods");
        if (zans.exists() && zans.isDirectory()) {
            if (Utils.copyDirectory(zans, getTempDirectory(), true)) {
                savedZans = true;
            }
        }
        File neiCfg = new File(getConfigDirectory(), "NEI.cfg");
        if (neiCfg.exists() && neiCfg.isFile()) {
            if (Utils.copyFile(neiCfg, getTempDirectory())) {
                savedNEICfg = true;
            }
        }
        File portalGunSounds = new File(getModsDirectory(), "PortalGunSounds.pak");
        if (portalGunSounds.exists() && portalGunSounds.isFile()) {
            savedPortalGunSounds = true;
            Utils.copyFile(portalGunSounds, getTempDirectory());
        }
        makeDirectories();
        addPercent(5);
        if (this.newLaunchMethod) {
            downloadMojangStuffNew();
            if (isServer) {
                for (File file : serverLibraries) {
                    file.mkdirs();
                    Utils.copyFile(new File(App.settings.getLibrariesDir(), file.getName()), file,
                            true);
                }
            }
        } else {
            downloadMojangStuffOld();
        }
        addPercent(5);
        if (isServer && hasJarMods()) {
            fireTask(App.settings.getLocalizedString("server.extractingjar"));
            fireSubProgressUnknown();
            Utils.unzip(getMinecraftJar(), getTempJarDirectory());
        }
        if (!isServer) {
            deleteMetaInf();
        }
        addPercent(5);
        if (selectedMods.size() != 0) {
            addPercent(40);
            fireTask(App.settings.getLocalizedString("instance.downloadingmods"));
            downloadMods(selectedMods);
            addPercent(40);
            installMods(selectedMods);
        } else {
            addPercent(80);
        }
        if (isServer && hasJarMods()) {
            fireTask(App.settings.getLocalizedString("server.zippingjar"));
            fireSubProgressUnknown();
            Utils.zip(getTempJarDirectory(), getMinecraftJar());
        }
        if (extractedTexturePack) {
            fireTask(App.settings.getLocalizedString("instance.zippingtexturepackfiles"));
            fireSubProgressUnknown();
            if (!getTexturePacksDirectory().exists()) {
                getTexturePacksDirectory().mkdir();
            }
            Utils.zip(getTempTexturePackDirectory(), new File(getTexturePacksDirectory(),
                    "TexturePack.zip"));
        }
        if (extractedResourcePack) {
            fireTask(App.settings.getLocalizedString("instance.zippingresourcepackfiles"));
            fireSubProgressUnknown();
            if (!getResourcePacksDirectory().exists()) {
                getResourcePacksDirectory().mkdir();
            }
            Utils.zip(getTempResourcePackDirectory(), new File(getResourcePacksDirectory(),
                    "ResourcePack.zip"));
        }
        configurePack();
        if (savedReis) {
            Utils.copyDirectory(new File(getTempDirectory(), "rei_minimap"), reis);
        }
        if (savedZans) {
            Utils.copyDirectory(new File(getTempDirectory(), "VoxelMods"), zans);
        }
        if (savedNEICfg) {
            Utils.copyFile(new File(getTempDirectory(), "NEI.cfg"), neiCfg, true);
        }
        if (savedPortalGunSounds) {
            Utils.copyFile(new File(getTempDirectory(), "PortalGunSounds.pak"), portalGunSounds,
                    true);
        }
        if (isServer) {
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.bat"),
                    new File(getRootDirectory(), "LaunchServer.bat"), "%%SERVERJAR%%",
                    getServerJar());
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.sh"),
                    new File(getRootDirectory(), "LaunchServer.sh"), "%%SERVERJAR%%",
                    getServerJar());
        }
        getConfigDirectory().delete();
        return true;
    }

    private void fireTask(String name) {
        firePropertyChange("doing", null, name);
    }

    private void fireProgress(int percent) {
        firePropertyChange("progress", null, percent);
    }

    private void fireSubProgress(int percent) {
        firePropertyChange("subprogress", null, percent);
    }

    private void fireSubProgressUnknown() {
        firePropertyChange("subprogressint", null, null);
    }

    private void addPercent(int percent) {
        this.percent = this.percent + percent;
        if (this.percent > 100) {
            this.percent = 100;
        }
        fireProgress(this.percent);
    }

    public void setSubPercent(int percent) {
        fireSubProgress(percent);
    }

    public void setDoingResources(String doing) {
        fireTask(doing);
        doneResources++;
        int progress = (100 * doneResources) / totalResources;
        fireSubProgress(progress);
    }

    public void setDownloadDone() {
        doneDownloads++;
        int progress = (100 * doneDownloads) / totalDownloads;
        fireSubProgress(progress);
    }

}
