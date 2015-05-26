/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.FileSystemData;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.Update;
import com.atlauncher.data.json.LauncherLibrary;
import com.atlauncher.evnt.manager.InstanceChangeManager;
import com.atlauncher.evnt.manager.PackChangeManager;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.thread.LoggingThread;
import com.atlauncher.utils.ATLauncherAPIUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.MojangAPIUtils;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.ClearDirVisitor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Settings class for storing all data for the Launcher and the settings of the user.
 *
 * @author Ryan
 */
public class Settings {
    // Users Settings
    private Server server; // Server to use for the Launcher
    private String forgeLoggingLevel; // Logging level to use when running Minecraft with Forge
    private int initialMemory; // Initial RAM to use when launching Minecraft
    private int maximumMemory; // Maximum RAM to use when launching Minecraft
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
    private boolean enablePackTags = false;
    private String proxyHost; // The proxies host
    private int proxyPort; // The proxies port
    private String proxyType; // The type of proxy (socks, http)
    private int concurrentConnections; // Number of concurrent connections to open when downloading
    private int daysOfLogsToKeep; // Number of days of logs to keep
    private Account account; // Account using the Launcher
    private Proxy proxy = null; // The proxy object if any
    private String theme; // The theme to use
    private String dateFormat; // The date format to use
    private boolean hideOldJavaWarning; // If the user has hidden the old Java warning
    private boolean hideJava8Warning; // If the user has hidden the Java 8 warning
    private boolean enableServerChecker; // If to enable SERVER checker
    private int serverCheckerWait; // Time to wait in minutes between checking SERVER status
    // General backup settings
    private boolean autoBackup; // Whether backups are created on instance close
    private String lastSelectedSync; // The last service selected for syncing
    private boolean notifyBackup; // Whether to notify the user on successful backup or restore
    // Dropbox settings
    private String dropboxFolderLocation; // Location of dropbox if defined by user
    // Packs, Instances and Accounts
    private LauncherVersion latestLauncherVersion; // Latest Launcher version
    private List<DownloadableFile> launcherFiles; // Files the Launcher needs to download
    private List<MinecraftServer> checkingServers = new ArrayList<MinecraftServer>();
    // Launcher Settings
    private JFrame parent; // Parent JFrame of the actual Launcher
    private Properties properties = new Properties(); // Properties to store everything in
    private LauncherConsole console; // The Launcher's Console
    private List<Server> servers = new ArrayList<Server>(); // Servers for the Launcher
    private List<Server> triedServers = new ArrayList<Server>(); // Servers tried to connect to
    private NewsTab newsPanel; // The news panel
    private boolean hadPasswordDialog = false; // If the user has seen the password dialog
    private boolean firstTimeRun = false; // If this is the first time the Launcher has been run
    private boolean offlineMode = false; // If offline mode is enabled
    private Process minecraftProcess = null; // The process minecraft is running on
    private Server originalServer = null; // Original Server user has saved
    private boolean minecraftLaunched = false; // If Minecraft has been Launched
    private String userAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, " +
            "" + "like Gecko) Chrome/28.0.1500.72 Safari/537.36";
    private boolean minecraftLoginServerUp = false; // If the Minecraft Login SERVER is up
    private boolean minecraftSessionServerUp = false; // If the Minecraft Session SERVER is up
    @SuppressWarnings("unused")
    private DropboxSync dropbox;
    private boolean languageLoaded = false;
    private Timer checkingServersTimer = null; // Timer used for checking servers

    public Settings() {
        checkFolders(); // Checks the setup of the folders and makes sure they're there
        clearTempDir(); // Cleans all files in the Temp Dir
        loadStartingProperties(); // Get users Console preference and Java Path
    }

    public void loadConsole() {
        console = new LauncherConsole();
        LogManager.start();
    }

