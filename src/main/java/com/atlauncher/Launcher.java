/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
package com.atlauncher;

import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Account;
import com.atlauncher.data.Constants;
import com.atlauncher.data.DownloadableFile;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.LauncherVersion;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.data.News;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackUsers;
import com.atlauncher.data.Server;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.components.LauncherBottomBar;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.ATLauncherAPIUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.mini2Dx.gettext.GetText;

import net.arikia.dev.drpc.DiscordRPC;
import okhttp3.OkHttpClient;

public class Launcher {
    // Holding all the data
    private LauncherVersion latestLauncherVersion; // Latest Launcher version
    private List<DownloadableFile> launcherFiles; // Files the Launcher needs to download
    private List<News> news = new ArrayList<>(); // News
    private Map<String, MinecraftVersion> minecraftVersions; // Minecraft versions
    public List<Pack> packs = new ArrayList<>(); // Packs in the Launcher
    public List<Instance> instances = new ArrayList<>(); // Users Installed Instances
    public List<InstanceV2> instancesV2 = new ArrayList<>(); // Users Installed Instances (new format)
    public List<Server> servers = new ArrayList<>(); // Users Installed Servers
    private List<Account> accounts = new ArrayList<>(); // Accounts in the Launcher
    private List<MinecraftServer> checkingServers = new ArrayList<>();
    public Account account; // Account using the Launcher

    // UI things
    private JFrame parent; // Parent JFrame of the actual Launcher
    private InstancesTab instancesPanel; // The instances panel
    private ServersTab serversPanel; // The instances panel
    private NewsTab newsPanel; // The news panel
    private PacksTab vanillaPacksPanel; // The vanilla packs panel
    private PacksTab featuredPacksPanel; // The featured packs panel
    private PacksTab packsPanel; // The packs panel
    private LauncherBottomBar bottomBar; // The bottom bar

    // Minecraft tracking variables
    private Process minecraftProcess = null; // The process minecraft is running on
    private boolean minecraftLaunched = false; // If Minecraft has been Launched

    // timer for server checking tool
    private Timer checkingServersTimer = null; // Timer used for checking servers

    public void checkIfWeCanLoad() {
        if (!Java.isUsingJavaSupportingLetsEncrypt()) {
            LogManager.warn("You're using an old version of Java that will not work!");

            DialogManager.optionDialog().setTitle(GetText.tr("Unsupported Java Version"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using an unsupported version of Java. You need to upgrade your Java to at minimum Java 8 version 101.<br/><br/>The launcher will not start until you do this.<br/><br/>If you're seeing this message even after installing a newer version, you may need to uninstall the old version first.<br/><br/>Click ok to open the Java download page and close the launcher."))
                            .build())
                    .addOption(GetText.tr("Ok")).setType(DialogManager.ERROR).show();

            OS.openWebBrowser("https://atl.pw/java8download");
            System.exit(0);
        }
    }

    public void loadEverything() {
        PerformanceManager.start();
        if (hasUpdatedFiles()) {
            downloadUpdatedFiles(); // Downloads updated files on the server
        }

        checkForLauncherUpdate();

        addExecutableBitToTools();

        loadNews(); // Load the news

        loadMinecraftVersions(); // Load info about the different Minecraft versions

        loadPacks(); // Load the Packs available in the Launcher

        loadUsers(); // Load the Testers and Allowed Players for the packs

        loadInstances(); // Load the users installed Instances

        loadServers(); // Load the users installed servers

        loadAccounts(); // Load the saved Accounts

        loadCheckingServers(); // Load the saved servers we're checking with the tool

        removeUnusedImages(); // remove unused pack images

        if (OS.isWindows() && !OS.is64Bit() && OS.isWindows64Bit()) {
            LogManager.warn("You're using 32 bit Java on a 64 bit Windows install!");

            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Running 32 Bit Java on 64 Bit Windows"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We have detected that you're running 64 bit Windows but not 64 bit Java.<br/><br/>This will cause severe issues playing all packs if not fixed.<br/><br/>Do you want to close the launcher and learn how to fix this issue now?"))
                            .build())
                    .setType(DialogManager.ERROR).show();

