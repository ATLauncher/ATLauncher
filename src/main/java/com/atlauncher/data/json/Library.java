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
package com.atlauncher.data.json;

import java.io.File;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;

@Json
public class Library {
    public String url;
    public String path;
    public String file;
    public String server;
    public String md5;
    public boolean force;
    public DownloadType download;
    public int filesize;
    public String depends;
    public String dependsGroup;

    public String getUrl() {
        return this.url;
    }

    public boolean hasPath() {
        return this.path != null;
    }

    public String getPath() {
        return this.path;
    }

    public String getFile() {
        return this.file;
    }

    public File getDownloadPath() {
        if (this.path == null) {
            return FileSystem.LIBRARIES.resolve(this.file).toFile();
        }

        return FileSystem.LIBRARIES.resolve(this.path).toFile();
    }

    public String getServer() {
        return this.server;
    }

    public String getMD5() {
        return this.md5;
    }

    public int getFilesize() {
        return this.filesize;
    }

    public boolean shouldForce() {
        return this.force;
    }

    public DownloadType getDownloadType() {
        return this.download;
    }

    public void setDownloadType(DownloadType type) {
        this.download = type;
    }

    public String getDepends() {
        return this.depends;
    }

    public String getDependsGroup() {
        return this.dependsGroup;
    }

    public boolean hasDepends() {
        return this.depends != null;
    }

    public boolean hasDependsGroup() {
        return this.dependsGroup != null;
    }

    public boolean forServer() {
        return this.server != null;
    }
}
