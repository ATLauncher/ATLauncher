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
package com.atlauncher.data;

import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class HashableDownloadable extends Downloadable {
    public HashableDownloadable(String url, File file, InstanceInstaller instanceInstaller, File copyTo) {
        super(url, file, null, -1, instanceInstaller, false, copyTo, copyTo != null ? true : false);
    }

    public HashableDownloadable(String url, File file, InstanceInstaller instanceInstaller) {
        this(url, file, instanceInstaller, null);
    }

    @Override
    protected void setRequestHeaders() {
        super.setRequestHeaders();

        if (this.file.exists() && this.hasLocalHash()) {
            Map<String, String> localHash = this.getLocalHash();
            String etag = localHash.get("etag");

            if (etag != null) {
                this.connection.setRequestProperty("If-None-Match", etag);
            }
        }
    }

    @Override
    public boolean needToDownload() {
        try {
            if (this.getConnection().getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Map<String, String> localHash = this.getLocalHash();
                String sha1 = localHash.get("sha1");

                if (this.file.exists() && Utils.getSHA1(this.file).equals(sha1)) {
                    LogManager.debug("When downloading " + this.url
                            + " a 304 not modified was sent back, so we don't need to redownload");
                    this.copyFile();
                    return false;
                }
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return true;
    }

    public File getLocalHashFile() {
        return new File(this.file.getAbsolutePath() + ".hash");
    }

    public boolean hasLocalHash() {
        return this.getLocalHashFile().exists() && this.getLocalHash() != null;
    }

    public Map<String, String> getLocalHash() {
        try {
            if (!this.getLocalHashFile().exists()) {
                this.getLocalHashFile().createNewFile();
            }

            java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
            }.getType();

            return Gsons.DEFAULT.fromJson(new FileReader(this.getLocalHashFile()), type);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public void saveLocalHash(Map<String, String> hash) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!this.getLocalHashFile().exists()) {
                this.getLocalHashFile().createNewFile();
            }

            fw = new FileWriter(this.getLocalHashFile());
            bw = new BufferedWriter(fw);
            bw.write(Gsons.DEFAULT.toJson(hash));
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                LogManager
                        .logStackTrace("Exception while trying to close FileWriter/BufferedWriter for saving instances "
                                + "json file.", e);
            }
        }
    }

    @Override
    protected void afterDownload() {
        this.saveFileHash(this.file);
    }

    protected void saveFileHash(File fileToHash) {
        HashMap<String, String> localHash = new HashMap<String, String>();

        localHash.put("sha1", Utils.getSHA1(fileToHash == null ? this.file : fileToHash));
        localHash.put("etag", this.getConnection().getHeaderField("ETag"));

        this.saveLocalHash(localHash);
    }
}
