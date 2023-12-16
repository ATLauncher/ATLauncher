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
package com.atlauncher.workers;

import java.awt.Window;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Utils;

/**
 * @since 2023 / 12 / 16
 */
public class MissingModScanner {

    public static void scanMissingMods(Instance instance) {
        scanMissingMods(instance, App.launcher.getParent());
    }

    public static void scanMissingMods(Instance instance, Window parent) {
        PerformanceManager.start("Instance::scanMissingMods - CheckForAddedMods");

        // files to scan
        List<Path> files = new ArrayList<>();

        // find the mods that have been added by the user manually
        for (Path path : Arrays.asList(instance.ROOT.resolve("mods"), instance.ROOT.resolve("disabledmods"),
            instance.ROOT.resolve("resourcepacks"), instance.ROOT.resolve("jarmods"))) {
            if (!Files.exists(path)) {
                continue;
            }

            com.atlauncher.data.Type fileType = path.equals(instance.ROOT.resolve("resourcepacks"))
                ? com.atlauncher.data.Type.resourcepack
                : (path.equals(instance.ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                : com.atlauncher.data.Type.mods);

            try (Stream<Path> stream = Files.list(path)) {
                files.addAll(stream
                    .filter(file -> !Files.isDirectory(file) && Utils.isAcceptedModFile(file)).filter(
                        file -> instance.launcher.mods.stream()
                            .noneMatch(mod -> mod.type == fileType
                                && mod.file.equals(file.getFileName().toString())))
                    .collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace("Error scanning missing mods", e);
            }
        }

        if (files.size() != 0) {
            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Scanning New Mods"), 0,
                GetText.tr("Scanning New Mods"), parent);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> mods = files.parallelStream()
                    .map(file -> {
                        com.atlauncher.data.Type fileType = file.getParent().equals(instance.ROOT.resolve("resourcepacks"))
                            ? com.atlauncher.data.Type.resourcepack
                            : (file.getParent().equals(instance.ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                            : com.atlauncher.data.Type.mods);

                        return DisableableMod.generateMod(file.toFile(), fileType,
                            !file.getParent().equals(instance.ROOT.resolve("disabledmods")));
                    })
                    .collect(Collectors.toList());

                if (!App.settings.dontCheckModsOnCurseForge) {
                    Map<Long, DisableableMod> murmurHashes = new HashMap<>();

                    mods.stream()
                        .filter(dm -> dm.curseForgeProject == null && dm.curseForgeFile == null)
                        .filter(dm -> dm.getFile(instance.ROOT, instance.id) != null).forEach(dm -> {
                            try {
                                long hash = Hashing
                                    .murmur(dm.disabled ? dm.getDisabledFile(instance).toPath()
                                        : dm
                                        .getFile(instance.ROOT, instance.id).toPath());
                                murmurHashes.put(hash, dm);
                            } catch (Throwable t) {
                                LogManager.logStackTrace(t);
                            }
                        });

                    if (murmurHashes.size() != 0) {
                        CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                            .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

                        if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                            int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                                .toArray();

                            if (projectIdsFound.length != 0) {
                                Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                                    .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    fingerprintResponse.exactMatches.stream()
                                        .filter(em -> em != null && em.file != null
                                            && murmurHashes.containsKey(em.file.packageFingerprint))
                                        .forEach(foundMod -> {
                                            DisableableMod dm = murmurHashes
                                                .get(foundMod.file.packageFingerprint);

                                            // add CurseForge information
                                            dm.curseForgeProjectId = foundMod.id;
                                            dm.curseForgeFile = foundMod.file;
                                            dm.curseForgeFileId = foundMod.file.id;

                                            CurseForgeProject curseForgeProject = foundProjects
                                                .get(foundMod.id);

                                            if (curseForgeProject != null) {
                                                dm.curseForgeProject = curseForgeProject;
                                                dm.name = curseForgeProject.name;
                                                dm.description = curseForgeProject.summary;
                                            }

                                            LogManager.debug("Found matching mod from CurseForge called "
                                                + dm.curseForgeFile.displayName);
                                        });
                                }
                            }
                        }
                    }
                }

                if (!App.settings.dontCheckModsOnModrinth) {
                    Map<String, DisableableMod> sha1Hashes = new HashMap<>();

                    mods.stream()
                        .filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
                        .filter(dm -> dm.getFile(instance.ROOT, instance.id) != null).forEach(dm -> {
                            try {
                                sha1Hashes.put(Hashing
                                    .sha1(dm.disabled ? dm.getDisabledFile(instance).toPath()
                                        : dm
                                        .getFile(instance.ROOT, instance.id).toPath())
                                    .toString(), dm);
                            } catch (Throwable t) {
                                LogManager.logStackTrace(t);
                            }
                        });

                    if (sha1Hashes.size() != 0) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                            .getVersionsFromSha1Hashes(keys.toArray(new String[keys.size()]));

                        if (modrinthVersions != null && modrinthVersions.size() != 0) {
                            String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                .toArray(String[]::new);

                            if (projectIdsFound.length != 0) {
                                Map<String, ModrinthProject> foundProjects = ModrinthApi
                                    .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                        ModrinthVersion version = entry.getValue();
                                        ModrinthProject project = foundProjects.get(version.projectId);

                                        if (project != null) {
                                            DisableableMod dm = sha1Hashes.get(entry.getKey());

                                            // add Modrinth information
                                            dm.modrinthProject = project;
                                            dm.modrinthVersion = version;

                                            if (!dm.isFromCurseForge()
                                                || App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                                                dm.name = project.title;
                                                dm.description = project.description;
                                            }

                                            LogManager.debug(String.format(
                                                "Found matching mod from Modrinth called %s with file %s",
                                                project.title, version.name));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mods.forEach(mod -> LogManager.info("Found extra mod with name of " + mod.file));
                instance.launcher.mods.addAll(mods);
                instance.save();
                progressDialog.close();
            }));

            progressDialog.start();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForAddedMods");

        PerformanceManager.start("Instance::scanMissingMods - CheckForRemovedMods");
        // next remove any mods that the no longer exist in the filesystem
        List<DisableableMod> removedMods = instance.launcher.mods.parallelStream().filter(mod -> {
            if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                return false;
            }

            if (mod.disabled) {
                return (mod.getDisabledFile(instance) != null && !mod.getDisabledFile(instance).exists());
            } else {
                return (mod.getFile(instance) != null && !mod.getFile(instance).exists());
            }
        }).collect(Collectors.toList());

        if (removedMods.size() != 0) {
            removedMods.forEach(mod -> LogManager.info("Mod no longer in filesystem: " + mod.file));
            instance.launcher.mods.removeAll(removedMods);
            instance.save();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForRemovedMods");
    }
}
