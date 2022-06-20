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
package com.atlauncher.gui.tabs.tools;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Runtime;
import com.atlauncher.data.Runtimes;
import com.atlauncher.evnt.listener.AccountListener;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.AccountManager;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.Download;
import com.atlauncher.utils.*;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mini2Dx.gettext.GetText;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 15 / 06 / 2022
 */
public class ToolsViewModel implements IToolsViewModel, SettingsListener, AccountListener {
    private static final Logger LOG = LogManager.getLogger(ToolsViewModel.class);

    private Consumer<Boolean> onCanRunNetworkCheckerChanged;
    private Consumer<Boolean> onSkinUpdaterEnabledChanged;

    public ToolsViewModel() {
        SettingsManager.addListener(this);
        AccountManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        onCanRunNetworkCheckerChanged.accept(canRunNetworkChecker());
    }

    @Override
    public void onAccountsChanged() {
        onSkinUpdaterEnabledChanged.accept(skinUpdaterEnabled());
    }

    private boolean skinUpdaterEnabled() {
        return Data.ACCOUNTS.size() != 0;
    }

    @Override
    public boolean isDebugEnabled() {
        return LOG.isDebugEnabled();
    }

    @Override
    public boolean isLaunchInDebugEnabled() {
        return !OS.isUsingFlatpak() && !isDebugEnabled();
    }

    @Override
    public void launchInDebug() {
        Analytics.sendEvent("DebugMode", "Run", "Tool");

        OS.relaunchInDebugMode();
    }

    @Override
    public void clearDownloads() {
        Analytics.sendEvent("DownloadClearer", "Run", "Tool");

        for (File file : FileSystem.DOWNLOADS.toFile().listFiles()) {
            if (!file.equals(FileSystem.TECHNIC_DOWNLOADS.toFile())) {
                Utils.delete(file);
            }
        }

        for (File file : FileSystem.TECHNIC_DOWNLOADS.toFile().listFiles()) {
            Utils.delete(file);
        }

        for (File file : FileSystem.FAILED_DOWNLOADS.toFile().listFiles()) {
            Utils.delete(file);
        }
    }

    @Override
    public void clearLogs() {
        Analytics.sendEvent("LogClearer", "Run", "Tool");

        if (Files.exists(FileSystem.LOGS.resolve("old"))) {
            for (File file : FileSystem.LOGS.resolve("old").toFile().listFiles()) {
                Utils.delete(file);
            }
        }
    }

    private boolean canRunNetworkChecker() {
        return App.settings.enableLogs;
    }

