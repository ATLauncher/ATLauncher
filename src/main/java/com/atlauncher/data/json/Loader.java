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
package com.atlauncher.data.json;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.loaders.forge.DownloadsItem;
import com.atlauncher.data.loaders.forge.ForgeInstallProfile;
import com.atlauncher.data.loaders.forge.Version;
import com.atlauncher.data.loaders.forge.Library;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

@Json
public class Loader {
    private String type;
    private String version;
    private String minecraft;

    public String getType() {
        return this.type;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMinecraft() {
        return this.minecraft;
    }

    public File downloadAndExtractInstaller(InstanceInstaller instanceInstaller) {
        File saveTo = new File(App.settings.getTempDir(),
                "/forge-" + this.minecraft + "-" + this.version + "-installer.jar");
        File extractLocation = new File(App.settings.getTempDir(),
                "forge-" + this.minecraft + "-" + this.version + "-installer");
        Downloadable download = new Downloadable(
                "https://files.minecraftforge.net/maven/net/minecraftforge/forge/" + this.minecraft + "-" + this.version
                        + "/forge-" + this.minecraft + "-" + this.version + "-installer.jar",
                saveTo, instanceInstaller);

        if (extractLocation.exists()) {
            Utils.delete(extractLocation);
        }

        download.download();

        extractLocation.mkdir();
        Utils.unzip(saveTo, extractLocation);
        Utils.delete(saveTo);

        return extractLocation;
    }

    public List<Downloadable> getLibraries(File extractedDir, InstanceInstaller instanceInstaller) {
        ArrayList<Downloadable> librariesToDownload = new ArrayList<Downloadable>();

        try {
            ForgeInstallProfile installProfile = Gsons.DEFAULT.fromJson(
                    new FileReader(new File(extractedDir, "install_profile.json")), ForgeInstallProfile.class);
            Version version = Gsons.DEFAULT.fromJson(new FileReader(new File(extractedDir, "version.json")),
                    Version.class);

            for (Library library : installProfile.getLibraries()) {
                DownloadsItem artifact = library.getDownloads().getArtifact();
                File downloadTo = new File(App.settings.getGameLibrariesDir(), artifact.getPath());

                if (!artifact.hasUrl()) {
                    File extractedLibraryFile = new File(extractedDir, "maven/" + artifact.getPath());

                    if (extractedLibraryFile.exists()
                            && (!downloadTo.exists() || Utils.getSHA1(downloadTo) != artifact.getSha1())) {
                        Utils.copyFile(extractedLibraryFile, downloadTo, true);
                    } else {
                        LogManager.warn("Cannot resolve Forge loader install profile library with name of "
                                + library.getName());
                    }
                } else {
                    librariesToDownload.add(new Downloadable(artifact.getUrl(), downloadTo, artifact.getSha1(),
                            artifact.getSize(), instanceInstaller, false));
                }
            }

            for (Library library : version.getLibraries()) {
                DownloadsItem artifact = library.getDownloads().getArtifact();
                File downloadTo = new File(App.settings.getGameLibrariesDir(), artifact.getPath());

                if (!artifact.hasUrl()) {
                    File extractedLibraryFile = new File(extractedDir, "maven/" + artifact.getPath());

                    if (extractedLibraryFile.exists()
                            && (!downloadTo.exists() || Utils.getSHA1(downloadTo) != artifact.getSha1())) {
                        Utils.copyFile(extractedLibraryFile, downloadTo, true);
                    } else {
                        LogManager
                                .warn("Cannot resolve Forge loader version library with name of " + library.getName());
                    }
                } else {
                    librariesToDownload.add(new Downloadable(artifact.getUrl(), downloadTo, artifact.getSha1(),
                            artifact.getSize(), instanceInstaller, false));
                }
            }
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            LogManager.logStackTrace(e);
        }

        return librariesToDownload;
    }
}
