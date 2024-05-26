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
package com.atlauncher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.LWJGLVersions;
import com.atlauncher.data.Pack;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.VersionManifestVersion;

public final class Data {

    public static Map<String, Object> CONFIG = new HashMap<>();
    public static Map<String, Object> CONFIG_OVERRIDES = new HashMap<>();

    public static final List<Pack> PACKS = new LinkedList<>();

    public static final Map<String, VersionManifestVersion> MINECRAFT = new HashMap<>();
    public static LWJGLVersions LWJGL_VERSIONS = null;
    public static JavaRuntimes JAVA_RUNTIMES = null;
}
