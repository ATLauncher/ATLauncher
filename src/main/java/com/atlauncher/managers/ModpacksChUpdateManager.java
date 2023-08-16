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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import okhttp3.CacheControl;

public class ModpacksChUpdateManager {
    /**
     * Modpacks.ch instance update checking
     */
    private static final Map<UUID, BehaviorSubject<Optional<ModpacksChPackVersion>>>
        MODPACKS_CH_INSTANCE_LATEST_VERSION = new HashMap<>();

    /**
     * Get the update behavior subject for a given instance.
     *
     * @param instance Instance to get behavior subject for
     * @return behavior subject for said instance updates.
     */
    private static BehaviorSubject<Optional<ModpacksChPackVersion>> getSubject(Instance instance){
        MODPACKS_CH_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance.getUUID(),
            BehaviorSubject.createDefault(Optional.empty())
        );
        return MODPACKS_CH_INSTANCE_LATEST_VERSION.get(instance.getUUID());
    }

    /**
     * Get an observable for an instances update.
     * <p>
     * Please do not cast to a behavior subject.
     * @param instance Instance to get an observable for
     * @return Update observable
     */
    public static Observable<Optional<ModpacksChPackVersion>> getObservable(Instance instance) {
        return getSubject(instance);
    }


    /**
     * Get the latest version of an instance
     * @param instance Instance to get version of
     * @return Latest version, or null if no newer version is found
     */
    public static ModpacksChPackVersion getLatestVersion(Instance instance) {
        return getSubject(instance).getValue().orElse(null);
    }

    /**
     * Check for new updates.
     * <p>
     * Updates observables.
     */
    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.modpacksch.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to modpacks.ch instances");

        InstanceManager.getInstances().parallelStream().filter(
                i -> i.launcher.modpacksChPackManifest != null && i.launcher.modpacksChPackVersionManifest != null)
                .forEach(i -> {
                    ModpacksChPackManifest packManifest = com.atlauncher.network.Download.build()
                            .setUrl(String.format("%s/modpack/%d", Constants.MODPACKS_CH_API_URL,
                                    i.launcher.modpacksChPackManifest.id))
                            .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                            .asClass(ModpacksChPackManifest.class);

                    if (packManifest == null) {
                        return;
                    }

                    ModpacksChPackVersion latestVersion = packManifest.versions.stream().sorted(
                            Comparator.comparingInt((ModpacksChPackVersion version) -> version.id).reversed())
                            .findFirst().orElse(null);

                    getSubject(i).onNext(Optional.ofNullable(latestVersion));
                });

        PerformanceManager.end();
    }
}
