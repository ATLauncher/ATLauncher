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
package com.atlauncher.data.mojang;

import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Hashing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AssetObject {
    private String hash;
    private long size;

    public String getHash() {
        return this.hash;
    }

    public long getSize() {
        return this.size;
    }

    public boolean needToDownload(Path path) {
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            return true;
        }

        long size = 0;

        try {
            size = Files.size(path);
        } catch (IOException e) {
            LogManager.logStackTrace("Error getting file size from " + path, e);
        }

        return (size != this.size) || (!Hashing.HashCode.fromString(this.hash).equals(Hashing.sha1(path)));
    }
}
