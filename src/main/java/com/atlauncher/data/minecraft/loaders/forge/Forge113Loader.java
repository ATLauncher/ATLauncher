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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.FileUtils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class Forge113Loader extends ForgeLoader {
    @Override
    public ForgeInstallProfile getInstallProfile() {
        ForgeInstallProfile installProfile = super.getInstallProfile();

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

    @Override
    public void copyLocalLibraries() {
        Version version = getVersion();
        ForgeInstallProfile installProfile = getInstallProfile();
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

    public Version getVersion() {
        Version version = null;

        try (InputStreamReader fileReader = new InputStreamReader(
                new FileInputStream(new File(this.tempDir, "version.json")), StandardCharsets.UTF_8)) {
            version = Gsons.DEFAULT.fromJson(fileReader, Version.class);
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }

        return version;
    }

    public void runProcessors() {
        ForgeInstallProfile installProfile = this.getInstallProfile();

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

    public List<Library> getInstallLibraries() {
        return new ArrayList<>(this.getInstallProfile().getLibraries());
    }

    public List<Library> getLibraries() {
        return new ArrayList<>(this.getVersion().libraries);
    }

    public Arguments getArguments() {
        int forgeMajorVersion = Integer.parseInt(this.version.substring(0, this.version.indexOf(".")));

        // forge >= 37 needs some altering of the "-DignoreList" argument
        if (forgeMajorVersion < 37) {
            return this.getVersion().arguments;
        }

        List<ArgumentRule> jvmArgs = this.getVersion().arguments.jvm.stream().map(arg -> {
            if (arg.getValueAsString().startsWith("-DignoreList=")) {
                return new ArgumentRule(String.format("%s,client-%s.jar", arg.getValueAsString(), this.minecraft));
            }

            return arg;
        }).collect(Collectors.toList());

        return new Arguments(this.getVersion().arguments.game, jvmArgs);
    }

    public String getMainClass() {
        return this.getVersion().mainClass;
    }

    @Override
    public String getServerJar() {
        Library forgeLibrary = this.getVersion().libraries.stream()
                .filter(library -> library.name.startsWith("net.minecraftforge:forge")).findFirst().orElse(null);

        if (forgeLibrary != null) {
            return forgeLibrary.downloads.artifact.path
                    .substring(forgeLibrary.downloads.artifact.path.lastIndexOf("/") + 1);
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
