/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atlauncher.annot.Json;

@Json
public class ForgeInstallProfile {
    public String version;
    public String target; // in <= 1.12.3
    public Integer spec;
    public String json;
    public String path;
    public String filePath; // in <= 1.12.3
    public String minecraft;
    public String minecraftArguments; // in <= 1.12.3
    public String mainClass; // in <= 1.12.3
    public ForgeInstallProfile versionInfo; // in <= 1.12.3
    public ForgeInstallProfile install; // in <= 1.12.3

    public Map<String, Data> data;
    public List<Processor> processors;
    private List<ForgeLibrary> libraries;

    public List<ForgeLibrary> getLibraries() {
        if (this.versionInfo != null) { // in <= 1.12.3
            return this.versionInfo.getLibraries().stream().filter(l -> l != null).collect(Collectors.toList());
        }

        return this.libraries;
    }
}
