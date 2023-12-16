/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LWJGLLibrary;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MinecraftError;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.data.Type;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingFile;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.loaders.forge.FMLLibrariesConstants;
import com.atlauncher.data.minecraft.loaders.forge.FMLLibrary;
import com.atlauncher.exceptions.CommandException;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LWJGLManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.CommandExecutor;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.SecurityUtils;
import com.atlauncher.utils.Utils;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import okhttp3.OkHttpClient;

/**
 * @since 2023 / 12 / 16
 *
 * Use case to launch an instance
 */
public class InstanceLauncherUseCase {

    /**
     * This will prepare the instance for launch. It will download the assets,
     * Minecraft jar and libraries, as well as organise the libraries, ready to be
     * played.
     */
    public static boolean prepareForLaunch(Instance instance, ProgressDialog progressDialog, Path nativesTempDir, Path lwjglNativesTempDir) {
        PerformanceManager.start();
        OkHttpClient httpClient = Network.createProgressClient(progressDialog);

        // make sure latest manifest is being used
        PerformanceManager.start("Grabbing Latest Manifest");
        try {
            progressDialog.setLabel(GetText.tr("Grabbing Latest Manifest"));
            VersionManifestVersion minecraftVersionManifest = MinecraftManager
                .getMinecraftVersion(instance.id);

            com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
                .setUrl(minecraftVersionManifest.url).hash(minecraftVersionManifest.sha1)
                .size(minecraftVersionManifest.size)
                .downloadTo(FileSystem.MINECRAFT_VERSIONS_JSON.resolve(minecraftVersionManifest.id + ".json"))
                .withHttpClient(httpClient);

            MinecraftVersion minecraftVersion = download.asClass(MinecraftVersion.class);

            if (minecraftVersion != null) {
                instance.setUpdatedValues(minecraftVersion);
                instance.save();
            }
        } catch (Exception e) {
            // ignored
        }
        progressDialog.doneTask();
        PerformanceManager.end("Grabbing Latest Manifest");

        PerformanceManager.start("Downloading Minecraft");
        try {
            progressDialog.setLabel(GetText.tr("Downloading Minecraft"));
            com.atlauncher.network.Download clientDownload = com.atlauncher.network.Download.build()
                .setUrl(instance.downloads.client.url).hash(instance.downloads.client.sha1).size(instance.downloads.client.size)
                .withHttpClient(httpClient).downloadTo(instance.getMinecraftJarLibraryPath());

            if (clientDownload.needToDownload()) {
                progressDialog.setTotalBytes(instance.downloads.client.size);
                clientDownload.downloadFile();
            }

            progressDialog.doneTask();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            PerformanceManager.end("Downloading Minecraft");
            PerformanceManager.end();
            return false;
        }
        PerformanceManager.end("Downloading Minecraft");

        if (instance.logging != null) {
            PerformanceManager.start("Downloading Logging Config");
            try {
                progressDialog.setLabel(GetText.tr("Downloading Logging Config"));

                LoggingFile loggingFile = instance.logging.client.file;

                com.atlauncher.network.Download loggerDownload = com.atlauncher.network.Download.build()
                    .setUrl(loggingFile.url).hash(loggingFile.sha1)
                    .size(loggingFile.size).downloadTo(FileSystem.RESOURCES_LOG_CONFIGS.resolve(loggingFile.id))
                    .withHttpClient(httpClient);

                if (loggerDownload.needToDownload()) {
                    progressDialog.setTotalBytes(loggingFile.size);
                    loggerDownload.downloadFile();
                }

                progressDialog.doneTask();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                PerformanceManager.end("Downloading Logging Config");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Downloading Logging Config");
        } else {
            progressDialog.doneTask();
        }

        // download libraries
        PerformanceManager.start("Downloading Libraries");
        progressDialog.setLabel(GetText.tr("Downloading Libraries"));
        DownloadPool librariesPool = new DownloadPool();

        List<Library> librariesMissingWithNoUrl = instance.libraries.stream()
            .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                && library.downloads.artifact.url != null && library.downloads.artifact.url.isEmpty()
                && !Files.exists(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path)))
            .collect(Collectors.toList());
        if (librariesMissingWithNoUrl.size() != 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Missing Libraries Found"))
                .setContent(new HTMLBuilder().center()
                    .text(GetText.tr(
                        "This instance cannot be started due to missing libraries that cannot be downloaded.<br/><br/>Please reinstall the instance to create those libraries and be able to start this instance again."))
                    .build())
                .setType(DialogManager.ERROR).show();
            return false;
        }

