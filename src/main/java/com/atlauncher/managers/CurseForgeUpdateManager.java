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
package com.atlauncher.managers;

import java.util.Comparator;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.CurseForgeProjectLatestFile;
import com.atlauncher.utils.CurseForgeApi;

public class CurseForgeUpdateManager {
    public static CurseForgeProjectLatestFile getLatestVersion(Instance instance) {
        return Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        PerformanceManager.start();
        LogManager.info("Checking for updates to CurseForge instances");

        boolean refreshInstancesPanel = Data.INSTANCES.parallelStream()
                .filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId()).map(i -> {
                    boolean wasUpdated = false;

                    CurseForgeProject curseForgeMod = CurseForgeApi.getProjectById(
                            i.launcher.curseForgeManifest != null ? i.launcher.curseForgeManifest.projectID
                                    : i.launcher.curseForgeProject.id);

                    if (curseForgeMod == null) {
                        return false;
                    }

                    CurseForgeProjectLatestFile latestVersion = curseForgeMod.latestFiles.stream()
                            .sorted(Comparator.comparingInt((CurseForgeProjectLatestFile file) -> file.id).reversed())
                            .findFirst().orElse(null);

                    if (latestVersion == null) {
                        return false;
                    }

                    // if there is a change to the latestversion for an instance (but not a first
                    // time write), then refresh instances panel
                    if (Data.CURSEFORGE_INSTANCE_LATEST_VERSION.containsKey(i)
                            && Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(i).id != latestVersion.id) {
                        wasUpdated = true;
                    }

                    Data.CURSEFORGE_INSTANCE_LATEST_VERSION.put(i, latestVersion);

                    return wasUpdated;
                }).anyMatch(b -> b);

        if (refreshInstancesPanel) {
            App.launcher.reloadInstancesPanel();
        }

        PerformanceManager.end();
    }
}
