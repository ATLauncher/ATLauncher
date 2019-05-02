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

import com.atlauncher.annot.Json;

import java.util.List;
import java.util.Map;

@Json
public class ForgeInstallProfile {
    private String version;
    private String target; // in <= 1.12.3
    private String json;
    private String path;
    private String filePath; // in <= 1.12.3
    private String minecraft;
    private String minecraftArguments; // in <= 1.12.3
    private String mainClass; // in <= 1.12.3
    private ForgeInstallProfile versionInfo; // in <= 1.12.3
    private ForgeInstallProfile install; // in <= 1.12.3

    private Map<String, Data> data;
    private List<Processor> processors;
    private List<Library> libraries;

    public String getVersion() {
        return this.version;
    }

    public String getTarget() {
        return this.target;
    }

    public String getJson() {
        return this.json;
    }

    public String getPath() {
        return this.path;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getMinecraft() {
        return this.minecraft;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public ForgeInstallProfile getVersionInfo() {
        return this.versionInfo;
    }

    public ForgeInstallProfile getInstall() {
        return this.install;
    }

    public Map<String, Data> getData() {
        return this.data;
    }

    public List<Processor> getProcessors() {
        return this.processors;
    }

    public List<Library> getLibraries() {
        if (this.versionInfo != null) { // in <= 1.12.3
            return this.versionInfo.libraries;
        }

        return this.libraries;
    }
}
