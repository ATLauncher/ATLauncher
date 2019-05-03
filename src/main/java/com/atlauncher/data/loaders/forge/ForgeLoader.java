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
package com.atlauncher.data.loaders.forge;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.ForgeXzDownloadable;
import com.atlauncher.data.HashableDownloadable;
import com.atlauncher.data.loaders.Loader;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class ForgeLoader implements Loader {
    protected String version;
    protected String minecraft;
    protected boolean latest;
    protected boolean recommended;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;

    @Override
    public void set(String version, String minecraft, String yarn, String loader, boolean latest, boolean recommended,
            File tempDir, InstanceInstaller instanceInstaller) {
        this.minecraft = minecraft;
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (version != null) {
            this.version = version;
        } else if (latest) {
            LogManager.debug("Downloading latest Forge version");
            this.version = this.getLatestVersion();
        } else if (recommended) {
            LogManager.debug("Downloading recommended Forge version");
            this.version = this.getRecommendedVersion();
        }
    }

    public ForgePromotions getPromotions() {
        try {
            Downloadable promotionsSlimJson = new Downloadable(
                    "https://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json", false);

            String contents = promotionsSlimJson.getContents();

            return Gsons.DEFAULT.fromJson(contents, ForgePromotions.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
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
    public void downloadAndExtractInstaller() {
        File saveTo = new File(App.settings.getLoadersDir(),
                "/forge-" + this.minecraft + "-" + this.version + "-installer.jar");
        HashableDownloadable download = new HashableDownloadable(
                "https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + this.minecraft + "-" + this.version
                        + "/forge-" + this.minecraft + "-" + this.version + "-installer.jar",
                saveTo, instanceInstaller);

        if (download.needToDownload()) {
            this.instanceInstaller.addTotalDownloadedBytes(download.getFilesize());
            download.download(true);
        }

        this.tempDir.mkdir();
        Utils.unzip(saveTo, this.tempDir);
    }

    public ForgeInstallProfile getInstallProfile() {
        ForgeInstallProfile installProfile = null;

        try {
            installProfile = Gsons.DEFAULT.fromJson(new FileReader(new File(this.tempDir, "install_profile.json")),
                    ForgeInstallProfile.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return installProfile;
    }

    @Override
    public List<Downloadable> getDownloadableLibraries() {
        List<Downloadable> librariesToDownload = new ArrayList<Downloadable>();

        ForgeInstallProfile installProfile = this.getInstallProfile();

        for (Library library : installProfile.getLibraries()) {
            File downloadTo = Utils.convertMavenIdentifierToFile(library.getName(), App.settings.getGameLibrariesDir());

            // forge universal
            if (library.getName().equals(installProfile.getInstall().getPath())) {
                File extractedLibraryFile = new File(this.tempDir, installProfile.getInstall().getFilePath());

                if (extractedLibraryFile.exists()) {
                    if (!downloadTo.exists()) {
                        LogManager.debug("Copying " + extractedLibraryFile.getAbsolutePath() + " to "
                                + downloadTo.getAbsolutePath());
                        new File(downloadTo.getAbsolutePath().substring(0,
                                downloadTo.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
                        Utils.copyFile(extractedLibraryFile, downloadTo, true);
                    }
                } else {
                    LogManager.warn(
                            "Cannot resolve Forge loader install profile library with name of " + library.getName());
                }
                continue;
            }

            String urlBase = library.hasUrl() ? library.getUrl() : "https://libraries.minecraft.net/";

            String url = urlBase + Utils.convertMavenIdentifierToPath(library.getName());

            if (library.isUsingPackXz()) {
                librariesToDownload.add(new ForgeXzDownloadable(url, downloadTo, instanceInstaller));
            } else {
                librariesToDownload.add(new HashableDownloadable(url, downloadTo, instanceInstaller));
            }
        }

        return librariesToDownload;
    }

    @Override
    public void runProcessors() {

    }

    @Override
    public List<String> getLibraries() {
        ForgeInstallProfile installProfile = this.getInstallProfile();
        List<String> libraries = new ArrayList<String>();

        for (Library library : installProfile.getLibraries()) {
            libraries.add(Utils.convertMavenIdentifierToPath(library.getName()));
        }

        return libraries;
    }

    @Override
    public List<String> getArguments() {
        List<String> arguments = new ArrayList<String>();

        for (String argument : this.getInstallProfile().getVersionInfo().getMinecraftArguments().split(" ")) {
            arguments.add(argument);
        }

        return arguments;
    }

    @Override
    public String getMainClass() {
        return this.getInstallProfile().getVersionInfo().getMainClass();
    }
}
