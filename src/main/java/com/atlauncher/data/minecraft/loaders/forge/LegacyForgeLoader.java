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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.network.Download;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import okhttp3.OkHttpClient;

public class LegacyForgeLoader implements Loader {
    private static final Logger LOG = LogManager.getLogger(LegacyForgeLoader.class);

    protected String version;
    protected String rawVersion;

    protected String downloadUrl;
    protected Long downloadSize;
    protected String downloadSha1;
    protected Path downloadPath;

    protected String minecraft;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.version = versionOverride.version;
            this.rawVersion = versionOverride.rawVersion;
            if (Utils.matchVersion(this.minecraft, "1.2", true, true)) {
                Pair<String, Long> downloadable = versionOverride.downloadables
                        .get(instanceInstaller.isServer ? "server" : "client");
                if (downloadable != null) {
                    if (downloadable.right() != null) {
                        this.downloadSize = downloadable.right();
                    }

                    if (downloadable.left() != null) {
                        this.downloadSha1 = downloadable.left();
                    }
                }
            } else {
                Pair<String, Long> universalDownloadable = versionOverride.downloadables.get("universal");
                if (universalDownloadable != null) {
                    if (universalDownloadable.right() != null) {
                        this.downloadSize = universalDownloadable.right();
                    }

                    if (universalDownloadable.left() != null) {
                        this.downloadSha1 = universalDownloadable.left();
                    }
                }
            }
        } else if (metadata.containsKey("version")) {
            this.version = (String) metadata.get("version");
            this.rawVersion = this.minecraft + "-" + this.version;

            if (metadata.containsKey("rawVersion")) {
                this.rawVersion = (String) metadata.get("rawVersion");
            }
        } else if ((boolean) metadata.get("latest")) {
            LOG.debug("Downloading latest Forge version");
            this.version = this.getLatestVersion();
        } else if ((boolean) metadata.get("recommended")) {
            LOG.debug("Downloading recommended Forge version");
            this.version = getRecommendedVersion(this.minecraft);
        }

        String fileIdentifier = Utils.matchVersion(this.minecraft, "1.2", true, true)
                ? (instanceInstaller.isServer ? "server" : "client")
                : "universal";

        this.downloadPath = FileSystem.LOADERS
                .resolve("forge-" + this.minecraft + "-" + this.version + "-" + fileIdentifier + ".zip");
        this.downloadUrl = Constants.DOWNLOAD_SERVER + "/maven/net/minecraftforge/forge/" + this.minecraft + "-"
                + this.version + "/forge-" + this.minecraft + "-" + this.version + "-" + fileIdentifier + ".zip";

        if (metadata.containsKey(fileIdentifier + "Size")) {
            Object value = metadata.get(fileIdentifier + "Size");

            if (value instanceof Double) {
                this.downloadSize = ((Double) metadata.get(fileIdentifier + "Size")).longValue();
            } else if (value instanceof Long) {
                this.downloadSize = (Long) metadata.get(fileIdentifier + "Size");
            }
        }

        if (metadata.containsKey(fileIdentifier + "Sha1")) {
            this.downloadSha1 = (String) metadata.get(fileIdentifier + "Sha1");
        }
    }

    public static ForgePromotions getPromotions() {
        return Download.build().cached().setUrl(Constants.FORGE_PROMOTIONS_FILE).asClass(ForgePromotions.class);
    }

    public String getLatestVersion() {
        return LegacyForgeLoader.getPromotion(ForgePromotionType.LATEST, this.minecraft);
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
        DownloadPool pool = new DownloadPool();

        // first download the universal/client/server zip
        Download forgeDownload = Download.build().setUrl(this.downloadUrl).downloadTo(downloadPath)
                .withInstanceInstaller(instanceInstaller).withHttpClient(httpClient);

        if (!instanceInstaller.isServer) {
            forgeDownload = forgeDownload.copyTo(instanceInstaller.root.resolve("bin/modpack.jar"));
        }

        if (downloadSize != null) {
            forgeDownload = forgeDownload.size(this.downloadSize);
        }

        if (downloadSha1 != null) {
            forgeDownload = forgeDownload.hash(this.downloadSha1);
        }

        if (forgeDownload.needToDownload()) {
            if (downloadSize != null) {
                instanceInstaller.setTotalBytes(downloadSize);
            } else {
                instanceInstaller.setTotalBytes(forgeDownload.getFilesize());
            }
        }
        pool.add(forgeDownload);

        List<FMLLibrary> fmlLibraries = FMLLibrariesConstants.fmlLibraries.get(this.minecraft);

        if (fmlLibraries != null) {
            fmlLibraries.forEach((library) -> {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                        .setUrl(String.format("%s/fmllibs/%s", Constants.DOWNLOAD_SERVER, library.name))
                        .downloadTo(instanceInstaller.root.resolve("lib/" + library.name)).hash(library.sha1Hash)
                        .size(library.size)
                        .withInstanceInstaller(instanceInstaller).withHttpClient(httpClient);

                pool.add(download);
            });
        }

        DownloadPool smallPool = pool.downsize();
        smallPool.downloadAll();
    }

    @Override
    public void runProcessors() {
    }

    @Override
    public List<Library> getLibraries() {
        return new ArrayList<>();
    }

    @Override
    public Arguments getArguments() {
        return null;
    }

    @Override
    public String getMainClass() {
        return null;
    }

    @Override
    public String getServerJar() {
        return downloadPath.getFileName().toString().replace(".zip", ".jar");
    }

    @Override
    public boolean useMinecraftLibraries() {
        return !this.instanceInstaller.isServer;
    }

    @Override
    public boolean useMinecraftArguments() {
        return false;
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
