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
import java.util.concurrent.TimeUnit;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;

import okhttp3.CacheControl;

public class ModpacksChUpdateManager {
    public static ModpacksChPackVersion getLatestVersion(Instance instance) {
        return Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        PerformanceManager.start();
        LogManager.info("Checking for updates to modpacks.ch instances");

        boolean refreshInstancesPanel = Data.INSTANCES.parallelStream().filter(
                i -> i.launcher.modpacksChPackManifest != null && i.launcher.modpacksChPackVersionManifest != null)
                .map(i -> {
                    boolean wasUpdated = false;

                    ModpacksChPackManifest packManifest = com.atlauncher.network.Download.build()
                            .setUrl(String.format("%s/modpack/%d", Constants.MODPACKS_CH_API_URL,
                                    i.launcher.modpacksChPackManifest.id))
                            .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                            .asClass(ModpacksChPackManifest.class);

                    if (packManifest == null) {
                        return false;
                    }

                    ModpacksChPackVersion latestVersion = packManifest.versions.stream().sorted(
                            Comparator.comparingInt((ModpacksChPackVersion version) -> version.updated).reversed())
                            .findFirst().orElse(null);

                    if (latestVersion == null) {
                        return false;
                    }

                    // if there is a change to the latestversion for an instance (but not a first
                    // time write), then refresh instances panel
                    if (Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.containsKey(i)
                            && Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.get(i).id != latestVersion.id) {
                        wasUpdated = true;
                    }

                    Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.put(i, latestVersion);

                    return wasUpdated;
                }).anyMatch(b -> b);

        if (refreshInstancesPanel) {
            App.launcher.reloadInstancesPanel();
        }

        PerformanceManager.end();
    }
}
