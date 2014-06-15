/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlauncher.App;
import com.atlauncher.Update;
import com.atlauncher.adapter.ColorTypeAdapter;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.LauncherBottomBar;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Settings class for storing all data for the Launcher and the settings of the user
 * 
 * @author Ryan
 */
public class Settings {

    // Users Settings
    private Server server; // Server to use for the Launcher
    private String forgeLoggingLevel; // Logging level to use when running Minecraft with Forge
    private int ram; // RAM to use when launching Minecraft
    private int permGen; // PermGenSize to use when launching Minecraft in MB
    private int windowWidth; // Width of the Minecraft window
    private int windowHeight; // Height of the Minecraft window
    private boolean maximiseMinecraft; // If Minecraft should start maximised
    private boolean saveCustomMods; // If custom mods should be saved between updates/reinstalls
    private boolean usingCustomJavaPath; // If the user is using a custom java path
    private String javaPath; // Users path to Java
    private String javaParamaters; // Extra Java paramaters when launching Minecraft
    private boolean advancedBackup; // If advanced backup is enabled
    private boolean sortPacksAlphabetically; // If to sort packs default alphabetically
    private boolean keepLauncherOpen; // If we should close the Launcher after Minecraft has closed
    private boolean enableConsole; // If to show the console by default
    private boolean enableTrayIcon; // If to enable tray icon
    private boolean enableLeaderboards; // If to enable the leaderboards
    private boolean enableLogs; // If to enable logs
    private boolean enableOpenEyeReporting; // If to enable OpenEye reporting
    private boolean enableProxy = false; // If proxy is in use
    private String proxyHost; // The proxies host
    private int proxyPort; // The proxies port
    private String proxyType; // The type of proxy (socks, http)
    private int concurrentConnections; // Number of concurrent connections to open when downloading
    private Account account; // Account using the Launcher
    private String addedPacks; // The Semi Public packs the user has added to the Launcher
    private Proxy proxy = null; // The proxy object if any
    private String theme; // The theme to use
    private String dateFormat; // The date format to use
    private boolean hideOldJavaWarning; // If the user has hidden the old Java warning
    private boolean enableServerChecker; // If to enable server checker
    private int serverCheckerWait; // Time to wait in minutes between checking server status

    // General backup settings
    private boolean autoBackup; // Whether backups are created on instance close
    private String lastSelectedSync; // The last service selected for syncing
    private boolean notifyBackup; // Whether to notify the user on successful backup or restore

    // Dropbox settings
    private String dropboxFolderLocation; // Location of dropbox if defined by user

    // Packs, Instances and Accounts
    private List<DownloadableFile> launcherFiles; // Files the Launcher needs to download
    private List<News> news; // News
    private List<MinecraftVersion> minecraftVersions; // Minecraft versions
    private List<Pack> packs; // Packs in the Launcher
    private List<Instance> instances = new ArrayList<Instance>(); // Users Installed Instances
    private List<Account> accounts = new ArrayList<Account>(); // Accounts in the Launcher
    private List<MinecraftServer> checkingServers = new ArrayList<MinecraftServer>();

    // Directories and Files for the Launcher
    private File baseDir, backupsDir, configsDir, themesDir, jsonDir, versionsDir, imagesDir,
            skinsDir, jarsDir, commonConfigsDir, resourcesDir, librariesDir, languagesDir,
            downloadsDir, usersDownloadsFolder, instancesDir, serversDir, tempDir,
            instancesDataFile, checkingServersFile, userDataFile, propertiesFile;

