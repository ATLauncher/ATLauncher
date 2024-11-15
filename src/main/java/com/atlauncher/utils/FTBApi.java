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
import com.atlauncher.data.ftb.FTBPackList;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.data.ftb.FTBPackVersionModsManifest;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;

import okhttp3.CacheControl;

/**
 * Various utility methods for interacting with the FTB API.
 */
public class FTBApi {
    public static List<FTBPackManifest> searchModPacks(String query, int page) {
        String url = String.format("%s/modpack/search/50", Constants.FTB_API_URL);

        if (query != null && !query.isEmpty()) {
            url += String.format("?term=%s", query);
        }

        FTBPackList packList = Download.build().setUrl(url).asType(FTBPackList.class);

        if (packList == null || (packList.status != null && packList.status.equals("error"))) {
            return new ArrayList<>();
        }

        List<Integer> packsToShow = packList.packs.stream().skip((page - 1) * Constants.FTB_PAGINATION_SIZE)
                .limit(Constants.FTB_PAGINATION_SIZE).collect(Collectors.toList());

        List<FTBPackManifest> packs = packsToShow.parallelStream()
                .map(packId -> getModpackManifest(packId))
                .filter(p -> p.versions != null).collect(Collectors.toList());

        return packs;
    }

    public static List<FTBPackManifest> getModPacks(int page, String sort) {
        FTBPackList packList = Download.build()
                .setUrl(String.format("%s/modpack/%s/50", Constants.FTB_API_URL, sort))
                .asType(FTBPackList.class);

        if (packList == null || (packList.status != null && packList.status.equals("error"))) {
            return new ArrayList<>();
        }

        List<Integer> packsToShow = packList.packs.stream().skip((page - 1) * Constants.FTB_PAGINATION_SIZE)
                .limit(Constants.FTB_PAGINATION_SIZE).collect(Collectors.toList());

        List<FTBPackManifest> packs = packsToShow.parallelStream()
                .map(packId -> getModpackManifest(packId))
                .filter(p -> p != null && p.versions != null).collect(Collectors.toList());

        return packs;
    }

    public static FTBPackVersionModsManifest getModsManifest(int packId, int versionId) {
        try {
            FTBPackVersionModsManifest modsManifest = Download.build()
                    .setUrl(String.format("%s/modpack/%s/%s/mods", Constants.FTB_API_URL,
                            packId,
                            versionId))
                    .cached(new CacheControl.Builder().maxStale(5, TimeUnit.MINUTES).build())
                    .asClassWithThrow(FTBPackVersionModsManifest.class);

            if (modsManifest == null || (modsManifest.status != null && modsManifest.status.equals("error"))) {
                return null;
            }

            return modsManifest;
        } catch (Exception e) {
            LogManager.logStackTrace("Error calling mods endpoint for FTB", e);
        }

        return null;
    }

    public static FTBPackManifest getModpackManifest(int packId) {
        return getModpackManifest(Integer.toString(packId));
    }

    public static FTBPackManifest getModpackManifest(String packId) {
        try {
            FTBPackManifest modsManifest = Download.build()
                    .setUrl(String.format("%s/modpack/%s", Constants.FTB_API_URL,
                            packId))
                    .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                    .asClassWithThrow(FTBPackManifest.class);

            if (modsManifest == null || (modsManifest.status != null && modsManifest.status.equals("error"))) {
                return null;
            }

            return modsManifest;
        } catch (Exception e) {
            LogManager.logStackTrace("Error calling modpack endpoint for FTB", e);
        }

        return null;
    }
}
