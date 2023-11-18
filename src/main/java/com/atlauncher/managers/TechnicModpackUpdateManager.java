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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.atlauncher.Gsons;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSolderModpack;
import com.atlauncher.network.DownloadException;
import com.atlauncher.utils.TechnicApi;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class TechnicModpackUpdateManager {

    /**
     * Technic Non Solder instance update checking
     */
    private static final Map<UUID, BehaviorSubject<Optional<TechnicModpack>>>
        TECHNIC_INSTANCE_LATEST_VERSION = new ConcurrentHashMap<>();

    /**
     * Technic Solder instance update checking
     */
    private static final Map<UUID, BehaviorSubject<Optional<TechnicSolderModpack>>>
        TECHNIC_SOLDER_INSTANCE_LATEST_VERSION = new ConcurrentHashMap<>();

    /**
     * Get the update behavior subject for a given instance.
     *
     * @param instance Instance to get behavior subject for
     * @return behavior subject for said instance updates.
     */
    private static BehaviorSubject<Optional<TechnicModpack>> getSubject(UUID instance){
        TECHNIC_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance,
            BehaviorSubject.createDefault(Optional.empty())
        );
        return TECHNIC_INSTANCE_LATEST_VERSION.get(instance);
    }

    /**
     * Get the solder update behavior subject for a given instance.
     *
     * @param instance Instance to get behavior subject for
     * @return solder behavior subject for said instance updates.
     */
    private static BehaviorSubject<Optional<TechnicSolderModpack>> getSolderSubject(UUID instance){
        TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance,
            BehaviorSubject.createDefault(Optional.empty())
        );
        return TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.get(instance);
    }

    /**
     * Get an observable for an instances update.
     * <p>
     * Please do not cast to a behavior subject.
     * @param instance Instance to get an observable for
     * @return Update observable
     */
    public static Observable<Optional<TechnicModpack>> getObservable(UUID instance) {
        return getSubject(instance);
    }

    /**
     * Get an observable for a solder instances update.
     * <p>
     * Please do not cast to a behavior subject.
     * @param instance Instance to get an observable for
     * @return Solder update observable
     */
    public static Observable<Optional<TechnicSolderModpack>> getSolderObservable(UUID instance) {
        return getSolderSubject(instance);
    }

    /**
     * Get the latest version of an instance
     * @param instance Instance to get version of
     * @return Latest version, or null if no newer version is found
     */
    public static TechnicModpack getUpToDateModpack(UUID instance) {
        return getSubject(instance).getValue().orElse(null);
    }

    /**
     * Get the latest version of a solder instance
     * @param instance Instance to get version of
     * @return Latest solder version, or null if no newer version is found
     */
    public static TechnicSolderModpack getUpToDateSolderModpack(UUID instance) {
        return getSolderSubject(instance).getValue().orElse(null);
    }

    /**
     * Check for new updates.
     * <p>
     * Updates observables.
     */
    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to Technic Modpack instances");

        InstanceManager.getInstances().parallelStream()
                .filter(i -> i.isTechnicPack() && i.launcher.checkForUpdates).forEach(i -> {
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

                    if (technicModpack != null & i.isTechnicSolderPack()) {
                        TechnicSolderModpack technicSolderModpack = TechnicApi.getSolderModpackBySlug(
                                technicModpack.solder,
                                technicModpack.name);

                        getSolderSubject(i.getUUID()).onNext(Optional.ofNullable(technicSolderModpack));
                    } else {
                        getSubject(i.getUUID()).onNext(Optional.ofNullable(technicModpack));
                    }
                });

        PerformanceManager.end();
    }
}