    public void loadEverything() {
        if (App.forceOfflineMode) {
            this.offlineMode = true;
        }

        setupServers(); // Setup the servers available to use in the Launcher
        findActiveServers(); // Find active servers
        loadServerProperty(false); // Get users Server preference
        if (hasUpdatedFiles()) {
            downloadUpdatedFiles(); // Downloads updated files on the SERVER
        }

        checkForLauncherUpdate();

        downloadExternalLibraries();

        if (!Utils.checkAuthLibLoaded()) {
            LogManager.error("AuthLib was not loaded into the classpath!");
        }

        loadNews(); // Load the news

        this.languageLoaded = true; // Languages are now loaded

        loadMinecraftVersions(); // Load info about the different Minecraft versions

        PackManager.loadPacks(); // Load the Packs available in the Launcher

        loadUsers(); // Load the Testers and Allowed Players for the packs

        InstanceManager.loadInstances(); // Load the users installed Instances

        AccountManager.loadAccounts(); // Load the saved Accounts

        loadCheckingServers(); // Load the saved servers we're checking with the tool

        loadProperties(); // Load the users Properties

        if (this.isUsingCustomJavaPath()) {
            checkForValidJavaPath(true); // Checks for a valid Java path
        }

        console.setupLanguage(); // Setup language on the console

        clearAllLogs(); // Clear all the old logs out

        AccountManager.checkUUIDs(); // Check for accounts UUID's and add them if necessary

        InstanceManager.changeUserLocks(); // Changes any instances user locks to UUIDs if available

        AccountManager.checkForNameChanges(); // Check account for username changes

        LogManager.debug("Checking for access to master SERVER");
        OUTER:
        for (Pack pack : Data.PACKS) {
            if (pack.isTester()) {
                for (Server server : this.servers) {
                    if (server.getName().equals("Master Server (Testing Only)")) {
                        server.setUserSelectable(true);
                        LogManager.debug("Access to master SERVER granted");
                        break OUTER; // Don't need to check anymore so break the outer loop
                    }
                }
            }
        }
        LogManager.debug("Finished checking for access to master SERVER");

        loadServerProperty(true); // Get users Server preference

        if (Utils.isWindows() && this.javaPath.contains("x86")) {
            LogManager.warn("You're using 32 bit Java on a 64 bit Windows install!");
            String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize("common.no")};
            int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                    .INSTANCE.localizeWithReplace("settings.running32bit", "<br/><br/>")), Language.INSTANCE.localize
                    ("settings.running32bittitle"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    options, options[0]);
            if (ret == 0) {
                Utils.openBrowser("http://www.atlauncher.com/help/32bit/");
                System.exit(0);
            }
        }

        if (!Utils.isJava7OrAbove(true) && !this.hideOldJavaWarning) {
            LogManager.warn("You're using an old unsupported version of Java (Java 6 or older)!");
            String[] options = {Language.INSTANCE.localize("common.download"), Language.INSTANCE.localize("common" +
                    ".ok"), Language.INSTANCE.localize("instance" + "" +
                    ".dontremindmeagain")};
            int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                    .INSTANCE.localizeWithReplace("settings.unsupportedjava", "<br/><br/>")), Language.INSTANCE
                    .localize("settings.unsupportedjavatitle"), JOptionPane.DEFAULT_OPTION, JOptionPane
                    .ERROR_MESSAGE, null, options, options[0]);
            if (ret == 0) {
                Utils.openBrowser("http://atl.pw/java7download");
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

        if (this.enableLogs) {
            App.TASKPOOL.execute(new Runnable() {
                @Override
                public void run() {
                    ATLauncherAPIUtils.postSystemInfo();
                }
            });
        }
    }

    public void checkForValidJavaPath(boolean save) {
        File java = new File(App.settings.getJavaPath(), "bin" + File.separator + "java" +
                (Utils.isWindows() ? ".exe" : ""));

        if (!java.exists()) {
            LogManager.error("Custom Java Path Is Incorrect! Defaulting to valid value!");
            this.setJavaPath(Utils.getJavaHome());

            if (save) {
                this.saveProperties();
            }
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

        for (Account account : AccountManager.getAccounts()) {
            if (account.isRemembered()) {
                matches = true;
            }
        }

        if (matches) {
            String[] options = {Language.INSTANCE.localize("common.ok"), Language.INSTANCE.localize("account" + "" +
                    ".removepasswords")};
            int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                    .INSTANCE.localizeWithReplace("account.securitywarning", "<br/>")), Language.INSTANCE.localize
                    ("account.securitywarningtitle"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    options, options[0]);

            if (ret == 1) {
                for (Account account : AccountManager.getAccounts()) {
                    if (account.isRemembered()) {
                        account.setRemember(false);
                    }
                }

                AccountManager.saveAccounts();
            }
        }

        this.saveProperties();
    }

    public void clearAllLogs() {
        try {
            for (int i = 0; i < 3; i++) {
                Path p = FileSystem.BASE_DIR.resolve(Constants.LAUNCHER_NAME + "-Log-" + i + ".txt");
                Files.deleteIfExists(p);
            }

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.LOGS, this.logFilter())) {
                for (Path file : stream) {
                    if (file.getFileName().toString().equals(LoggingThread.filename)) {
                        continue;
                    }

                    Files.deleteIfExists(file);
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    private DirectoryStream.Filter<Path> logFilter() {
        return new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path o) throws IOException {
                return Files.isRegularFile(o) && o.startsWith(Constants.LAUNCHER_NAME + "-Log_") && o.endsWith(".log");
            }
        };
    }

    public void checkResources() {
        LogManager.debug("Checking if using old format of resources");
        Path indexes = FileSystem.RESOURCES.resolve("indexes");
        if (!Files.exists(indexes) || !Files.isDirectory(indexes)) {
            final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("settings" + "" +
                    ".rearrangingresources"), 0, Language.INSTANCE.localize("settings.rearrangingresources"), null);
            Thread t = new Thread() {
                @Override
                public void run() {
                    Path indexes = FileSystem.RESOURCES.resolve("indexes");
                    Path virtual = FileSystem.RESOURCES.resolve("virtual");
                    Path objects = FileSystem.RESOURCES.resolve("objects");

                    Path tmp = FileSystem.TMP.resolve("assets");
                    Path legacy = virtual.resolve("legacy");

                    try {
                        FileUtils.createDirectory(tmp);
                        FileUtils.moveDirectory(FileSystem.RESOURCES, tmp);
                        FileUtils.createDirectory(indexes);
                        FileUtils.createDirectory(objects);
                        FileUtils.createDirectory(virtual);
                        FileUtils.createDirectory(legacy);
                        FileUtils.moveDirectory(tmp, legacy);
                        FileUtils.deleteDirectory(tmp);
                    } catch (Exception e) {
                        LogManager.logStackTrace(e);
                    }
                }
            };
        }
    }

    public void checkAccountUUIDs() {
        LogManager.debug("Checking account UUID's");
        LogManager.info("Checking account UUID's!");
        for (Account account : Data.ACCOUNTS) {
            if (account.isUUIDNull()) {
                account.setUUID(MojangAPIUtils.getUUID(account.getMinecraftUsername()));
                AccountManager.saveAccounts();
            }
        }
        LogManager.debug("Finished checking account UUID's");
    }

    public void changeInstanceUserLocks() {
        LogManager.debug("Changing instances user locks to UUID's");

        boolean wereChanges = false;

        for (Instance instance : Data.INSTANCES) {
            if (instance.getInstalledBy() != null) {
                boolean found = false;

                for (Account account : Data.ACCOUNTS) {
                    // This is the user who installed this so switch to their UUID
                    if (account.getMinecraftUsername().equalsIgnoreCase(instance.getInstalledBy())) {
                        found = true;
                        wereChanges = true;

                        instance.removeInstalledBy();

                        // If the accounts UUID is null for whatever reason, don't set the lock
                        if (!account.isUUIDNull()) {
                            instance.setUserLock(account.getUUIDNoDashes());
                        }
                        break;
                    }
                }

                // If there were no accounts with that username, we remove the lock and old installed by
                if (!found) {
                    wereChanges = true;

                    instance.removeInstalledBy();
                    instance.removeUserLock();
                }
            }
        }

        if (wereChanges) {
            AccountManager.saveAccounts();
            InstanceManager.saveInstances();
        }

        LogManager.debug("Finished changing instances user locks to UUID's");
    }

    public void checkMojangStatus() {
        try {
            Downloadable dl = new Downloadable("http://status.mojang.com/check", false);
            String resp = dl.toString();

            if (resp == null) {
                this.minecraftLoginServerUp = false;
                this.minecraftSessionServerUp = false;
                return;
            }

            JsonArray array = Gsons.PARSER.parse(resp).getAsJsonArray();
            for (JsonElement e : array) {
                JsonObject obj = e.getAsJsonObject();
                if (obj.has("authserver.mojang.com")) {
                    if (obj.get("authserver.mojang.com").getAsString().equalsIgnoreCase("green")) {
                        this.minecraftLoginServerUp = true;
                    }
                } else if (obj.has("session.minecraft.net")) {
                    if (obj.get("session.minecraft.net").getAsString().equalsIgnoreCase("green")) {
                        this.minecraftSessionServerUp = true;
                    }
                }
            }
        } catch (Exception e) {
            this.minecraftLoginServerUp = false;
            this.minecraftSessionServerUp = false;
        }
    }

    public Status getMojangStatus() {
        if (minecraftLoginServerUp && minecraftSessionServerUp) {
            return Status.ONLINE;
        } else if (!minecraftLoginServerUp && !minecraftSessionServerUp) {
            return Status.OFFLINE;
        } else {
            return Status.PARTIAL;
        }
    }

    public boolean launcherHasUpdate() {
        try {
            this.latestLauncherVersion = JsonFile.of("version.json", LauncherVersion.class);
        } catch (Exception e) {
            LogManager.logStackTrace("Exception when loading latest launcher version!", e);
        }

        return this.latestLauncherVersion != null && Constants.VERSION.needsUpdate(this.latestLauncherVersion);
    }

    public boolean launcherHasBetaUpdate() {
        try {
            Downloadable downloadable = new Downloadable("https://api.atlauncher.com/v1/build/atlauncher/build/",
                    false);
            APIResponseInt response = downloadable.fromJson(APIResponseInt.class);
            return response.getData() > Constants.VERSION.getBuild();
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            return false;
        }
    }

    public void downloadUpdate() {
        try {
            Path self = Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String path = URLDecoder.decode(self.toString(), "UTF-8");
            String target;
            String saveAs = self.getFileName().toString();
            if (path.contains(".exe")) {
                target = "exe";
            } else {
                target = "JAR";
            }

            Path output = FileSystem.TMP.resolve(saveAs);
            LogManager.info("Downloading Launcher Update");
            Downloadable update = new Downloadable(Constants.LAUNCHER_NAME + "." + target, output, true);
            update.download();
            this.runUpdate(path, output.toAbsolutePath().toString());
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    public void downloadBetaUpdate() {
        try {
            File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            String path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
            String toget;
            String saveAs = thisFile.getName();
            if (path.contains(".exe")) {
                toget = "exe";
            } else {
                toget = "JAR";
            }
            Path newFile = FileSystem.TMP.resolve(saveAs);
            LogManager.info("Downloading Launcher Update");
            Downloadable update = new Downloadable("https://api.atlauncher.com/v1/build/atlauncher/download/" +
                    toget, newFile, false);
            update.download();
            runUpdate(path, newFile.toString());
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void runUpdate(String currentPath, String temporaryUpdatePath) {
        List<String> arguments = new ArrayList<String>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
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

        LogManager.info("Running launcher update with command " + arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        System.exit(0);
    }

    private void getFileHashes() {
        this.launcherFiles = null;
        Downloadable download = new Downloadable("launcher/json/hashes.json", true);
        java.lang.reflect.Type type = new TypeToken<List<DownloadableFile>>() {
        }.getType();

        try {
            this.launcherFiles = download.fromJson(type);
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", download.toString());
            LogManager.logStackTrace("Error loading in file hashes! See error details at " + result, e);
        }
    }

    /**
     * This checks the servers hashes.json file and gets the files that the Launcher needs to have
     */
    private ArrayList<Downloadable> getLauncherFiles() {
        getFileHashes(); // Get File Hashes
        if (this.launcherFiles == null) {
            this.offlineMode = true;
            return null;
        }
        ArrayList<Downloadable> downloads = new ArrayList<>();
        for (DownloadableFile file : this.launcherFiles) {
            if (file.isLauncher()) {
                continue;
            }
            downloads.add(file.getDownloadable());
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
                            LogManager.info("Downloading Launcher File " + download.to.getFileName().toString());
                            try {
                                download.download();
                            } catch (IOException e) {
                                e.printStackTrace(System.err);
                            }
                        }
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }

        LogManager.info("Finished downloading updated files!");

        if (Language.INSTANCE.getCurrent() != null) {
            try {
                Language.INSTANCE.reload(Language.INSTANCE.getCurrent());
            } catch (IOException e) {
                LogManager.logStackTrace("Couldn't reload language " + Language.INSTANCE.getCurrent(), e);
            }
        }
    }

    /**
     * This checks the servers hashes.json file and looks for new/updated files that differ from what the user has
     */
    public boolean hasUpdatedFiles() {
        if (isInOfflineMode()) {
            return false;
        }
        LogManager.info("Checking for updated files!");
        List<Downloadable> downloads = getLauncherFiles();

        if (downloads == null) {
            this.offlineMode = true;
            return false;
        }

        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                LogManager.info("Updates found!");
                return true; // 1 file needs to be updated so there is updated files
            }
        }

        LogManager.info("No updates found!");
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
                    downloadUpdatedFiles(); // Downloads updated files on the SERVER
                }
                checkForLauncherUpdate();
                loadNews(); // Load the news
                reloadNewsPanel(); // Reload news panel
                PackManager.loadPacks(); // Load the Packs available in the Launcher
                loadUsers(); // Load the Testers and Allowed Players for the packs
                InstanceManager.loadInstances(); // Load the users installed Instances
                InstanceChangeManager.change();
                dialog.setVisible(false); // Remove the dialog
                dialog.dispose(); // Dispose the dialog
            }
        });
        dialog.setVisible(true);
    }

    private void checkForLauncherUpdate() {
        LogManager.debug("Checking for launcher update");

        if (launcherHasUpdate()) {
            if (!App.wasUpdated) {
                downloadUpdate(); // Update the Launcher
            } else {
                String[] options = {"Ok"};
                JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph("Update failed. " +
                                "Please click Ok to close " + "the launcher and open up the downloads " +
                                "page.<br/><br/>Download " + "the update and replace the old " + Constants
                        .LAUNCHER_NAME + " " +
                                "file."), "Update Failed!", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);
                Utils.openBrowser("http://www.atlauncher.com/downloads/");
                System.exit(0);
            }
        } else if (Constants.VERSION.isBeta() && launcherHasBetaUpdate()) {
            downloadBetaUpdate();
        }

        LogManager.debug("Finished checking for launcher update");
    }

    /**
     * Downloads and loads all external libraries used by the launcher as specified in the Configs/JSON/libraries.json
     * file.
     */
    private void downloadExternalLibraries() {
        LogManager.debug("Downloading external libraries");
        List<LauncherLibrary> libraries;

        try {
            java.lang.reflect.Type type = new TypeToken<List<LauncherLibrary>>() {
            }.getType();

            libraries = JsonFile.of("libraries.json", type);
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            libraries = new LinkedList<>();
        }

        ExecutorService executor = Utils.generateDownloadExecutor();

        for (final LauncherLibrary library : libraries) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Downloadable download = library.getDownloadable();

                    if (download.needToDownload()) {
                        LogManager.info("Downloading library " + library.getFilename() + "!");
                        try {
                            download.download();
                        } catch (IOException e) {
                            LogManager.logStackTrace(e);
                        }
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        for (LauncherLibrary library : libraries) {
            Path path = library.getFilePath();

            if (library.shouldAutoLoad() && !Utils.addToClasspath(path)) {
                LogManager.error("Couldn't add " + path + " to the classpath!");
                if (library.shouldExitOnFail()) {
                    LogManager.error("Library is necessary so launcher will exit!");
                    System.exit(1);
                }
            }
        }

        LogManager.debug("Finished downloading external libraries");
    }

    /**
     * Checks the directory to make sure all the necessary folders are there
     */
    private void checkFolders() {
        try {
            for (Field field : FileSystem.class.getDeclaredFields()) {
                Path p = (Path) field.get(null);
                if (!Files.exists(p)) {
                    FileUtils.createDirectory(p);
                }

                if (!Files.isDirectory(p)) {
                    Files.delete(p);
                    FileUtils.createDirectory(p);
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    /**
     * Deletes all files in the Temp directory
     */
    public void clearTempDir() {
        try {
            Files.walkFileTree(FileSystem.TMP, new ClearDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace("Error clearing temp directory at " + FileSystem.TMP, e);
        }
    }

    /**
     * Sets the main parent JFrame reference for the Launcher
     *
     * @param parent The Launcher main JFrame
     */
    public void setParentFrame(JFrame parent) {
        this.parent = parent;
    }

    /**
     * Load the users Server preference from file
     */
    public void loadServerProperty(boolean userSelectableOnly) {
        LogManager.debug("Loading SERVER to use");
        try {
            this.properties.load(new FileInputStream(FileSystemData.PROPERTIES.toFile()));
            String serv = this.properties.getProperty("SERVER", "Auto");
            if (this.isServerByName(serv)) {
                if (!userSelectableOnly || server.isUserSelectable()) {
                    this.server = this.getServerByName(serv);
                    this.originalServer = this.server;
                }

                if (this.server == null) {
                    LogManager.warn("Server " + serv + " is invalid");
                    this.server = this.getServerByName("Auto");
                    this.originalServer = this.server;

                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading SERVER to use");
    }

    /**
     * Load the users Console preference from file
     */
    public void loadStartingProperties() {
        try {
            if (!Files.exists(FileSystemData.PROPERTIES)) {
                Files.createFile(FileSystemData.PROPERTIES);
            }
        } catch (IOException e) {
            String[] options = {"OK"};
            JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph("Cannot create the config file" +
                            ".<br/><br/>Make sure you're running the Launcher from somewhere with<br/>write" +
                            " permissions for your user account such as your Home/Users folder or desktop."),
                    "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
            System.exit(0);
        }

        try {
            this.properties.load(new FileInputStream(FileSystemData.PROPERTIES.toFile()));

            this.theme = properties.getProperty("theme", Constants.LAUNCHER_NAME);

            this.dateFormat = properties.getProperty("dateformat", "dd/M/yyy");
            if (!this.dateFormat.equalsIgnoreCase("dd/M/yyy") && !this.dateFormat.equalsIgnoreCase("M/dd/yyy") &&
                    !this.dateFormat.equalsIgnoreCase("yyy/M/dd")) {
                this.dateFormat = "dd/M/yyy";
            }

            this.enablePackTags = Boolean.parseBoolean(properties.getProperty("enablepacktags", "false"));

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole", "true"));

            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon", "true"));

            if (!properties.containsKey("usingcustomjavapath")) {
                this.usingCustomJavaPath = false;
                this.javaPath = Utils.getJavaHome();
            } else {
                this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty("usingcustomjavapath", "false"));
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
                if (!this.proxyType.equals("SOCKS") && !this.proxyType.equals("HTTP") && !this.proxyType.equals
                        ("DIRECT")) {
                    this.enableProxy = false;
                }

                this.configureProxy();
            } else {
                this.proxyHost = "";
                this.proxyPort = 0;
                this.proxyType = "";
            }

            this.concurrentConnections = Integer.parseInt(properties.getProperty("concurrentconnections", "8"));
            if (this.concurrentConnections < 1) {
                this.concurrentConnections = 8;
            }

            this.daysOfLogsToKeep = Integer.parseInt(properties.getProperty("daysoflogstokeep", "7"));
            if (this.daysOfLogsToKeep < 1 || this.daysOfLogsToKeep > 30) {
                this.daysOfLogsToKeep = 7;
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void configureProxy() {
        Network.CLIENT.setProxy(this.getProxy());
        Network.PROGRESS_CLIENT.setProxy(this.getProxy());
    }

    public boolean enabledPackTags() {
        return this.enablePackTags;
    }

    public void setPackTags(boolean enablePackTags) {
        this.enablePackTags = enablePackTags;
    }

    /**
     * Load the properties from file
     */
    public void loadProperties() {
        LogManager.debug("Loading properties");
        try {
            this.properties.load(new FileInputStream(FileSystemData.PROPERTIES.toFile()));
            this.firstTimeRun = Boolean.parseBoolean(properties.getProperty("firsttimerun", "true"));

            this.hadPasswordDialog = Boolean.parseBoolean(properties.getProperty("hadpassworddialog", "false"));

            this.hideOldJavaWarning = Boolean.parseBoolean(properties.getProperty("hideoldjavawarning", "false"));

            this.hideJava8Warning = Boolean.parseBoolean(properties.getProperty("hidejava8warning", "false"));

            String lang = properties.getProperty("language", "English");
            if (!isLanguageByName(lang)) {
                LogManager.warn("Invalid language " + lang + ". Defaulting to English!");
                lang = "English";
            }

            Language.INSTANCE.load(lang);

            this.forgeLoggingLevel = properties.getProperty("forgelogginglevel", "INFO");
            if (!this.forgeLoggingLevel.equalsIgnoreCase("SEVERE") && !this.forgeLoggingLevel.equalsIgnoreCase
                    ("WARNING") && !this.forgeLoggingLevel.equalsIgnoreCase("INFO") && !this.forgeLoggingLevel
                    .equalsIgnoreCase("CONFIG") && !this.forgeLoggingLevel.equalsIgnoreCase("FINE") && !this
                    .forgeLoggingLevel.equalsIgnoreCase("FINER") && !this.forgeLoggingLevel.equalsIgnoreCase
                    ("FINEST")) {
                LogManager.warn("Invalid Forge Logging level " + this.forgeLoggingLevel + ". Defaulting to INFO!");
                this.forgeLoggingLevel = "INFO";
            }

            if (Utils.is64Bit()) {
                int halfRam = (Utils.getMaximumRam() / 1000) * 512;
                int defaultRam = (halfRam >= 4096 ? 4096 : halfRam); // Default ram
                this.maximumMemory = Integer.parseInt(properties.getProperty("ram", defaultRam + ""));
                if (this.maximumMemory > Utils.getMaximumRam()) {
                    LogManager.warn("Tried to allocate " + this.maximumMemory + "MB of Ram but only " + Utils
                            .getMaximumRam() + "MB is available to use!");
                    this.maximumMemory = defaultRam; // User tried to allocate too much ram, set it
                    // back to
                    // half, capped at 4GB
                }
            } else {
                this.maximumMemory = Integer.parseInt(properties.getProperty("ram", "1024"));
                if (this.maximumMemory > Utils.getMaximumRam()) {
                    LogManager.warn("Tried to allocate " + this.maximumMemory + "MB of Maximum Ram but only " + Utils
                            .getMaximumRam() + "MB is available to use!");
                    this.maximumMemory = 1024; // User tried to allocate too much ram, set it back
                    // to 1GB
                }
            }

            this.initialMemory = Integer.parseInt(properties.getProperty("initialmemory", "256"));
            if (this.initialMemory > Utils.getMaximumRam()) {
                LogManager.warn("Tried to allocate " + this.initialMemory + "MB of Initial Ram but only " + Utils
                        .getMaximumRam() + "MB is available to use!");
                this.initialMemory = 256; // User tried to allocate too much ram, set it back to
                // 256MB
            } else if (this.initialMemory > this.maximumMemory) {
                LogManager.warn("Tried to allocate " + this.initialMemory + "MB of Initial Ram but maximum ram is " +
                        this.maximumMemory + "MB which is less!");
                this.initialMemory = 256; // User tried to allocate too much ram, set it back to 256MB
            }

            // Default PermGen to 256 for 64 bit systems and 128 for 32 bit systems
            this.permGen = Integer.parseInt(properties.getProperty("permGen", (Utils.is64Bit() ? "256" : "128")));

            this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
            if (this.windowWidth > Utils.getMaximumWindowWidth()) {
                LogManager.warn("Tried to set window width to " + this.windowWidth + " pixels but the maximum is " +
                        Utils.getMaximumWindowWidth() + " pixels!");
                this.windowWidth = Utils.getMaximumWindowWidth(); // User tried to make screen size
                // wider than they have
            }

            this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));
            if (this.windowHeight > Utils.getMaximumWindowHeight()) {
                LogManager.warn("Tried to set window height to " + this.windowHeight + " pixels but the maximum is "
                        + Utils.getMaximumWindowHeight() + " pixels!");
                this.windowHeight = Utils.getMaximumWindowHeight(); // User tried to make screen
                // size wider than they have
            }

            this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty("usingcustomjavapath", "false"));

            if (isUsingCustomJavaPath()) {
                this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
            } else {
                this.javaPath = Utils.getJavaHome();
            }

            this.javaParamaters = properties.getProperty("javaparameters", "");

            this.maximiseMinecraft = Boolean.parseBoolean(properties.getProperty("maximiseminecraft", "false"));

            this.saveCustomMods = Boolean.parseBoolean(properties.getProperty("savecustommods", "true"));

            this.advancedBackup = Boolean.parseBoolean(properties.getProperty("advancedbackup", "false"));

            this.sortPacksAlphabetically = Boolean.parseBoolean(properties.getProperty("sortpacksalphabetically",
                    "false"));

            this.keepLauncherOpen = Boolean.parseBoolean(properties.getProperty("keeplauncheropen", "true"));

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole", "true"));

            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon", "true"));

            this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty("enableleaderboards", "false"));

            this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));

            this.enableServerChecker = Boolean.parseBoolean(properties.getProperty("enableserverchecker", "false"));

            this.enableOpenEyeReporting = Boolean.parseBoolean(properties.getProperty("enableopeneyereporting",
                    "true"));

            this.enableProxy = Boolean.parseBoolean(properties.getProperty("enableproxy", "false"));

            if (this.enableProxy) {
                this.proxyHost = properties.getProperty("proxyhost", null);

                this.proxyPort = Integer.parseInt(properties.getProperty("proxyport", "0"));
                if (this.proxyPort <= 0 || this.proxyPort > 65535) {
                    // Proxy port is invalid so disable proxy
                    LogManager.warn("Tried to set proxy port to " + this.proxyPort + " which is not a valid port! " +
                            "Proxy support disabled!");
                    this.enableProxy = false;
                }

                this.proxyType = properties.getProperty("proxytype", "");
                if (!this.proxyType.equals("SOCKS") && !this.proxyType.equals("HTTP") && !this.proxyType.equals
                        ("DIRECT")) {
                    // Proxy type is invalid so disable proxy
                    LogManager.warn("Tried to set proxy type to " + this.proxyType + " which is not valid! Proxy " +
                            "support disabled!");
                    this.enableProxy = false;
                }
            } else {
                this.proxyHost = "";
                this.proxyPort = 0;
                this.proxyType = "";
            }

            this.serverCheckerWait = Integer.parseInt(properties.getProperty("servercheckerwait", "5"));
            if (this.serverCheckerWait < 1 || this.serverCheckerWait > 30) {
                // Server checker wait should be between 1 and 30
                LogManager.warn("Tried to set SERVER checker wait to " + this.serverCheckerWait + " which is not " +
                        "valid! Must be between 1 and 30. Setting back to default of 5!");
                this.serverCheckerWait = 5;
            }

            this.concurrentConnections = Integer.parseInt(properties.getProperty("concurrentconnections", "8"));
            if (this.concurrentConnections < 1) {
                // Concurrent connections should be more than or equal to 1
                LogManager.warn("Tried to set the number of concurrent connections to " + this.concurrentConnections
                        + " which is not valid! Must be 1 or more. Setting back to default of 8!");
                this.concurrentConnections = 8;
            }

            this.daysOfLogsToKeep = Integer.parseInt(properties.getProperty("daysoflogstokeep", "7"));
            if (this.daysOfLogsToKeep < 1 || this.daysOfLogsToKeep > 30) {
                // Days of logs to keep should be 1 or more but less than 30
                LogManager.warn("Tried to set the number of days worth of logs to keep to " + this.daysOfLogsToKeep +
                        " which is not valid! Must be between 1 and 30 inclusive. Setting back to default of 7!");
                this.daysOfLogsToKeep = 7;
            }

            this.theme = properties.getProperty("theme", Constants.LAUNCHER_NAME);

            this.dateFormat = properties.getProperty("dateformat", "dd/M/yyy");
            if (!this.dateFormat.equalsIgnoreCase("dd/M/yyy") && !this.dateFormat.equalsIgnoreCase("M/dd/yyy") &&
                    !this.dateFormat.equalsIgnoreCase("yyy/M/dd")) {
                LogManager.warn("Tried to set the date format to " + this.dateFormat + " which is not valid! Setting " +
                        "back to default of dd/M/yyy!");
                this.dateFormat = "dd/M/yyy";
            }

            String lastAccountTemp = properties.getProperty("lastaccount", "");
            if (!lastAccountTemp.isEmpty()) {
                // Set the account. If it's null, that's okay, it uses that to signify nobody logged in
                AccountManager.setActiveAccount(AccountManager.getAccountByName(lastAccountTemp));
            }

            String addedPacks = properties.getProperty("addedpacks", null);
            List<String> semiPublicPackCodes = new LinkedList<>();
            if (addedPacks != null) {
                if (addedPacks.contains(",")) {
                    Collections.addAll(semiPublicPackCodes, addedPacks.split(","));
                } else {
                    semiPublicPackCodes.add(addedPacks);
                }
            }
            PackManager.setSemiPublicPackCodes(semiPublicPackCodes);

            this.autoBackup = Boolean.parseBoolean(properties.getProperty("autobackup", "false"));
            this.notifyBackup = Boolean.parseBoolean(properties.getProperty("notifybackup", "true"));
            this.dropboxFolderLocation = properties.getProperty("dropboxlocation", "");
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading properties");
    }

    /**
     * Save the properties to file
     */
    public void saveProperties() {
        try {
            properties.setProperty("firsttimerun", "false");
            properties.setProperty("hadpassworddialog", "true");
            properties.setProperty("hideoldjavawarning", this.hideOldJavaWarning + "");
            properties.setProperty("hidejava8warning", this.hideJava8Warning + "");
            properties.setProperty("language", Language.INSTANCE.getCurrent());
            properties.setProperty("SERVER", this.server.getName());
            properties.setProperty("forgelogginglevel", this.forgeLoggingLevel);
            properties.setProperty("initialmemory", this.initialMemory + "");
            properties.setProperty("ram", this.maximumMemory + "");
            properties.setProperty("permGen", this.permGen + "");
            properties.setProperty("windowwidth", this.windowWidth + "");
            properties.setProperty("windowheight", this.windowHeight + "");
            properties.setProperty("usingcustomjavapath", (this.usingCustomJavaPath) ? "true" : "false");
            properties.setProperty("javapath", this.javaPath);
            properties.setProperty("javaparameters", this.javaParamaters);
            properties.setProperty("maximiseminecraft", (this.maximiseMinecraft) ? "true" : "false");
            properties.setProperty("savecustommods", (this.saveCustomMods) ? "true" : "false");
            properties.setProperty("advancedbackup", (this.advancedBackup) ? "true" : "false");
            properties.setProperty("sortpacksalphabetically", (this.sortPacksAlphabetically) ? "true" : "false");
            properties.setProperty("keeplauncheropen", (this.keepLauncherOpen) ? "true" : "false");
            properties.setProperty("enableconsole", (this.enableConsole) ? "true" : "false");
            properties.setProperty("enabletrayicon", (this.enableTrayIcon) ? "true" : "false");
            properties.setProperty("enableleaderboards", (this.enableLeaderboards) ? "true" : "false");
            properties.setProperty("enablelogs", (this.enableLogs) ? "true" : "false");
            properties.setProperty("enablepacktags", (this.enablePackTags) ? "true" : "false");
            properties.setProperty("enableserverchecker", (this.enableServerChecker) ? "true" : "false");
            properties.setProperty("enableopeneyereporting", (this.enableOpenEyeReporting) ? "true" : "false");
            properties.setProperty("enableproxy", (this.enableProxy) ? "true" : "false");
            properties.setProperty("proxyhost", this.proxyHost);
            properties.setProperty("proxyport", this.proxyPort + "");
            properties.setProperty("proxytype", this.proxyType);
            properties.setProperty("servercheckerwait", this.serverCheckerWait + "");
            properties.setProperty("concurrentconnections", this.concurrentConnections + "");
            properties.setProperty("daysoflogstokeep", this.daysOfLogsToKeep + "");
            properties.setProperty("theme", this.theme);
            properties.setProperty("dateformat", this.dateFormat);

            if (AccountManager.getActiveAccount() != null) {
                properties.setProperty("lastaccount", AccountManager.getActiveAccount().getUsername());
            } else {
                properties.setProperty("lastaccount", "");
            }

            properties.setProperty("addedpacks", PackManager.getSemiPublicPackCodesForProperties());
            properties.setProperty("autobackup", this.autoBackup ? "true" : "false");
            properties.setProperty("notifybackup", this.notifyBackup ? "true" : "false");
            properties.setProperty("dropboxlocation", this.dropboxFolderLocation);
            this.properties.store(new FileOutputStream(FileSystemData.PROPERTIES.toFile()), Constants.LAUNCHER_NAME +
                    " Settings");
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void addAccount(Account account) {
        Data.ACCOUNTS.add(account);
    }

    public void addCheckingServer(MinecraftServer server) {
        this.checkingServers.add(server);
        this.saveCheckingServers();
    }

    public void removeCheckingServer(MinecraftServer server) {
        this.checkingServers.remove(server);
        this.saveCheckingServers();
        this.startCheckingServers();
    }

    /**
     * The servers available to use in the Launcher
     * <p/>
     * These MUST be hardcoded in order for the Launcher to make the initial connections to download files
     */
    private void setupServers() {
        this.servers = new ArrayList<>(Arrays.asList(Constants.SERVERS));
    }

    private void findActiveServers() {
        LogManager.debug("Finding servers to use");
        Downloadable dl = new Downloadable(this.getMasterFileURL("launcher/json/servers.json"), false);
        try {
            java.lang.reflect.Type type = new TypeToken<List<Server>>() {
            }.getType();
            this.servers = dl.fromJson(type);
        } catch (Exception e) {
            String res = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", dl.toString());
            LogManager.logStackTrace("Exception when reading in the servers see " + res, e);
            this.servers = new ArrayList<>(Arrays.asList(Constants.SERVERS));
        }
    }

    public boolean disableServerGetNext() {
        this.server.disableServer(); // Disable the SERVER
        for (Server server : this.servers) {
            if (!server.isDisabled() && server.isUserSelectable()) {
                LogManager.warn("Server " + this.server.getName() + " Not Available! Switching To " + server.getName());
                this.server = server; // Setup next available SERVER
                return true;
            }
        }
        return false;
    }

    public void clearTriedServers() {
        if (this.triedServers.size() != 0) {
            this.triedServers = new ArrayList<Server>(); // Clear the list
            this.server = this.originalServer;
        }
    }

    public boolean getNextServer() {
        this.triedServers.add(this.server);
        for (Server server : this.servers) {
            if (!this.triedServers.contains(server) && !server.isDisabled()) {
                LogManager.warn("Server " + this.server.getName() + " Not Available! Switching To " + server.getName());
                this.server = server; // Setup next available SERVER
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadNews() {
        LogManager.debug("Loading news");

        try {
            java.lang.reflect.Type type = new TypeToken<List<News>>() {
            }.getType();
            Data.NEWS.clear();
            Data.NEWS.addAll((List<News>) JsonFile.of("news.json", type));
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading news");
    }

    /**
     * Loads info about the different Minecraft versions
     */
    private void loadMinecraftVersions() {
        LogManager.debug("Loading Minecraft versions");

        Data.MINECRAFT_VERSIONS.clear();
        try {
            java.lang.reflect.Type type = new TypeToken<List<MinecraftVersion>>() {
            }.getType();
            List<MinecraftVersion> versions = JsonFile.of("minecraftversions.json", type);

            for (MinecraftVersion version : versions) {
                Data.MINECRAFT_VERSIONS.put(version.getVersion(), version);
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }


        LogManager.info("[Background] Checking Minecraft Versions Started");
        ExecutorService executor = Utils.generateDownloadExecutor();
        for (final Entry<String, MinecraftVersion> entry : Data.MINECRAFT_VERSIONS.entrySet()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    entry.getValue().loadVersion();
                }
            });
        }
        LogManager.info("[Background] Checking Minecraft Versions Complete");
        executor.shutdown();
        LogManager.debug("Finished loading Minecraft versions");
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    private void loadUsers() {
        LogManager.debug("Loading users");

        Downloadable download = new Downloadable("launcher/json/users.json", true);

        try {
            java.lang.reflect.Type type = new TypeToken<List<PackUsers>>() {
            }.getType();
            List<PackUsers> users = download.fromJson(type);

            if (users == null) {
                this.offlineMode = true;
                return;
            }

            for (PackUsers user : users) {
                user.addUsers();
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading users");
    }

    /**
     * Loads the user servers added for checking
     */
    private void loadCheckingServers() {
        LogManager.debug("Loading servers to check");

        try {
            if (Files.exists(FileSystemData.CHECKING_SERVERS)) {
                byte[] bits = Files.readAllBytes(FileSystemData.CHECKING_SERVERS);
                Data.CHECKING_SERVERS.addAll((List<MinecraftServer>) Gsons.DEFAULT.fromJson(new String(bits),
                        MinecraftServer.LIST_TYPE));
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading servers to check");
    }

    public void saveCheckingServers() {
        try {
            if (!Files.exists(FileSystemData.CHECKING_SERVERS)) {
                Files.createFile(FileSystemData.CHECKING_SERVERS);
            }

            String data = Gsons.DEFAULT.toJson(Data.CHECKING_SERVERS);
            Files.write(FileSystemData.CHECKING_SERVERS, data.getBytes(), StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    public List<MinecraftServer> getCheckingServers() {
        return this.checkingServers;
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
        App.TRAY_MENU.setMinecraftLaunched(launched);
    }

    public boolean isUsingMacApp() {
        return Utils.isMac() && Files.exists(FileSystem.BASE_DIR.getParent().resolve("MacOS"));
    }

    public boolean isUsingNewMacApp() {
        return Files.exists(FileSystem.BASE_DIR.getParent().getParent().resolve("MacOS").resolve
                ("universalJavaApplicationStub"));
    }

    public MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        if (Data.MINECRAFT_VERSIONS.containsKey(version)) {
            return Data.MINECRAFT_VERSIONS.get(version);
        }
        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }

    private DirectoryStream.Filter<Path> languagesFilter() {
        return new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path o) throws IOException {
                return Files.isRegularFile(o) && o.toString().endsWith(".lang");
            }
        };
    }

    /**
     * Get the Languages available in the Launcher
     *
     * @return The Languages available in the Launcher
     */
    public List<String> getLanguages() {
        List<String> langs = new LinkedList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.LANGUAGES, this.languagesFilter())) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                langs.add(name.substring(0, name.lastIndexOf(".")));
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return langs;
    }

    /**
     * Get the Servers available in the Launcher
     *
     * @return The Servers available in the Launcher
     */
    public List<Server> getServers() {
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
        Downloadable download = new Downloadable("ping", true);
        String test = download.toString();
        if (test != null && test.equalsIgnoreCase("pong")) {
            PackChangeManager.reload();
            InstanceChangeManager.change();
        } else {
            this.offlineMode = true;
        }
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
     * Sets the panel used for News
     *
     * @param newsPanel News Panel
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
     * Finds a Server from the given name
     *
     * @param name Name of the Server to find
     * @return Server if the SERVER is found from the name
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
     * Finds if a language is available
     *
     * @param name The name of the Language
     * @return true if found, false if not
     */
    public boolean isLanguageByName(String name) {
        return this.getLanguages().contains(name.toLowerCase());
    }

    /**
     * Finds if a SERVER is available
     *
     * @param name The name of the Server
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
<<<<<<< HEAD
     * Finds if an Account is available
     *
     * @param username The username of the Account
     * @return true if found, false if not
     */
    public boolean isAccountByName(String username) {
        for (Account account : Data.ACCOUNTS) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the URL for a file on the user selected SERVER
=======
     * Gets the URL for a file on the user selected server
>>>>>>> cc798dd546680cdd5865380f8b7573e665804780
     *
     * @param filename Filename including directories on the SERVER
     * @return URL of the file
     */
    public String getFileURL(String filename) {
        return this.server.getFileURL(filename);
    }

    /**
     * Gets the URL for a file on the master SERVER
     *
     * @param filename Filename including directories on the SERVER
     * @return URL of the file or null if no master SERVER defined
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

    public String getLog() {
        return this.console.getLog();
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
            LogManager.error("Killing Minecraft");
            this.minecraftProcess.destroy();
            this.minecraftProcess = null;
        } else {
            LogManager.error("Cannot kill Minecraft as there is no instance open!");
        }
    }

    /**
     * Sets the users current active Language
     *
     * @param language The language to set to
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
     * @return The users set SERVER
     */
    public Server getServer() {
        return this.server;
    }

    /**
     * Sets the users current active Server
     *
     * @param server The SERVER to set to
     */
    public void setServer(Server server) {
        this.server = server;
        this.originalServer = server;
    }

    /**
     * Gets the users saved Server
     *
     * @return The users saved SERVER
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

    public int getInitialMemory() {
        return this.initialMemory;
    }

    public void setInitialMemory(int initialMemory) {
        this.initialMemory = initialMemory;
    }

    public int getMaximumMemory() {
        return this.maximumMemory;
    }

    public void setMaximumMemory(int memory) {
        this.maximumMemory = memory;
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
        this.usingCustomJavaPath = !javaPath.equalsIgnoreCase(Utils.getJavaHome());
        this.javaPath = javaPath;
    }

    public String getJavaParameters() {
        return this.javaParamaters;
    }

    public void setJavaParameters(String javaParamaters) {
        this.javaParamaters = javaParamaters;
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
     * If the user has selected to display packs alphabetically or not
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

    public void setEnableConsole(boolean enableConsole) {
        this.enableConsole = enableConsole;
    }

    public void setKeepLauncherOpen(boolean keepLauncherOpen) {
        this.keepLauncherOpen = keepLauncherOpen;
    }

    public String getLastSelectedSync() {
        if (this.lastSelectedSync == null) {
            setLastSelectedSync("Dropbox");
        }
        return this.lastSelectedSync;
    }

    public void setLastSelectedSync(String lastSelected) {
        this.lastSelectedSync = lastSelected;
        saveProperties();
    }

    public boolean getNotifyBackup() {
        return this.notifyBackup;
    }

    public void setNotifyBackup(boolean notify) {
        this.notifyBackup = notify;
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

    public void setAutoBackup(boolean enableBackup) {
        this.autoBackup = enableBackup;
        saveProperties();
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

    public String getProxyHost() {
        return this.proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return this.proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyType() {
        return this.proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }

    public int getServerCheckerWait() {
        return this.serverCheckerWait;
    }

    public void setServerCheckerWait(int serverCheckerWait) {
        this.serverCheckerWait = serverCheckerWait;
    }

    public int getServerCheckerWaitInMilliseconds() {
        return this.serverCheckerWait * 60 * 1000;
    }

    public int getConcurrentConnections() {
        return this.concurrentConnections;
    }

    public void setConcurrentConnections(int concurrentConnections) {
        this.concurrentConnections = concurrentConnections;
    }

    public int getDaysOfLogsToKeep() {
        return this.daysOfLogsToKeep;
    }

    public void setDaysOfLogsToKeep(int daysOfLogsToKeep) {
        this.daysOfLogsToKeep = daysOfLogsToKeep;
    }

    public String getTheme() {
        return this.theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Path getThemeFile() {
        Path theme = FileSystem.THEMES.resolve(this.theme + ".zip");
        if (Files.exists(theme)) {
            return theme;
        } else {
            return null;
        }
    }

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        Timestamper.updateDateFormat();
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
                LogManager.warn("Tried to set proxy type to " + this.proxyType + " which is not valid! Proxy support " +
                        "disabled!");
                this.enableProxy = false;
                return null;
            }
            this.proxy = new Proxy(type, new InetSocketAddress(this.proxyHost, this.proxyPort));
        }
        return this.proxy;
    }

    public Proxy getProxyForAuth() {
        if (!this.enableProxy) {
            return Proxy.NO_PROXY;
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
                LogManager.warn("Tried to set proxy type to " + this.proxyType + " which is not valid! Proxy support " +
                        "disabled!");
                this.enableProxy = false;
                return Proxy.NO_PROXY;
            }
            this.proxy = new Proxy(type, new InetSocketAddress(this.proxyHost, this.proxyPort));
        }
        return this.proxy;
    }

    public String getUserAgent() {
        return this.userAgent + Constants.LAUNCHER_NAME + "/" + Constants.VERSION;
    }

    public void restartLauncher() {
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = null;
        try {
            path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        List<String> arguments = new ArrayList<String>();

        if (this.isUsingMacApp()) {
            arguments.add("open");
            arguments.add("-n");
            arguments.add(FileSystem.BASE_DIR.getParent().getParent().toString());

        } else {
            String jpath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            if (Utils.isWindows()) {
                jpath += "w";
            }
            arguments.add(jpath);
            arguments.add("-JAR");
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
}
