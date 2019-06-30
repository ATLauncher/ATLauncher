/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Constants;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.network.Download;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.workers.NewInstanceInstaller;
import com.google.gson.reflect.TypeToken;

public class ForgeLoader implements Loader {
    protected String installerUrl;
    protected String version;
    protected String rawVersion;
    protected String minecraft;
    protected File tempDir;
    protected NewInstanceInstaller instanceInstaller;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, NewInstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.version = versionOverride.getVersion();
            this.rawVersion = versionOverride.getRawVersion();

            this.installerUrl = Constants.FORGE_MAVEN + "/" + this.rawVersion + "/forge-" + this.rawVersion
                    + "-installer.jar";
        } else if (metadata.containsKey("version")) {
            this.version = (String) metadata.get("version");
            this.rawVersion = this.minecraft + "-" + this.version;

            if (metadata.containsKey("rawVersion")) {
                this.rawVersion = (String) metadata.get("rawVersion");
            }

            this.installerUrl = Constants.FORGE_MAVEN + "/" + this.rawVersion + "/forge-" + this.rawVersion
                    + "-installer.jar";
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Forge version");
            this.version = this.getLatestVersion();
            this.installerUrl = Constants.FORGE_MAVEN + "/" + this.minecraft + "-" + this.version
                    + (this.minecraft.equals("1.10") ? "-1.10.0" : "") + "/forge-" + this.minecraft + "-" + this.version
                    + (this.minecraft.equals("1.10") ? "-1.10.0" : "") + "-installer.jar";
        } else if ((boolean) metadata.get("recommended")) {
            LogManager.debug("Downloading recommended Forge version");
            this.version = this.getRecommendedVersion();
            this.installerUrl = Constants.FORGE_MAVEN + "/" + this.minecraft + "-" + this.version
                    + (this.minecraft.equals("1.10") ? "-1.10.0" : "") + "/forge-" + this.minecraft + "-" + this.version
                    + (this.minecraft.equals("1.10") ? "-1.10.0" : "") + "-installer.jar";
        }
    }

    public ForgePromotions getPromotions() {
        return Download.build().setUrl(String.format("%s/promotions_slim.json", Constants.FORGE_MAVEN))
                .asClass(ForgePromotions.class);
    }

    public String getLatestVersion() {
        ForgePromotions promotions = this.getPromotions();

        if (promotions == null || !promotions.hasPromo(this.minecraft + "-latest")) {
            return null;
        }

        return promotions.getPromo(this.minecraft + "-latest");
    }

    public String getRecommendedVersion() {
        ForgePromotions promotions = this.getPromotions();

        if (promotions == null || !promotions.hasPromo(this.minecraft + "-recommended")) {
            return null;
        }

        return promotions.getPromo(this.minecraft + "-recommended");
    }

    @Override
    public void downloadAndExtractInstaller() throws Exception {
        Download.build().setUrl(this.installerUrl)
                .downloadTo(new File(App.settings.getLoadersDir(),
                        "/forge-" + this.minecraft + "-" + this.version + "-installer.jar").toPath())
                .unzipTo(this.tempDir.toPath()).downloadFile();

        this.copyLocalLibraries();
    }

    public void copyLocalLibraries() {
        ForgeInstallProfile installProfile = getInstallProfile();
        installProfile.getLibraries().stream().forEach(library -> {
            // copy over any local files from the loader zip file
            if (installProfile.install != null && installProfile.install.filePath != null
                    && library.name.equalsIgnoreCase(installProfile.install.path)) {
                FileUtils.copyFile(new File(tempDir, installProfile.install.filePath).toPath(),
                        new File(App.settings.getGameLibrariesDir(), library.downloads.artifact.path).toPath(), true);
            }
        });
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

    @Override
    public void runProcessors() {

    }

    @Override
    public List<Library> getLibraries() {
        ForgeInstallProfile installProfile = this.getInstallProfile();

        return installProfile.getLibraries().stream().filter(
                library -> !this.instanceInstaller.isServer() || !library.name.equals(installProfile.install.path))
                .collect(Collectors.toList());
    }

    @Override
    public Arguments getArguments() {
        return new Arguments(Arrays.asList(this.getInstallProfile().versionInfo.minecraftArguments.split(" ")).stream()
                .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList()));
    }

    @Override
    public String getMainClass() {
        return this.getInstallProfile().versionInfo.mainClass;
    }

    @Override
    public String getServerJar() {
        return this.getInstallProfile().install.filePath;
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
        java.lang.reflect.Type type = new TypeToken<APIResponse<List<ATLauncherApiForgeVersions>>>() {
        }.getType();

        APIResponse<List<ATLauncherApiForgeVersions>> data = Download.build()
                .setUrl(String.format("%sforge-versions/%s", Constants.API_BASE_URL, minecraft)).asType(type);

        return data.getData().stream().map(version -> new LoaderVersion(version.getVersion(), version.getRawVersion(),
                version.isRecommended(), "Forge")).collect(Collectors.toList());
    }

    @Override
    public List<Library> getInstallLibraries() {
        return null;
    }
}
