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
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.data.Instance;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.utils.ModrinthApi;

public class ModrinthModpackUpdateManager {
    public static ModrinthVersion getLatestVersion(Instance instance) {
        return Data.MODRINTH_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.modrinth.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to Modrinth instances");

        boolean refreshInstancesPanel = Data.INSTANCES.parallelStream()
                .filter(i -> i.isModrinthPack()).map(i -> {
                    boolean wasUpdated = false;

                    List<ModrinthVersion> packVersions = ModrinthApi.getVersions(i.launcher.modrinthProject.id);

                    if (packVersions == null) {
                        return false;
                    }

                    ModrinthVersion latestVersion = packVersions.stream()
                            .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed())
                            .findFirst().orElse(null);

                    if (latestVersion == null) {
                        return false;
                    }

                    // if there is a change to the latestversion for an instance (but not a first
                    // time write), then refresh instances panel
                    if (Data.MODRINTH_INSTANCE_LATEST_VERSION.containsKey(i)
                            && !Data.MODRINTH_INSTANCE_LATEST_VERSION.get(i).id.equals(latestVersion.id)) {
                        wasUpdated = true;
                    }

                    // updated if there is no latest version stored yet but the instance has update
                    if (!Data.MODRINTH_INSTANCE_LATEST_VERSION.containsKey(i)
                            && !i.launcher.modrinthVersion.id.equals(latestVersion.id)) {
                        wasUpdated = true;
                    }

                    Data.MODRINTH_INSTANCE_LATEST_VERSION.put(i, latestVersion);

                    return wasUpdated;
                }).anyMatch(b -> b);

        if (refreshInstancesPanel) {
            App.launcher.reloadInstancesPanel();
        }

        PerformanceManager.end();
    }
}
