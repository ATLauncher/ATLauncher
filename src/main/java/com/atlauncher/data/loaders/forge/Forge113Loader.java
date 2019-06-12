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
package com.atlauncher.data.loaders.forge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Forge113Loader extends ForgeLoader {
    @Override
    public ForgeInstallProfile getInstallProfile() {
        ForgeInstallProfile installProfile = super.getInstallProfile();

        installProfile.getData().put("SIDE", new Data("client", "server"));
        installProfile.getData().put("MINECRAFT_JAR",
                new Data(instanceInstaller.getMinecraftJarLibrary("client").getAbsolutePath(),
                        instanceInstaller.getMinecraftJarLibrary("server").getAbsolutePath()));

        return installProfile;
    }

    public Version getVersion() {
        Version version = null;

        try {
            version = Gsons.DEFAULT.fromJson(new FileReader(new File(this.tempDir, "version.json")), Version.class);
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            LogManager.logStackTrace(e);
        }

        return version;
    }

    @Override
    public List<Downloadable> getDownloadableLibraries() {
        List<Downloadable> librariesToDownload = new ArrayList<>();

        File librariesDirectory = this.instanceInstaller.isServer() ? this.instanceInstaller.getLibrariesDirectory()
                : App.settings.getGameLibrariesDir();

        ForgeInstallProfile installProfile = this.getInstallProfile();

        for (Library library : installProfile.getLibraries()) {
            DownloadsItem artifact = library.getDownloads().getArtifact();
            File downloadTo = new File(App.settings.getGameLibrariesDir(), artifact.getPath());
            File finalDownloadTo = new File(librariesDirectory, artifact.getPath());

            if (!artifact.hasUrl()) {
                File extractedLibraryFile = new File(this.tempDir, "maven/" + artifact.getPath());

                if (extractedLibraryFile.exists() && (!finalDownloadTo.exists()
                        || (finalDownloadTo.exists() && Utils.getSHA1(finalDownloadTo) != artifact.getSha1()))) {
                    finalDownloadTo.getParentFile().mkdirs();
                    if (!Utils.copyFile(extractedLibraryFile, finalDownloadTo, true)) {
                        LogManager.error("Failed to copy forge library file");
                        instanceInstaller.cancel(true);
                    }
                } else {
                    LogManager.error("Failed to find and verify forge library file");
                    instanceInstaller.cancel(true);
                }
            } else {
                librariesToDownload.add(new Downloadable(artifact.getUrl(), downloadTo, artifact.getSha1(),
                        artifact.getSize(), instanceInstaller, false, finalDownloadTo, true));
            }
        }

        Version version = this.getVersion();

        for (Library library : version.getLibraries()) {
            DownloadsItem artifact = library.getDownloads().getArtifact();
            File downloadTo = new File(App.settings.getGameLibrariesDir(), artifact.getPath());
            File finalDownloadTo = new File(librariesDirectory, artifact.getPath());

            if (!artifact.hasUrl()) {
                File extractedLibraryFile = new File(this.tempDir, "maven/" + artifact.getPath());

                if (extractedLibraryFile.exists()
                        && (!finalDownloadTo.exists() || Utils.getSHA1(finalDownloadTo) != artifact.getSha1())) {

                    new File(finalDownloadTo.getAbsolutePath().substring(0,
                            finalDownloadTo.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();
                    Utils.copyFile(extractedLibraryFile, finalDownloadTo, true);
                } else {
                    LogManager.warn("Cannot resolve Forge loader version library with name of " + library.getName());
                }

                if (this.instanceInstaller.isServer()) {
                    Utils.copyFile(extractedLibraryFile,
                            new File(this.instanceInstaller.getRootDirectory(), downloadTo.getName()), true);
                }
            } else {
                librariesToDownload.add(new Downloadable(artifact.getUrl(), downloadTo, artifact.getSha1(),
                        artifact.getSize(), instanceInstaller, false, finalDownloadTo, true));
            }
        }

        return librariesToDownload;
    }

    public void runProcessors() {
        ForgeInstallProfile installProfile = this.getInstallProfile();

        for (Processor processor : installProfile.getProcessors()) {
            if (!instanceInstaller.isCancelled()) {
                try {
                    processor.process(installProfile, this.tempDir, instanceInstaller);
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                    LogManager.error("Failed to process processor with jar " + processor.getJar());
                    instanceInstaller.cancel(true);
                }
            }
        }
    }

    public List<String> getLibraries() {
        List<String> libraries = new ArrayList<>();

        if (this.instanceInstaller.isServer()) {
            for (Library library : this.getInstallProfile().getLibraries()) {
                libraries.add(library.getDownloads().getArtifact().getPath());
            }
        }

        for (Library library : this.getVersion().getLibraries()) {
            libraries.add(library.getDownloads().getArtifact().getPath());
        }

        return libraries;
    }

    public List<String> getArguments() {
        return this.getVersion().getArguments().get("game");
    }

    public String getMainClass() {
        return this.getVersion().getMainClass();
    }

    @Override
    public String getServerJar() {
        for (Library library : this.getVersion().getLibraries()) {
            DownloadsItem artifact = library.getDownloads().getArtifact();

            if (!artifact.hasUrl()) {
                return artifact.getPath().substring(artifact.getPath().lastIndexOf("/") + 1,
                        artifact.getPath().length());
            }
        }

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
}
