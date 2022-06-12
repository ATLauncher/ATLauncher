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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.modpacksch.ModpacksChPackList;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackVersionModsManifest;
import com.atlauncher.network.Download;

import okhttp3.CacheControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Various utility methods for interacting with the Modpacks.ch API.
 */
public class ModpacksChApi {
    private static final Logger LOG = LogManager.getLogger(ModpacksChApi.class);

    public static List<ModpacksChPackManifest> searchModPacks(String query, int page) {
        String url = String.format("%s/modpack/search/50", Constants.MODPACKS_CH_API_URL);

        if (query != null && !query.isEmpty()) {
            url += String.format("?term=%s", query);
        }

        ModpacksChPackList packList = Download.build().setUrl(url).asType(ModpacksChPackList.class);

        if (packList.status != null && packList.status.equals("error")) {
            return new ArrayList<>();
        }

        List<Integer> packsToShow = packList.packs.stream().skip((page - 1) * Constants.MODPACKS_CH_PAGINATION_SIZE)
                .limit(Constants.MODPACKS_CH_PAGINATION_SIZE).collect(Collectors.toList());

        List<ModpacksChPackManifest> packs = packsToShow.parallelStream()
                .map(packId -> com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/modpack/%s", Constants.MODPACKS_CH_API_URL, packId))
                        .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                        .asClass(ModpacksChPackManifest.class))
                .filter(p -> p.versions != null).collect(Collectors.toList());

        return packs;
    }

    public static List<ModpacksChPackManifest> getModPacks(int page, String sort) {
        ModpacksChPackList packList = Download.build()
                .setUrl(String.format("%s/modpack/%s/50", Constants.MODPACKS_CH_API_URL, sort))
                .asType(ModpacksChPackList.class);

        if (packList.status != null && packList.status.equals("error")) {
            return new ArrayList<>();
        }

        List<Integer> packsToShow = packList.packs.stream().skip((page - 1) * Constants.MODPACKS_CH_PAGINATION_SIZE)
                .limit(Constants.MODPACKS_CH_PAGINATION_SIZE).collect(Collectors.toList());

        List<ModpacksChPackManifest> packs = packsToShow.parallelStream()
                .map(packId -> com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/modpack/%s", Constants.MODPACKS_CH_API_URL, packId))
                        .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                        .asClass(ModpacksChPackManifest.class))
                .filter(p -> p.versions != null).collect(Collectors.toList());

        return packs;
    }

    public static ModpacksChPackVersionModsManifest getModsManifest(int packId, int versionId) {
        try {
            ModpacksChPackVersionModsManifest modsManifest = Download.build()
                    .setUrl(String.format("%s/modpack/%s/%s/mods", Constants.MODPACKS_CH_API_URL,
                            packId,
                            versionId))
                    .cached(new CacheControl.Builder().maxStale(5, TimeUnit.MINUTES).build())
                    .asClassWithThrow(ModpacksChPackVersionModsManifest.class);

            if (modsManifest.status != null && modsManifest.status.equals("error")) {
                return null;
            }

            return modsManifest;
        } catch (Exception e) {
            LOG.error("Error calling mods endpoint for Modpacks.ch", e);
        }

        return null;
    }
}