        // get non native libraries otherwise we double up
        instance.libraries.stream()
            .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                && library.downloads.artifact.url != null && !library.downloads.artifact.url.isEmpty()
                && !library.hasNativeForOS())
            .distinct()
            .map(l -> LWJGLManager.shouldReplaceLWJGL3(instance)
                ? LWJGLManager.getReplacementLWJGL3Library(instance, l)
                : l)
            .forEach(library -> {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                    .setUrl(library.downloads.artifact.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                    .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                    .withHttpClient(httpClient);

                librariesPool.add(download);
            });

        instance.libraries.stream().filter(Library::hasNativeForOS)
            .map(l -> LWJGLManager.shouldReplaceLWJGL3(instance)
                ? LWJGLManager.getReplacementLWJGL3Library(instance, l)
                : l)
            .forEach(library -> {
                com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

                librariesPool.add(new com.atlauncher.network.Download().setUrl(download.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(download.path)).hash(download.sha1)
                    .size(download.size)
                    .withHttpClient(httpClient));
            });

        // legacy forge, so check the libs folder
        if (instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isForge()
            && Utils.matchVersion(instance.id, "1.5", true, true)) {
            List<FMLLibrary> fmlLibraries = FMLLibrariesConstants.fmlLibraries.get(instance.id);

            if (fmlLibraries != null) {
                fmlLibraries.forEach((library) -> {
                    com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                        .setUrl(String.format("%s/fmllibs/%s", Constants.DOWNLOAD_SERVER, library.name))
                        .downloadTo(FileSystem.LIBRARIES.resolve("fmllib/" + library.name))
                        .copyTo(instance.ROOT.resolve("lib/" + library.name)).hash(library.sha1Hash)
                        .size(library.size).withHttpClient(httpClient);

                    librariesPool.add(download);
                });
            }
        }

        DownloadPool smallLibrariesPool = librariesPool.downsize();

        progressDialog.setTotalBytes(smallLibrariesPool.totalSize());

        smallLibrariesPool.downloadAll();

        progressDialog.doneTask();
        PerformanceManager.end("Downloading Libraries");

        // download Java runtime
        PerformanceManager.start("Java Runtime");
        if (instance.javaVersion != null && Data.JAVA_RUNTIMES != null && Optional
            .ofNullable(instance.launcher.useJavaProvidedByMinecraft).orElse(App.settings.useJavaProvidedByMinecraft)) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeSystemString = JavaRuntimes.getSystem();

            String runtimeToUse = Optional.ofNullable(instance.launcher.javaRuntimeOverride).orElse(instance.javaVersion.component);

            if (runtimesForSystem.containsKey(runtimeToUse)
                && runtimesForSystem.get(runtimeToUse).size() != 0) {
                // #. {0} is the version of Java were downloading
                progressDialog.setLabel(GetText.tr("Downloading Java Runtime {0}",
                    runtimesForSystem.get(runtimeToUse).get(0).version.name));

                JavaRuntime runtimeToDownload = runtimesForSystem.get(runtimeToUse).get(0);

                try {
                    JavaRuntimeManifest javaRuntimeManifest = com.atlauncher.network.Download.build()
                        .setUrl(runtimeToDownload.manifest.url).size(runtimeToDownload.manifest.size)
                        .hash(runtimeToDownload.manifest.sha1).downloadTo(FileSystem.MINECRAFT_RUNTIMES
                            .resolve(runtimeToUse).resolve("manifest.json"))
                        .asClassWithThrow(JavaRuntimeManifest.class);

                    DownloadPool pool = new DownloadPool();

                    // create root directory
                    Path runtimeSystemDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                        .resolve(runtimeSystemString);
                    Path runtimeDirectory = runtimeSystemDirectory.resolve(runtimeToUse);
                    FileUtils.createDirectory(runtimeDirectory);

                    // create all the directories
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.DIRECTORY) {
                            FileUtils.createDirectory(runtimeDirectory.resolve(key));
                        }
                    });

                    // collect the files we need to download
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.FILE) {
                            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                                .setUrl(file.downloads.raw.url).downloadTo(runtimeDirectory.resolve(key))
                                .hash(file.downloads.raw.sha1).size(file.downloads.raw.size)
                                .executable(file.executable).withHttpClient(httpClient);

                            pool.add(download);
                        }
                    });

                    DownloadPool smallPool = pool.downsize();

                    progressDialog.setTotalBytes(smallPool.totalSize());

                    smallPool.downloadAll();

                    // write out the version file (theres also a .sha1 file created, but we're not
                    // doing that)
                    Files.write(runtimeSystemDirectory.resolve(".version"),
                        runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                    // Files.write(runtimeSystemDirectory.resolve(runtimeToUse
                    // + ".sha1"), runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to download Java runtime", e);
                }
            }
        }
        progressDialog.doneTask();
        PerformanceManager.end("Java Runtime");

        // organise assets
        PerformanceManager.start("Organising Resources 1");
        progressDialog.setLabel(GetText.tr("Organising Resources"));
        MojangAssetIndex assetIndex = instance.assetIndex;

        AssetIndex index = com.atlauncher.network.Download.build().setUrl(assetIndex.url).hash(assetIndex.sha1)
            .size(assetIndex.size).downloadTo(FileSystem.RESOURCES_INDEXES.resolve(assetIndex.id + ".json"))
            .withHttpClient(httpClient).asClass(AssetIndex.class);

        DownloadPool pool = new DownloadPool();

        index.objects.forEach((key, object) -> {
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);

            com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(url)
                .downloadTo(FileSystem.RESOURCES_OBJECTS.resolve(filename)).hash(object.hash).size(object.size)
                .withHttpClient(httpClient);

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        if (smallPool.size() != 0) {
            progressDialog.setLabel(GetText.tr("Downloading Resources"));

            progressDialog.setTotalBytes(smallPool.totalSize());

            smallPool.downloadAll();
        }
        PerformanceManager.end("Organising Resources 1");

        // copy resources to instance
        if (index.mapToResources || assetIndex.id.equalsIgnoreCase("legacy")) {
            PerformanceManager.start("Organising Resources 2");
            progressDialog.setLabel(GetText.tr("Organising Resources"));

            index.objects.forEach((key, object) -> {
                String filename = object.hash.substring(0, 2) + "/" + object.hash;

                Path downloadedFile = FileSystem.RESOURCES_OBJECTS.resolve(filename);
                Path assetPath = index.mapToResources ? instance.ROOT.resolve("resources/" + key)
                    : FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(key);

                if (!Files.exists(assetPath)) {
                    FileUtils.copyFile(downloadedFile, assetPath, true);
                }
            });
            PerformanceManager.end("Organising Resources 2");
        }

        progressDialog.doneTask();

        progressDialog.setLabel(GetText.tr("Organising Libraries"));

        // extract natives to a temp dir
        PerformanceManager.start("Extracting Natives");
        boolean useSystemGlfw = Optional.ofNullable(instance.launcher.useSystemGlfw).orElse(App.settings.useSystemGlfw);
        boolean useSystemOpenAl = Optional.ofNullable(instance.launcher.useSystemOpenAl).orElse(App.settings.useSystemOpenAl);
        instance.libraries.stream().filter(Library::shouldInstall)
            .map(l -> LWJGLManager.shouldReplaceLWJGL3(instance)
                ? LWJGLManager.getReplacementLWJGL3Library(instance, l)
                : l)
            .forEach(library -> {
                if (library.hasNativeForOS()) {
                    if (library.name.contains("glfw") && useSystemGlfw) {
                        LogManager.warn("useSystemGlfw was enabled, not using glfw natives from Minecraft");
                        return;
                    }

                    if (library.name.contains("openal") && useSystemOpenAl) {
                        LogManager.warn("useSystemOpenAl was enabled, not using openal natives from Minecraft");
                        return;
                    }

                    Path nativePath = FileSystem.LIBRARIES.resolve(library.getNativeDownloadForOS().path);

                    ArchiveUtils.extract(nativePath, nativesTempDir, name -> {
                        if (library.extract != null && library.extract.shouldExclude(name)) {
                            return null;
                        }

                        // keep META-INF folder as per normal
                        if (name.startsWith("META-INF")) {
                            return name;
                        }

                        // don't extract folders
                        if (name.endsWith("/")) {
                            return null;
                        }

                        // if it has a / then extract just to root
                        if (name.contains("/")) {
                            return name.substring(name.lastIndexOf("/") + 1);
                        }

                        return name;
                    });
                }
            });

        progressDialog.doneTask();
        PerformanceManager.end("Extracting Natives");

        if (LWJGLManager.shouldUseLegacyLWJGL(instance)) {
            PerformanceManager.start("Extracting Legacy LWJGL");
            progressDialog.setLabel(GetText.tr("Extracting Legacy LWJGL"));

            LWJGLLibrary library = LWJGLManager.getLegacyLWJGLLibrary();

            if (library != null) {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(library.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(library.path)).unzipTo(lwjglNativesTempDir)
                    .hash(library.sha1).size(library.size).withHttpClient(httpClient);

                if (download.needToDownload()) {
                    progressDialog.setTotalBytes(library.size);

                    try {
                        download.downloadFile();
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }
                } else {
                    download.runPostProcessors();
                }
            }

            progressDialog.doneTask();
            PerformanceManager.end("Extracting Legacy LWJGL");
        }

        if (instance.usesCustomMinecraftJar()) {
            PerformanceManager.start("Creating custom minecraft.jar");
            progressDialog.setLabel(GetText.tr("Creating custom minecraft.jar"));

            if (Files.exists(instance.getCustomMinecraftJarLibraryPath())) {
                FileUtils.delete(instance.getCustomMinecraftJarLibraryPath());
            }

            if (!Utils.combineJars(instance.getMinecraftJar(), instance.getRoot().resolve("bin/modpack.jar").toFile(),
                instance.getCustomMinecraftJar())) {
                LogManager.error("Failed to combine jars into custom minecraft.jar");
                PerformanceManager.end("Creating custom minecraft.jar");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Creating custom minecraft.jar");
        }
        progressDialog.doneTask();

        if (App.settings.scanModsOnLaunch) {
            PerformanceManager.start("Scanning mods for Fractureiser");
            progressDialog.setLabel(GetText.tr("Scanning mods for Fractureiser"));

            List<Path> foundInfections = new ArrayList<>();
            try {
                foundInfections = SecurityUtils.scanForFractureiser(instance.getModPathsFromFilesystem());
            } catch (InterruptedException e) {
                LogManager.logStackTrace("Failed to scan all mods for Fractureiser", e);
            }
            PerformanceManager.end("Scanning mods for Fractureiser");

            if (foundInfections.size() != 0) {
                LogManager.error("Infections have been found in your mods. See the below list of paths");
                foundInfections.forEach(p -> LogManager.error(p.toAbsolutePath().toString()));
                return false;
            }
        }
        progressDialog.doneTask();

        PerformanceManager.end();
        return true;
    }

    public static boolean launch(Instance instance, boolean offline) {
        final AbstractAccount account = instance.launcher.account == null ? AccountManager.getSelectedAccount()
            : AccountManager.getAccountByName(instance.launcher.account);

        if (account == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                .setContent(new HTMLBuilder().center()
                    .text(GetText.tr("Cannot play instance as you have no account selected.")).build())
                .setType(DialogManager.ERROR).show();

            if (AccountManager.getAccounts().size() == 0) {
                App.navigate(UIConstants.LAUNCHER_ACCOUNTS_TAB);
            }

            App.launcher.setMinecraftLaunched(false);
            return false;
        }

        // if Microsoft account must login again, then make sure to do that
        if (!offline && account instanceof MicrosoftAccount && ((MicrosoftAccount) account).mustLogin) {
            if (!((MicrosoftAccount) account).ensureAccountIsLoggedIn()) {
                LogManager.info("You must login to your account before continuing.");
                return false;
            }
        }

        String playerName = account.minecraftUsername;

        if (offline) {
            playerName = DialogManager.okDialog().setTitle(GetText.tr("Offline Player Name"))
                .setContent(GetText.tr("Choose your offline player name:")).showInput(playerName);

            if (playerName == null || playerName.isEmpty()) {
                LogManager.info("No player name provided for offline launch, so cancelling launch.");
                return false;
            }
        }

        final String username = offline ? playerName : account.minecraftUsername;

        int maximumMemory = (instance.launcher.maximumMemory == null) ? App.settings.maximumMemory
            : instance.launcher.maximumMemory;
        if ((maximumMemory < instance.launcher.requiredMemory)
            && (instance.launcher.requiredMemory <= OS.getSafeMaximumRam())) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficient Ram"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                    "This pack has set a minimum amount of ram needed to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                    instance.launcher.requiredMemory)).build())
                .setType(DialogManager.ERROR).show();

            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }
        int permGen = (instance.launcher.permGen == null) ? App.settings.metaspace : instance.launcher.permGen;
        if (permGen < instance.launcher.requiredPermGen) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficent Permgen"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                    "This pack has set a minimum amount of permgen to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                    instance.launcher.requiredPermGen)).build())
                .setType(DialogManager.ERROR).show();
            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling permgen warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }

        Path nativesTempDir = FileSystem.TEMP.resolve("natives-" + UUID.randomUUID().toString().replace("-", ""));
        Path lwjglNativesTempDir = FileSystem.TEMP
            .resolve("lwjgl-natives-" + UUID.randomUUID().toString().replace("-", ""));

        try {
            Files.createDirectory(nativesTempDir);
        } catch (IOException e2) {
            LogManager.logStackTrace(e2, false);
        }

        if (LWJGLManager.shouldUseLegacyLWJGL(instance)) {
            try {
                Files.createDirectory(lwjglNativesTempDir);
            } catch (IOException e2) {
                LogManager.logStackTrace(e2, false);
            }
        }

        Analytics.trackEvent(AnalyticsEvent.forStartInstanceLaunch(instance, offline));

        ProgressDialog<Boolean> prepareDialog = new ProgressDialog<>(GetText.tr("Preparing For Launch"),
            9,
            GetText.tr("Preparing For Launch"));
        prepareDialog.addThread(new Thread(() -> {
            LogManager.info("Preparing for launch!");
            prepareDialog.setReturnValue(prepareForLaunch(instance, prepareDialog, nativesTempDir, lwjglNativesTempDir));
            prepareDialog.close();
        }));
        prepareDialog.start();

        if (prepareDialog.getReturnValue() == null || !prepareDialog.getReturnValue()) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "prepare_failure"));
            LogManager.error(
                "Failed to prepare instance " + instance.launcher.name + " for launch. Check the logs and try again.");
            return false;
        }

        Thread launcher = new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(false);
                }

                LogManager.info(String.format("Launching pack %s %s (%s) for Minecraft %s", instance.launcher.pack,
                    instance.launcher.version, instance.getPlatformName(), instance.id));

                Process process = null;

                boolean enableCommands = Optional.ofNullable(instance.launcher.enableCommands)
                    .orElse(App.settings.enableCommands);
                String preLaunchCommand = Optional.ofNullable(instance.launcher.preLaunchCommand)
                    .orElse(App.settings.preLaunchCommand);
                String postExitCommand = Optional.ofNullable(instance.launcher.postExitCommand)
                    .orElse(App.settings.postExitCommand);
                String wrapperCommand = Optional.ofNullable(instance.launcher.wrapperCommand)
                    .orElse(App.settings.wrapperCommand);
                if (!enableCommands) {
                    wrapperCommand = null;
                }

                if (account instanceof MojangAccount) {
                    MojangAccount mojangAccount = (MojangAccount) account;
                    LoginResponse session;

                    if (offline) {
                        session = new LoginResponse(mojangAccount.username);
                        session.setOffline();
                    } else {
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<LoginResponse> loginDialog = new ProgressDialog<>(
                            GetText.tr("Logging Into Minecraft"), 0, GetText.tr("Logging Into Minecraft"),
                            "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(mojangAccount.login());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        session = loginDialog.getReturnValue();

                        if (session == null) {
                            Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "mojang_no_session"));
                            App.launcher.setMinecraftLaunched(false);
                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }
                            return;
                        }
                    }

                    if (enableCommands && preLaunchCommand != null) {
                        if (!executeCommand(instance, preLaunchCommand)) {
                            LogManager.error("Failed to execute pre-launch command");

                            Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "pre_launch_failure"));
                            App.launcher.setMinecraftLaunched(false);

                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }

                            return;
                        }
                    }

                    process = MCLauncher.launch(mojangAccount, instance, session, nativesTempDir,
                        LWJGLManager.shouldUseLegacyLWJGL(instance) ? lwjglNativesTempDir : null,
                        wrapperCommand, username);
                } else if (account instanceof MicrosoftAccount) {
                    MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;

                    if (!offline) {
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<Boolean> loginDialog = new ProgressDialog<>(GetText.tr("Logging Into Minecraft"),
                            0, GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(microsoftAccount.ensureAccessTokenValid());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        if (!(Boolean) loginDialog.getReturnValue()) {
                            LogManager.error("Failed to login");
                            Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "microsoft_login_failure"));
                            App.launcher.setMinecraftLaunched(false);
                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }
                            DialogManager.okDialog().setTitle(GetText.tr("Error Logging In"))
                                .setContent(GetText.tr("Couldn't login with Microsoft account"))
                                .setType(DialogManager.ERROR).show();
                            return;
                        }
                    }

                    if (enableCommands && preLaunchCommand != null) {
                        if (!executeCommand(instance, preLaunchCommand)) {
                            LogManager.error("Failed to execute pre-launch command");

                            Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "pre_launch_failure"));
                            App.launcher.setMinecraftLaunched(false);

                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }

                            return;
                        }
                    }

                    process = MCLauncher.launch(microsoftAccount, instance, nativesTempDir,
                        LWJGLManager.shouldUseLegacyLWJGL(instance) ? lwjglNativesTempDir : null,
                        wrapperCommand, username);
                }

                if (process == null) {
                    Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "no_process"));
                    LogManager.error("Failed to get process for Minecraft");
                    App.launcher.setMinecraftLaunched(false);
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(true);
                    }
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunched(instance, offline));

                if (instance.getPack() != null && instance.getPack().isLoggingEnabled() && !instance.launcher.isDev
                    && App.settings.enableLogs) {
                    App.TASKPOOL.execute(() -> {
                        instance.addPlay(instance.launcher.version);
                    });
                }

                if ((App.autoLaunch != null && App.closeLauncher)
                    || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                    Analytics.endSession();
                    System.exit(0);
                }

                try {
                    if (!OS.isArm() && Optional.ofNullable(instance.launcher.enableDiscordIntegration)
                        .orElse(App.settings.enableDiscordIntegration)) {
                        App.ensureDiscordIsInitialized();

                        String playing = instance.launcher.pack
                            + (instance.launcher.multiMCManifest != null ? " (" + instance.launcher.version + ")" : "");

                        DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder("");
                        presence.setDetails(playing);
                        presence.setStartTimestamps(System.currentTimeMillis());

                        if (instance.getPack() != null && instance.getPack().hasDiscordImage()) {
                            presence.setBigImage(instance.getPack().getSafeName().toLowerCase(Locale.ENGLISH), playing);
                            presence.setSmallImage("atlauncher", "ATLauncher");
                        } else {
                            presence.setBigImage("atlauncher", playing);
                        }

                        DiscordRPC.discordUpdatePresence(presence.build());
                    }
                } catch (Throwable t) {
                    // ignored
                }

                App.launcher.showKillMinecraft(process);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(isr);
                String line;
                int detectedError = 0;

                String replaceUUID = account.uuid.replace("-", "");

                while ((line = br.readLine()) != null) {
                    if (line.contains("java.lang.OutOfMemoryError")
                        || line.contains("There is insufficient memory for the Java Runtime Environment")) {
                        detectedError = MinecraftError.OUT_OF_MEMORY;
                    }

                    if (line.contains("java.util.ConcurrentModificationException")
                        && Utils.matchVersion(instance.id, "1.6", true, true)) {
                        detectedError = MinecraftError.CONCURRENT_MODIFICATION_ERROR_1_6;
                    }

                    if (line.contains(
                        "has been compiled by a more recent version of the Java Runtime (class file version 60.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_16_OR_NEWER;
                    }

                    if (line.contains(
                        "has been compiled by a more recent version of the Java Runtime (class file version 61.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_17_OR_NEWER;
                    }

                    if (line.contains(
                        "class jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class")) {
                        detectedError = MinecraftError.USING_NEWER_JAVA_THAN_8;
                    }

                    if (!LogManager.showDebug) {
                        line = line.replace(account.minecraftUsername, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.username, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.uuid, "**UUID**");
                        line = line.replace(replaceUUID, "**UUID**");
                    }

                    if (account.getAccessToken() != null) {
                        line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
                    }

                    if (line.contains("log4j:")) {
                        try {
                            // start of a new event so clear string builder
                            if (line.contains("<log4j:Event>")) {
                                sb.setLength(0);
                            }

                            sb.append(line);

                            // end of the xml object so parse it
                            if (line.contains("</log4j:Event>")) {
                                LogManager.minecraftLog4j(sb.toString());
                                sb.setLength(0);
                            }

                            continue;
                        } catch (Exception e) {
                            // ignored
                        }
                    }

                    LogManager.minecraft(line);
                }
                App.launcher.hideKillMinecraft();
                if (App.launcher.getParent() != null && App.settings.keepLauncherOpen) {
                    App.launcher.getParent().setVisible(true);
                }
                long end = System.currentTimeMillis();
                if (!OS.isArm() && App.discordInitialized) {
                    try {
                        DiscordRPC.discordClearPresence();
                    } catch (Throwable t) {
                        // ignored
                    }
                }
                int exitValue = 0; // Assume we exited fine
                try {
                    exitValue = process.exitValue(); // Try to get the real exit value
                } catch (IllegalThreadStateException e) {
                    process.destroy(); // Kill the process
                }
                if (!App.settings.keepLauncherOpen) {
                    App.console.setVisible(false); // Hide the console to pretend we've closed
                }

                if (exitValue != 0) {
                    LogManager.error(
                        "Oh no. Minecraft crashed. Please check the logs for any errors and provide these logs when asking for support.");

                    if (instance.getPack() != null && !instance.getPack().system) {
                        LogManager.info("Checking for modifications to the pack since installation.");
                        instance.launcher.mods.forEach(mod -> {
                            if (!mod.userAdded && mod.wasSelected && mod.disabled) {
                                LogManager.warn("The mod " + mod.name + " (" + mod.file + ") has been disabled.");
                            }
                        });

                        Files.list(
                            instance.ROOT.resolve("mods")).filter(
                                file -> Files.isRegularFile(file)
                                    && instance.launcher.mods.stream()
                                    .noneMatch(m -> m.type == Type.mods && !m.userAdded
                                        && m.getFile(instance).toPath().equals(file)))
                            .forEach(newMod -> {
                                LogManager.warn("The mod " + newMod.getFileName().toString() + " has been added.");
                            });
                    }
                }

                if (detectedError != 0) {
                    MinecraftError.showInformationPopup(detectedError);
                }

                if (enableCommands && postExitCommand != null) {
                    if (!executeCommand(instance, postExitCommand)) {
                        LogManager.error("Failed to execute post-exit command");
                    }
                }

                App.launcher.setMinecraftLaunched(false);
                final int timePlayed = (int) (end - start) / 1000;
                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchCompleted(instance, offline, timePlayed));
                if (instance.getPack() != null && instance.getPack().isLoggingEnabled() && !instance.launcher.isDev
                    && App.settings.enableLogs) {
                    if (timePlayed > 0) {
                        App.TASKPOOL.submit(() -> {
                            instance.addTimePlayed(timePlayed, instance.launcher.version);
                        });
                    }
                }
                if (App.settings.enableAutomaticBackupAfterLaunch) {
                    instance.backup();
                }
                if (App.settings.keepLauncherOpen) {
                    App.launcher.updateData();
                }
                if (Files.isDirectory(nativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(nativesTempDir);
                }
                if (Files.isDirectory(lwjglNativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(lwjglNativesTempDir);
                }
                if (instance.usesCustomMinecraftJar() && Files.exists(instance.getCustomMinecraftJarLibraryPath())) {
                    FileUtils.delete(instance.getCustomMinecraftJarLibraryPath());
                }
                if (!App.settings.keepLauncherOpen) {
                    Analytics.endSession();
                    System.exit(0);
                }
            } catch (Exception e1) {
                LogManager.logStackTrace(e1);
                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(instance, offline, "exception"));
                App.launcher.setMinecraftLaunched(false);
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(true);
                }
            }
        });

        instance.setLastPlayed(Instant.now());
        instance.incrementNumberOfPlays();
        instance.save();

        launcher.start();
        return true;
    }

    private static boolean executeCommand(Instance instance, String command) {
        try {
            CommandExecutor.executeCommand(instance, command);
            return true;
        } catch (CommandException e) {
            String content = GetText.tr("Error executing command");

            if (e.getMessage() != null) {
                content += ":" + System.lineSeparator() + e.getLocalizedMessage();
            }

            content += System.lineSeparator() + GetText.tr("Check the console for details");

            DialogManager.okDialog().setTitle(GetText.tr("Error executing command")).setContent(content)
                .setType(DialogManager.ERROR).show();

            return false;
        }
    }
}
