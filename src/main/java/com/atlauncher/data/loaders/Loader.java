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
package com.atlauncher.data.loaders;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.Downloadable;
import com.atlauncher.workers.InstanceInstaller;

public interface Loader {
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller);

    public void downloadAndExtractInstaller();

    public List<Downloadable> getDownloadableLibraries();

    public void runProcessors();

    public List<String> getLibraries();

    public List<String> getArguments();

    public String getMainClass();

    public String getServerJar();

    public boolean useMinecraftLibraries();

    public boolean useMinecraftArguments();
}
