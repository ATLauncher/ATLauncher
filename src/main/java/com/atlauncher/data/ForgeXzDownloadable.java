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

import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.io.File;

public class ForgeXzDownloadable extends HashableDownloadable {
    private File packXzFile;
    private File packFile;
    private File finalFile;
    private File copyTo;

    public ForgeXzDownloadable(String url, File file, InstanceInstaller instanceInstaller, File copyTo) {
        super(url + ".pack.xz", new File(file.getAbsolutePath() + ".pack.xz"), instanceInstaller);

        this.packXzFile = new File(file.getAbsolutePath() + ".pack.xz");
        this.packFile = new File(file.getAbsolutePath() + ".pack");
        this.finalFile = file;
        this.copyTo = copyTo;
    }

    @Override
    public boolean needToDownload() {
        if (!this.finalFile.exists()) {
            return true;
        }

        return super.needToDownload();
    }

    @Override
    protected void afterDownload() {
        Utils.unXZPackFile(this.packXzFile, this.packFile, this.finalFile);

        super.saveFileHash(this.finalFile);

        Utils.delete(this.packXzFile);
        Utils.delete(this.packFile);

        if (this.copyTo != null && !this.copyTo.getAbsolutePath().equalsIgnoreCase(this.finalFile.getAbsolutePath())) {
            if (this.copyTo.exists()) {
                Utils.delete(this.copyTo);
            }

            new File(this.copyTo.getAbsolutePath().substring(0,
                    this.copyTo.getAbsolutePath().lastIndexOf(File.separatorChar))).mkdirs();

            Utils.copyFile(this.finalFile, this.copyTo, true);
        }
    }
}