    @Override
    public void onCanRunNetworkCheckerChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(canRunNetworkChecker());
        onCanRunNetworkCheckerChanged = onChanged;
    }

    private final String[] HOSTS = {"authserver.mojang.com", "session.minecraft.net", "libraries.minecraft.net",
        "launchermeta.mojang.com", "launcher.mojang.com", Constants.API_HOST, Constants.PASTE_HOST,
        Constants.DOWNLOAD_HOST, Constants.FABRIC_HOST, Constants.FORGE_HOST, Constants.QUILT_HOST,
        Constants.CURSEFORGE_CORE_API_HOST, Constants.MODRINTH_HOST, Constants.MODPACKS_CH_HOST};

    @Override
    public int hostsLength() {
        return HOSTS.length;
    }

    @Override
    public void runNetworkChecker(Consumer<Void> onTaskComplete, Consumer<Void> onFail, Consumer<Void> onSuccess) {
        Analytics.sendEvent("NetworkChecker", "Run", "Tool");
        StringBuilder results = new StringBuilder();

        // Connection to CDN
        results.append("Ping results to " + Constants.DOWNLOAD_HOST + " was ")
            .append(Utils.pingAddress(Constants.DOWNLOAD_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.DOWNLOAD_HOST + " was ")
            .append(Utils.traceRoute(Constants.DOWNLOAD_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to ATLauncher API
        results.append("Ping results to " + Constants.API_HOST + " was ")
            .append(Utils.pingAddress(Constants.API_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.API_HOST + " was ")
            .append(Utils.traceRoute(Constants.API_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to CurseForge Core API
        results.append("Ping results to " + Constants.CURSEFORGE_CORE_API_HOST + " was ")
            .append(Utils.pingAddress(Constants.CURSEFORGE_CORE_API_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.CURSEFORGE_CORE_API_HOST + " was ")
            .append(Utils.traceRoute(Constants.CURSEFORGE_CORE_API_HOST))
            .append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to Modrinth API
        results.append("Ping results to " + Constants.MODRINTH_HOST + " was ")
            .append(Utils.pingAddress(Constants.MODRINTH_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.MODRINTH_HOST + " was ")
            .append(Utils.traceRoute(Constants.MODRINTH_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to Modpacks.ch API
        results.append("Ping results to " + Constants.MODPACKS_CH_HOST + " was ")
            .append(Utils.pingAddress(Constants.MODPACKS_CH_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.MODPACKS_CH_HOST + " was ")
            .append(Utils.traceRoute(Constants.MODPACKS_CH_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to Fabric CDN
        results.append("Ping results to " + Constants.FABRIC_HOST + " was ")
            .append(Utils.pingAddress(Constants.FABRIC_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.FABRIC_HOST + " was ")
            .append(Utils.traceRoute(Constants.FABRIC_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to Forge CDN
        results.append("Ping results to " + Constants.FORGE_HOST + " was ")
            .append(Utils.pingAddress(Constants.FORGE_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.FORGE_HOST + " was ")
            .append(Utils.traceRoute(Constants.FORGE_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);

        // Connection to Quilt CDN
        results.append("Ping results to " + Constants.QUILT_HOST + " was ")
            .append(Utils.pingAddress(Constants.QUILT_HOST));
        onTaskComplete.accept(null);

        results.append("Tracert to " + Constants.QUILT_HOST + " was ")
            .append(Utils.traceRoute(Constants.QUILT_HOST)).append("\n\n----------------\n\n");
        onTaskComplete.accept(null);


        // Resolution of key services
        for (String host : HOSTS) {
            try {
                String resolvedHosts = Arrays.stream(InetAddress.getAllByName(host))
                    .map(InetAddress::getHostAddress).collect(Collectors.joining(", "));
                results.append("Resolution of ").append(host).append(" was ").append(resolvedHosts)
                    .append("\n\n");
            } catch (Exception e1) {
                results.append("Resolution of ").append(host).append(" failed: ").append(e1.toString())
                    .append("\n\n");
            }

            onTaskComplete.accept(null);
        }

        results.append("----------------\n\n");

        // Response Code Test
        try {
            results.append(String.format("Response code to %s was %d\n\n----------------\n\n",
                Constants.DOWNLOAD_SERVER,
                Download.build()
                    .setUrl(String.format("%s/launcher/json/files.json", Constants.DOWNLOAD_SERVER))
                    .getResponseCode()));
        } catch (Exception e1) {
            results.append(String.format("Exception thrown when connecting to %s\n\n----------------\n\n",
                Constants.DOWNLOAD_SERVER));
            results.append(e1);
        }
        onTaskComplete.accept(null);

        // Ping Pong Test
        results.append(String.format("Response to ping on %s was %s\n\n----------------\n\n",
            Constants.DOWNLOAD_SERVER,
            Download.build().setUrl(String.format("%s/ping", Constants.DOWNLOAD_SERVER)).asString()));
        onTaskComplete.accept(null);

        // Speed Test
        File file = FileSystem.TEMP.resolve("100MB.bin").toFile();
        if (file.exists()) {
            Utils.delete(file);
        }
        long started = System.currentTimeMillis();
        try {
            Download.build().setUrl(String.format("%s/100MB.bin", Constants.DOWNLOAD_SERVER))
                .downloadTo(file.toPath()).downloadFile();
        } catch (Exception e2) {
            results.append(
                String.format("Exception thrown when downloading 100MB.bin from %s\n\n----------------\n\n",
                    Constants.DOWNLOAD_SERVER));
            results.append(e2);
        }

        long timeTaken = System.currentTimeMillis() - started;
        float bps = file.length() / (timeTaken / 1000);
        float kbps = bps / 1024;
        float mbps = kbps / 1024;
        String speed = (mbps < 1
            ? (kbps < 1 ? String.format("%.2f B/s", bps) : String.format("%.2f " + "KB/s", kbps))
            : String.format("%.2f MB/s", mbps));
        results.append(
            String.format("Download speed to %s was %s, " + "" + "taking %.2f seconds to download 100MB",
                Constants.DOWNLOAD_SERVER, speed, (timeTaken / 1000.0)));
        onTaskComplete.accept(null);

        String result = Utils.uploadPaste(Constants.LAUNCHER_NAME + " Network Test Log", results.toString());
        if (result.contains(Constants.PASTE_CHECK_URL)) {
            LOG.info("Network Test has finished running, you can view the results at {}", result);
            onTaskComplete.accept(null);
            onSuccess.accept(null);
        } else {
            LOG.error("Network Test failed to submit to {}!", Constants.LAUNCHER_NAME);
            onTaskComplete.accept(null);
            onFail.accept(null);
        }
    }

    private Consumer<Boolean> _onCanDownloadRuntimeChanged;
    private Consumer<Boolean> _onCanRemoveDownloadChanged;
    private boolean canDownloadRuntime(){
        return !OS.isLinux();
    }

    private boolean canRemoveDownload(){
        return !OS.isLinux() && Java.hasInstalledRuntime();
    }

    @Override
    public void onCanDownloadRuntimeChanged(Consumer<Boolean> onChanged) {
        _onCanDownloadRuntimeChanged = onChanged;
        onChanged.accept(canDownloadRuntime());
    }

    @Override
    public void onCanRemoveDownloadChanged(Consumer<Boolean> onChanged) {
        _onCanRemoveDownloadChanged = onChanged;
        onChanged.accept(canRemoveDownload());
    }

    private void updateDownloadButtons(){
        _onCanDownloadRuntimeChanged.accept(canDownloadRuntime());
        _onCanRemoveDownloadChanged.accept(canRemoveDownload());
    }

    @Override
    public void removeRuntime(Consumer<Void> onFail, Consumer<Void> onSuccess) {
        Analytics.sendEvent("RuntimeDownloader", "Remove", "Tool");

        String oldPath = App.settings.javaPath;

        if (FileUtils.deleteDirectory(FileSystem.RUNTIMES)) {

            // switch back to use default
            App.settings.javaPath = OS.getDefaultJavaPath();
            App.settings.save();

            // remove the path from any custom paths set for instances
            InstanceManager.getInstances().stream()
                .filter(i -> i.launcher.javaPath != null && i.launcher.javaPath.contains(oldPath)).forEach(i -> {
                    i.launcher.javaPath = null;
                    i.save();
                });

            onSuccess.accept(null);
        } else {
            LOG.error("Runtime removal failed!");
            onFail.accept(null);
        }
        updateDownloadButtons();
    }

    private void downloadRuntimePost(String path) {
        App.settings.javaPath = path;
        App.settings.save();
    }

    @Override
    public boolean downloadRuntime(
        NetworkProgressable progressbar,
        Consumer<Void> onTaskComplete,
        Consumer<String> newLabel,
        Consumer<Void> clearDownloadedBytes
    ) {
        Analytics.sendEvent("RuntimeDownloader", "Run", "Tool");

        Runtimes runtimes = Download.build().cached()
            .setUrl(String.format("%s/launcher/json/runtimes.json", Constants.DOWNLOAD_SERVER))
            .asClass(Runtimes.class);
        onTaskComplete.accept(null);

        Runtime runtime = runtimes.getRuntimeForOS();

        if (runtime != null) {
            File runtimeFolder = FileSystem.RUNTIMES.resolve(runtime.version).toFile();
            File releaseFile = new File(runtimeFolder, "release");

            // no need to download/extract
            if (releaseFile.exists()) {
                downloadRuntimePost(runtimeFolder.getAbsolutePath());
                LOG.info("Runtime downloaded!");
                updateDownloadButtons();
                return true;
            }

            if (!runtimeFolder.exists()) {
                runtimeFolder.mkdirs();
            }

            String url = String.format("%s/%s", Constants.DOWNLOAD_SERVER, runtime.url);
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File downloadFile = new File(runtimeFolder, fileName);
            File unpackedFile = new File(runtimeFolder, fileName.replace(".xz", ""));

            OkHttpClient httpClient = Network.createProgressClient(progressbar);

            com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(url)
                .hash(runtime.sha1).size(runtime.size).withHttpClient(httpClient)
                .downloadTo(downloadFile.toPath());

            if (download.needToDownload()) {
                newLabel.accept(GetText.tr("Downloading"));
                progressbar.setTotalBytes(runtime.size);

                try {
                    download.downloadFile();
                } catch (IOException e1) {
                    LOG.error("error", e1);
                    LOG.error("Runtime downloaded failed to run!");
                    updateDownloadButtons();
                    return false;
                }

                clearDownloadedBytes.accept(null);
            }

            onTaskComplete.accept(null);

            newLabel.accept(GetText.tr("Extracting"));

            try {
                Utils.unXZFile(downloadFile, unpackedFile);
            } catch (IOException e2) {
                LOG.error("error", e2);
                LOG.error("Runtime downloaded failed to run!");
                updateDownloadButtons();
                return false;
            }

            ArchiveUtils.extract(unpackedFile.toPath(), runtimeFolder.toPath());
            Utils.delete(unpackedFile);

            downloadRuntimePost(runtimeFolder.getAbsolutePath());
            LOG.info("Runtime downloaded!");
            updateDownloadButtons();
            return true;
        }

        LOG.error("Runtime downloaded failed to run!");
        updateDownloadButtons();
        return false;
    }

    @Override
    public void onSkinUpdaterEnabledChanged(Consumer<Boolean> onChanged) {
        this.onSkinUpdaterEnabledChanged = onChanged;
        onChanged.accept(skinUpdaterEnabled());
    }

    @Override
    public int accountCount() {
        return Data.ACCOUNTS.size();
    }

    @Override
    public void updateSkins(Consumer<Void> onTaskComplete) {
        Analytics.sendEvent("SkinUpdater", "Run", "Tool");

        Data.ACCOUNTS.forEach(account -> {
            account.updateSkin();
            onTaskComplete.accept(null);
        });
    }
}
