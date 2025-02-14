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
package com.atlauncher.data.minecraft.loaders.paper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.graphql.GetPaperLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.workers.InstanceInstaller;

import okhttp3.OkHttpClient;

public class PaperLoader implements Loader {
    protected String build;
    protected String filename;
    protected String downloadUrl;
    protected String sha256;
    protected String minecraft;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;
    protected Path downloadPath;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.build = versionOverride.version;
        } else if (metadata.containsKey("build")) {
            this.build = (String) metadata.get("build");
        }

        this.filename = (String) metadata.get("filename");
        this.downloadPath = FileSystem.LOADERS.resolve(filename);
        this.downloadUrl = (String) metadata.get("downloadUrl");
        this.sha256 = (String) metadata.get("sha256");
    }

    @Override
    public void downloadAndExtractInstaller() throws Exception {
    }

    @Override
    public void runProcessors() {
        OkHttpClient httpClient = Network.createProgressClient(instanceInstaller);

        Path serverPath = this.instanceInstaller.root.resolve(this.filename);
        Download download = Download.build().setUrl(this.downloadUrl).downloadTo(downloadPath).copyTo(serverPath)
                .withInstanceInstaller(instanceInstaller).withHttpClient(httpClient);

        if (this.sha256 != null) {
            download = download.hash(this.sha256);
        }

        if (download.needToDownload()) {
            instanceInstaller.setTotalBytes(download.getFilesize());
        }

        try {
            download.downloadFile();
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to download Paper", e);
        }
    }

    @Override
    public List<Library> getLibraries() {
        return new ArrayList<>();
    }

    @Override
    public Arguments getArguments() {
        return new Arguments(new ArrayList<>());
    }

    @Override
    public String getMainClass() {
        return "";
    }

    @Override
    public String getServerJar() {
        return this.filename;
    }

    @Override
    public Path getServerJarPath() {
        return null;
    }

    @Override
    public boolean useMinecraftLibraries() {
        return true;
    }

    @Override
    public boolean useMinecraftArguments() {
        return true;
    }

    public static List<LoaderVersion> getChoosableVersions(String minecraft) {
        GetPaperLoaderVersionsForMinecraftVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetPaperLoaderVersionsForMinecraftVersionQuery(minecraft));

        if (response == null) {
            return new ArrayList<>();
        }

        List<String> disabledVersions = ConfigManager.getConfigItem("loaders.paper.disabledVersions",
                new ArrayList<>());

        return response.loaderVersions().paper().stream()
                .filter(fv -> !disabledVersions.contains(fv.build()))
                .map(version -> new LoaderVersion(Integer.toString(version.build()), version.promoted(), "Paper"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Library> getInstallLibraries() {
        return new ArrayList<>();
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(build, false, "Paper");
    }
}
