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
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.curse.CurseModLatestFile;
import com.atlauncher.utils.CurseApi;

public class CurseForgeUpdateManager {
    public static CurseModLatestFile getLatestVersion(Instance instance) {
        return Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        PerformanceManager.start();
        LogManager.info("Checking for updates to CurseForge instances");
        Data.INSTANCES.parallelStream().filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId()).forEach(i -> {
            CurseMod curseForgeMod = CurseApi
                    .getModById(i.launcher.curseManifest != null ? i.launcher.curseManifest.projectID
                            : i.launcher.curseForgeProject.id);

            CurseModLatestFile latestVersion = curseForgeMod.latestFiles.stream()
                    .sorted(Comparator.comparingInt((CurseModLatestFile file) -> file.id).reversed()).findFirst()
                    .orElse(null);

            Data.CURSEFORGE_INSTANCE_LATEST_VERSION.put(i, latestVersion);
        });

        App.launcher.reloadInstancesPanel();
        PerformanceManager.end();
    }
}
