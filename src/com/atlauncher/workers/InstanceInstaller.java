/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.workers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.atlauncher.App;
import com.atlauncher.data.Action;
import com.atlauncher.data.DecompType;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Download;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.Type;
import com.atlauncher.data.mojang.AssetIndex;
import com.atlauncher.data.mojang.AssetObject;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.atlauncher.data.mojang.Library;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.gui.ModsChooser;
import com.atlauncher.utils.Base64;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {

    private String instanceName;
    private Pack pack;
    private PackVersion version;
    private boolean isReinstall;
    private boolean isServer;
    private String jarOrder;
    private boolean instanceIsCorrupt = false; // If the instance should be set as corrupt
    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedOptionsTxt = false; // If options.txt was found and saved
    private boolean savedServersDat = false; // If servers.dat was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved
    private boolean extractedTexturePack = false; // If there is an extracted texturepack
    private boolean extractedResourcePack = false; // If there is an extracted resourcepack
    private String caseAllFiles = null;
    private int permgen = 0;
    private int memory = 0;
    private String librariesNeeded = null;
    private String nativesNeeded = null;
    private String extraArguments = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private int percent = 0; // Percent done installing
    private ArrayList<Mod> allMods;
    private ArrayList<Mod> selectedMods;
    private int totalDownloads = 0; // Total number of downloads to download
    private int doneDownloads = 0; // Total number of downloads downloaded
    private int totalBytes = 0; // Total number of bytes to download
    private int downloadedBytes = 0; // Total number of bytes downloaded
    private Instance instance = null;
    private ArrayList<DisableableMod> modsInstalled;
    private ArrayList<File> serverLibraries;
    private ArrayList<Action> actions;
    private final Gson gson; // GSON Parser
    private ArrayList<String> forgeLibraries = new ArrayList<String>();

    public InstanceInstaller(String instanceName, Pack pack, PackVersion version,
            boolean isReinstall, boolean isServer) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        if (isServer) {
            serverLibraries = new ArrayList<File>();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
        builder.registerTypeAdapter(File.class, new FileTypeAdapter());
        builder.setPrettyPrinting();
        this.gson = builder.create();
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public boolean isServer() {
        return this.isServer;
    }

    public boolean isLegacy() {
        return this.version.getMinecraftVersion().isLegacy();
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public ArrayList<DisableableMod> getModsInstalled() {
        return this.modsInstalled;
    }

    public String getInstanceSafeName() {
        return this.instanceName.replaceAll("[^A-Za-z0-9]", "");
    }

    public File getRootDirectory() {
        if (isServer) {
            return new File(App.settings.getServersDir(), pack.getSafeName() + "_"
                    + version.getSafeVersion());
        }
        return new File(App.settings.getInstancesDir(), getInstanceSafeName());
    }

    public File getTempDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.getSafeVersion());
    }

    public File getTempJarDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.getSafeVersion() + "_JarTemp");
    }

    public File getTempActionsDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.getSafeVersion() + "_ActionsTemp");
    }

    public File getTempTexturePackDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.getSafeVersion() + "_TexturePackTemp");
    }

    public File getTempResourcePackDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_"
                + version.getSafeVersion() + "_ResourcePackTemp");
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

    public File getIC2LibDirectory() {
        return new File(getModsDirectory(), "ic2");
    }

    public File getDenLibDirectory() {
        return new File(getModsDirectory(), "denlib");
    }

    public File getDependencyDirectory() {
        return new File(getModsDirectory(), this.version.getMinecraftVersion().getVersion());
    }

    public File getPluginsDirectory() {
        return new File(getRootDirectory(), "plugins");
    }

    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    public File getDisabledModsDirectory() {
        return new File(getRootDirectory(), "disabledmods");
    }

    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public boolean hasActions() {
        if (this.actions == null) {
            return false;
        }
        return this.actions.size() != 0;
    }

    public PackVersion getVersion() {
        return this.version;
    }

    public File getMinecraftJar() {
        if (isServer) {
            return new File(getRootDirectory(), "minecraft_server."
                    + this.version.getMinecraftVersion().getVersion() + ".jar");
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
            if (!isLegacy()) {
                jarOrder = jarOrder + "," + file;
            } else {
                jarOrder = file + "," + jarOrder;
            }
        }
    }

    public boolean wasModInstalled(String mod) {
        if (instance != null) {
            return instance.wasModInstalled(mod);
        }
        return false;
    }

    public boolean isReinstall() {
        return this.isReinstall;
    }

    public boolean isModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.getName().equalsIgnoreCase(name)) {
                return true;
            }
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
            directories = new File[] { getRootDirectory(), getModsDirectory(), getTempDirectory(),
                    getLibrariesDirectory() };
        } else {
            directories = new File[] { getRootDirectory(), getModsDirectory(),
                    getDisabledModsDirectory(), getTempDirectory(), getJarModsDirectory(),
                    getBinDirectory(), getNativesDirectory() };
        }
        for (File directory : directories) {
            directory.mkdir();
        }
        if (this.version.getMinecraftVersion().usesCoreMods()) {
            getCoreModsDirectory().mkdir();
        }
    }

    private ArrayList<Downloadable> getDownloadableMods() {
        ArrayList<Downloadable> mods = new ArrayList<Downloadable>();

        String files = "";

        for (Mod mod : this.selectedMods) {
            if (mod.isServerDownload()) {
                files = files + mod.getURL() + "|||";
            }
        }

        HashMap<String, Integer> fileSizes = null;

        if (!files.isEmpty()) {
            String base64Files = Base64.encodeBytes(files.getBytes());
            fileSizes = new HashMap<String, Integer>();
            String returnValue = null;
            do {
                try {
                    returnValue = Utils.sendPostData(App.settings.getFileURL("getfilesizes.php"),
                            base64Files, "files");
                } catch (IOException e1) {
                    App.settings.logStackTrace(e1);
                }
                if (returnValue == null) {
                    if (!App.settings.getNextServer()) {
                        App.settings
                                .log("Couldn't get filesizes of files from all ATLauncher servers. Continuing regardless!",
                                        LogMessageType.warning, false);
                    }
                }
            } while (returnValue == null);
            if (returnValue != null) {
                try {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(returnValue));
                    Document document = builder.parse(is);
                    document.getDocumentElement().normalize();
                    NodeList nodeList = document.getElementsByTagName("file");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element element = (Element) node;
                            String url = element.getAttribute("url");
                            int size = Integer.parseInt(element.getAttribute("size"));
                            fileSizes.put(url, size);
                        }
                    }
                } catch (SAXException e) {
                    App.settings.logStackTrace(e);
                } catch (ParserConfigurationException e) {
                    App.settings.logStackTrace(e);
                } catch (IOException e) {
                    App.settings.logStackTrace(e);
                }
            }
        }

        for (Mod mod : this.selectedMods) {
            if (mod.isServerDownload()) {
                Downloadable downloadable;
                int size = -1;
                if (fileSizes.containsKey(mod.getURL())) {
                    size = fileSizes.get(mod.getURL());
                }
                if (mod.hasMD5()) {
                    downloadable = new Downloadable(mod.getURL(), new File(
                            App.settings.getDownloadsDir(), mod.getFile()), mod.getMD5(), size,
                            this, true);
                } else {
                    downloadable = new Downloadable(mod.getURL(), new File(
                            App.settings.getDownloadsDir(), mod.getFile()), null, size, this, true);
                }
                mods.add(downloadable);
            }
        }

        return mods;
    }

    private void loadActions() {
        this.actions = new ArrayList<Action>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(
                    this.version.getVersion(), false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("action");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String mod = element.getAttribute("mod");
                    String action = element.getAttribute("action");
                    Type type = null;
                    if (element.hasAttribute("type")) {
                        type = Type.valueOf(element.getAttribute("type"));
                    }
                    String after = element.getAttribute("after");
                    String saveAs = element.getAttribute("saveas");
                    Boolean client = (element.getAttribute("client").equalsIgnoreCase("yes") ? true
                            : false);
                    Boolean server = (element.getAttribute("server").equalsIgnoreCase("yes") ? true
                            : false);
                    Action thing = null;
                    if (element.hasAttribute("type")) {
                        thing = new Action(action, type, after, saveAs, client, server);
                    } else {
                        thing = new Action(action, after, saveAs, client, server);
                    }
                    for (String modd : mod.split(",")) {
                        if (isModByName(modd)) {
                            thing.addMod(getModByName(modd));
                        }
                    }
                    actions.add(thing);
                }
            }
        } catch (SAXException e) {
            App.settings.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

    private void doActions() {
        for (Action action : this.actions) {
            action.execute(this);
        }
    }

    private boolean hasOptionalMods() {
        for (Mod mod : allMods) {
            if (mod.isOptional()) {
                return true;
            }
        }
        return false;
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

    public ArrayList<Mod> getModsInCategory(String category) {
        ArrayList<Mod> mods = new ArrayList<Mod>();
        for (Mod mod : allMods) {
            if (mod.getCategory() == null) {
                continue; // Has no category so hurry along
            }
            if (!mod.isOptional()) {
                continue; // Only for Optional Mods
            }
            if (mod.getCategory().equalsIgnoreCase(category)) {
                mods.add(mod);
            }
        }
        return mods;
    }

    public ArrayList<String> getCategories() {
        ArrayList<String> categories = new ArrayList<String>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(
                    this.version.getVersion(), false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("category");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    categories.add(element.getAttribute("id"));
                }
            }
        } catch (SAXException e) {
            App.settings.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return categories;
    }

    public String getCategoryName(String id) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(
                    this.version.getVersion(), false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("category");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getAttribute("id").equalsIgnoreCase(id)) {
                        return element.getAttribute("name");
                    }
                }
            }
        } catch (SAXException e) {
            App.settings.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return null;
    }

    public String getCategoryDescription(String id) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(
                    this.version.getVersion(), false)));
            Document document = builder.parse(is);
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("category");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getAttribute("id").equalsIgnoreCase(id)) {
                        return element.getAttribute("description");
                    }
                }
            }
        } catch (SAXException e) {
            App.settings.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return null;
    }

    private void downloadResources() {
        fireTask(App.settings.getLocalizedString("instance.downloadingresources"));
        fireSubProgressUnknown();
        ExecutorService executor = Executors.newFixedThreadPool(8);
        ArrayList<Downloadable> downloads = getResources();
        totalBytes = 0;
        downloadedBytes = 0;

        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                totalBytes += download.getFilesize();
            }
        }

        fireSubProgress(0); // Show the subprogress bar
        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    if (download.needToDownload()) {
                        fireTask(App.settings.getLocalizedString("common.downloading") + " "
                                + download.getFilename());
                        download.download(true);
                    } else {
                        download.copyFile();
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadLibraries() {
        fireTask(App.settings.getLocalizedString("instance.downloadinglibraries"));
        fireSubProgressUnknown();
        ExecutorService executor;
        ArrayList<Downloadable> downloads = getLibraries();
        totalBytes = 0;
        downloadedBytes = 0;

        executor = Executors.newFixedThreadPool(8);

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    if (download.needToDownload()) {
                        totalBytes += download.getFilesize();
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(0); // Show the subprogress bar

        executor = Executors.newFixedThreadPool(8);

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    if (download.needToDownload()) {
                        fireTask(App.settings.getLocalizedString("common.downloading") + " "
                                + download.getFilename());
                        download.download(true);
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadMods(ArrayList<Mod> mods) {
        fireSubProgressUnknown();
        ExecutorService executor;
        ArrayList<Downloadable> downloads = getDownloadableMods();
        totalBytes = 0;
        downloadedBytes = 0;

        executor = Executors.newFixedThreadPool(8);

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    if (download.needToDownload()) {
                        totalBytes += download.getFilesize();
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(0); // Show the subprogress bar

        executor = Executors.newFixedThreadPool(8);

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    if (download.needToDownload()) {
                        download.download(true);
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(-1); // Hide the subprogress bar

        for (Mod mod : mods) {
            if (!downloads.contains(mod) && !isCancelled()) {
                fireTask(App.settings.getLocalizedString("common.downloading") + " "
                        + (mod.isFilePattern() ? mod.getName() : mod.getFile()));
                mod.download(this);
                fireSubProgress(-1); // Hide the subprogress bar
            }
        }
    }

    private void organiseLibraries() {
        fireTask(App.settings.getLocalizedString("instance.organisinglibraries"));
        fireSubProgressUnknown();
        if (!isServer) {
            for (String libraryFile : forgeLibraries) {
                Utils.copyFile(new File(App.settings.getLibrariesDir(), libraryFile),
                        getBinDirectory());
            }
            for (Library library : this.version.getMinecraftVersion().getMojangVersion()
                    .getLibraries()) {
                if (library.shouldInstall()) {
                    if (library.shouldExtract()) {
                        Utils.unzip(library.getFile(), getNativesDirectory(),
                                library.getExtractRule());
                    } else {
                        Utils.copyFile(library.getFile(), getBinDirectory());
                    }
                }
            }
        }
        if (isServer) {
            Utils.copyFile(new File(App.settings.getJarsDir(), "minecraft_server."
                    + this.version.getMinecraftVersion().getVersion() + ".jar"), getRootDirectory());
        } else {
            Utils.copyFile(new File(App.settings.getJarsDir(), this.version.getMinecraftVersion()
                    .getVersion() + ".jar"), new File(getBinDirectory(), "minecraft.jar"), true);
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void doCaseConversions(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isFile()
                    && (file.getName().endsWith("jar") || file.getName().endsWith("zip") || file
                            .getName().endsWith("litemod"))) {
                if (this.caseAllFiles != null) {
                    if (this.caseAllFiles.equalsIgnoreCase("upper")) {
                        file.renameTo(new File(file.getParentFile(), file.getName()
                                .substring(0, file.getName().lastIndexOf(".")).toUpperCase()
                                + file.getName().substring(file.getName().lastIndexOf("."),
                                        file.getName().length())));
                    } else if (this.caseAllFiles.equalsIgnoreCase("lower")) {
                        file.renameTo(new File(file.getParentFile(), file.getName().toLowerCase()));
                    }
                }
            }
        }
    }

    private ArrayList<Downloadable> getResources() {
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>(); // All the files
        File objectsFolder = new File(App.settings.getResourcesDir(), "objects");
        File indexesFolder = new File(App.settings.getResourcesDir(), "indexes");
        File virtualFolder = new File(App.settings.getResourcesDir(), "virtual");
        String assetVersion = this.version.getMinecraftVersion().getMojangVersion().getAssets();
        File virtualRoot = new File(virtualFolder, assetVersion);
        File indexFile = new File(indexesFolder, assetVersion + ".json");
        objectsFolder.mkdirs();
        indexesFolder.mkdirs();
        virtualFolder.mkdirs();
        try {
            new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("indexes/" + assetVersion
                    + ".json"), indexFile, null, this, false).download(false);
            AssetIndex index = (AssetIndex) this.gson.fromJson(new FileReader(indexFile),
                    AssetIndex.class);

            if (index.isVirtual()) {
                virtualRoot.mkdirs();
            }

            for (Map.Entry<String, AssetObject> entry : index.getObjects().entrySet()) {
                AssetObject object = entry.getValue();
                String filename = object.getHash().substring(0, 2) + "/" + object.getHash();
                File file = new File(objectsFolder, filename);
                File virtualFile = new File(virtualRoot, entry.getKey());
                if (object.needToDownload(file)) {
                    downloads.add(new Downloadable(MojangConstants.RESOURCES_BASE.getURL(filename),
                            file, object.getHash(), (int) object.getSize(), this, false,
                            virtualFile, index.isVirtual()));
                } else {
                    if (index.isVirtual()) {
                        virtualFile.mkdirs();
                        Utils.copyFile(file, virtualFile, true);
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            App.settings.logStackTrace(e);
        } catch (JsonIOException e) {
            App.settings.logStackTrace(e);
        } catch (FileNotFoundException e) {
            App.settings.logStackTrace(e);
        }

        return downloads;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Downloadable> getLibraries() {
        ArrayList<Downloadable> libraries = new ArrayList<Downloadable>();

        // Now read in the library jars needed from the pack
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(pack.getXML(
                    this.version.getVersion(), false)));
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
                    forgeLibraries.add(file);
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
                        libraries.add(new Downloadable(App.settings.getFileURL(url), downloadTo,
                                md5, this, false));
                    } else {
                        libraries.add(new Downloadable(url, downloadTo, md5, this, false));
                    }
                }
            }
        } catch (SAXException e) {
            App.settings.logStackTrace(e);
        } catch (ParserConfigurationException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }

        // Now read in the library jars needed from Mojang
        if (!isServer) {
            for (Library library : this.version.getMinecraftVersion().getMojangVersion()
                    .getLibraries()) {
                if (library.shouldInstall()) {
                    if (!library.shouldExtract()) {
                        if (librariesNeeded == null) {
                            this.librariesNeeded = library.getFile().getName();
                        } else {
                            this.librariesNeeded += "," + library.getFile().getName();
                        }
                    }
                    libraries.add(new Downloadable(library.getURL(), library.getFile(), null, this,
                            false));
                }
            }
        }

        if (isServer) {
            libraries.add(new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("versions/"
                    + this.version.getMinecraftVersion().getVersion() + "/minecraft_server."
                    + this.version.getMinecraftVersion().getVersion() + ".jar"), new File(
                    App.settings.getJarsDir(), "minecraft_server."
                            + this.version.getMinecraftVersion().getVersion() + ".jar"), null,
                    this, false));
        } else {
            libraries.add(new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("versions/"
                    + this.version.getMinecraftVersion().getVersion() + "/"
                    + this.version.getMinecraftVersion().getVersion() + ".jar"), new File(
                    App.settings.getJarsDir(), this.version.getMinecraftVersion().getVersion()
                            + ".jar"), null, this, false));
        }
        return libraries;
    }

    public static String getEtag(String etag) {
        if (etag == null) {
            etag = "-";
        } else if ((etag.startsWith("\"")) && (etag.endsWith("\""))) {
            etag = etag.substring(1, etag.length() - 1);
        }
        return etag;
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
            App.settings.logStackTrace(e);
        }
    }

    public void configurePack() {
        // Download the configs zip file
        fireTask(App.settings.getLocalizedString("instance.downloadingconfigs"));
        File configs = new File(App.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version.getVersion()
                + "/Configs.zip";
        Downloadable configsDownload = new Downloadable(path, configs, null, this, true);
        this.totalBytes = configsDownload.getFilesize();
        this.downloadedBytes = 0;
        configsDownload.download(true); // Download the file

        // Extract the configs zip file
        fireSubProgressUnknown();
        fireTask(App.settings.getLocalizedString("instance.extractingconfigs"));
        Utils.unzip(configs, getRootDirectory());
        Utils.delete(configs);
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
            return "minecraft_server." + this.version.getMinecraftVersion().getVersion() + ".jar";
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

    public boolean hasForge() {
        for (Mod mod : selectedMods) {
            if (!mod.installOnServer() && isServer) {
                continue;
            }
            if (mod.getType() == Type.forge) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Mod> getMods() {
        return this.allMods;
    }

    public boolean shouldCoruptInstance() {
        return this.instanceIsCorrupt;
    }

    public String getCaseAllFiles() {
        return this.caseAllFiles;
    }

    public int getPermGen() {
        return this.permgen;
    }

    public int getMemory() {
        return this.memory;
    }

    public String getLibrariesNeeded() {
        return this.librariesNeeded;
    }

    public String getExtraArguments() {
        return this.extraArguments;
    }

    public String getMinecraftArguments() {
        return this.version.getMinecraftVersion().getMojangVersion().getMinecraftArguments();
    }

    public String getMainClass() {
        if (this.mainClass == null) {
            return this.version.getMinecraftVersion().getMojangVersion().getMainClass();
        }
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

        ArrayList<Mod> modss = new ArrayList<Mod>();

        for (Mod mod : mods) {
            if (!mod.isOptional()) {
                modss.add(mod); // Add all non optional mods
            }
        }

        for (String category : getCategories()) {
            for (Mod mod : mods) {
                if (mod.isOptional()) {
                    if (!mod.getCategory().isEmpty()) {
                        if (mod.getCategory().equalsIgnoreCase(category)) {
                            modss.add(mod); // Add optional mods based upon their category
                        }
                    }
                }
            }
        }

        for (Mod mod : mods) {
            if (!modss.contains(mod)) {
                modss.add(mod); // Add the rest
            }
        }

        return modss;
    }

    protected Boolean doInBackground() throws Exception {
        if (this.isReinstall) {
            if (this.pack.getUpdateMessage(this.version.getVersion()) != null) {
                if (this.isCancelled()) {
                    return false;
                }
                String[] options = { App.settings.getLocalizedString("common.ok"),
                        App.settings.getLocalizedString("common.cancel") };
                JEditorPane ep = new JEditorPane("text/html", "<html>"
                        + this.pack.getUpdateMessage(this.version.getVersion()) + "</html>");
                ep.setEditable(false);
                ep.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            Utils.openBrowser(e.getURL());
                        }
                    }
                });
                int ret = JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        ep,
                        App.settings.getLocalizedString("common.reinstalling") + " "
                                + this.pack.getName(), JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    App.settings.log("Instance Install Cancelled After Viewing Message!",
                            LogMessageType.error, false);
                    cancel(true);
                    return false;
                }
            }
        } else {
            if (this.pack.getInstallMessage(this.version.getVersion()) != null) {
                if (this.isCancelled()) {
                    return false;
                }
                String[] options = { App.settings.getLocalizedString("common.ok"),
                        App.settings.getLocalizedString("common.cancel") };
                JEditorPane ep = new JEditorPane("text/html", "<html>"
                        + this.pack.getInstallMessage(this.version.getVersion()) + "</html>");
                ep.setEditable(false);
                ep.addHyperlinkListener(new HyperlinkListener() {
                    @Override
                    public void hyperlinkUpdate(HyperlinkEvent e) {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            Utils.openBrowser(e.getURL());
                        }
                    }
                });
                int ret = JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        ep,
                        App.settings.getLocalizedString("common.installing") + " "
                                + this.pack.getName(), JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    App.settings.log("Instance Install Cancelled After Viewing Message!",
                            LogMessageType.error, false);
                    cancel(true);
                    return false;
                }
            }
        }
        this.allMods = sortMods(this.pack.getMods(this.version.getVersion(), isServer));
        loadActions(); // Load all the actions up for the pack
        this.permgen = this.pack.getPermGen(this.version.getVersion());
        this.memory = this.pack.getMemory(this.version.getVersion());
        this.caseAllFiles = this.pack.getCaseAllFiles(this.version.getVersion());
        selectedMods = new ArrayList<Mod>();
        if (allMods.size() != 0 && hasOptionalMods()) {
            ModsChooser modsChooser = new ModsChooser(this);
            modsChooser.setVisible(true);
            if (modsChooser.wasClosed()) {
                this.cancel(true);
                return false;
            }
            selectedMods = modsChooser.getSelectedMods();
        }
        if (!hasOptionalMods()) {
            selectedMods = allMods;
        }
        modsInstalled = new ArrayList<DisableableMod>();
        for (Mod mod : selectedMods) {
            modsInstalled.add(new DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(),
                    mod.getFile(), mod.getType(), mod.getColour(), mod.getDescription(), false));
        }
        this.instanceIsCorrupt = true; // From this point on the instance is corrupt
        getTempDirectory().mkdirs(); // Make the temp directory
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
        File optionsTXT = new File(getRootDirectory(), "options.txt");
        if (optionsTXT.exists() && optionsTXT.isFile()) {
            if (Utils.copyFile(optionsTXT, getTempDirectory())) {
                savedOptionsTxt = true;
            }
        }
        File serversDAT = new File(getRootDirectory(), "servers.dat");
        if (serversDAT.exists() && serversDAT.isFile()) {
            if (Utils.copyFile(serversDAT, getTempDirectory())) {
                savedServersDat = true;
            }
        }
        File portalGunSounds = new File(getModsDirectory(), "PortalGunSounds.pak");
        if (portalGunSounds.exists() && portalGunSounds.isFile()) {
            savedPortalGunSounds = true;
            Utils.copyFile(portalGunSounds, getTempDirectory());
        }
        makeDirectories();
        addPercent(5);
        this.mainClass = pack.getMainClass(this.version.getVersion());
        this.extraArguments = pack.getExtraArguments(this.version.getVersion());
        if (this.version.getMinecraftVersion().hasResources()) {
            downloadResources(); // Download Minecraft Resources
            if (isCancelled()) {
                return false;
            }
        }
        downloadLibraries(); // Download Libraries
        if (isCancelled()) {
            return false;
        }
        organiseLibraries(); // Organise the libraries
        if (isCancelled()) {
            return false;
        }
        if (isServer) {
            for (File file : serverLibraries) {
                file.mkdirs();
                Utils.copyFile(new File(App.settings.getLibrariesDir(), file.getName()), file, true);
            }
        }
        addPercent(5);
        if (isServer && hasJarMods()) {
            fireTask(App.settings.getLocalizedString("server.extractingjar"));
            fireSubProgressUnknown();
            Utils.unzip(getMinecraftJar(), getTempJarDirectory());
        }
        if (!isServer && hasJarMods() && !hasForge()) {
            deleteMetaInf();
        }
        addPercent(5);
        if (selectedMods.size() != 0) {
            addPercent(40);
            fireTask(App.settings.getLocalizedString("instance.downloadingmods"));
            downloadMods(selectedMods);
            if (isCancelled()) {
                return false;
            }
            addPercent(40);
            installMods(selectedMods);
        } else {
            addPercent(80);
        }
        if (isCancelled()) {
            return false;
        }
        doCaseConversions(getModsDirectory());
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
        if (isCancelled()) {
            return false;
        }
        if (hasActions()) {
            doActions();
        }
        if (isCancelled()) {
            return false;
        }
        if (this.pack.hasConfigs(this.version.getVersion())) {
            configurePack();
        }
        // Copy over common configs if any
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getRootDirectory());
        }
        if (savedReis) {
            Utils.copyDirectory(new File(getTempDirectory(), "rei_minimap"), reis);
        }
        if (savedZans) {
            Utils.copyDirectory(new File(getTempDirectory(), "VoxelMods"), zans);
        }
        if (savedNEICfg) {
            Utils.copyFile(new File(getTempDirectory(), "NEI.cfg"), neiCfg, true);
        }
        if (savedOptionsTxt) {
            Utils.copyFile(new File(getTempDirectory(), "options.txt"), optionsTXT, true);
        }
        if (savedServersDat) {
            Utils.copyFile(new File(getTempDirectory(), "servers.dat"), serversDAT, true);
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
        return true;
    }

    public void resetDownloadedBytes(int bytes) {
        totalBytes = bytes;
        downloadedBytes = 0;
    }

    public void fireTask(String name) {
        firePropertyChange("doing", null, name);
    }

    private void fireProgress(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        firePropertyChange("progress", null, percent);
    }

    private void fireSubProgress(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        firePropertyChange("subprogress", null, percent);
    }

    private void fireSubProgress(int percent, String paint) {
        if (percent > 100) {
            percent = 100;
        }
        String[] info = new String[2];
        info[0] = "" + percent;
        info[1] = paint;
        firePropertyChange("subprogress", null, info);
    }

    public void fireSubProgressUnknown() {
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
        if (percent > 100) {
            percent = 100;
        }
        fireSubProgress(percent);
    }

    public void setDownloadDone() {
        this.doneDownloads++;
        float progress;
        if (this.totalDownloads > 0) {
            progress = ((float) this.doneDownloads / (float) this.totalDownloads) * 100;
        } else {
            progress = 0;
        }
        fireSubProgress((int) progress);
    }

    public void addDownloadedBytes(int bytes) {
        this.downloadedBytes += bytes;
        float progress;
        if (this.totalBytes > 0) {
            progress = ((float) this.downloadedBytes / (float) this.totalBytes) * 100;
        } else {
            progress = 0;
        }
        fireSubProgress((int) progress,
                String.format("%.2f", ((float) this.downloadedBytes / 1024 / 1024)) + " MB / "
                        + String.format("%.2f", ((float) this.totalBytes / 1024 / 1024)) + " MB");
    }
}