    // Launcher Settings
    private JFrame parent; // Parent JFrame of the actual Launcher
    private Properties properties = new Properties(); // Properties to store everything in
    private LauncherConsole console; // The Launcher's Console
    private ArrayList<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
    private ArrayList<Server> triedServers = new ArrayList<Server>(); // Servers tried to connect to
    private InstancesTab instancesPanel; // The instances panel
    private NewsTab newsPanel; // The news panel
    private PacksTab packsPanel; // The packs panel
    private LauncherBottomBar bottomBar; // The bottom bar
    private boolean hadPasswordDialog = false; // If the user has seen the password dialog
    private boolean firstTimeRun = false; // If this is the first time the Launcher has been run
    private boolean offlineMode = false; // If offline mode is enabled
    private Process minecraftProcess = null; // The process minecraft is running on
    private Server originalServer = null; // Original Server user has saved
    private boolean minecraftLaunched = false; // If Minecraft has been Launched
    private String version = Constants.VERSION; // Version of the Launcher
    private String userAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 Safari/537.36";
    private boolean minecraftLoginServerUp = false; // If the Minecraft Login server is up
    private boolean minecraftSessionServerUp = false; // If the Minecraft Session server is up
    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static Gson altGson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .registerTypeAdapter(Date.class, new DateTypeAdapter())
            .registerTypeAdapter(File.class, new FileTypeAdapter()).create();
    public static Gson themeGson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Color.class, new ColorTypeAdapter()).create();
    @SuppressWarnings("unused")
    private DropboxSync dropbox;
    private boolean languageLoaded = false;
    private Timer checkingServersTimer = null; // Timer used for checking servers

    public Settings() {
        setupFiles(); // Setup all the file and directory variables
        checkFolders(); // Checks the setup of the folders and makes sure they're there
        clearTempDir(); // Cleans all files in the Temp Dir
        rotateLogFiles(); // Rotates the log files
        loadStartingProperties(); // Get users Console preference and Java Path
    }

    public void loadConsole() {
        console = new LauncherConsole();
    }

    public void setupFiles() {
        baseDir = Utils.getCoreGracefully();
        usersDownloadsFolder = new File(System.getProperty("user.home"), "Downloads");
        backupsDir = new File(baseDir, "Backups");
        configsDir = new File(baseDir, "Configs");
        themesDir = new File(configsDir, "Themes");
        jsonDir = new File(configsDir, "JSON");
        versionsDir = new File(configsDir, "Versions");
        imagesDir = new File(configsDir, "Images");
        skinsDir = new File(imagesDir, "Skins");
        jarsDir = new File(configsDir, "Jars");
        commonConfigsDir = new File(configsDir, "Common");
        resourcesDir = new File(configsDir, "Resources");
        librariesDir = new File(configsDir, "Libraries");
        languagesDir = new File(configsDir, "Languages");
        downloadsDir = new File(baseDir, "Downloads");
        instancesDir = new File(baseDir, "Instances");
        serversDir = new File(baseDir, "Servers");
        tempDir = new File(baseDir, "Temp");
        instancesDataFile = new File(configsDir, "instancesdata");
        checkingServersFile = new File(configsDir, "checkingservers.json");
        userDataFile = new File(configsDir, "userdata");
        propertiesFile = new File(configsDir, "ATLauncher.conf");
    }

    public void loadEverything() {
        setupServers(); // Setup the servers available to use in the Launcher
        loadServerProperty(false); // Get users Server preference
        if (hasUpdatedFiles()) {
            downloadUpdatedFiles(); // Downloads updated files on the server
        }
        loadNews(); // Load the news
        this.languageLoaded = true; // Languages are now loaded
        loadMinecraftVersions(); // Load info about the different Minecraft versions
        loadPacks(); // Load the Packs available in the Launcher
        loadUsers(); // Load the Testers and Allowed Players for the packs
        loadInstances(); // Load the users installed Instances
        loadAccounts(); // Load the saved Accounts
        loadCheckingServers(); // Load the saved servers we're checking with the tool
        loadProperties(); // Load the users Properties
        console.setupLanguage(); // Setup language on the console
        checkResources(); // Check for new format of resources
        OUTER: for (Pack pack : this.packs) {
            if (pack.isTester()) {
                for (Server server : this.servers) {
                    if (server.getName() == "Master Server (Testing Only)") {
                        server.setUserSelectable(true);
                        break OUTER; // Don't need to check anymore so break the outer loop
                    }
                }
            }
        }
        loadServerProperty(true); // Get users Server preference
        if (Utils.isWindows() && this.javaPath.contains("x86")) {
            String[] options = { App.settings.getLocalizedString("common.yes"),
                    App.settings.getLocalizedString("common.no") };
            int ret = JOptionPane.showOptionDialog(
                    App.settings.getParent(),
                    "<html><p align=\"center\">"
                            + App.settings
                                    .getLocalizedString("settings.running32bit", "<br/><br/>")
                            + "</p></html>", App.settings
                            .getLocalizedString("settings.running32bittitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            if (ret == 0) {
                Utils.openBrowser("http://www.atlauncher.com/help/32bit/");
                System.exit(0);
            }
        }
        if (!Utils.isJava7OrAbove() && !this.hideOldJavaWarning) {
            String[] options = { App.settings.getLocalizedString("common.download"),
                    App.settings.getLocalizedString("common.ok"),
                    App.settings.getLocalizedString("instance.dontremindmeagain") };
            int ret = JOptionPane.showOptionDialog(
                    App.settings.getParent(),
                    "<html><p align=\"center\">"
                            + App.settings.getLocalizedString("settings.unsupportedjava",
                                    "<br/><br/>") + "</p></html>",
                    App.settings.getLocalizedString("settings.unsupportedjavatitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            if (ret == 0) {
                Utils.openBrowser("http://www.oracle.com/technetwork/java/javase/downloads/jre7-downloads-1880261.html");
                System.exit(0);
            } else if (ret == 2) {
                this.hideOldJavaWarning = true;
                this.saveProperties();
            }
        }
        if (this.advancedBackup) {
            dropbox = new DropboxSync();
        }
        if (!this.hadPasswordDialog) {
            checkAccounts(); // Check accounts with stored passwords
        }

        if (this.enableServerChecker) {
            this.startCheckingServers();
        }
    }

    public void startCheckingServers() {
        if (this.checkingServersTimer != null) {
            // If it's not null, cancel and purge tasks left
            this.checkingServersTimer.cancel();
            this.checkingServersTimer.purge(); // not sure if needed or not
        }

        if (this.enableServerChecker) {
            this.checkingServersTimer = new Timer();
            this.checkingServersTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (MinecraftServer server : checkingServers) {
                        server.checkServer();
                    }
                }
            }, 0, this.getServerCheckerWaitInMilliseconds());
        }
    }

    public void checkAccounts() {
        boolean matches = false;
        if (this.accounts != null || this.accounts.size() >= 1) {
            for (Account account : this.accounts) {
                if (account.isRemembered()) {
                    matches = true;
                }
            }
        }
        if (matches) {
            String[] options = { App.settings.getLocalizedString("common.ok"),
                    App.settings.getLocalizedString("account.removepasswords") };
            int ret = JOptionPane.showOptionDialog(
                    App.settings.getParent(),
                    "<html><p align=\"center\">"
                            + App.settings.getLocalizedString("account.securitywarning", "<br/>")
                            + "</p></html>",
                    App.settings.getLocalizedString("account.securitywarningtitle"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            if (ret == 1) {
                for (Account account : this.accounts) {
                    if (account.isRemembered()) {
                        account.setRemember(false);
                    }
                }
                this.saveAccounts();
            }
        }
        this.saveProperties();
    }

    public void checkResources() {
        File indexesDir = new File(this.resourcesDir, "indexes");
        if (!indexesDir.exists() || !indexesDir.isDirectory()) {
            final ProgressDialog dialog = new ProgressDialog(
                    getLocalizedString("settings.rearrangingresources"), 0,
                    getLocalizedString("settings.rearrangingresources"), null);
            Thread thread = new Thread() {
                public void run() {
                    File indexesDir = new File(getResourcesDir(), "indexes");
                    File objectsDir = new File(getResourcesDir(), "objects");
                    File virtualDir = new File(getResourcesDir(), "virtual");
                    File legacyDir = new File(virtualDir, "legacy");
                    File tempDir = new File(getTempDir(), "assets");
                    tempDir.mkdir();
                    Utils.moveDirectory(getResourcesDir(), tempDir);
                    indexesDir.mkdirs();
                    objectsDir.mkdirs();
                    virtualDir.mkdirs();
                    legacyDir.mkdirs();
                    Utils.moveDirectory(tempDir, legacyDir);
                    Utils.delete(tempDir);
                    Utils.spreadOutResourceFiles(legacyDir);
                    dialog.close();
                }
            };
            dialog.addThread(thread);
            dialog.start();

        }
    }

    public void checkMojangStatus() {
        JSONParser parser = new JSONParser();
        try {
            Downloadable download = new Downloadable("http://status.mojang.com/check", false);
            String response = download.getContents();
            if (response == null) {
                minecraftSessionServerUp = false;
                minecraftLoginServerUp = false;
                return;
            }
            Object obj = parser.parse(response);
            JSONArray jsonObject = (JSONArray) obj;
            Iterator<JSONObject> iterator = jsonObject.iterator();
            while (iterator.hasNext()) {
                JSONObject object = iterator.next();
                if (object.containsKey("authserver.mojang.com")) {
                    if (((String) object.get("authserver.mojang.com")).equalsIgnoreCase("green")) {
                        minecraftLoginServerUp = true;
                    }
                } else if (object.containsKey("session.minecraft.net")) {
                    if (((String) object.get("session.minecraft.net")).equalsIgnoreCase("green")) {
                        minecraftSessionServerUp = true;
                    }
                }
            }
        } catch (ParseException e) {
            this.logStackTrace(e);
            minecraftSessionServerUp = false;
            minecraftLoginServerUp = false;
        }
    }

    public Status getMojangStatus() {
        if (minecraftLoginServerUp && minecraftSessionServerUp)
            return Status.ONLINE;
        else if (!minecraftLoginServerUp && !minecraftSessionServerUp)
            return Status.OFFLINE;
        else
            return Status.PARTIAL;
    }

    public boolean launcherHasUpdate() {
        for (DownloadableFile file : this.launcherFiles) {
            if (file.isLauncher()) {
                if (getVersion().contains("-dev") || file.getSHA1().equalsIgnoreCase(getVersion())) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void downloadUpdate() {
        try {
            File thisFile = new File(Update.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath());
            String path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
            String toget;
            String saveAs = thisFile.getName();
            if (path.contains(".exe")) {
                toget = "exe";
            } else {
                toget = "jar";
            }
            File newFile = new File(getTempDir(), saveAs);
            log("Downloading Launcher Update");
            Downloadable update = new Downloadable("ATLauncher." + toget, newFile, null, null, true);
            update.download(false);
            runUpdate(path, newFile.getAbsolutePath());
        } catch (IOException e) {
            this.logStackTrace(e);
        }
    }

    public void runUpdate(String currentPath, String temporaryUpdatePath) {
        List<String> arguments = new ArrayList<String>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);
        arguments.add("-cp");
        arguments.add(temporaryUpdatePath);
        arguments.add("com.atlauncher.Update");
        arguments.add(currentPath);
        arguments.add(temporaryUpdatePath);

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        log("Running launcher update with command " + arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            this.logStackTrace(e);
        }

        System.exit(0);
    }

    private void getFileHashes() {
        this.launcherFiles = null;
        Downloadable download = new Downloadable("launcher/json/hashes.json", true);
        java.lang.reflect.Type type = new TypeToken<List<DownloadableFile>>() {
        }.getType();
        this.launcherFiles = gson.fromJson(download.getContents(), type);
    }

    /**
     * This checks the servers hashes.xml file and gets the files that the Launcher needs to have
     */
    private ArrayList<Downloadable> getLauncherFiles() {
        getFileHashes(); // Get File Hashes
        if (this.launcherFiles == null) {
            this.offlineMode = true;
            return null;
        }
        if (launcherHasUpdate()) {
            if (!App.wasUpdated) {
                downloadUpdate(); // Update the Launcher
            } else {
                String[] options = { "Ok" };
                int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                        "<html><p align=\"center\">Launcher Update failed. Please click Ok to close "
                                + "the launcher and open up the downloads page.<br/><br/>Download "
                                + "the update and replace the old ATLauncher file.</p></html>",
                        "Update Failed!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);
                if (ret == 0) {
                    Utils.openBrowser("http://www.atlauncher.com/downloads/");
                    System.exit(0);
                }
            }
        }
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>();
        for (DownloadableFile file : this.launcherFiles) {
            if (!file.isLauncher()) {
                downloads.add(file.getDownloadable());
            }
        }
        return downloads;
    }

    public void downloadUpdatedFiles() {
        ArrayList<Downloadable> downloads = getLauncherFiles();
        if (downloads != null) {
            ExecutorService executor = Executors.newFixedThreadPool(this.concurrentConnections);
            for (final Downloadable download : downloads) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (download.needToDownload()) {
                            log("Downloading Launcher File " + download.getFile().getName());
                            download.download(false);
                        }
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }
    }

    /**
     * This checks the servers hashes.xml file and looks for new/updated files that differ from what
     * the user has
     */
    public boolean hasUpdatedFiles() {
        if (isInOfflineMode()) {
            return false;
        }
        log("Checking for updated files!");
        ArrayList<Downloadable> downloads = getLauncherFiles();
        if (downloads == null) {
            this.offlineMode = true;
            return false;
        }
        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                log("Updates found!");
                return true; // 1 file needs to be updated so there is updated files
            }
        }
        log("No updates found!");
        return false; // No updates
    }

    public void reloadLauncherData() {
        final JDialog dialog = new JDialog(this.parent, ModalityType.APPLICATION_MODAL);
        dialog.setSize(300, 100);
        dialog.setTitle("Updating Launcher");
        dialog.setLocationRelativeTo(App.settings.getParent());
        dialog.setLayout(new FlowLayout());
        dialog.setResizable(false);
        dialog.add(new JLabel("Updating Launcher... Please Wait"));
        App.TASKPOOL.execute(new Runnable() {

            @Override
            public void run() {
                if (hasUpdatedFiles()) {
                    downloadUpdatedFiles(); // Downloads updated files on the server
                }
                loadNews(); // Load the news
                reloadNewsPanel(); // Reload news panel
                loadPacks(); // Load the Packs available in the Launcher
                reloadPacksPanel(); // Reload packs panel
                loadUsers(); // Load the Testers and Allowed Players for the packs
                loadInstances(); // Load the users installed Instances
                reloadInstancesPanel(); // Reload instances panel
                dialog.setVisible(false); // Remove the dialog
                dialog.dispose(); // Dispose the dialog
            }
        });
        dialog.setVisible(true);
    }

    /**
     * Checks the directory to make sure all the necessary folders are there
     */
    private void checkFolders() {
        File[] files = { backupsDir, configsDir, themesDir, jsonDir, commonConfigsDir, imagesDir,
                skinsDir, jarsDir, resourcesDir, librariesDir, languagesDir, downloadsDir,
                instancesDir, serversDir, tempDir };
        for (File file : files) {
            if (!file.exists()) {
                file.mkdir();
            }
            if (!file.isDirectory()) {
                if (file.delete()) {
                    file.mkdir();
                }

            }
        }
    }

    /**
     * Returns the base directory
     * 
     * @return File object for the base directory
     */
    public File getBaseDir() {
        return this.baseDir;
    }

    /**
     * Returns the backups directory
     * 
     * @return File object for the backups directory
     */
    public File getBackupsDir() {
        return this.backupsDir;
    }

    /**
     * Returns the configs directory
     * 
     * @return File object for the configs directory
     */
    public File getConfigsDir() {
        return this.configsDir;
    }

    /**
     * Returns the themes directory
     * 
     * @return File object for the themes directory
     */
    public File getThemesDir() {
        return this.themesDir;
    }

    /**
     * Returns the JSON directory
     * 
     * @return File object for the JSON directory
     */
    public File getJSONDir() {
        return this.jsonDir;
    }

    /**
     * Returns the Versions directory
     * 
     * @return File object for the Versions directory
     */
    public File getVersionsDir() {
        return this.versionsDir;
    }

    /**
     * Returns the common configs directory
     * 
     * @return File object for the common configs directory
     */
    public File getCommonConfigsDir() {
        return this.commonConfigsDir;
    }

    /**
     * Returns the images directory
     * 
     * @return File object for the images directory
     */
    public File getImagesDir() {
        return this.imagesDir;
    }

    /**
     * Returns the skins directory
     * 
     * @return File object for the skins directory
     */
    public File getSkinsDir() {
        return this.skinsDir;
    }

    /**
     * Returns the jars directory
     * 
     * @return File object for the jars directory
     */
    public File getJarsDir() {
        return this.jarsDir;
    }

    /**
     * Returns the resources directory
     * 
     * @return File object for the resources directory
     */
    public File getResourcesDir() {
        return this.resourcesDir;
    }

    public File getVirtualAssetsDir() {
        return new File(this.resourcesDir, "virtual");
    }

    public File getObjectsAssetsDir() {
        return new File(this.resourcesDir, "objects");
    }

    public File getLegacyVirtualAssetsDir() {
        return new File(getVirtualAssetsDir(), "legacy");
    }

    /**
     * Returns the libraries directory
     * 
     * @return File object for the libraries directory
     */
    public File getLibrariesDir() {
        return this.librariesDir;
    }

    /**
     * Returns the languages directory
     * 
     * @return File object for the languages directory
     */
    public File getLanguagesDir() {
        return this.languagesDir;
    }

    /**
     * Returns the downloads directory
     * 
     * @return File object for the downloads directory
     */
    public File getDownloadsDir() {
        return this.downloadsDir;
    }

    /**
     * Returns the downloads directory for the user
     * 
     * @return File object for the downloads directory for the users account
     */
    public File getUsersDownloadsDir() {
        return this.usersDownloadsFolder;
    }

    /**
     * Returns the instances directory
     * 
     * @return File object for the instances directory
     */
    public File getInstancesDir() {
        return this.instancesDir;
    }

    /**
     * Returns the servers directory
     * 
     * @return File object for the servers directory
     */
    public File getServersDir() {
        return this.serversDir;
    }

    /**
     * Returns the temp directory
     * 
     * @return File object for the temp directory
     */
    public File getTempDir() {
        return this.tempDir;
    }

    /**
     * Deletes all files in the Temp directory
     */
    public void clearTempDir() {
        Utils.deleteContents(getTempDir());
    }

    public void rotateLogFiles() {
        File logFile1 = new File(getBaseDir(), "ATLauncher-Log-1.txt");
        File logFile2 = new File(getBaseDir(), "ATLauncher-Log-2.txt");
        File logFile3 = new File(getBaseDir(), "ATLauncher-Log-3.txt");
        if (logFile3.exists()) {
            Utils.delete(logFile3);
        }
        if (logFile2.exists()) {
            logFile2.renameTo(logFile3);
        }
        if (logFile1.exists()) {
            logFile1.renameTo(logFile2);
        }
        try {
            logFile1.createNewFile();
        } catch (IOException e) {
            String[] options = { "OK" };
            JOptionPane.showOptionDialog(null,
                    "<html><p align=\"center\">Cannot create the log file.<br/><br/>Make sure"
                            + " you are running the Launcher from somewhere with<br/>write"
                            + " permissions for your user account such as your Home/Users folder"
                            + " or desktop.</p></html>", "Warning", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            System.exit(0);
        }
    }

    /**
     * Returns the instancesdata file
     * 
     * @return File object for the instancesdata file
     */
    public File getInstancesDataFile() {
        return instancesDataFile;
    }

    /**
     * Returns the checkingservers file
     * 
     * @return File object for the checkingservers file
     */
    public File getCheckingServersFile() {
        return checkingServersFile;
    }

    /**
     * Sets the main parent JFrame reference for the Launcher
     * 
     * @param parent
     *            The Launcher main JFrame
     */
    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Load the users Server preference from file
     */
    public void loadServerProperty(boolean userSelectableOnly) {
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            String serv = properties.getProperty("server", "Auto");
            if (isServerByName(serv)) {
                if (!userSelectableOnly || (userSelectableOnly && server.isUserSelectable())) {
                    this.server = getServerByName(serv);
                    this.originalServer = this.server;
                }
            }
            if (this.server == null) {
                log("Server " + serv + " is invalid", LogMessageType.warning, false);
                this.server = getServerByName("Auto"); // Server not found, use default of Auto
                this.originalServer = this.server;
            }
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Load the users Console preference from file
     */
    public void loadStartingProperties() {
        try {
            if (!propertiesFile.exists()) {
                propertiesFile.createNewFile();
            }
        } catch (IOException e) {
            String[] options = { "OK" };
            JOptionPane.showOptionDialog(null,
                    "<html><p align=\"center\">Cannot create the config file.<br/><br/>Make sure"
                            + " you are running the Launcher from somewhere with<br/>write"
                            + " permissions for your user account such as your Home/Users folder"
                            + " or desktop.</p></html>", "Warning", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            System.exit(0);
        }
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            this.theme = properties.getProperty("theme", "ATLauncher");
            this.dateFormat = properties.getProperty("dateformat", "dd/M/yyy");
            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                    "true"));
            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon",
                    "true"));
            if (!properties.containsKey("usingcustomjavapath")) {
                this.usingCustomJavaPath = false;
                this.javaPath = Utils.getJavaHome();
            } else {
                this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty(
                        "usingcustomjavapath", "false"));
                if (isUsingCustomJavaPath()) {
                    this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
                } else {
                    this.javaPath = Utils.getJavaHome();
                }
            }

            this.enableProxy = Boolean.parseBoolean(properties.getProperty("enableproxy", "false"));

            if (this.enableProxy) {
                this.proxyHost = properties.getProperty("proxyhost", null);

                this.proxyPort = Integer.parseInt(properties.getProperty("proxyport", "0"));
                if (this.proxyPort <= 0 || this.proxyPort > 65535) {
                    this.enableProxy = false;
                }

                this.proxyType = properties.getProperty("proxytype", "");
                if (!this.proxyType.equals("SOCKS") && !this.proxyType.equals("HTTP")
                        && !this.proxyType.equals("DIRECT")) {
                    this.enableProxy = false;
                }
            } else {
                this.proxyHost = "";
                this.proxyPort = 0;
                this.proxyType = "";
            }

            this.concurrentConnections = Integer.parseInt(properties.getProperty(
                    "concurrentconnections", "8"));
            if (this.concurrentConnections < 1) {
                this.concurrentConnections = 8;
            }
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Load the properties from file
     */
    public void loadProperties() {
        try {
            this.properties.load(new FileInputStream(propertiesFile));
            this.firstTimeRun = Boolean
                    .parseBoolean(properties.getProperty("firsttimerun", "true"));

            this.hadPasswordDialog = Boolean.parseBoolean(properties.getProperty(
                    "hadpassworddialog", "false"));

            this.hideOldJavaWarning = Boolean.parseBoolean(properties.getProperty(
                    "hideoldjavawarning", "false"));

            String lang = properties.getProperty("language", "English");
            if (isLanguageByName(lang)) {
                Language.INSTANCE.load(lang);
            } else {
                log("Invalid language " + lang + ". Defaulting to English!",
                        LogMessageType.warning, false);
            }

            this.forgeLoggingLevel = properties.getProperty("forgelogginglevel", "INFO");
            if (!this.forgeLoggingLevel.equalsIgnoreCase("SEVERE")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("WARNING")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("INFO")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("CONFIG")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINE")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINER")
                    && !this.forgeLoggingLevel.equalsIgnoreCase("FINEST")) {
                log("Invalid Forge Logging level " + this.forgeLoggingLevel
                        + ". Defaulting to INFO!", LogMessageType.warning, false);
                this.forgeLoggingLevel = "INFO";
            }

            if (Utils.is64Bit()) {
                int halfRam = (Utils.getMaximumRam() / 1000) * 512;
                int defaultRam = (halfRam >= 4096 ? 4096 : halfRam); // Default ram
                this.ram = Integer.parseInt(properties.getProperty("ram", defaultRam + ""));
                if (this.ram > Utils.getMaximumRam()) {
                    log("Tried to allocate " + this.ram + "MB of Ram but only "
                            + Utils.getMaximumRam() + "MB is available to use!",
                            LogMessageType.warning, false);
                    this.ram = defaultRam; // User tried to allocate too much ram, set it back to
                    // half, capped at 4GB
                }
            } else {
                this.ram = Integer.parseInt(properties.getProperty("ram", "1024"));
                if (this.ram > Utils.getMaximumRam()) {
                    log("Tried to allocate " + this.ram + "MB of Ram but only "
                            + Utils.getMaximumRam() + "MB is available to use!",
                            LogMessageType.warning, false);
                    this.ram = 1024; // User tried to allocate too much ram, set it back to 1GB
                }
            }

            // Default PermGen to 256 for 64 bit systems and 64 for 32 bit systems
            this.permGen = Integer.parseInt(properties.getProperty("permGen",
                    (Utils.is64Bit() ? "256" : "128")));

            this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
            if (this.windowWidth > Utils.getMaximumWindowWidth()) {
                log("Tried to set window width to " + this.windowWidth
                        + " pixels but the maximum is " + Utils.getMaximumWindowWidth()
                        + " pixels!", LogMessageType.warning, false);
                this.windowWidth = Utils.getMaximumWindowWidth(); // User tried to make screen size
                // wider than they have
            }

            this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));
            if (this.windowHeight > Utils.getMaximumWindowHeight()) {
                log("Tried to set window height to " + this.windowHeight
                        + " pixels but the maximum is " + Utils.getMaximumWindowHeight()
                        + " pixels!", LogMessageType.warning, false);
                this.windowHeight = Utils.getMaximumWindowHeight(); // User tried to make screen
                // size wider than they have
            }

            this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty(
                    "usingcustomjavapath", "false"));

            if (isUsingCustomJavaPath()) {
                this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
            } else {
                this.javaPath = Utils.getJavaHome();
                if (this.isUsingMacApp()) {
                    File oracleJava = new File(
                            "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
                    if (oracleJava.exists() && oracleJava.canExecute()) {
                        this.setJavaPath("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home");
                    }
                }
            }

            this.javaParamaters = properties.getProperty("javaparameters", "");

            this.maximiseMinecraft = Boolean.parseBoolean(properties.getProperty(
                    "maximiseminecraft", "false"));

            this.saveCustomMods = Boolean.parseBoolean(properties.getProperty("savecustommods",
                    "true"));

            this.advancedBackup = Boolean.parseBoolean(properties.getProperty("advancedbackup",
                    "false"));

            this.sortPacksAlphabetically = Boolean.parseBoolean(properties.getProperty(
                    "sortpacksalphabetically", "false"));

            this.keepLauncherOpen = Boolean.parseBoolean(properties.getProperty("keeplauncheropen",
                    "true"));

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole",
                    "true"));

            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon",
                    "true"));

            this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty(
                    "enableleaderboards", "false"));

            this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));

            this.enableServerChecker = Boolean.parseBoolean(properties.getProperty(
                    "enableserverchecker", "false"));

            this.enableOpenEyeReporting = Boolean.parseBoolean(properties.getProperty(
                    "enableopeneyereporting", "true"));

            this.enableProxy = Boolean.parseBoolean(properties.getProperty("enableproxy", "false"));

            if (this.enableProxy) {
                this.proxyHost = properties.getProperty("proxyhost", null);

                this.proxyPort = Integer.parseInt(properties.getProperty("proxyport", "0"));
                if (this.proxyPort <= 0 || this.proxyPort > 65535) {
                    // Proxy port is invalid so disable proxy
                    log("Tried to set proxy port to " + this.proxyPort
                            + " which is not a valid port! Proxy support disabled!",
                            LogMessageType.warning, false);
                    this.enableProxy = false;
                }

                this.proxyType = properties.getProperty("proxytype", "");
                if (!this.proxyType.equals("SOCKS") && !this.proxyType.equals("HTTP")
                        && !this.proxyType.equals("DIRECT")) {
                    // Proxy type is invalid so disable proxy
                    log("Tried to set proxy type to " + this.proxyType
                            + " which is not valid! Proxy support disabled!",
                            LogMessageType.warning, false);
                    this.enableProxy = false;
                }
            } else {
                this.proxyHost = "";
                this.proxyPort = 0;
                this.proxyType = "";
            }

            this.serverCheckerWait = Integer.parseInt(properties.getProperty("servercheckerwait",
                    "5"));
            if (this.serverCheckerWait < 1 || this.serverCheckerWait > 30) {
                // Server checker wait should be between 1 and 30
                log("Tried to set server checker wait to "
                        + this.serverCheckerWait
                        + " which is not valid! Must be between 1 and 30. Setting back to default of 5!",
                        LogMessageType.warning, false);
                this.serverCheckerWait = 5;
            }

            this.concurrentConnections = Integer.parseInt(properties.getProperty(
                    "concurrentconnections", "8"));
            if (this.concurrentConnections < 1) {
                // Concurrent connections should be more than or equal to 1
                log("Tried to set the number of concurrent connections to "
                        + this.concurrentConnections
                        + " which is not valid! Must be 1 or more. Setting back to default of 8!",
                        LogMessageType.warning, false);
                this.concurrentConnections = 8;
            }

            this.theme = properties.getProperty("theme", "ATLauncher");

            this.dateFormat = properties.getProperty("dateformat", "dd/M/yyy");

            String lastAccountTemp = properties.getProperty("lastaccount", "");
            if (!lastAccountTemp.isEmpty()) {
                if (isAccountByName(lastAccountTemp)) {
                    this.account = getAccountByName(lastAccountTemp);
                } else {
                    log("The Account " + lastAccountTemp
                            + " is no longer available. Logging out of Account!",
                            LogMessageType.warning, false);
                    this.account = null; // Account not found
                }
            }

            this.addedPacks = properties.getProperty("addedpacks", "");
            this.autoBackup = Boolean.parseBoolean(properties.getProperty("autobackup", "false"));
            this.notifyBackup = Boolean
                    .parseBoolean(properties.getProperty("notifybackup", "true"));
            this.dropboxFolderLocation = properties.getProperty("dropboxlocation", "");
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    /**
     * Save the properties to file
     */
    public void saveProperties() {
        try {
            properties.setProperty("firsttimerun", "false");
            properties.setProperty("hadpassworddialog", "true");
            properties.setProperty("hideoldjavawarning", this.hideOldJavaWarning + "");
            properties.setProperty("language", Language.INSTANCE.getCurrent());
            properties.setProperty("server", this.server.getName());
            properties.setProperty("forgelogginglevel", this.forgeLoggingLevel);
            properties.setProperty("ram", this.ram + "");
            properties.setProperty("permGen", this.permGen + "");
            properties.setProperty("windowwidth", this.windowWidth + "");
            properties.setProperty("windowheight", this.windowHeight + "");
            properties.setProperty("usingcustomjavapath", (this.usingCustomJavaPath) ? "true"
                    : "false");
            properties.setProperty("javapath", this.javaPath);
            properties.setProperty("javaparameters", this.javaParamaters);
            properties
                    .setProperty("maximiseminecraft", (this.maximiseMinecraft) ? "true" : "false");
            properties.setProperty("savecustommods", (this.saveCustomMods) ? "true" : "false");
            properties.setProperty("advancedbackup", (this.advancedBackup) ? "true" : "false");
            properties.setProperty("sortpacksalphabetically",
                    (this.sortPacksAlphabetically) ? "true" : "false");
            properties.setProperty("keeplauncheropen", (this.keepLauncherOpen) ? "true" : "false");
            properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
            properties.setProperty("enabletrayicon", (this.enableTrayIcon) ? "true" : "false");
            properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true"
                    : "false");
            properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            properties.setProperty("enableserverchecker", (this.enableServerChecker) ? "true"
                    : "false");
            properties.setProperty("enableopeneyereporting", (this.enableOpenEyeReporting) ? "true"
                    : "false");
            properties.setProperty("enableproxy", (this.enableProxy) ? "true" : "false");
            properties.setProperty("proxyhost", this.proxyHost);
            properties.setProperty("proxyport", this.proxyPort + "");
            properties.setProperty("proxytype", this.proxyType);
            properties.setProperty("servercheckerwait", this.serverCheckerWait + "");
            properties.setProperty("concurrentconnections", this.concurrentConnections + "");
            properties.setProperty("theme", this.theme);
            properties.setProperty("dateformat", this.dateFormat);
            if (account != null) {
                properties.setProperty("lastaccount", account.getUsername());
            } else {
                properties.setProperty("lastaccount", "");
            }
            properties.setProperty("addedpacks", this.addedPacks);
            properties.setProperty("autobackup", this.autoBackup ? "true" : "false");
            properties.setProperty("notifybackup", this.notifyBackup ? "true" : "false");
            properties.setProperty("dropboxlocation", this.dropboxFolderLocation);
            this.properties.store(new FileOutputStream(propertiesFile), "ATLauncher Settings");
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public void addCheckingServer(MinecraftServer server) {
        this.checkingServers.add(server);
        this.saveCheckingServers();
    }

    /**
     * Switch account currently used and save it
     * 
     * @param account
     *            Account to switch to
     */
    public void switchAccount(Account account) {
        if (account == null) {
            log("Logging out of account");
            this.account = null;
        } else {
            if (account.isReal()) {
                log("Changed account to " + account);
                this.account = account;
            } else {
                log("Logging out of account");
                this.account = null;
            }
        }
        reloadPacksPanel();
        reloadInstancesPanel();
        reloadAccounts();
        saveProperties();
    }

    /**
     * The servers available to use in the Launcher
     * <p/>
     * These MUST be hardcoded in order for the Launcher to make the initial connections to download
     * files
     */
    private void setupServers() {
        this.servers = new ArrayList<Server>(Arrays.asList(Constants.SERVERS));
    }

    public boolean disableServerGetNext() {
        this.server.disableServer(); // Disable the server
        for (Server server : this.servers) {
            if (!server.isDisabled() && server.isUserSelectable()) {
                log("Server " + this.server.getName() + " Not Available! Switching To "
                        + server.getName(), LogMessageType.warning, true);
                this.server = server; // Setup next available server
                return true;
            }
        }
        return false;
    }

    public void clearTriedServers() {
        this.triedServers = new ArrayList<Server>(); // Clear the list
    }

    public boolean getNextServer() {
        this.triedServers.add(this.server);
        for (Server server : this.servers) {
            if (!this.triedServers.contains(server) && !server.isDisabled()
                    && server.isUserSelectable()) {
                log("Server " + this.server.getName() + " Not Available! Switching To "
                        + server.getName(), LogMessageType.warning, true);
                this.server = server; // Setup next available server
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadNews() {
        try {
            java.lang.reflect.Type type = new TypeToken<List<News>>() {
            }.getType();
            this.news = gson.fromJson(new FileReader(new File(getJSONDir(), "news.json")), type);
        } catch (JsonSyntaxException e) {
            logStackTrace(e);
        } catch (JsonIOException e) {
            logStackTrace(e);
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        }
    }

    /**
     * Loads info about the different Minecraft versions
     */
    private void loadMinecraftVersions() {
        try {
            java.lang.reflect.Type type = new TypeToken<List<MinecraftVersion>>() {
            }.getType();
            this.minecraftVersions = gson.fromJson(new FileReader(new File(getJSONDir(),
                    "minecraftversions.json")), type);
        } catch (JsonSyntaxException e) {
            logStackTrace(e);
        } catch (JsonIOException e) {
            logStackTrace(e);
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        }
        log("[Background] Checking Minecraft Versions Started");
        ExecutorService executor = Executors.newFixedThreadPool(this.concurrentConnections);
        for (final MinecraftVersion mv : this.minecraftVersions) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mv.loadVersion();
                }
            });
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                log("[Background] Checking Minecraft Versions Complete");
            }
        });
        executor.shutdown();
        while (!executor.isShutdown()) {
        }
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    private void loadPacks() {
        try {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {
            }.getType();
            this.packs = gson.fromJson(new FileReader(new File(getJSONDir(), "packs.json")), type);
        } catch (JsonSyntaxException e) {
            logStackTrace(e);
        } catch (JsonIOException e) {
            logStackTrace(e);
        } catch (FileNotFoundException e) {
            logStackTrace(e);
        }
        for (Pack pack : this.packs) {
            pack.processVersions();
        }
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    private void loadUsers() {
        Downloadable download = new Downloadable("launcher/json/users.json", true);
        List<PackUsers> packUsers = null;
        try {
            java.lang.reflect.Type type = new TypeToken<List<PackUsers>>() {
            }.getType();
            packUsers = gson.fromJson(download.getContents(), type);
        } catch (JsonSyntaxException e) {
            logStackTrace(e);
        } catch (JsonIOException e) {
            logStackTrace(e);
        }
        if (packUsers == null) {
            this.offlineMode = true;
            return;
        }
        for (PackUsers pu : packUsers) {
            pu.addUsers();
        }
    }

    /**
     * Loads the user installed Instances
     */
    private void loadInstances() {
        this.instances = new ArrayList<Instance>(); // Reset the instances list
        if (instancesDataFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(instancesDataFile);
                ObjectInputStream objIn = new ObjectInputStream(in);
                try {
                    Object obj;
                    while ((obj = objIn.readObject()) != null) {
                        if (obj instanceof Instance) {
                            File dir = new File(getInstancesDir(), ((Instance) obj).getSafeName());
                            if (!dir.exists()) {
                                continue; // Skip the instance since the folder doesn't exist
                            }
                            Instance instance = (Instance) obj;
                            if (!instance.hasBeenConverted()) {
                                log("Instance "
                                        + instance.getName()
                                        + " is being converted! This is normal and should only appear once!",
                                        LogMessageType.warning, false);
                                instance.convert();
                            }
                            if (!instance.getDisabledModsDirectory().exists()) {
                                instance.getDisabledModsDirectory().mkdir();
                            }
                            instances.add(instance);
                            if (isPackByName(instance.getPackName())) {
                                instance.setRealPack(getPackByName(instance.getPackName()));
                            }
                        }
                    }
                } catch (EOFException e) {
                    // Don't log this, it always happens when it gets to the end of the file
                } finally {
                    objIn.close();
                    in.close();
                }
            } catch (Exception e) {
                logStackTrace(e);
            }
            saveInstances(); // Save the instances to new json format
            Utils.delete(instancesDataFile); // Remove old instances data file
        } else {
            for (String folder : this.getInstancesDir().list(Utils.getInstanceFileFilter())) {
                File instanceDir = new File(this.getInstancesDir(), folder);
                FileReader fileReader;
                try {
                    fileReader = new FileReader(new File(instanceDir, "instance.json"));
                } catch (FileNotFoundException e) {
                    logStackTrace(e);
                    continue; // Instance.json not found for some reason, continue before loading
                }
                Instance instance = Settings.gson.fromJson(fileReader, Instance.class);
                if (instance == null) {
                    continue;
                }
                if (!instance.getDisabledModsDirectory().exists()) {
                    instance.getDisabledModsDirectory().mkdir();
                }
                if (isPackByName(instance.getPackName())) {
                    instance.setRealPack(getPackByName(instance.getPackName()));
                }
                this.instances.add(instance);
            }
            if (instancesDataFile.exists()) {
                Utils.delete(instancesDataFile); // Remove old instances data file
            }
        }
    }

    public void saveInstances() {
        for (Instance instance : this.instances) {
            File instanceFile = new File(instance.getRootDirectory(), "instance.json");
            try {
                if (!instanceFile.exists()) {
                    instanceFile.createNewFile();
                }

                FileWriter fw = new FileWriter(instanceFile);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(Settings.gson.toJson(instance));
                bw.close();
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    /**
     * Loads the saved Accounts
     */
    private void loadAccounts() {
        if (userDataFile.exists()) {
            try {
                FileInputStream in = new FileInputStream(userDataFile);
                ObjectInputStream objIn = new ObjectInputStream(in);
                try {
                    Object obj;
                    while ((obj = objIn.readObject()) != null) {
                        if (obj instanceof Account) {
                            accounts.add((Account) obj);
                        }
                    }
                } catch (EOFException e) {
                    // Don't log this, it always happens when it gets to the end of the file
                } finally {
                    objIn.close();
                    in.close();
                }
            } catch (Exception e) {
                logStackTrace(e);
            }
        }
    }

    public void saveAccounts() {
        FileOutputStream out = null;
        ObjectOutputStream objOut = null;
        try {
            out = new FileOutputStream(userDataFile);
            objOut = new ObjectOutputStream(out);
            for (Account account : accounts) {
                objOut.writeObject(account);
            }
        } catch (IOException e) {
            logStackTrace(e);
        } finally {
            try {
                objOut.close();
                out.close();
            } catch (IOException e) {
                logStackTrace(e);
            }
        }
    }

    public void removeAccount(Account account) {
        if (this.account == account) {
            switchAccount(null);
        }
        accounts.remove(account);
        saveAccounts();
        reloadAccounts();
    }

    /**
     * Loads the user servers added for checking
     */
    private void loadCheckingServers() {
        this.checkingServers = new ArrayList<MinecraftServer>(); // Reset the list
        if (checkingServersFile.exists()) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(checkingServersFile);
            } catch (FileNotFoundException e) {
                logStackTrace(e);
                return;
            }

            this.checkingServers = gson.fromJson(fileReader, MinecraftServer.LIST_TYPE);

            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveCheckingServers() {
        try {
            if (!checkingServersFile.exists()) {
                checkingServersFile.createNewFile();
            }

            FileWriter fw = new FileWriter(checkingServersFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(Settings.gson.toJson(this.checkingServers));
            bw.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

    /**
     * Finds out if this is the first time the Launcher has been run
     * 
     * @return true if the Launcher hasn't been run and setup yet, false for otherwise
     */
    public boolean isFirstTimeRun() {
        return this.firstTimeRun;
    }

    public boolean isMinecraftLaunched() {
        return this.minecraftLaunched;
    }

    public void setMinecraftLaunched(boolean launched) {
        this.minecraftLaunched = launched;
        ((TrayMenu) App.TRAY_MENU).setMinecraftLaunched(launched);
    }

    /**
     * Get the Packs available in the Launcher
     * 
     * @return The Packs available in the Launcher
     */
    public List<Pack> getPacks() {
        return this.packs;
    }

    /**
     * Get the Packs available in the Launcher sorted alphabetically
     * 
     * @return The Packs available in the Launcher sorted alphabetically
     */
    public ArrayList<Pack> getPacksSortedAlphabetically() {
        ArrayList<Pack> packs = new ArrayList<Pack>(this.packs);
        Collections.sort(packs, new Comparator<Pack>() {
            public int compare(Pack result1, Pack result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });
        return packs;
    }

    /**
     * Get the Packs available in the Launcher sorted positionally
     * 
     * @return The Packs available in the Launcher sorted by position
     */
    public ArrayList<Pack> getPacksSortedPositionally() {
        ArrayList<Pack> packs = new ArrayList<Pack>(this.packs);
        Collections.sort(packs, new Comparator<Pack>() {
            public int compare(Pack result1, Pack result2) {
                return result1.getPosition() - result2.getPosition();
            }
        });
        return packs;
    }

    public void setPackVisbility(Pack pack, boolean collapsed) {
        if (pack != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().add(pack.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().remove(pack.getName());
                }
            }
            saveAccounts();
            reloadPacksPanel();
        }
    }

    public boolean isUsingMacApp() {
        return Utils.isMac() && new File(baseDir.getParentFile().getParentFile(), "MacOS").exists();
    }

    public void setInstanceVisbility(Instance instance, boolean collapsed) {
        if (instance != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().add(instance.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().remove(instance.getName());
                }
            }
            saveAccounts();
            reloadInstancesPanel();
        }
    }

    /**
     * Get the Instances available in the Launcher
     * 
     * @return The Instances available in the Launcher
     */
    public List<Instance> getInstances() {
        return this.instances;
    }

    /**
     * Get the Instances available in the Launcher sorted alphabetically
     * 
     * @return The Instances available in the Launcher sorted alphabetically
     */
    public ArrayList<Instance> getInstancesSorted() {
        ArrayList<Instance> instances = new ArrayList<Instance>(this.instances);
        Collections.sort(instances, new Comparator<Instance>() {
            public int compare(Instance result1, Instance result2) {
                return result1.getName().compareTo(result2.getName());
            }
        });
        return instances;
    }

    public void setInstanceUnplayable(Instance instance) {
        instance.setUnplayable(); // Set the instance as unplayable
        saveInstances(); // Save the instancesdata file
        reloadInstancesPanel(); // Reload the instances tab
    }

    /**
     * Removes an instance from the Launcher
     */
    public void removeInstance(Instance instance) {
        if (this.instances.remove(instance)) { // Remove the instance
            Utils.delete(instance.getRootDirectory());
            saveInstances(); // Save the instancesdata file
            reloadInstancesPanel(); // Reload the instances panel
        }
    }

    public String apiCall(String username, String action, String extra1, String extra2,
            String extra3, boolean debug) {
        String response = "";
        try {
            String data = URLEncoder.encode("username", "UTF-8") + "="
                    + URLEncoder.encode(username, "UTF-8");
            data += "&" + URLEncoder.encode("action", "UTF-8") + "="
                    + URLEncoder.encode(action, "UTF-8");
            data += "&" + URLEncoder.encode("extra1", "UTF-8") + "="
                    + URLEncoder.encode(extra1, "UTF-8");
            data += "&" + URLEncoder.encode("extra2", "UTF-8") + "="
                    + URLEncoder.encode(extra2, "UTF-8");
            data += "&" + URLEncoder.encode("extra3", "UTF-8") + "="
                    + URLEncoder.encode(extra3, "UTF-8");

            URL url = new URL(Constants.API_BASE_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response += line;
                if (debug) {
                    log("API Call Response: " + line);
                }
            }
            wr.close();
        } catch (Exception e) {
            logStackTrace(e);
        }
        return response;
    }

    public void apiCall(String username, String action, String extra1, String extra2, String extra3) {
        apiCall(username, action, extra1, extra2, extra3, false);
    }

    public void apiCall(String username, String action, String extra1, String extra2) {
        apiCall(username, action, extra1, extra2, "", false);
    }

    public void apiCall(String username, String action, String extra1) {
        apiCall(username, action, extra1, "", "", false);
    }

    public void apiCall(String username, String action) {
        apiCall(username, action, "", "", "", false);
    }

    public String apiCallReturn(String username, String action, String extra1, String extra2,
            String extra3) {
        return apiCall(username, action, extra1, extra2, extra3, false);
    }

    public String apiCallReturn(String username, String action, String extra1, String extra2) {
        return apiCall(username, action, extra1, extra2, "", false);
    }

    public String apiCallReturn(String username, String action, String extra1) {
        return apiCall(username, action, extra1, "", "", false);
    }

    public String apiCallReturn(String username, String action) {
        return apiCall(username, action, "", "", "", false);
    }

    public boolean canViewSemiPublicPackByCode(String packCode) {
        for (String code : this.addedPacks.split(",")) {
            if (Utils.getMD5(code).equalsIgnoreCase(packCode)) {
                return true;
            }
        }
        return false;
    }

    public MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        for (MinecraftVersion minecraftVersion : minecraftVersions) {
            if (minecraftVersion.getVersion().equalsIgnoreCase(version)) {
                return minecraftVersion;
            }
        }
        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }

    public boolean semiPublicPackExistsFromCode(String packCode) {
        String packCodeMD5 = Utils.getMD5(packCode);
        for (Pack pack : this.packs) {
            if (pack.isSemiPublic()) {
                if (pack.getCode().equalsIgnoreCase(packCodeMD5)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean addPack(String packCode) {
        String packCodeMD5 = Utils.getMD5(packCode);
        for (Pack pack : this.packs) {
            if (pack.isSemiPublic() && !App.settings.canViewSemiPublicPackByCode(packCodeMD5)) {
                if (pack.getCode().equalsIgnoreCase(packCodeMD5)) {
                    if (pack.isTester()) {
                        return false;
                    }
                    this.addedPacks += packCode + ",";
                    this.saveProperties();
                    this.reloadInstancesPanel();
                    return true;
                }
            }
        }
        return false;
    }

    public void removePack(String packCode) {
        for (String code : this.addedPacks.split(",")) {
            if (Utils.getMD5(code).equalsIgnoreCase(packCode)) {
                this.addedPacks = this.addedPacks.replace(code + ",", ""); // Remove the string
                this.saveProperties();
                this.reloadInstancesPanel();
            }
        }
    }

    /**
     * Get the Accounts added to the Launcher
     * 
     * @return The Accounts added to the Launcher
     */
    public List<Account> getAccounts() {
        return this.accounts;
    }

    /**
     * Get the News for the Launcher
     * 
     * @return The News items
     */
    public List<News> getNews() {
        return this.news;
    }

    /**
     * Get the News for the Launcher in HTML for display on the news panel.
     * 
     * @return The HTML for displaying on the News Panel
     */
    public String getNewsHTML() {
        String news = "<html>";
        for (News newsItem : App.settings.getNews()) {
            news += newsItem.getHTML();
            if (App.settings.getNews().get(App.settings.getNews().size() - 1) != newsItem) {
                news += "<hr/>";
            }
        }
        return news;
    }

    /**
     * Get the Languages available in the Launcher
     * 
     * @return The Languages available in the Launcher
     */
    public List<String> getLanguages() {
        List<String> langs = new LinkedList<String>();
        for (File file : this.getLanguagesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".lang");
            }
        })) {
            langs.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
        }
        return langs;
    }

    /**
     * Get the Servers available in the Launcher
     * 
     * @return The Servers available in the Launcher
     */
    public ArrayList<Server> getServers() {
        return this.servers;
    }

    /**
     * Determines if offline mode is enabled or not
     * 
     * @return true if offline mode is enabled, false otherwise
     */
    public boolean isInOfflineMode() {
        return this.offlineMode;
    }

    public void checkOnlineStatus() {
        for (Server server : servers) {
            server.enableServer();
        }
        this.offlineMode = false;
        Downloadable download = new Downloadable("launcher/users.xml", true);
        String test = download.getContents();
        if (test != null && test.equalsIgnoreCase("pong")) {
            this.offlineMode = false;
            reloadPacksPanel();
            reloadInstancesPanel();
        } else {
            this.offlineMode = true;
        }
    }

    /**
     * Sets the launcher to offline mode
     */
    public void setOfflineMode() {
        this.offlineMode = true;
    }

    /**
     * Sets the launcher to online mode
     */
    public void setOnlineMode() {
        this.offlineMode = false;
    }

    /**
     * Returns the JFrame reference of the main Launcher
     * 
     * @return Main JFrame of the Launcher
     */
    public Window getParent() {
        return this.parent;
    }

    /**
     * Sets the panel used for Instances
     * 
     * @param instancesPanel
     *            Instances Panel
     */
    public void setInstancesPanel(InstancesTab instancesPanel) {
        this.instancesPanel = instancesPanel;
    }

    /**
     * Reloads the panel used for Instances
     */
    public void reloadInstancesPanel() {
        if (instancesPanel != null) {
            this.instancesPanel.reload(); // Reload the instances panel
        }
    }

    /**
     * Sets the panel used for Packs
     * 
     * @param packsPanel
     *            Packs Panel
     */
    public void setPacksPanel(PacksTab packsPanel) {
        this.packsPanel = packsPanel;
    }

    /**
     * Sets the panel used for News
     * 
     * @param newsPanel
     *            News Panel
     */
    public void setNewsPanel(NewsTab newsPanel) {
        this.newsPanel = newsPanel;
    }

    /**
     * Reloads the panel used for News
     */
    public void reloadNewsPanel() {
        this.newsPanel.reload(); // Reload the news panel
    }

    /**
     * Reloads the panel used for Packs
     */
    public void reloadPacksPanel() {
        this.packsPanel.reload(); // Reload the instances panel
    }

    /**
     * Sets the bottom bar
     * 
     * @param bottomBar
     *            The Bottom Bar
     */
    public void setBottomBar(LauncherBottomBar bottomBar) {
        this.bottomBar = bottomBar;
    }

    /**
     * Reloads the bottom bar accounts combobox
     */
    public void reloadAccounts() {
        if (this.bottomBar == null) {
            return; // Bottom Bar hasnt been made yet, so don't do anything
        }
        this.bottomBar.reloadAccounts(); // Reload the Bottom Bar accounts combobox
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     * 
     * @param name
     *            The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public boolean isInstance(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given ID number
     * 
     * @param id
     *            ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack
     *             If ID is not found
     */
    public Pack getPackByID(int id) throws InvalidPack {
        for (Pack pack : packs) {
            if (pack.getID() == id) {
                return pack;
            }
        }
        throw new InvalidPack("No pack exists with ID " + id);
    }

    /**
     * Checks if there is a pack by the given name
     * 
     * @param name
     *            name of the Pack to find
     * @return True if the pack is found from the name
     */
    public boolean isPackByName(String name) {
        for (Pack pack : packs) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Pack from the given name
     * 
     * @param name
     *            name of the Pack to find
     * @return Pack if the pack is found from the name
     */
    public Pack getPackByName(String name) {
        for (Pack pack : packs) {
            if (pack.getName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Checks if there is an instance by the given name
     * 
     * @param name
     *            name of the Instance to find
     * @return True if the instance is found from the name
     */
    public boolean isInstanceByName(String name) {
        for (Instance instance : instances) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is an instance by the given name
     * 
     * @param name
     *            name of the Instance to find
     * @return True if the instance is found from the name
     */
    public boolean isInstanceBySafeName(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Instance from the given name
     * 
     * @param name
     *            name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public Instance getInstanceByName(String name) {
        for (Instance instance : instances) {
            if (instance.getName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Finds a Instance from the given name
     * 
     * @param name
     *            name of the Instance to find
     * @return Instance if the instance is found from the name
     */
    public Instance getInstanceBySafeName(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Finds a Server from the given name
     * 
     * @param name
     *            Name of the Server to find
     * @return Server if the server is found from the name
     */
    private Server getServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Finds an Account from the given username
     * 
     * @param username
     *            Username of the Account to find
     * @return Account if the Account is found from the username
     */
    private Account getAccountByName(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Finds if a language is available
     * 
     * @param name
     *            The name of the Language
     * @return true if found, false if not
     */
    public boolean isLanguageByName(String name) {
        return this.getLanguages().contains(name.toLowerCase());
    }

    /**
     * Finds if a server is available
     * 
     * @param name
     *            The name of the Server
     * @return true if found, false if not
     */
    public boolean isServerByName(String name) {
        for (Server server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds if an Account is available
     * 
     * @param username
     *            The username of the Account
     * @return true if found, false if not
     */
    public boolean isAccountByName(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the URL for a file on the user selected server
     * 
     * @param filename
     *            Filename including directories on the server
     * @return URL of the file
     */
    public String getFileURL(String filename) {
        return this.server.getFileURL(filename);
    }

    /**
     * Gets the URL for a file on the master server
     * 
     * @param filename
     *            Filename including directories on the server
     * @return URL of the file or null if no master server defined
     */
    public String getMasterFileURL(String filename) {
        for (Server server : this.servers) {
            if (server.isMaster()) {
                return server.getFileURL(filename);
            }
        }
        return null;
    }

    /**
     * Finds out if the Launcher Console is visible or not
     * 
     * @return true if the console is visible, false if it's been hidden
     */
    public boolean isConsoleVisible() {
        return this.console.isVisible();
    }

    /**
     * Gets the Launcher's current Console instance
     * 
     * @return The Launcher's Console instance
     */
    public LauncherConsole getConsole() {
        return this.console;
    }

    public void clearConsole() {
        this.console.clearConsole();
    }

    public void addConsoleListener(WindowAdapter wa) {
        this.console.addWindowListener(wa);
    }

    public String getLog() {
        return this.console.getLog();
    }

    /**
     * Log a non Minecraft related info message to the console
     */
    public void log(String message) {
        this.console.log(message, LogMessageType.info, false);
    }

    /**
     * Log a Minecraft related message to the console
     */
    public void logMinecraft(String message) {
        this.console.logMinecraft(message);
    }

    /**
     * Logs a stack trace to the console window
     * 
     * @param exception
     *            The exception to show in the console
     */
    public void logStackTrace(Exception exception) {
        exception.printStackTrace();
        log(exception.getMessage(), LogMessageType.error, false);
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.toString() != null) {
                log(element.toString(), LogMessageType.error, false);
            }
        }
    }

    /**
     * Logs a stack trace to the console window with a custom message before it
     * 
     * @param message
     *            A message regarding the stack trace to show before it providing more insight
     * @param exception
     *            The exception to show in the console
     */
    public void logStackTrace(String message, Exception exception) {
        exception.printStackTrace();
        log(message, LogMessageType.error, false);
        log(exception.getMessage(), LogMessageType.error, false);
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.toString() != null) {
                log(element.toString(), LogMessageType.error, false);
            }
        }
    }

    /**
     * Log something to the console
     */
    public void log(String message, LogMessageType type, boolean isMinecraft) {
        this.console.log(message, type, isMinecraft);
    }

    public void showKillMinecraft(Process minecraft) {
        this.minecraftProcess = minecraft;
        this.console.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        this.console.hideKillMinecraft();
    }

    public void killMinecraft() {
        if (this.minecraftProcess != null) {
            log("Killing Minecraft", LogMessageType.error, false);
            this.minecraftProcess.destroy();
            this.minecraftProcess = null;
        } else {
            log("Cannot kill Minecraft as there is no instance open!", LogMessageType.error, false);
        }
    }

    /**
     * Sets the users current active Language
     * 
     * @param language
     *            The language to set to
     */
    public void setLanguage(String language) {
        try {
            Language.INSTANCE.load(language);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Gets the users current active Server
     * 
     * @return The users set server
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Gets the users saved Server
     * 
     * @return The users saved server
     */
    public Server getOriginalServer() {
        return this.originalServer;
    }

    /**
     * Gets the users setting for Forge Logging Level
     * 
     * @return The users setting for Forge Logging Level
     */
    public String getForgeLoggingLevel() {
        return this.forgeLoggingLevel;
    }

    /**
     * Sets the users setting for Forge Logging Level
     */
    public void setForgeLoggingLevel(String forgeLoggingLevel) {
        this.forgeLoggingLevel = forgeLoggingLevel;
    }

    /**
     * Sets the users current active Server
     * 
     * @param server
     *            The server to set to
     */
    public void setServer(Server server) {
        this.server = server;
        this.originalServer = server;
    }

    public int getMemory() {
        return this.ram;
    }

    public void setMemory(int memory) {
        this.ram = memory;
    }

    public int getPermGen() {
        return this.permGen;
    }

    public void setPermGen(int permGen) {
        this.permGen = permGen;
    }

    public int getWindowWidth() {
        return this.windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return this.windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public boolean isUsingCustomJavaPath() {
        return this.usingCustomJavaPath;
    }

    public String getJavaPath() {
        return this.javaPath;
    }

    public void setJavaPath(String javaPath) {
        if (javaPath.equalsIgnoreCase(Utils.getJavaHome())) {
            this.usingCustomJavaPath = false;
        } else {
            this.usingCustomJavaPath = true;
        }
        this.javaPath = javaPath;
    }

    public String getJavaParameters() {
        return this.javaParamaters;
    }

    public void setJavaParameters(String javaParamaters) {
        this.javaParamaters = javaParamaters;
    }

    public Account getAccount() {
        return this.account;
    }

    /**
     * If the user has selected to start Minecraft maximised
     * 
     * @return true if yes, false if not
     */
    public boolean startMinecraftMaximised() {
        return this.maximiseMinecraft;
    }

    public void setStartMinecraftMaximised(boolean maximiseMinecraft) {
        this.maximiseMinecraft = maximiseMinecraft;
    }

    public boolean saveCustomMods() {
        return this.saveCustomMods;
    }

    public void setSaveCustomMods(boolean saveCustomMods) {
        this.saveCustomMods = saveCustomMods;
    }

    /**
     * If the user has selected to enable advanced backups
     * 
     * @return true if yes, false if not
     */
    public boolean isAdvancedBackupsEnabled() {
        return this.advancedBackup;
    }

    public void setAdvancedBackups(boolean advancedBackup) {
        this.advancedBackup = advancedBackup;
    }

    /**
     * If the user has selected to display packs alphabetically or nto
     * 
     * @return true if yes, false if not
     */
    public boolean sortPacksAlphabetically() {
        return this.sortPacksAlphabetically;
    }

    public void setSortPacksAlphabetically(boolean sortPacksAlphabetically) {
        this.sortPacksAlphabetically = sortPacksAlphabetically;
    }

    /**
     * If the user has selected to show the console always or not
     * 
     * @return true if yes, false if not
     */
    public boolean enableConsole() {
        return this.enableConsole;
    }

    /**
     * If the user has selected to keep the launcher open after Minecraft has closed
     * 
     * @return true if yes, false if not
     */
    public boolean keepLauncherOpen() {
        return this.keepLauncherOpen;
    }

    /**
     * If the user has selected to enable the tray icon
     * 
     * @return true if yes, false if not
     */
    public boolean enableTrayIcon() {
        return this.enableTrayIcon;
    }

    /**
     * Set the Launcher console's visibility
     * 
     * @param visible
     *            The Launcher console's visibility
     */
    public void setConsoleVisible(boolean visible) {
        this.setConsoleVisible(visible, true);
    }

    /**
     * Set the Launcher console's visibility
     * 
     * @param visible
     *            The Launcher console's visibility
     */
    public void setConsoleVisible(boolean visible, boolean updateBottomBar) {
        if (!visible) {
            App.settings.log("Hiding console");
            if (updateBottomBar) {
                this.bottomBar.hideConsole();
            }
        } else {
            App.settings.log("Showing console");
            if (updateBottomBar) {
                this.bottomBar.showConsole();
            }
        }
        this.console.setVisible(visible);
        ((TrayMenu) App.TRAY_MENU).setConsoleVisible(visible);
    }

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public void setKeepLauncherOpen(boolean keepLauncherOpen) {
        this.keepLauncherOpen = keepLauncherOpen;
    }

    public void setLastSelectedSync(String lastSelected) {
        this.lastSelectedSync = lastSelected;
        saveProperties();
    }

    public String getLastSelectedSync() {
        if (this.lastSelectedSync == null)
            setLastSelectedSync("Dropbox");
        return this.lastSelectedSync;
    }

    public void setNotifyBackup(boolean notify) {
        this.notifyBackup = notify;
        saveProperties();
    }

    public boolean getNotifyBackup() {
        return this.notifyBackup;
    }

    public void setAutoBackup(boolean enableBackup) {
        this.autoBackup = enableBackup;
        saveProperties();
    }

    public String getDropboxLocation() {
        return this.dropboxFolderLocation;
    }

    public void setDropboxLocation(String dropboxLoc) {
        this.dropboxFolderLocation = dropboxLoc;
        saveProperties();
    }

    public boolean getAutoBackup() {
        return this.autoBackup;
    }

    public void setEnableTrayIcon(boolean enableTrayIcon) {
        this.enableTrayIcon = enableTrayIcon;
    }

    public boolean enableLeaderboards() {
        return this.enableLeaderboards;
    }

    public void setEnableLeaderboards(boolean enableLeaderboards) {
        this.enableLeaderboards = enableLeaderboards;
    }

    public boolean enableLogs() {
        return this.enableLogs;
    }

    public void setEnableLogs(boolean enableLogs) {
        this.enableLogs = enableLogs;
    }

    public boolean enableServerChecker() {
        return this.enableServerChecker;
    }

    public void setEnableServerChecker(boolean enableServerChecker) {
        this.enableServerChecker = enableServerChecker;
    }

    public boolean enableOpenEyeReporting() {
        return this.enableOpenEyeReporting;
    }

    public void setEnableOpenEyeReporting(boolean enableOpenEyeReporting) {
        this.enableOpenEyeReporting = enableOpenEyeReporting;
    }

    public boolean getEnableProxy() {
        return this.enableProxy;
    }

    public void setEnableProxy(boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public String getProxyHost() {
        return this.proxyHost;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public String getProxyType() {
        return this.proxyType;
    }

    public void setServerCheckerWait(int serverCheckerWait) {
        this.serverCheckerWait = serverCheckerWait;
    }

    public int getServerCheckerWait() {
        return this.serverCheckerWait;
    }

    public int getServerCheckerWaitInMilliseconds() {
        return this.serverCheckerWait * 60 * 1000;
    }

    public void setConcurrentConnections(int concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public int getConcurrentConnections() {
        return this.concurrentConnections;
    }

    public String getTheme() {
        return this.theme;
    }

    public File getThemeFile() {
        File theme = new File(this.themesDir, this.theme + ".json");
        if (theme.exists()) {
            return theme;
        } else {
            return null;
        }
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        Timestamper.updateDateFormat();
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public Proxy getProxy() {
        if (!this.enableProxy) {
            return null;
        }
        if (this.proxy == null) {
            Type type;
            if (this.proxyType.equals("HTTP")) {
                type = Proxy.Type.HTTP;
            } else if (this.proxyType.equals("SOCKS")) {
                type = Proxy.Type.SOCKS;
            } else if (this.proxyType.equals("DIRECT")) {
                type = Proxy.Type.DIRECT;
            } else {
                // Oh noes, problem!
                log("Tried to set proxy type to " + this.proxyType
                        + " which is not valid! Proxy support disabled!", LogMessageType.warning,
                        false);
                this.enableProxy = false;
                return null;
            }
            this.proxy = new Proxy(type, new InetSocketAddress(this.proxyHost, this.proxyPort));
        }
        return this.proxy;
    }

    public String getVersion() {
        return this.version;
    }

    public String getUserAgent() {
        return this.userAgent + " ATLauncher/" + this.version;
    }

    public String getLocalizedString(String string) {
        return Language.INSTANCE.localize(string);
    }

    public String getLocalizedString(String string, String replace) {
        return Language.INSTANCE.localize(string).replace("%s", replace);
    }

    public void restartLauncher() {
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation()
                .getPath());
        String path = null;
        try {
            path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logStackTrace(e);
        } catch (IOException e) {
            logStackTrace(e);
        }

        List<String> arguments = new ArrayList<String>();

        if (this.isUsingMacApp()) {
            arguments.add("open");
            arguments.add("-n");
            arguments
                    .add(baseDir.getParentFile().getParentFile().getParentFile().getAbsolutePath());

        } else {
            String jpath = System.getProperty("java.home") + File.separator + "bin"
                    + File.separator + "java";
            if (Utils.isWindows()) {
                jpath += "w";
            }
            arguments.add(jpath);
            arguments.add("-jar");
            arguments.add(path);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public boolean isLanguageLoaded() {
        return this.languageLoaded;
    }

    public void cloneInstance(Instance instance, String clonedName) {
        Instance clonedInstance = (Instance) instance.clone();
        if (clonedInstance == null) {
            App.settings.log(
                    "Error Occured While Cloning Instance! Instance Object Couldn't Be Cloned!",
                    LogMessageType.error, false);
        } else {
            clonedInstance.setName(clonedName);
            clonedInstance.getRootDirectory().mkdir();
            Utils.copyDirectory(instance.getRootDirectory(), clonedInstance.getRootDirectory());
            this.instances.add(clonedInstance);
            this.saveInstances();
            this.reloadInstancesPanel();
        }
    }

}