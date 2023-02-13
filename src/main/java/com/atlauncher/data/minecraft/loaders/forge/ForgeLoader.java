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
package com.atlauncher.data.minecraft.loaders.forge;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.graphql.GetForgeLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Pair;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;

public class ForgeLoader implements Loader {
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

            Pair<String, Long> installerDownloadable = versionOverride.downloadables.get("installer");
            if (installerDownloadable != null) {
                if (installerDownloadable.right() != null) {
                    this.installerSize = installerDownloadable.right();
                }

                if (installerDownloadable.left() != null) {
                    this.installerSha1 = installerDownloadable.left();
                }
            }
        } else if (metadata.containsKey("version")) {
            this.version = (String) metadata.get("version");
            this.rawVersion = this.minecraft + "-" + this.version;

            if (metadata.containsKey("rawVersion")) {
                this.rawVersion = (String) metadata.get("rawVersion");
            }
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Forge version");
            this.version = this.getLatestVersion();
        } else if ((boolean) metadata.get("recommended")) {
            LogManager.debug("Downloading recommended Forge version");
            this.version = getRecommendedVersion(this.minecraft);
        }

        this.installerPath = FileSystem.LOADERS
                .resolve("forge-" + this.minecraft + "-" + this.version + "-installer.jar");
        this.installerUrl = Constants.DOWNLOAD_SERVER + "/maven/net/minecraftforge/forge/" + this.minecraft + "-"
                + this.version + "/forge-" + this.minecraft + "-" + this.version + "-installer.jar";

        if (metadata.containsKey("installerSize")) {
            Object value = metadata.get("installerSize");

            if (value instanceof Double) {
                this.installerSize = ((Double) metadata.get("installerSize")).longValue();
            } else if (value instanceof Long) {
                this.installerSize = (Long) metadata.get("installerSize");
            }
        }

        if (metadata.containsKey("installerSha1")) {
            this.installerSha1 = (String) metadata.get("installerSha1");
        }
    }

    public static ForgePromotions getPromotions() {
        return Download.build().cached().setUrl(Constants.FORGE_PROMOTIONS_FILE).asClass(ForgePromotions.class);
    }

    public String getLatestVersion() {
        return ForgeLoader.getPromotion(ForgePromotionType.LATEST, this.minecraft);
    }

    public static String getLatestVersion(String minecraft) {
        ForgePromotions promotions = getPromotions();

        if (promotions == null || !promotions.hasPromo(minecraft + "-latest")) {
            return null;
        }

        return promotions.getPromo(minecraft + "-latest");
    }

    public static String getRecommendedVersion(String minecraft) {
        ForgePromotions promotions = getPromotions();

        if (promotions == null || !promotions.hasPromo(minecraft + "-recommended")) {
            return null;
        }

        return promotions.getPromo(minecraft + "-recommended");
    }

    public static String getPromotion(ForgePromotionType promotionType, String minecraft) {
        if (promotionType == ForgePromotionType.LATEST) {
            return getLatestVersion(minecraft);
        }

        return getRecommendedVersion(minecraft);
    }

    @Override
    public void downloadAndExtractInstaller() throws Exception {
        OkHttpClient httpClient = Network.createProgressClient(instanceInstaller);

        Download download = Download.build().setUrl(this.installerUrl).downloadTo(installerPath)
                .withInstanceInstaller(instanceInstaller).withHttpClient(httpClient).unzipTo(this.tempDir.toPath());

        if (installerSize != null) {
            download = download.size(this.installerSize);
        }

        if (installerSha1 != null) {
            download = download.hash(this.installerSha1);
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

    public void copyLocalLibraries() {
        ForgeInstallProfile installProfile = getInstallProfile();

        if (installProfile.spec != null) {
            getLibraries().forEach(library -> {
                // copy over any local files from the loader zip file
                if (library.name.equalsIgnoreCase(installProfile.path)) {
                    FileUtils.copyFile(new File(tempDir, "maven/" + library.downloads.artifact.path).toPath(),
                            FileSystem.LIBRARIES.resolve(library.downloads.artifact.path), true);
                }
            });
        } else {
            this.getLibraries().forEach(library -> {
                // copy over any local files from the loader zip file
                if (installProfile.install != null && installProfile.install.filePath != null
                        && library.name.equalsIgnoreCase(installProfile.install.path)) {
                    FileUtils.copyFile(new File(tempDir, installProfile.install.filePath).toPath(),
                            FileSystem.LIBRARIES.resolve(library.downloads.artifact.path), true);
                }
            });
        }
    }

    public ForgeInstallProfile getInstallProfile() {
        ForgeInstallProfile installProfile = null;

        try {
            installProfile = Gsons.MINECRAFT.fromJson(new FileReader(new File(this.tempDir, "install_profile.json")),
                    ForgeInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return installProfile;
    }

    public ForgeInstallProfile getVersionInfo() {
        if (this.getInstallProfile().versionInfo != null) {
            return this.getInstallProfile().versionInfo;
        }

        ForgeInstallProfile versionInfo = null;

        try {
            versionInfo = Gsons.MINECRAFT.fromJson(new FileReader(new File(this.tempDir, "version.json")),
                    ForgeInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return versionInfo;
    }

    @Override
    public void runProcessors() {

    }

    @Override
    public List<Library> getLibraries() {
        ForgeInstallProfile installProfile = this.getInstallProfile();

        List<ForgeLibrary> libraries;

        if (installProfile.spec != null) {
            libraries = this.getVersionInfo().getLibraries();
        } else {
            libraries = installProfile.getLibraries();
        }

        return new ArrayList<>(libraries);
    }

    @Override
    public Arguments getArguments() {
        return new Arguments(Arrays.stream(this.getVersionInfo().minecraftArguments.split(" "))
                .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList()));
    }

    @Override
    public String getMainClass() {
        return this.getVersionInfo().mainClass;
    }

    @Override
    public String getServerJar() {
        if (this.getInstallProfile().install != null) {
            return this.getInstallProfile().install.filePath;
        }

        Library library = this.getLibraries().stream()
                .filter(lib -> lib.name.equalsIgnoreCase(this.getInstallProfile().path)).findFirst()
                .orElseGet(() -> this.getLibraries().get(this.getLibraries().size() - 1));

        return library.downloads.artifact.path.substring(library.downloads.artifact.path.lastIndexOf('/') + 1);
    }

    @Override
    public boolean useMinecraftLibraries() {
        return !this.instanceInstaller.isServer;
    }

    @Override
    public boolean useMinecraftArguments() {
        return false;
    }

    public static List<LoaderVersion> getChoosableVersions(String minecraft) {
        java.lang.reflect.Type type = new TypeToken<APIResponse<List<ATLauncherApiForgeVersion>>>() {
        }.getType();

        try {
            if (ConfigManager.getConfigItem("useGraphql.loaderVersions", false) == true) {
                GetForgeLoaderVersionsForMinecraftVersionQuery.Data response = GraphqlClient
                        .callAndWait(new GetForgeLoaderVersionsForMinecraftVersionQuery(minecraft));

                if (response == null) {
                    return new ArrayList<LoaderVersion>();
                }

                List<String> disabledVersions = ConfigManager.getConfigItem("loaders.forge.disabledVersions",
                        new ArrayList<String>());

                return response.loaderVersions().forge().stream().filter(fv -> !disabledVersions.contains(
                        fv.version()))
                        .map(version -> {
                            LoaderVersion lv = new LoaderVersion(version.version(), version.rawVersion(),
                                    version.recommended(),
                                    "Forge");

                            if (version.installerSha1Hash() != null && version.installerSize() != null) {
                                lv.downloadables.put("installer",
                                        new Pair<String, Long>(version.installerSha1Hash(), version.installerSize()
                                                .longValue()));
                            }

                            if (version.universalSha1Hash() != null && version.universalSize() != null) {
                                lv.downloadables.put("universal",
                                        new Pair<String, Long>(version.universalSha1Hash(), version.universalSize()
                                                .longValue()));
                            }

                            if (version.clientSha1Hash() != null && version.clientSize() != null) {
                                lv.downloadables.put("client",
                                        new Pair<String, Long>(version.clientSha1Hash(), version.clientSize()
                                                .longValue()));
                            }

                            if (version.serverSha1Hash() != null && version.serverSize() != null) {
                                lv.downloadables.put("server",
                                        new Pair<String, Long>(version.serverSha1Hash(), version.serverSize()
                                                .longValue()));
                            }

                            return lv;
                        })
                        .collect(Collectors.toList());
            } else {
                APIResponse<List<ATLauncherApiForgeVersion>> data = Download.build()
                        .setUrl(String.format("%sforge-versions/%s", Constants.API_BASE_URL, minecraft))
                        .asTypeWithThrow(type);

                List<String> disabledVersions = ConfigManager.getConfigItem("loaders.forge.disabledVersions",
                        new ArrayList<String>());

                return data.getData().stream().filter(fv -> !disabledVersions.contains(fv.version))
                        .map(version -> {
                            LoaderVersion lv = new LoaderVersion(version.version, version.rawVersion,
                                    version.recommended,
                                    "Forge");

                            if (version.installerSha1Hash != null && version.installerSize != null) {
                                lv.downloadables.put("installer",
                                        new Pair<String, Long>(version.installerSha1Hash, version.installerSize));
                            }

                            if (version.universalSha1Hash != null && version.universalSize != null) {
                                lv.downloadables.put("universal",
                                        new Pair<String, Long>(version.universalSha1Hash, version.universalSize));
                            }

                            if (version.clientSha1Hash != null && version.clientSize != null) {
                                lv.downloadables.put("client",
                                        new Pair<String, Long>(version.clientSha1Hash, version.clientSize));
                            }

                            if (version.serverSha1Hash != null && version.serverSize != null) {
                                lv.downloadables.put("server",
                                        new Pair<String, Long>(version.serverSha1Hash, version.serverSize));
                            }

                            return lv;
                        })
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            return new ArrayList<LoaderVersion>();
        }
    }

    @Override
    public List<Library> getInstallLibraries() {
        return null;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(version, rawVersion, false, "Forge");
    }
}
