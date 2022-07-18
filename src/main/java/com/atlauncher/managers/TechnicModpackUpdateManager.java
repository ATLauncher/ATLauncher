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

import java.io.IOException;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.Gsons;
import com.atlauncher.data.Instance;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSolderModpack;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.TechnicApi;

public class TechnicModpackUpdateManager {
    public static TechnicModpack getUpToDateModpack(Instance instance) {
        return Data.TECHNIC_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static TechnicSolderModpack getUpToDateSolderModpack(Instance instance) {
        return Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.get(instance);
    }

    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to Technic Modpack instances");

        boolean refreshInstancesPanel = Data.INSTANCES.parallelStream()
                .filter(i -> i.isTechnicPack() && i.launcher.checkForUpdates).map(i -> {
                    boolean wasUpdated = false;

                    TechnicModpack technicModpack = null;

                    try {
                        technicModpack = TechnicApi.getModpackBySlugWithThrow(i.launcher.technicModpack.name);
                    } catch (DownloadException e) {
                        if (e.response != null) {
                            LogManager.debug(Gsons.DEFAULT.toJson(e.response));

                            if (e.statusCode == 404) {
                                LogManager.error(String.format(
                                        "Technic pack with name of %s no longer exists, disabling update checks.",
                                        i.launcher.technicModpack.displayName));
                                i.launcher.checkForUpdates = false;
                                i.save();
                            }
                        }
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }

                    if (technicModpack == null) {
                        return false;
                    }

                    if (i.isTechnicSolderPack()) {
                        TechnicSolderModpack technicSolderModpack = TechnicApi.getSolderModpackBySlug(
                                technicModpack.solder,
                                technicModpack.name);

                        if (technicSolderModpack == null) {
                            return false;
                        }

                        // if there is a change to the latest key for an instance (but not a first time
                        // write), then refresh instances panel
                        if (Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.containsKey(i)
                                && !Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.get(i).latest
                                        .equals(technicSolderModpack.latest)) {
                            wasUpdated = true;
                        }

                        // updated if there is no latest version stored yet but the instance has update
                        if (!Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.containsKey(i)
                                && !technicSolderModpack.latest.equals(i.launcher.version)) {
                            wasUpdated = true;
                        }

                        Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.put(i, technicSolderModpack);
                    } else {
                        // if there is a change to the latest key for an instance (but not a first time
                        // write), then refresh instances panel
                        if (Data.TECHNIC_INSTANCE_LATEST_VERSION.containsKey(i)
                                && !Data.TECHNIC_INSTANCE_LATEST_VERSION.get(i).version
                                        .equals(technicModpack.version)) {
                            wasUpdated = true;
                        }

                        // updated if there is no latest version stored yet but the instance has update
                        if (!Data.TECHNIC_INSTANCE_LATEST_VERSION.containsKey(i)
                                && !technicModpack.version.equals(i.launcher.version)) {
                            wasUpdated = true;
                        }

                        Data.TECHNIC_INSTANCE_LATEST_VERSION.put(i, technicModpack);

                        wasUpdated = !technicModpack.version.equalsIgnoreCase(i.launcher.technicModpack.version);
                    }

                    return wasUpdated;
                }).anyMatch(b -> b);

        if (refreshInstancesPanel) {
            App.launcher.reloadInstancesPanel();
        }

        PerformanceManager.end();
    }
}
