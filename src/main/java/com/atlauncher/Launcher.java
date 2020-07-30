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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.atlauncher.data.Constants;
import com.atlauncher.data.DownloadableFile;
import com.atlauncher.data.LauncherVersion;
import com.atlauncher.data.MinecraftServer;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.data.News;
import com.atlauncher.data.PackUsers;
import com.atlauncher.data.Server;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.NewsTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
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
    public List<Server> servers = new ArrayList<>(); // Users Installed Servers
    private List<MinecraftServer> checkingServers = new ArrayList<>();

    // UI things
    private JFrame parent; // Parent JFrame of the actual Launcher
    private InstancesTab instancesPanel; // The instances panel
    private ServersTab serversPanel; // The instances panel
    private NewsTab newsPanel; // The news panel
    private PacksTab vanillaPacksPanel; // The vanilla packs panel
    private PacksTab featuredPacksPanel; // The featured packs panel
    private PacksTab packsPanel; // The packs panel

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

        PackManager.loadPacks(); // Load the Packs available in the Launcher

        loadUsers(); // Load the Testers and Allowed Players for the packs

        InstanceManager.loadInstances(); // Load the users installed Instances

        loadServers(); // Load the users installed servers

        AccountManager.loadAccounts(); // Load the saved Accounts

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
            PackManager.loadPacks(); // Load the Packs available in the Launcher
            reloadVanillaPacksPanel(); // Reload packs panel
            reloadFeaturedPacksPanel(); // Reload packs panel
            reloadPacksPanel(); // Reload packs panel
            loadUsers(); // Load the Testers and Allowed Players for the packs
            InstanceManager.loadInstances(); // Load the users installed Instances
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
                .addAll(PackManager.getPacks().stream().map(p -> p.getSafeName().toLowerCase() + ".png").collect(Collectors.toList()));
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

    public void setServerVisibility(Server server, boolean collapsed) {
        if (server != null && AccountManager.getSelectedAccount().isReal()) {
            if (collapsed) {
                // Closed It
                if (!AccountManager.getSelectedAccount().getCollapsedServers().contains(server.name)) {
                    AccountManager.getSelectedAccount().getCollapsedServers().add(server.name);
                }
            } else {
                // Opened It
                if (AccountManager.getSelectedAccount().getCollapsedServers().contains(server.name)) {
                    AccountManager.getSelectedAccount().getCollapsedServers().remove(server.name);
                }
            }
            AccountManager.saveAccounts();
            reloadServersPanel();
        }
    }

    public ArrayList<Server> getServersSorted() {
        ArrayList<Server> servers = new ArrayList<>(this.servers);
        servers.sort(Comparator.comparing(s -> s.name));
        return servers;
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

    public boolean isServer(String name) {
        return this.servers.stream()
                .anyMatch(s -> s.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
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
}
