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

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.data.Downloadable;

import java.io.File;
import java.nio.file.Path;

@Json
public class LauncherLibrary {
    private String name;
    private String filename;
    private String url;
    private String version;
    private String md5;
    private boolean atlauncherDownload;
    private boolean autoLoad;
    private boolean exitOnFail;

    public String getName() {
        return this.name;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getUrl() {
        return this.url;
    }

    public String getVersion() {
        return this.version;
    }

    public Path getFilePath() {
        return FileSystem.LAUNCHER_LIBRARIES.resolve(this.filename);
    }

    public Downloadable getDownloadable() {
        return new Downloadable(this.url, FileSystem.LAUNCHER_LIBRARIES.resolve(this.filename), this.md5, null, this
                .atlauncherDownload);
    }

    public String getMd5() {
        return this.md5;
    }

    public boolean isATLauncherDownload() {
        return this.atlauncherDownload;
    }

    public boolean shouldAutoLoad() {
        return this.autoLoad;
    }

    public boolean shouldExitOnFail() {
        return this.exitOnFail;
    }
}
