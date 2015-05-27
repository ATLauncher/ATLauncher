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
package com.atlauncher.data.version;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.data.mojang.MojangVersion;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;

@Json
public class MinecraftVersion {
    private String version;
    private boolean server;
    private boolean legacy;
    private boolean coremods;
    private boolean resources;
    private MojangVersion mojangVersion;

    public void loadVersion() {
        Path versionFile = FileSystem.VERSIONS.resolve(this.version + ".json");

        if (!App.skipMinecraftVersionDownloads) {
            Downloadable download = new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("versions/" + this.version +
                    "/" + this.version + ".json"), versionFile, false);
            if (download.needToDownload()) {
                try {
                    download.download();
                } catch (Exception e) {
                    LogManager.logStackTrace(e);
                }
            }
        }

        try {
            mojangVersion = Gsons.DEFAULT_ALT.fromJson(new FileReader(versionFile.toFile()), MojangVersion.class);
        } catch (JsonSyntaxException | JsonIOException | FileNotFoundException e) {
            LogManager.logStackTrace(e);
        }
    }

    public boolean canCreateServer() {
        return this.server;
    }

    public String getVersion() {
        return this.version;
    }

    public MojangVersion getMojangVersion() {
        return this.mojangVersion;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public boolean usesCoreMods() {
        return this.coremods;
    }

    public boolean hasResources() {
        return this.resources;
    }

    public String toString() {
        return this.version;
    }

}