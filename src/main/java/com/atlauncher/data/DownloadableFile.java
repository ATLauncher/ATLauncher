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
package com.atlauncher.data;

import com.atlauncher.FileSystem;
import com.atlauncher.constants.Constants;
import com.atlauncher.network.Download;
import com.atlauncher.utils.OS;

public class DownloadableFile {
    public String name;
    public String folder;
    public int size;
    public String sha1;
    public String arch;
    public String os;

    public boolean isLauncher() {
        return this.name.equals("launcher");
    }

    public boolean isFiles() {
        return this.name.equals("files.json");
    }

    public boolean isForArchAndOs() {
        if (os != null) {
            if (os.equalsIgnoreCase("osx") && !OS.isMac()) {
                return false;
            }

            if (os.equalsIgnoreCase("windows") && !OS.isWindows()) {
                return false;
            }

            if (os.equalsIgnoreCase("linux") && !OS.isLinux()) {
                return false;
            }
        }

        if (arch != null) {
            return (arch.equalsIgnoreCase("arm64") && (OS.is64Bit() && OS.isArm()))
                    || (arch.equalsIgnoreCase("arm") && (!OS.is64Bit() && OS.isArm()))
                    || (arch.equalsIgnoreCase("x86") && (!OS.is64Bit() && !OS.isArm()))
                    || (arch.equalsIgnoreCase("x64") && (OS.is64Bit() && !OS.isArm()));
        }

        return true;
    }

    public Download getDownload() {
        return Download.build()
                .setUrl(String.format("%s/launcher/%s/%s", Constants.DOWNLOAD_SERVER, this.folder.toLowerCase(),
                        this.name))
                .downloadTo(FileSystem.CONFIGS.resolve(this.folder + "/" + this.name)).size(this.size).hash(this.sha1);
    }
}
