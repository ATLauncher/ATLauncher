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
package com.atlauncher.managers;

import java.util.Comparator;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.utils.CurseForgeApi;

public class CurseForgeUpdateManager {
    public static CurseForgeFile getLatestVersion(Instance instance) {
        return Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.curseforge.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to CurseForge instances");

        int[] projectIdsFound = Data.INSTANCES.parallelStream()
                .filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId())
                .mapToInt(i -> i.launcher.curseForgeManifest != null
                        ? i.launcher.curseForgeManifest.projectID
                        : i.launcher.curseForgeProject.id)
                .toArray();

        Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi.getProjectsAsMap(projectIdsFound);

        if (foundProjects != null) {

            boolean refreshInstancesPanel = Data.INSTANCES.parallelStream()
                    .filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId()).map(i -> {
                        boolean wasUpdated = false;

                        CurseForgeProject curseForgeMod = foundProjects.get(i.launcher.curseForgeManifest != null
                                ? i.launcher.curseForgeManifest.projectID
                                : i.launcher.curseForgeProject.id);

                        if (curseForgeMod == null) {
                            return false;
                        }

                        CurseForgeFile latestVersion = curseForgeMod.latestFiles.stream()
                                .sorted(Comparator.comparingInt((
                                        CurseForgeFile file) -> file.id).reversed())
                                .filter(f -> {
                                    if (i.launcher.curseForgeFile != null) {
                                        if (i.launcher.curseForgeFile.isReleaseType()) {
                                            return f.isReleaseType();
                                        }

                                        if (i.launcher.curseForgeFile.isBetaType()) {
                                            return f.isReleaseType() || f.isBetaType();
                                        }
                                    }

                                    return true;
                                })
                                .findFirst().orElse(null);
                        System.out.println(latestVersion);

                        if (latestVersion == null) {
                            return false;
                        }

                        // if there is a change to the latestversion for an instance (but not a first
                        // time write), then refresh instances panel
                        if (Data.CURSEFORGE_INSTANCE_LATEST_VERSION.containsKey(i)
                                && Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(i).id != latestVersion.id) {
                            wasUpdated = true;
                        }

                        // updated if there is no latest version stored yet but the instance has update
                        if (!Data.CURSEFORGE_INSTANCE_LATEST_VERSION.containsKey(i)
                                && latestVersion.id != i.launcher.curseForgeFile.id) {
                            wasUpdated = true;
                        }

                        Data.CURSEFORGE_INSTANCE_LATEST_VERSION.put(i, latestVersion);

                        return wasUpdated;
                    }).anyMatch(b -> b);

            if (refreshInstancesPanel) {
                App.launcher.reloadInstancesPanel();
            }
        }

        PerformanceManager.end();
    }
}
