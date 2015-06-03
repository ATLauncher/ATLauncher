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
import com.atlauncher.Update;
import com.atlauncher.collection.DownloadPool;
import com.atlauncher.data.json.LauncherLibrary;
import com.atlauncher.data.version.LauncherVersion;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftVersionManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.thread.LoggingThread;
import com.atlauncher.utils.ATLauncherAPIUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.MojangAPIUtils;
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
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

/**
 * Settings class for storing all data for the Launcher and the settings of the user.
 *
 * @author Ryan
 */
public class OldSettings {
    // Users Settings
    private Server server; // Server to use for the Launcher

    private LauncherVersion latestLauncherVersion; // Latest Launcher version
    private List<MinecraftServer> checkingServers = new ArrayList<>();

    // Launcher Settings
    private JFrame parent; // Parent JFrame of the actual Launcher
    private LauncherConsole console; // The Launcher's Console
    private List<Server> servers = new ArrayList<>(); // Servers for the Launcher
    private List<Server> triedServers = new ArrayList<>(); // Servers tried to connect to
    private NewsTab newsPanel; // The news panel
    private boolean offlineMode = false; // If offline mode is enabled
    private Process minecraftProcess = null; // The process minecraft is running on
    private Server originalServer = null; // Original Server user has saved
    private boolean minecraftLaunched = false; // If Minecraft has been Launched
    private boolean minecraftLoginServerUp = false; // If the Minecraft Login server is up
    private boolean minecraftSessionServerUp = false; // If the Minecraft Session server is up
    private DropboxSync dropbox;
    private boolean languageLoaded = false;
    private Timer checkingServersTimer = null; // Timer used for checking servers

    public OldSettings() {
        checkFolders(); // Checks the setup of the folders and makes sure they're there
        clearTempDir(); // Cleans all files in the Temp Dir
    }

    public void loadConsole() {
        console = new LauncherConsole();
        LogManager.start();
    }

