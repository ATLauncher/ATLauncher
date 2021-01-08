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
package com.atlauncher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.multimc.MultiMCInstanceConfig;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;

import org.zeroturnaround.zip.ZipUtil;

public class ImportPackUtils {
    public static boolean loadFromUrl(String url) {
        return loadFromUrl(new Download().setUrl(url).downloadTo(FileSystem.TEMP.resolve("import.zip")), null, null);
    }

    public static boolean loadFromUrl(Download download, Integer projectId, Integer fileId) {
        try {
            download.downloadFile();
        } catch (IOException e) {
            LogManager.error("Failed to download modpack file");
            return false;
        }

        return loadCurseForgeFormat(download.to.toFile(), projectId, fileId);
    }

    public static boolean loadFromFile(File file) {
        if (ZipUtil.containsEntry(file, "manifest.json")) {
            return loadCurseForgeFormat(file, null, null);
        }

        Path tmpDir = FileSystem.TEMP.resolve("multimcimport" + file.getName().toString().toLowerCase());

        ZipUtil.unpack(file, tmpDir.toFile());

        if (tmpDir.toFile().list().length == 1
                && ZipUtil.containsEntry(file, tmpDir.toFile().list()[0] + "/mmc-pack.json")) {
            return loadMultiMCFormat(tmpDir.resolve(tmpDir.toFile().list()[0]));
        }

        FileUtils.deleteDirectory(tmpDir);

        LogManager.error("Unknown format for importing");

        return false;
    }

    public static boolean loadCurseForgeFormat(File file, Integer projectId, Integer fileId) {
        if (!file.getName().endsWith(".zip")) {
            LogManager.error("Cannot install as the file was not a zip file");
            return false;
        }

        Path tmpDir = FileSystem.TEMP.resolve("curseforgeimport" + file.getName().toString().toLowerCase());

        try {
            CurseForgeManifest manifest = Gsons.MINECRAFT.fromJson(new String(ZipUtil.unpackEntry(file, "manifest.json")),
                    CurseForgeManifest.class);

            if (projectId != null) {
                manifest.projectID = projectId;
            }

            if (fileId != null) {
                manifest.fileID = fileId;
            }

            if (!manifest.manifestType.equals("minecraftModpack")) {
                LogManager.error("Cannot install as the manifest is not a Minecraft Modpack");
                return false;
            }

            if (manifest.manifestVersion != 1) {
                LogManager.error("Cannot install as the manifest is version " + manifest.manifestVersion
                        + " which I cannot install");
                return false;
            }

            ZipUtil.unpack(file, tmpDir.toFile());

            new InstanceInstallerDialog(manifest, tmpDir);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to install CurseForge pack", e);
            FileUtils.deleteDirectory(tmpDir);
            return false;
        }

        return true;
    }

    public static boolean loadMultiMCFormat(Path extractedPath) {
        try (FileReader fileReader = new FileReader(extractedPath.resolve("mmc-pack.json").toFile());
                InputStream instanceCfgStream = new FileInputStream(extractedPath.resolve("instance.cfg").toFile())) {
            MultiMCManifest manifest = Gsons.MINECRAFT.fromJson(fileReader, MultiMCManifest.class);

            Properties props = new Properties();
            props.load(instanceCfgStream);
            manifest.config = new MultiMCInstanceConfig(props);

            if (manifest.formatVersion != 1) {
                LogManager.error("Cannot install as the format is version " + manifest.formatVersion
                        + " which I cannot install");
                return false;
            }

            new InstanceInstallerDialog(manifest, extractedPath);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to install MultiMC pack", e);
            return false;
        }

        return true;
    }
}
