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
package com.atlauncher.data.minecraft.loaders.fabric;

import java.nio.file.Files;
import java.nio.file.Path;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;

@Json
public class FabricLibrary extends Library {

    public FabricLibrary(String name, String url) {
        this.name = name;

        Downloads downloads = new Downloads();
        Download artifact = new Download();
        artifact.path = Utils.convertMavenIdentifierToPath(name);
        artifact.url = String.format("%s%s", url, artifact.path);

        Path localLibraryPath = FileSystem.LIBRARIES.resolve(artifact.path);

        if (Files.exists(localLibraryPath)) {
            artifact.size = localLibraryPath.toFile().length();

            Hashing.HashCode sha1 = Hashing.sha1(localLibraryPath);
            if (sha1 != null) {
                artifact.sha1 = sha1.toString();
            }
        }

        downloads.artifact = artifact;

        this.downloads = downloads;
    }

    public FabricLibrary(String name) {
        this(name, Constants.FABRIC_MAVEN);
    }
}
