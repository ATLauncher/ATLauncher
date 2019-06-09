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
package com.atlauncher.data;

import java.io.File;

import com.atlauncher.App;

public class DownloadableFile {
    private String name;
    private String folder;
    private int size;
    private String md5;
    private String sha1;

    public boolean isLauncher() {
        return this.name.equals("Launcher");
    }

    public String getMD5() {
        return this.md5;
    }

    public String getSHA1() {
        return this.sha1;
    }

    public Downloadable getDownloadable() {
        File file = new File(new File(App.settings.getConfigsDir(), this.folder), this.name);
        if (this.folder.equalsIgnoreCase("Skins")) {
            file = new File(App.settings.getSkinsDir(), this.name);
        }
        return new Downloadable("launcher/" + this.folder.toLowerCase() + "/" + this.name, file, this.sha1, this
                .size, null, true);
    }
}
