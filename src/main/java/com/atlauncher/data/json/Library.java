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
package com.atlauncher.data.json;

import com.atlauncher.App;
import com.atlauncher.annot.Json;

import java.io.File;

@Json
public class Library {
    private String url;
    private String path;
    private String file;
    private String server;
    private String md5;
    private boolean force;
    private DownloadType download;
    private int filesize;
    private String depends;
    private String dependsGroup;

    public String getUrl() {
        return this.url;
    }

    public String getPath() {
        return this.path;
    }

    public String getFile() {
        return this.file;
    }

    public File getDownloadPath() {
        if (this.path == null) {
            return new File(App.settings.getGameLibrariesDir(), this.file);
        }

        return new File(App.settings.getGameLibrariesDir(), this.path);
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
