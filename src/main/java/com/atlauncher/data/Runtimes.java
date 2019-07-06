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
import java.io.IOException;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.zeroturnaround.zip.ZipUtil;

@Json
public class Runtimes {
    public RuntimesOS osx;
    public RuntimesOS windows;

    public String download() {
        if (OS.isLinux()) {
            return null;
        }

        RuntimesOS metaForOS = OS.isWindows() ? this.windows : this.osx;
        Runtime runtime = null;

        if (OS.is64Bit() && metaForOS.x64 != null) {
            runtime = metaForOS.x64;
        } else if (!OS.is64Bit() && metaForOS.x86 != null) {
            runtime = metaForOS.x86;
        }

        if (runtime != null) {
            File runtimeFolder = new File(App.settings.getRuntimesDir(), runtime.version);
            File releaseFile = new File(runtimeFolder, "release");

            // no need to download/extract
            if (releaseFile.exists()) {
                return runtimeFolder.getAbsolutePath();
            }

            if (!runtimeFolder.exists()) {
                runtimeFolder.mkdirs();
            }

            String url = String.format("%s/%s", Constants.DOWNLOAD_SERVER, runtime.url);
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File downloadFile = new File(runtimeFolder, fileName);
            File unpackedFile = new File(runtimeFolder, fileName.replace(".xz", ""));

            com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(url)
                    .hash(runtime.sha1).size(runtime.size).downloadTo(downloadFile.toPath());

            if (download.needToDownload()) {
                LogManager.info("Downloading runtime version " + runtime.version);

                try {
                    download.downloadFile();
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                    return null;
                }
            }

            LogManager.info("Extracting runtime version " + runtime.version);

            try {
                Utils.unXZFile(downloadFile, unpackedFile);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                return null;
            }

            ZipUtil.unpack(unpackedFile, runtimeFolder);
            Utils.delete(unpackedFile);

            return runtimeFolder.getAbsolutePath();
        }

        return null;
    }
}
