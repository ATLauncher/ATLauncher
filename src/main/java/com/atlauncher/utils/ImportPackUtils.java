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

import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.ATLauncherApiCurseModpack;
import com.atlauncher.data.Constants;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.curse.pack.CurseManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.multimc.MultiMCInstanceConfig;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.google.gson.reflect.TypeToken;

import org.zeroturnaround.zip.ZipUtil;

import okhttp3.CacheControl;

public class ImportPackUtils {
    /**
     * Can have multiple formats:
     *
     * Simple root url:
     *
     * https://www.curseforge.com/minecraft/modpacks/madpack-4
     *
     * Main install button on a project:
     *
     * https://www.curseforge.com/minecraft/modpacks/madpack-4/download?client=y
     *
     * Specific file buttons:
     *
     * https://www.curseforge.com/minecraft/modpacks/madpack-4/download/2719411
     * https://www.curseforge.com/minecraft/modpacks/madpack-4/download/2719411?client=y
     * https://www.curseforge.com/minecraft/modpacks/madpack-4/files/2719411
     */
    public static boolean loadFromCurseForgeUrl(String url) {
        if (!url.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
            LogManager.error("Cannot install as the url was not a Curse modpack url");
            return false;
        }

        Pattern pattern = Pattern.compile(
                "https:\\/\\/www\\.curseforge\\.com\\/minecraft\\/modpacks\\/([a-zA-Z0-9-]+)\\/?(?:download|files)?\\/?([0-9]+)?");
        Matcher matcher = pattern.matcher(url);

        if (!matcher.find() || matcher.groupCount() < 2) {
            LogManager.error("Cannot install as the url was not a valid Curse modpack url");
            return false;
        }

        String packSlug = matcher.group(1);
        Integer projectId;
        Integer fileId = null;

        if (matcher.groupCount() == 2 && matcher.group(2) != null) {
            fileId = Integer.parseInt(matcher.group(2));
        }

        java.lang.reflect.Type type = new TypeToken<APIResponse<ATLauncherApiCurseModpack>>() {
        }.getType();

        APIResponse<ATLauncherApiCurseModpack> curseModpackInfo = com.atlauncher.network.Download.build()
                .setUrl(String.format("%scurse-modpack/%s", Constants.API_BASE_URL, packSlug))
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build()).asType(type);

        if (curseModpackInfo.wasError() || curseModpackInfo.getData() == null
                || curseModpackInfo.getData().id == null) {
            LogManager.error(
                    "Cannot install as we couldn't convert the slug to a project id. Try using a zip file download instead.");
            return false;
        }

        projectId = curseModpackInfo.getData().id;

        if (fileId == null) {
            fileId = CurseApi.getModById(projectId).defaultFileId;
        }

        if (projectId == null || fileId == null) {
            LogManager.error(
                    "Cannot install as the id's couldn't be found. Try using a specific files install link instead.");
            return false;
        }

        CurseFile curseFile = CurseApi.getFileForMod(projectId, fileId);
        Path tempZip = FileSystem.TEMP.resolve(curseFile.fileName);

        return loadFromUrl(new Download().setUrl(curseFile.downloadUrl).downloadTo(tempZip).size(curseFile.fileLength)
                .fingerprint(curseFile.packageFingerprint), projectId, fileId);
    }

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
            CurseManifest manifest = Gsons.MINECRAFT.fromJson(new String(ZipUtil.unpackEntry(file, "manifest.json")),
                    CurseManifest.class);

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

    public static boolean loadModpacksChPack(Window parent, int packId) {
        try {
            ModpacksChPackManifest packManifest = com.atlauncher.network.Download.build()
                    .setUrl(String.format("%s/modpack/%d", Constants.MODPACKS_CH_API_URL, packId))
                    .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                    .asClass(ModpacksChPackManifest.class);

            new InstanceInstallerDialog(parent, packManifest);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to install Modpacks.ch pack", e);
            return false;
        }

        return true;
    }

    public static boolean loadCurseForgePack(Window parent, CurseMod curseForgeProject) {
        new InstanceInstallerDialog(parent, curseForgeProject);

        return true;
    }
}
