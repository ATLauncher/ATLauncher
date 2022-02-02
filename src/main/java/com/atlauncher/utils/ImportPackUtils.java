/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCInstanceConfig;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.nickymoe.SlugResponse;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ImportPackUtils {
    public static boolean loadFromUrl(String url) {
        if (url.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
            return loadFromCurseForgeUrl(url);
        }

        return loadFromUrl(new Download().setUrl(url).downloadTo(FileSystem.TEMP.resolve("import.zip")), null, null);
    }

    public static boolean loadFromCurseForgeUrl(String url) {
        if (!url.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
            LogManager.error("Cannot install as the url was not a CurseForge modpack url");
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
        Integer projectId = null;
        Integer fileId = null;

        LogManager.info(matcher.groupCount() + "");

        if (matcher.groupCount() == 2 && matcher.group(2) != null) {
            fileId = Integer.parseInt(matcher.group(2));
        }

        LogManager.debug("Found pack with slug " + packSlug + " and file id of " + fileId);

        // TODO: remove this and replace with lookup in CurseForge api
        SlugResponse modInfo = new Download()
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        "{\"query\":\"{\\n  addons(gameId: 432, section: \\\"Modpacks\\\", slug: \\\"" + packSlug
                                + "\\\") {\\n    id\\n    defaultFileId\\n  }\\n}\"}"))
                .setUrl("https://curse.nikky.moe/graphql").asClass(SlugResponse.class);

        projectId = modInfo.data.addons.get(0).id;

        if (fileId == null) {
            fileId = modInfo.data.addons.get(0).defaultFileId;
        }

        if (projectId == null || fileId == null) {
            LogManager.error(
                    "Cannot install as the id's couldn't be found. Try using a specific files install link instead.");
            return false;
        }

        LogManager.debug("Resolved to project id " + projectId + " and file id of " + fileId);

        CurseForgeFile curseFile = CurseForgeApi.getFileForProject(projectId, fileId);
        Path tempZip = FileSystem.TEMP.resolve(curseFile.fileName);

        try {
            new Download().setUrl(curseFile.downloadUrl).downloadTo(tempZip).size(curseFile.fileLength)
                    .fingerprint(curseFile.packageFingerprint).downloadFile();
        } catch (IOException e) {
            LogManager.error("Failed to download modpack file from Curse");
            return false;
        }

        return loadCurseForgeFormat(tempZip.toFile(), projectId, fileId);
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
        try {
            if (ArchiveUtils.archiveContainsFile(file.toPath(), "manifest.json")) {
                return loadCurseForgeFormat(file, null, null);
            }

            if (ArchiveUtils.archiveContainsFile(file.toPath(), "modrinth.index.json")) {
                return loadModrinthFormat(file);
            }

            Path tmpDir = FileSystem.TEMP.resolve("multimcimport" + file.getName().toString().toLowerCase());

            ArchiveUtils.extract(file.toPath(), tmpDir);

            if (tmpDir.toFile().list().length == 1
                    && ArchiveUtils.archiveContainsFile(file.toPath(), tmpDir.toFile().list()[0] + "/mmc-pack.json")) {
                return loadMultiMCFormat(tmpDir.resolve(tmpDir.toFile().list()[0]));
            }

            FileUtils.deleteDirectory(tmpDir);

            LogManager.error("Unknown format for importing");
        } catch (Throwable t) {
            LogManager.logStackTrace("Error in zip file for import", t);
        }

        return false;
    }

    public static boolean loadCurseForgeFormat(File file, Integer projectId, Integer fileId) {
        if (!file.getName().endsWith(".zip")) {
            LogManager.error("Cannot install as the file was not a zip file");
            return false;
        }

        Path tmpDir = FileSystem.TEMP.resolve("curseforgeimport" + file.getName().toString().toLowerCase());

        try {
            CurseForgeManifest manifest = Gsons.MINECRAFT.fromJson(ArchiveUtils.getFile(file.toPath(), "manifest.json"),
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
                LogManager.warn("Manifest is version " + manifest.manifestVersion + " which may be an issue!");
            }

            ArchiveUtils.extract(file.toPath(), tmpDir);

            new InstanceInstallerDialog(manifest, tmpDir);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to install CurseForge pack", e);
            FileUtils.deleteDirectory(tmpDir);
            return false;
        }

        return true;
    }

    public static boolean loadModrinthFormat(File file) {
        if (!file.getName().endsWith(".mrpack")) {
            LogManager.error("Cannot install as the file was not a mrpack file");
            return false;
        }

        Path tmpDir = FileSystem.TEMP.resolve("modrinthimport" + file.getName().toString().toLowerCase());

        try {
            ModrinthModpackManifest manifest = Gsons.MINECRAFT
                    .fromJson(ArchiveUtils.getFile(file.toPath(), "modrinth.index.json"),
                            ModrinthModpackManifest.class);

            if (!manifest.game.equals("minecraft")) {
                LogManager.error(
                        "Cannot install as the manifest is for game " + manifest.game + " and not for Minecraft");
                return false;
            }

            if (!manifest.dependencies.containsKey("minecraft")) {
                LogManager.error("Cannot install as the manifest doesn't contain a minecraft dependency");
                return false;
            }

            if (manifest.formatVersion != 1) {
                LogManager.warn("Manifest is version " + manifest.formatVersion + " which may be an issue!");
            }

            ArchiveUtils.extract(file.toPath(), tmpDir);

            new InstanceInstallerDialog(manifest, tmpDir);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to install Modrinth pack", e);
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