    public void loadEverything() {
        if (App.forceOfflineMode) {
            this.offlineMode = true;
        }

        if (this.hasUpdatedFiles()) {
            this.downloadUpdatedFiles();
        }
        checkForLauncherUpdate();

        downloadExternalLibraries();

        if (!Utils.checkAuthLibLoaded()) {
            LogManager.error("AuthLib was not loaded into the classpath!");
        }

        loadNews(); // Load the news

        this.languageLoaded = true; // Languages are now loaded

        MinecraftVersionManager.loadMinecraftVersions(); // Load info about the different Minecraft versions

        PackManager.loadPacks(); // Load the Packs available in the Launcher

        loadUsers(); // Load the Testers and Allowed Players for the packs

        InstanceManager.loadInstances(); // Load the users installed Instances

        loadCheckingServers(); // Load the saved servers we're checking with the tool

        console.setupLanguage(); // Setup language on the console

        clearOldLogs(); // Clear all the old logs out

        AccountManager.checkUUIDs(); // Check for accounts UUID's and add them if necessary

        InstanceManager.changeUserLocks(); // Changes any instances user locks to UUIDs if available

        AccountManager.checkForNameChanges(); // Check account for username changes

        LogManager.debug("Checking for access to master server");
        OUTER:
        for (Pack pack : Data.PACKS) {
            if (pack.isTester()) {
                for (Server server : this.servers) {
                    if (server.getName().equals("Master Server (Testing Only)")) {
                        server.setUserSelectable(true);
                        LogManager.debug("Access to master server granted");
                        break OUTER; // Don't need to check anymore so break the outer loop
                    }
                }
            }
        }
        LogManager.debug("Finished checking for access to master server");

        if (Utils.isWindows() && SettingsManager.getJavaPath().contains("x86")) {
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

        if (SettingsManager.isAdvancedBackupsEnabled()) {
            dropbox = new DropboxSync();
        }

        if (!SettingsManager.hasHadPasswordDialog()) {
            checkAccounts(); // Check accounts with stored passwords
        }

        if (SettingsManager.enableServerChecker()) {
            this.startCheckingServers();
        }

        if (SettingsManager.enableLogs()) {
            App.TASKPOOL.execute(new Runnable() {
                @Override
                public void run() {
                    ATLauncherAPIUtils.postSystemInfo();
                }
            });
        }
    }

    public void startCheckingServers() {
        if (this.checkingServersTimer != null) {
            // If it's not null, cancel and purge tasks left
            this.checkingServersTimer.cancel();
            this.checkingServersTimer.purge(); // not sure if needed or not
        }

        if (SettingsManager.enableServerChecker()) {
            this.checkingServersTimer = new Timer();
            this.checkingServersTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (MinecraftServer server : checkingServers) {
                        server.checkServer();
                    }
                }
            }, 0, SettingsManager.getServerCheckerWaitInMilliseconds());
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
    }

    public void clearOldLogs() {
        App.TASKPOOL.execute(new Runnable() {
            @Override
            public void run() {
                LogManager.debug("Clearing out old logs");

                Date toDeleteAfter = new Date();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(toDeleteAfter);
                calendar.add(Calendar.DATE, -(SettingsManager.getDaysOfLogsToKeep()));
                toDeleteAfter = calendar.getTime();

                for (File file : FileSystem.LOGS.toFile().listFiles(Utils.getLogsFileFilter())) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").parse(file.getName().replace
                                (Constants.LAUNCHER_NAME + "-Log_", "").replace(".log", ""));

                        if (date.before(toDeleteAfter)) {
                            FileUtils.delete(file.toPath());
                            LogManager.debug("Deleting log file " + file.getName());
                        }
                    } catch (java.text.ParseException e) {
                        LogManager.error("Invalid log file " + file.getName());
                    }
                }

                LogManager.debug("Finished clearing out old logs");
            }
        });
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
                target = "jar";
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
                toget = "jar";
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

    public boolean hasUpdatedFiles() {
        DownloadPool pool = this.getLauncherFiles();
        if (pool != null) {
            return pool.any();
        } else {
            return false;
        }
    }

    private DownloadPool getLauncherFiles() {
        Downloadable dl = new Downloadable("launcher/json/hashes.json", true);
        try {
            List<DownloadableFile> files = dl.fromJson(new TypeToken<List<DownloadableFile>>() {
            }.getType());
            if (files == null) {
                this.offlineMode = true;
                return null;
            }

            DownloadPool pool = new DownloadPool();
            for (DownloadableFile df : files) {
                if (df.isLauncher()) {
                    continue;
                }

                pool.add(df.getDownloadable());
            }

            return pool;
        } catch (Exception e) {
            String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Error", dl.toString());
            LogManager.logStackTrace("Error loading in file hashes, see error details @ " + result, e);
            return null;
        }
    }

    private void downloadUpdatedFiles() {
        LogManager.info("Downloading launcher files");
        DownloadPool pool = this.getLauncherFiles();
        if (pool != null) {
            pool.downloadAll();
        }
        LogManager.info("Finished downloading launcher files");

        if (Language.INSTANCE.getCurrent() != null) {
            try {
                Language.INSTANCE.reload(Language.INSTANCE.getCurrent());
            } catch (IOException e) {
                LogManager.logStackTrace("Couldn't reload language " + Language.INSTANCE.getCurrent(), e);
            }
        }
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
                    downloadUpdatedFiles();
                }
                checkForLauncherUpdate();
                loadNews(); // Load the news
                reloadNewsPanel(); // Reload news panel
                PackManager.loadPacks(); // Load the Packs available in the Launcher
                loadUsers(); // Load the Testers and Allowed Players for the packs
                InstanceManager.loadInstances(); // Load the users installed Instances
                EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.InstancesChangeEvent.class));
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
        try {
            java.lang.reflect.Type type = new TypeToken<List<LauncherLibrary>>() {
            }.getType();
            List<LauncherLibrary> libraries = JsonFile.of("libraries.json", type);
            ExecutorService executor = Utils.generateDownloadExecutor();
            for (final LauncherLibrary lib : libraries) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Downloadable dl = lib.getDownloadable();
                            if (dl.needToDownload()) {
                                dl.download();
                            }
                            Path path = lib.getFilePath();
                            if (lib.shouldAutoLoad() && !Utils.addToClasspath(path)) {
                                LogManager.error("Couldn't add " + path + " to the classpath!");
                                if (lib.shouldExitOnFail()) {
                                    LogManager.error("Library is necessary so launcher will exit!");
                                    System.exit(1);
                                }
                            }
                        } catch (Exception e) {
                            LogManager.logStackTrace("Error downloading library " + lib.getName(), e);
                        }
                    }
                });
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
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
                JsonFile file = new JsonFile(FileSystemData.CHECKING_SERVERS);
                Data.CHECKING_SERVERS.addAll((List<MinecraftServer>) file.convert(MinecraftServer.LIST_TYPE));
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

    private DirectoryStream.Filter<Path> languagesFilter() {
        return new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path o) throws IOException {
                return Files.isRegularFile(o) && o.toString().endsWith(".lang");
            }
        };
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
            EventHandler.EVENT_BUS.publish(new EventHandler.PacksChangeEvent(true));
            EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.InstancesChangeEvent.class));
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

    public String getUserAgent() {
        return "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.72 " +
                "Safari/537.36 " + Constants.LAUNCHER_NAME + "/" + Constants.VERSION;
    }

    public void restartLauncher() {
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = null;
        try {
            path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
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
}