            if (ret == 0) {
                OS.openWebBrowser("https://www.atlauncher.com/help/32bit/");
                System.exit(0);
            }
        }

        if (Java.isMinecraftJavaNewerThanJava8() && !App.settings.hideJava9Warning) {
            LogManager.warn("You're using a newer version of Java than Java 8! Modpacks may not launch!");

            int ret = DialogManager.optionDialog()
                    .setTitle(GetText.tr("Warning! You may not be able to play Minecraft"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using Java 9 or newer! Older modpacks may not work.<br/><br/>If you have issues playing some packs, you may need to install Java 8 and set it to be used in the launchers java settings"))
                            .build())
                    .addOption(GetText.tr("Download"), true).addOption(GetText.tr("Ok"))
                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openWebBrowser("https://atl.pw/java8download");
                System.exit(0);
            } else if (ret == 2) {
                App.settings.hideJava9Warning = true;
                App.settings.save();
            }
        }

        if (!Java.isJava7OrAbove(true) && !App.settings.hideOldJavaWarning) {
            LogManager.warn("You're using an old unsupported version of Java (Java 7 or older)!");

            int ret = DialogManager.optionDialog().setTitle(GetText.tr("Unsupported Java Version"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "You're using an unsupported version of Java. You should upgrade your Java to at minimum Java 7.<br/><br/>Without Java 7 some mods will refuse to load meaning you cannot play.<br/><br/>Click Download to go to the Java downloads page"))
                            .build())
                    .addOption(GetText.tr("Download"), true).addOption(GetText.tr("Ok"))
                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openWebBrowser("https://atl.pw/java8download");
                System.exit(0);
            } else if (ret == 2) {
                App.settings.hideOldJavaWarning = true;
                App.settings.save();
            }
        }

        if (App.settings.enableServerChecker) {
            this.startCheckingServers();
        }

        if (App.settings.enableLogs) {
            App.TASKPOOL.execute(ATLauncherAPIUtils::postSystemInfo);

            if (App.settings.enableAnalytics) {
                Analytics.startSession();
            }
        }
        PerformanceManager.end();
    }

    public void startCheckingServers() {
        PerformanceManager.start();
        if (this.checkingServersTimer != null) {
            // If it's not null, cancel and purge tasks left
            this.checkingServersTimer.cancel();
            this.checkingServersTimer.purge(); // not sure if needed or not
        }

        if (App.settings.enableServerChecker) {
            this.checkingServersTimer = new Timer();
            this.checkingServersTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (MinecraftServer server : checkingServers) {
                        server.checkServer();
                    }
                }
            }, 0, App.settings.serverCheckerWait * 1000);
        }
        PerformanceManager.end();
    }

    public boolean launcherHasUpdate() {
        try {
            this.latestLauncherVersion = Gsons.DEFAULT
                    .fromJson(new FileReader(FileSystem.JSON.resolve("version.json").toFile()), LauncherVersion.class);
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace("Exception when loading latest launcher version!", e);
        }

        return this.latestLauncherVersion != null && Constants.VERSION.needsUpdate(this.latestLauncherVersion);
    }

    public void downloadUpdate() {
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
            File newFile = FileSystem.TEMP.resolve(saveAs).toFile();
            LogManager.info("Downloading Launcher Update");
            Analytics.sendEvent("Update", "Launcher");

            ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Downloading Launcher Update"), 1,
                    GetText.tr("Downloading Launcher Update"));
            progressDialog.addThread(new Thread(() -> {
                com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/%s.%s", Constants.DOWNLOAD_SERVER, Constants.LAUNCHER_NAME, toget))
                        .withHttpClient(Network.createProgressClient(progressDialog)).downloadTo(newFile.toPath());

                progressDialog.setTotalBytes(download.getFilesize());

                try {
                    download.downloadFile();
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to download update", e);
                    progressDialog.setReturnValue(false);
                    progressDialog.close();
                    return;
                }

                progressDialog.setReturnValue(true);
                progressDialog.doneTask();
                progressDialog.close();
            }));
            progressDialog.start();

            if ((Boolean) progressDialog.getReturnValue()) {
                runUpdate(path, newFile.getAbsolutePath());
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void runUpdate(String currentPath, String temporaryUpdatePath) {
        List<String> arguments = new ArrayList<>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        if (OS.isWindows()) {
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

    /**
     * This checks the servers files.json file and gets the files that the Launcher
     * needs to have
     */
    private List<com.atlauncher.network.Download> getLauncherFiles() {
        if (this.launcherFiles == null) {
            java.lang.reflect.Type type = new TypeToken<List<DownloadableFile>>() {
            }.getType();

            try {
                this.launcherFiles = com.atlauncher.network.Download.build().cached()
                        .setUrl(String.format("%s/launcher/json/files.json", Constants.DOWNLOAD_SERVER)).asType(type);
            } catch (Exception e) {
                LogManager.logStackTrace("Error loading in file hashes!", e);
                return null;
            }
        }

        if (this.launcherFiles == null) {
            return null;
        }

        return this.launcherFiles.stream()
                .filter(file -> !file.isLauncher() && !file.isFiles() && file.isForArchAndOs())
                .map(DownloadableFile::getDownload).collect(Collectors.toList());
    }

    public void downloadUpdatedFiles() {
        ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Downloading Updates"), 1,
                GetText.tr("Downloading Updates"));
        progressDialog.addThread(new Thread(() -> {
            DownloadPool pool = new DownloadPool();
            OkHttpClient httpClient = Network.createProgressClient(progressDialog);
            pool.addAll(
                    getLauncherFiles().stream().map(dl -> dl.withHttpClient(httpClient)).collect(Collectors.toList()));
            DownloadPool smallPool = pool.downsize();

            progressDialog.setTotalBytes(smallPool.totalSize());

            pool.downloadAll();
            progressDialog.doneTask();
            progressDialog.close();
        }));
        progressDialog.start();

        LogManager.info("Finished downloading updated files!");
    }

    public boolean checkForUpdatedFiles() {
        this.launcherFiles = null;

        return hasUpdatedFiles();
    }

    /**
     * This checks the servers hashes.json file and looks for new/updated files that
     * differ from what the user has
     */
    public boolean hasUpdatedFiles() {
        LogManager.info("Checking for updated files!");
        List<com.atlauncher.network.Download> downloads = getLauncherFiles();

        if (downloads == null) {
            return false;
        }

        return downloads.stream().anyMatch(com.atlauncher.network.Download::needToDownload);
    }

    public void reloadLauncherData() {
        final JDialog dialog = new JDialog(this.parent, ModalityType.APPLICATION_MODAL);
        dialog.setSize(300, 100);
        dialog.setTitle("Updating Launcher");
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setLayout(new FlowLayout());
        dialog.setResizable(false);
        dialog.add(new JLabel(GetText.tr("Updating Launcher. Please Wait")));
        App.TASKPOOL.execute(() -> {
            if (hasUpdatedFiles()) {
                downloadUpdatedFiles(); // Downloads updated files on the server
            }
            checkForLauncherUpdate();
            addExecutableBitToTools();
            loadNews(); // Load the news
            reloadNewsPanel(); // Reload news panel
            loadPacks(); // Load the Packs available in the Launcher
            reloadVanillaPacksPanel(); // Reload packs panel
            reloadFeaturedPacksPanel(); // Reload packs panel
            reloadPacksPanel(); // Reload packs panel
            loadUsers(); // Load the Testers and Allowed Players for the packs
            loadInstances(); // Load the users installed Instances
            reloadInstancesPanel(); // Reload instances panel
            reloadServersPanel(); // Reload instances panel
            dialog.setVisible(false); // Remove the dialog
            dialog.dispose(); // Dispose the dialog
        });
        dialog.setVisible(true);
    }

    private void checkForLauncherUpdate() {
        PerformanceManager.start();
        if (App.noLauncherUpdate) {
            return;
        }

        LogManager.debug("Checking for launcher update");
        if (launcherHasUpdate()) {
            if (!App.wasUpdated) {
                downloadUpdate(); // Update the Launcher
            } else {
                DialogManager.okDialog().setTitle("Update Failed!")
                        .setContent(new HTMLBuilder().center()
                                .text(GetText.tr("Update failed. Please click Ok to close "
                                        + "the launcher and open up the downloads page.<br/><br/>Download "
                                        + "the update and replace the old " + Constants.LAUNCHER_NAME + " file."))
                                .build())
                        .setType(DialogManager.ERROR).show();
                OS.openWebBrowser("https://www.atlauncher.com/downloads/");
                System.exit(0);
            }
        }
        LogManager.debug("Finished checking for launcher update");
        PerformanceManager.end();
    }

    private void addExecutableBitToTools() {
        PerformanceManager.start();
        File[] files = FileSystem.TOOLS.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.canExecute()) {
                    LogManager.info("Executable bit being set on " + file.getName());
                    file.setExecutable(true);
                }
            }
        }
        PerformanceManager.end();
    }

    private void removeUnusedImages() {
        PerformanceManager.start();
        File[] files = FileSystem.IMAGES.toFile().listFiles();

        Set<String> packImageFilenames = new HashSet<>();
        packImageFilenames
                .addAll(packs.stream().map(p -> p.getSafeName().toLowerCase() + ".png").collect(Collectors.toList()));
        packImageFilenames.add("defaultimage.png");

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".png") && !packImageFilenames.contains(file.getName())) {
                    LogManager.info("Pack image no longer used, deleting file " + file.getName());
                    file.delete();
                }
            }
        }

        PerformanceManager.end();
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
        this.accounts.add(account);
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
     * Switch account currently used and save it
     *
     * @param account Account to switch to
     */
    public void switchAccount(Account account) {
        if (account == null) {
            LogManager.info("Logging out of account");
            this.account = null;
        } else {
            if (account.isReal()) {
                LogManager.info("Changed account to " + account);
                this.account = account;
            } else {
                LogManager.info("Logging out of account");
                this.account = null;
            }
        }
        refreshVanillaPacksPanel();
        refreshFeaturedPacksPanel();
        refreshPacksPanel();
        reloadInstancesPanel();
        reloadServersPanel();
        reloadAccounts();
        App.settings.save();
    }

    /**
     * Loads the languages for use in the Launcher
     */
    private void loadNews() {
        PerformanceManager.start();
        LogManager.debug("Loading news");
        try {
            java.lang.reflect.Type type = new TypeToken<List<News>>() {
            }.getType();
            File fileDir = FileSystem.JSON.resolve("newnews.json").toFile();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));

            this.news = Gsons.DEFAULT.fromJson(in, type);
            in.close();
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading news");
        PerformanceManager.end();
    }

    /**
     * Loads info about the different Minecraft versions
     */
    private void loadMinecraftVersions() {
        PerformanceManager.start();
        LogManager.debug("Loading Minecraft versions");

        this.minecraftVersions = new HashMap<>();

        try {
            java.lang.reflect.Type type = new TypeToken<List<MinecraftVersion>>() {
            }.getType();
            List<MinecraftVersion> list = Gsons.DEFAULT_ALT
                    .fromJson(new FileReader(FileSystem.JSON.resolve("minecraft.json").toFile()), type);

            if (list == null) {
                LogManager.error("Error loading Minecraft Versions. List was null. Exiting!");
                System.exit(1); // Cannot recover from this so exit
            }

            for (MinecraftVersion mv : list) {
                this.minecraftVersions.put(mv.version, mv);
            }
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading Minecraft versions");
        PerformanceManager.end();
    }

    /**
     * Loads the Packs for use in the Launcher
     */
    private void loadPacks() {
        PerformanceManager.start();
        LogManager.debug("Loading packs");
        try {
            java.lang.reflect.Type type = new TypeToken<List<Pack>>() {
            }.getType();
            this.packs = Gsons.DEFAULT_ALT.fromJson(new FileReader(FileSystem.JSON.resolve("packsnew.json").toFile()),
                    type);
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading packs");
        PerformanceManager.end();
    }

    /**
     * Loads the Testers and Allowed Players for the packs in the Launcher
     */
    private void loadUsers() {
        PerformanceManager.start();
        LogManager.debug("Loading users");
        List<PackUsers> packUsers = null;
        try {
            java.lang.reflect.Type type = new TypeToken<List<PackUsers>>() {
            }.getType();
            packUsers = Gsons.DEFAULT_ALT.fromJson(new FileReader(FileSystem.JSON.resolve("users.json").toFile()),
                    type);
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }
        if (packUsers == null) {
            return;
        }
        for (PackUsers pu : packUsers) {
            pu.addUsers();
        }
        LogManager.debug("Finished loading users");
        PerformanceManager.end();
    }

    /**
     * Loads the user installed Instances
     */
    private void loadInstances() {
        PerformanceManager.start();
        LogManager.debug("Loading instances");
        this.instances = new ArrayList<>(); // Reset the instances list
        this.instancesV2 = new ArrayList<>(); // Reset the instancesv2 list

        for (String folder : Optional.of(FileSystem.INSTANCES.toFile().list(Utils.getInstanceFileFilter()))
                .orElse(new String[0])) {
            File instanceDir = FileSystem.INSTANCES.resolve(folder).toFile();

            Instance instance = null;
            InstanceV2 instanceV2 = null;

            try {
                try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                    instanceV2 = Gsons.MINECRAFT.fromJson(fileReader, InstanceV2.class);
                    instanceV2.ROOT = instanceDir.toPath();
                    LogManager.debug("Loaded V2 instance from " + instanceDir);

                    if (instanceV2.launcher == null) {
                        instanceV2 = null;
                        throw new JsonSyntaxException("Error parsing instance.json as InstanceV2");
                    }
                } catch (JsonIOException | JsonSyntaxException ignored) {
                    try (FileReader fileReader = new FileReader(new File(instanceDir, "instance.json"))) {
                        instance = Gsons.DEFAULT.fromJson(fileReader, Instance.class);
                        instance.ROOT = instanceDir.toPath();
                        instance.convert();
                        LogManager.debug("Loaded V1 instance from " + instanceDir);
                    } catch (JsonIOException | JsonSyntaxException e) {
                        LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e);
                        continue;
                    }
                }
            } catch (Exception e2) {
                LogManager.logStackTrace("Failed to load instance in the folder " + instanceDir, e2);
                continue;
            }

            if (instance == null && instanceV2 == null) {
                LogManager.error("Failed to load instance in the folder " + instanceDir);
                continue;
            }

            if (instance != null) {
                if (!instance.getDisabledModsDirectory().exists()) {
                    instance.getDisabledModsDirectory().mkdir();
                }

                if (isPackByName(instance.getPackName())) {
                    instance.setRealPack(getPackByName(instance.getPackName()));
                }

                this.instances.add(instance);
            }

            if (instanceV2 != null) {
                this.instancesV2.add(instanceV2);
            }
        }

        LogManager.debug("Finished loading instances");
        PerformanceManager.end();
    }

    /**
     * Loads the user installed servers
     */
    private void loadServers() {
        PerformanceManager.start();
        LogManager.debug("Loading servers");
        this.servers = new ArrayList<>(); // Reset the servers list

        for (String folder : Optional.of(FileSystem.SERVERS.toFile().list(Utils.getServerFileFilter()))
                .orElse(new String[0])) {
            File serverDir = FileSystem.SERVERS.resolve(folder).toFile();

            Server server = null;

            try (FileReader fileReader = new FileReader(new File(serverDir, "server.json"))) {
                server = Gsons.MINECRAFT.fromJson(fileReader, Server.class);
                LogManager.debug("Loaded server from " + serverDir);
            } catch (Exception e) {
                LogManager.logStackTrace("Failed to load server in the folder " + serverDir, e);
                continue;
            }

            if (server == null) {
                LogManager.error("Failed to load server in the folder " + serverDir);
                continue;
            }

            this.servers.add(server);
        }

        LogManager.debug("Finished loading servers");
        PerformanceManager.end();
    }

    public void saveInstances() {
        for (Instance instance : this.instances) {
            File instanceFile = new File(instance.getRootDirectory(), "instance.json");
            FileWriter fw = null;
            BufferedWriter bw = null;
            try {
                if (!instanceFile.exists()) {
                    instanceFile.createNewFile();
                }

                fw = new FileWriter(instanceFile);
                bw = new BufferedWriter(fw);
                bw.write(Gsons.DEFAULT.toJson(instance));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            } finally {
                try {
                    if (bw != null) {
                        bw.close();
                    }
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace(
                            "Exception while trying to close FileWriter/BufferedWriter for saving instances "
                                    + "json file.",
                            e);
                }
            }
        }
    }

    /**
     * Loads the saved Accounts
     */
    private void loadAccounts() {
        PerformanceManager.start();
        LogManager.debug("Loading accounts");
        if (Files.exists(FileSystem.USER_DATA)) {
            FileInputStream in = null;
            ObjectInputStream objIn = null;
            try {
                in = new FileInputStream(FileSystem.USER_DATA.toFile());
                objIn = new ObjectInputStream(in);
                Object obj;
                while ((obj = objIn.readObject()) != null) {
                    if (obj instanceof Account) {
                        accounts.add((Account) obj);
                    }
                }
            } catch (EOFException e) {
                // Don't log this, it always happens when it gets to the end of the file
            } catch (IOException | ClassNotFoundException e) {
                LogManager.logStackTrace("Exception while trying to read accounts in from file.", e);
            } finally {
                try {
                    if (objIn != null) {
                        objIn.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace(
                            "Exception while trying to close FileInputStream/ObjectInputStream when reading in " + ""
                                    + "accounts.",
                            e);
                }
            }
        }
        LogManager.debug("Finished loading accounts");
        PerformanceManager.end();
    }

    public void saveAccounts() {
        FileOutputStream out = null;
        ObjectOutputStream objOut = null;
        try {
            out = new FileOutputStream(FileSystem.USER_DATA.toFile());
            objOut = new ObjectOutputStream(out);
            for (Account account : accounts) {
                objOut.writeObject(account);
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Exception while trying to close FileOutputStream/ObjectOutputStream when saving "
                                + "accounts.",
                        e);
            }
        }
    }

    public void removeAccount(Account account) {
        if (this.account == account) {
            if (accounts.size() == 1) {
                // if this was the only account, don't set an account
                switchAccount(null);
            } else {
                // if they have more accounts, switch to the first one
                switchAccount(accounts.get(0));
            }
        }
        accounts.remove(account);
        saveAccounts();
        reloadAccounts();
    }

    /**
     * Loads the user servers added for checking
     */
    private void loadCheckingServers() {
        PerformanceManager.start();
        LogManager.debug("Loading servers to check");
        this.checkingServers = new ArrayList<>(); // Reset the list
        if (Files.exists(FileSystem.CHECKING_SERVERS_JSON)) {
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(FileSystem.CHECKING_SERVERS_JSON.toFile());
            } catch (FileNotFoundException e) {
                LogManager.logStackTrace(e);
                return;
            }

            this.checkingServers = Gsons.DEFAULT.fromJson(fileReader, MinecraftServer.LIST_TYPE);

            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LogManager
                            .logStackTrace("Exception while trying to close FileReader when loading servers for server "
                                    + "checker" + " tool.", e);
                }
            }
        }
        LogManager.debug("Finished loading servers to check");
        PerformanceManager.end();
    }

    public void saveCheckingServers() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!Files.exists(FileSystem.CHECKING_SERVERS_JSON)) {
                Files.createFile(FileSystem.CHECKING_SERVERS_JSON);
            }

            fw = new FileWriter(FileSystem.CHECKING_SERVERS_JSON.toFile());
            bw = new BufferedWriter(fw);
            bw.write(Gsons.DEFAULT.toJson(this.checkingServers));
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Exception while trying to close FileWriter/BufferedWriter when saving servers for "
                                + "server checker tool.",
                        e);
            }
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
    public List<Pack> getPacksSortedAlphabetically(boolean isFeatured, boolean isSystem) {
        List<Pack> packs = new LinkedList<>();

        for (Pack pack : this.packs) {
            if (isFeatured) {
                if (!pack.isFeatured()) {
                    continue;
                }
            }

            if (isSystem) {
                if (pack.isSystem()) {
                    packs.add(pack);
                }
            } else {
                if (!pack.isSystem()) {
                    packs.add(pack);
                }
            }
        }

        packs.sort(Comparator.comparing(Pack::getName));
        return packs;
    }

    /**
     * Get the Packs available in the Launcher sorted by position
     *
     * @return The Packs available in the Launcher sorted by position
     */
    public List<Pack> getPacksSortedPositionally(boolean isFeatured, boolean isSystem) {
        List<Pack> packs = new LinkedList<>();

        for (Pack pack : this.packs) {
            if (isFeatured) {
                if (!pack.isFeatured()) {
                    continue;
                }
            }

            if (isSystem) {
                if (pack.isSystem()) {
                    packs.add(pack);
                }
            } else {
                if (!pack.isSystem()) {
                    packs.add(pack);
                }
            }
        }

        packs.sort(Comparator.comparingInt(Pack::getPosition));
        return packs;
    }

    public void setPackVisbility(Pack pack, boolean collapsed) {
        if (pack != null && account != null && account.isReal()) {
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
            reloadVanillaPacksPanel();
            reloadFeaturedPacksPanel();
            reloadPacksPanel();
        }
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

    public void setInstanceVisbility(InstanceV2 instanceV2, boolean collapsed) {
        if (instanceV2 != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedInstances().contains(instanceV2.launcher.name)) {
                    account.getCollapsedInstances().add(instanceV2.launcher.name);
                }
            } else {
                // Opened It
                if (account.getCollapsedInstances().contains(instanceV2.launcher.name)) {
                    account.getCollapsedInstances().remove(instanceV2.launcher.name);
                }
            }
            saveAccounts();
            reloadInstancesPanel();
        }
    }

    public void setServerVisibility(Server server, boolean collapsed) {
        if (server != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedServers().contains(server.name)) {
                    account.getCollapsedServers().add(server.name);
                }
            } else {
                // Opened It
                if (account.getCollapsedServers().contains(server.name)) {
                    account.getCollapsedServers().remove(server.name);
                }
            }
            saveAccounts();
            reloadServersPanel();
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
        ArrayList<Instance> instances = new ArrayList<>(this.instances);
        instances.sort(Comparator.comparing(Instance::getName));
        return instances;
    }

    public ArrayList<InstanceV2> getInstancesV2Sorted() {
        ArrayList<InstanceV2> instances = new ArrayList<>(this.instancesV2);
        instances.sort(Comparator.comparing(i -> i.launcher.name));
        return instances;
    }

    public ArrayList<Server> getServersSorted() {
        ArrayList<Server> servers = new ArrayList<>(this.servers);
        servers.sort(Comparator.comparing(s -> s.name));
        return servers;
    }

    public void setInstanceUnplayable(Instance instance) {
        instance.setUnplayable();
        saveInstances();
        reloadInstancesPanel();
    }

    /**
     * Removes an instance from the Launcher
     *
     * @param instance The Instance to remove from the launcher.
     */
    public void removeInstance(Instance instance) {
        if (this.instances.remove(instance)) {
            Utils.delete(instance.getRootDirectory());
            saveInstances();
            reloadInstancesPanel();
        }
    }

    public void removeInstance(InstanceV2 instance) {
        if (this.instancesV2.remove(instance)) {
            FileUtils.deleteDirectory(instance.getRoot());
            reloadInstancesPanel();
        }
    }

    public void removeServer(Server server) {
        if (this.servers.remove(server)) {
            FileUtils.deleteDirectory(server.getRoot());
            reloadServersPanel();
        }
    }

    public boolean canViewSemiPublicPackByCode(String packCode) {
        for (String code : App.settings.addedPacks) {
            if (Hashing.md5(code).equals(Hashing.HashCode.fromString(packCode))) {
                return true;
            }
        }
        return false;
    }

    public boolean isMinecraftVersion(String version) {
        return this.minecraftVersions.containsKey(version);
    }

    public MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        if (this.minecraftVersions.containsKey(version)) {
            return this.minecraftVersions.get(version);
        }
        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }

    public boolean semiPublicPackExistsFromCode(String packCode) {
        for (Pack pack : this.packs) {
            if (pack.isSemiPublic()) {
                if (Hashing.HashCode.fromString(pack.getCode()).equals(Hashing.md5(packCode))) {
                    return true;
                }
            }
        }
        return false;
    }

    public Pack getSemiPublicPackByCode(String packCode) {
        for (Pack pack : this.packs) {
            if (pack.isSemiPublic()) {
                if (Hashing.HashCode.fromString(pack.getCode()).equals(Hashing.md5(packCode))) {
                    return pack;
                }
            }
        }

        return null;
    }

    public boolean addPack(String packCode) {
        for (Pack pack : this.packs) {
            if (pack.isSemiPublic() && !App.launcher.canViewSemiPublicPackByCode(Hashing.md5(packCode).toString())) {
                if (Hashing.HashCode.fromString(pack.getCode()).equals(Hashing.md5(packCode))) {
                    if (pack.isTester()) {
                        return false;
                    }
                    App.settings.addedPacks.add(packCode);
                    App.settings.save();
                    this.refreshVanillaPacksPanel();
                    this.refreshFeaturedPacksPanel();
                    this.refreshPacksPanel();
                    return true;
                }
            }
        }
        return false;
    }

    public void removePack(String packCode) {
        for (String code : App.settings.addedPacks) {
            if (Hashing.md5(code).equals(Hashing.HashCode.fromString(packCode))) {
                App.settings.addedPacks.remove(packCode);
                App.settings.save();
                this.refreshVanillaPacksPanel();
                this.refreshFeaturedPacksPanel();
                this.refreshPacksPanel();
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
        for (News newsItem : App.launcher.getNews()) {
            news += newsItem.getHTML();
            if (App.launcher.getNews().get(App.launcher.getNews().size() - 1) != newsItem) {
                news += "<hr/>";
            }
        }
        news += "</html>";
        return news;
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
     * @param instancesPanel Instances Panel
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

    public void setServersPanel(ServersTab serversPanel) {
        this.serversPanel = serversPanel;
    }

    public void reloadServersPanel() {
        if (serversPanel != null) {
            this.serversPanel.reload(); // Reload the servers panel
        }
    }

    /**
     * Sets the panel used for Vanilla Packs
     *
     * @param vanillaPacksPanel Vanilla Packs Panel
     */
    public void setVanillaPacksPanel(PacksTab vanillaPacksPanel) {
        this.vanillaPacksPanel = vanillaPacksPanel;
    }

    /**
     * Sets the panel used for Featured Packs
     *
     * @param featuredPacksPanel Featured Packs Panel
     */
    public void setFeaturedPacksPanel(PacksTab featuredPacksPanel) {
        this.featuredPacksPanel = featuredPacksPanel;
    }

    /**
     * Sets the panel used for Packs
     *
     * @param packsPanel Packs Panel
     */
    public void setPacksPanel(PacksTab packsPanel) {
        this.packsPanel = packsPanel;
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
     * Reloads the panel used for Vanilla Packs
     */
    public void reloadVanillaPacksPanel() {
        this.vanillaPacksPanel.reload();
    }

    /**
     * Refreshes the panel used for Vanilla Packs
     */
    public void refreshVanillaPacksPanel() {
        this.vanillaPacksPanel.refresh();
    }

    /**
     * Reloads the panel used for Featured Packs
     */
    public void reloadFeaturedPacksPanel() {
        this.featuredPacksPanel.reload();
    }

    /**
     * Refreshes the panel used for Featured Packs
     */
    public void refreshFeaturedPacksPanel() {
        this.featuredPacksPanel.refresh();
    }

    /**
     * Reloads the panel used for Packs
     */
    public void reloadPacksPanel() {
        this.packsPanel.reload(); // Reload the instances panel
    }

    /**
     * Refreshes the panel used for Packs
     */
    public void refreshPacksPanel() {
        this.packsPanel.refresh(); // Refresh the instances panel
    }

    /**
     * Sets the bottom bar
     *
     * @param bottomBar The Bottom Bar
     */
    public void setBottomBar(LauncherBottomBar bottomBar) {
        this.bottomBar = bottomBar;
    }

    /**
     * Reloads the bottom bar accounts combobox
     */
    public void reloadAccounts() {
        if (this.bottomBar == null) {
            return; // Bottom Bar hasn't been made yet, so don't do anything
        }
        this.bottomBar.reloadAccounts(); // Reload the Bottom Bar accounts combobox
    }

    /**
     * Checks to see if there is already an instance with the name provided or not
     *
     * @param name The name of the instance to check for
     * @return True if there is an instance with the same name already
     */
    public boolean isInstance(String name) {
        for (Instance instance : instances) {
            if (instance.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", ""))) {
                return true;
            }
        }
        return this.instancesV2.stream()
                .anyMatch(i -> i.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    public boolean isServer(String name) {
        return this.servers.stream()
                .anyMatch(s -> s.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    /**
     * Finds a Pack from the given ID number
     *
     * @param id ID of the Pack to find
     * @return Pack if the pack is found from the ID
     * @throws InvalidPack If ID is not found
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
     * @param name name of the Pack to find
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
     * @param name name of the Pack to find
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
     * Finds a Pack from the given safe name
     *
     * @param name name of the Pack to find
     * @return Pack if the pack is found from the safe name
     */
    public Pack getPackBySafeName(String name) {
        for (Pack pack : packs) {
            if (pack.getSafeName().equalsIgnoreCase(name)) {
                return pack;
            }
        }
        return null;
    }

    /**
     * Checks if there is an instance by the given name
     *
     * @param name name of the Instance to find
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
     * @param name name of the Instance to find
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
     * @param name name of the Instance to find
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
     * @param name name of the Instance to find
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
     * Finds an Account from the given username
     *
     * @param username Username of the Account to find
     * @return Account if the Account is found from the username
     */
    public Account getAccountByName(String username) {
        for (Account account : accounts) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Finds if an Account is available
     *
     * @param username The username of the Account
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

    public void showKillMinecraft(Process minecraft) {
        this.minecraftProcess = minecraft;
        App.console.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        App.console.hideKillMinecraft();
    }

    public void killMinecraft() {
        if (this.minecraftProcess != null) {
            LogManager.error("Killing Minecraft");

            if (App.settings.enableDiscordIntegration && App.discordInitialized) {
                DiscordRPC.discordClearPresence();
            }

            this.minecraftProcess.destroy();
            this.minecraftProcess = null;
        } else {
            LogManager.error("Cannot kill Minecraft as there is no instance open!");
        }
    }

    public void cloneInstance(InstanceV2 instance, String clonedName) {
        InstanceV2 clonedInstance = Gsons.MINECRAFT.fromJson(Gsons.MINECRAFT.toJson(instance), InstanceV2.class);

        if (clonedInstance == null) {
            LogManager.error("Error Occurred While Cloning Instance! Instance Object Couldn't Be Cloned!");
        } else {
            clonedInstance.launcher.name = clonedName;
            clonedInstance.ROOT = FileSystem.INSTANCES.resolve(clonedInstance.getSafeName());
            FileUtils.createDirectory(clonedInstance.getRoot());
            Utils.copyDirectory(instance.getRoot().toFile(), clonedInstance.getRoot().toFile());
            clonedInstance.save();
            this.instancesV2.add(clonedInstance);
            this.reloadInstancesPanel();
        }
    }

    public String getPackInstallableCount() {
        int count = 0;
        for (Pack pack : this.getPacks()) {
            if (pack.canInstall()) {
                count++;
            }
        }
        return count + "";
    }
}
