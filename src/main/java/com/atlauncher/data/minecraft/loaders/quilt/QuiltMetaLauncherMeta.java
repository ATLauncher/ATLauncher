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
package com.atlauncher.data.minecraft.loaders.quilt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuiltMetaLauncherMeta {
    private final String version;
    private final Map<String, String> mainClass;
    private final Map<String, List<QuiltLibrary>> libraries;

    QuiltMetaLauncherMeta(String version, Map<String, String> mainClass, Map<String, List<QuiltLibrary>> libraries) {
        this.version = version;
        this.mainClass = mainClass;
        this.libraries = libraries;
    }

    public String getVersion() {
        return this.version;
    }

    public String getMainClass(boolean isServer) {
        if (isServer) {
            return this.mainClass.get("server");
        } else {
            return this.mainClass.get("client");
        }
    }

    public List<QuiltLibrary> getLibraries(boolean isServer) {

        List<QuiltLibrary> libraries = new ArrayList<>(this.libraries.get("common"));

        if (isServer) {
            libraries.addAll(this.libraries.get("server"));
        } else {
            libraries.addAll(this.libraries.get("client"));
        }

        return libraries;
    }
}
