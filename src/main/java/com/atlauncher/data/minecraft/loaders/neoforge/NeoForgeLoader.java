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
package com.atlauncher.data.minecraft.loaders.neoforge;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.graphql.GetNeoForgeLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import okhttp3.OkHttpClient;

public class NeoForgeLoader implements Loader {
    protected String installerUrl;
    protected String version;
    protected String rawVersion;
    protected Long installerSize;
    protected String installerSha1;
    protected String minecraft;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;
    protected Path installerPath;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.version = versionOverride.version;
            this.rawVersion = versionOverride.rawVersion;
        } else if (metadata.containsKey("version")) {
            this.version = (String) metadata.get("version");
            this.rawVersion = this.minecraft + "-" + this.version;

            if (metadata.containsKey("rawVersion")) {
                this.rawVersion = (String) metadata.get("rawVersion");
            }
        }

        boolean is1201Version = this.minecraft.equals("1.20.1");
        String artifactName = is1201Version ? "forge" : "neoforge";
        String versionName = is1201Version ? this.minecraft + "-" + this.version : this.rawVersion;

        this.installerPath = FileSystem.LOADERS.resolve(artifactName + "-" + versionName + "-installer.jar");
        this.installerUrl = Constants.NEOFORGE_MAVEN + "/net/neoforged/" + artifactName + "/" + versionName + "/"
                + artifactName + "-" + versionName + "-installer.jar";
    }

    @Override
    public void downloadAndExtractInstaller() throws Exception {
        getInstallerMetadata();

        OkHttpClient httpClient = Network.createProgressClient(instanceInstaller);

        Download download = Download.build().setUrl(this.installerUrl).downloadTo(installerPath)
                .withInstanceInstaller(instanceInstaller).withHttpClient(httpClient).unzipTo(this.tempDir.toPath());

        if (installerSize != null) {
            download = download.size(this.installerSize);
        }

        if (installerSha1 != null) {
            download = download.hash(this.installerSha1);

            if (ConfigManager.getConfigItem("loaders.neoforge.disableInstallerHashChecking", false)) {
                download = download.ignoreFailures();
            }
        }

        if (download.needToDownload()) {
            if (installerSize != null) {
                instanceInstaller.setTotalBytes(installerSize);
            } else {
                instanceInstaller.setTotalBytes(download.getFilesize());
            }
        }

        download.downloadFile();

        this.copyLocalLibraries();
    }

    private void getInstallerMetadata() {
        if (installerSha1 == null) {
            installerSha1 = Download.build().setUrl(this.installerUrl + ".sha1").asString();
        }
    }

    public Version getVersion() {
        Version version = null;

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(new File(this.tempDir, "version.json").toPath()), StandardCharsets.UTF_8)) {
            version = Gsons.DEFAULT.fromJson(fileReader, Version.class);
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }

        return version;
    }

    public void copyLocalLibraries() {
        Version version = getVersion();
        NeoForgeInstallProfile installProfile = getInstallProfile();
        version.libraries.forEach(library -> {
            // copy over any local files from the loader zip file
            if (library.name.equalsIgnoreCase(installProfile.path)) {
                FileUtils.copyFile(new File(tempDir, "maven/" + library.downloads.artifact.path).toPath(),
                        FileSystem.LIBRARIES.resolve(library.downloads.artifact.path), true);

                FileUtils.copyFile(
                        new File(tempDir,
                                "maven/" + library.downloads.artifact.path.substring(0,
                                        library.downloads.artifact.path.lastIndexOf(".jar")) + "-universal.jar")
                                .toPath(),
                        FileSystem.LIBRARIES.resolve(library.downloads.artifact.path.substring(0,
                                library.downloads.artifact.path.lastIndexOf(".jar")) + "-universal.jar"),
                        true);
            }
        });
    }

    public NeoForgeInstallProfile getInstallProfile() {
        NeoForgeInstallProfile installProfile = null;

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(new File(this.tempDir, "install_profile.json").toPath()), StandardCharsets.UTF_8)) {
            installProfile = Gsons.DEFAULT.fromJson(fileReader, NeoForgeInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        installProfile.data.put("SIDE", new Data("client", "server"));
        installProfile.data.put("ROOT", new Data(instanceInstaller.root.toAbsolutePath().toString()));
        installProfile.data.put("MINECRAFT_JAR",
                new Data(instanceInstaller.getMinecraftJarLibrary("client").getAbsolutePath(),
                        instanceInstaller.getMinecraftJarLibrary("server").getAbsolutePath()));
        installProfile.data.put("MINECRAFT_VERSION",
                new Data(FileSystem.MINECRAFT_VERSIONS_JSON
                        .resolve(instanceInstaller.minecraftVersionManifest.id + ".json").toAbsolutePath().toString()));
        installProfile.data.put("INSTALLER", new Data(installerPath.toAbsolutePath().toString()));
        installProfile.data.put("LIBRARY_DIR", new Data(FileSystem.LIBRARIES.toAbsolutePath().toString()));

        return installProfile;
    }

    public NeoForgeInstallProfile getVersionInfo() {
        NeoForgeInstallProfile versionInfo = null;

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(new File(this.tempDir, "version.json").toPath()), StandardCharsets.UTF_8)) {
            versionInfo = Gsons.DEFAULT.fromJson(fileReader, NeoForgeInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return versionInfo;
    }

    @Override
    public void runProcessors() {
        NeoForgeInstallProfile installProfile = this.getInstallProfile();

        installProfile.processors.forEach(processor -> {
            if (!instanceInstaller.isCancelled()) {
                try {
                    processor.process(installProfile, this.tempDir, instanceInstaller);
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                    LogManager.error("Failed to process processor with jar " + processor.getJar());
                    instanceInstaller.cancel(true);
                }
            }
        });
    }

    @Override
    public List<Library> getLibraries() {
        return new ArrayList<>(this.getVersion().libraries);
    }

    @Override
    public Arguments getArguments() {
        List<ArgumentRule> jvmArgs = this.getVersion().arguments.jvm.stream().map(arg -> {
            // we replace this as we prefix the MC jar with `client-` but NeoForge expects
            // `${MC_VERSION}.jar`
            if (arg.getValueAsString().startsWith("-DignoreList=")) {
                return new ArgumentRule(
                        arg.getValueAsString().replace("${version_name}.jar", "client-${version_name}.jar"));
            }

            return arg;
        }).collect(Collectors.toList());

        return new Arguments(this.getVersion().arguments.game, jvmArgs);
    }

    @Override
    public String getMainClass() {
        return this.getVersion().mainClass;
    }

    @Override
    public String getServerJar() {
        return null;
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
        GetNeoForgeLoaderVersionsForMinecraftVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetNeoForgeLoaderVersionsForMinecraftVersionQuery(minecraft));

        if (response == null) {
            return new ArrayList<>();
        }

        List<String> disabledVersions = ConfigManager.getConfigItem("loaders.neoforge.disabledVersions",
            new ArrayList<>());

        return response.loaderVersions().neoforge().stream().filter(fv -> !disabledVersions.contains(
                fv.version()))
                .map(version -> {
                    LoaderVersion lv = new LoaderVersion(version.version(), version.rawVersion(),
                            version.recommended(),
                            "NeoForge");

                    return lv;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Library> getInstallLibraries() {
        return new ArrayList<>(this.getInstallProfile().getLibraries());
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(version, rawVersion, false, "NeoForge");
    }
}
