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

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSearchResults;
import com.atlauncher.data.technic.TechnicSolderModpack;
import com.atlauncher.data.technic.TechnicSolderModpackManifest;
import com.atlauncher.network.Download;

import okhttp3.CacheControl;

/**
 * Various utility methods for interacting with the CurseForge API.
 */
public class TechnicApi {
    public static TechnicSearchResults getTrendingModpacks() {
        return Download.build()
                .setUrl(String.format("%s/trending?build=%s", Constants.TECHNIC_API_URL,
                        Constants.LAUNCHER_NAME.toLowerCase(Locale.ENGLISH)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(TechnicSearchResults.class);
    }

    public static TechnicSearchResults searchModpacks(String query) {
        return Download.build()
                .setUrl(String.format("%s/search?q=%s&build=%s", Constants.TECHNIC_API_URL, query,
                        Constants.LAUNCHER_NAME.toLowerCase(Locale.ENGLISH)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(TechnicSearchResults.class);
    }

    public static TechnicModpack getModpackBySlug(String slug) {
        return Download.build()
                .setUrl(String.format("%s/modpack/%s?build=%s", Constants.TECHNIC_API_URL, slug,
                        Constants.LAUNCHER_NAME.toLowerCase(Locale.ENGLISH)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(TechnicModpack.class);
    }

    public static TechnicModpack getModpackBySlugWithThrow(String slug) throws IOException {
        return Download.build()
                .setUrl(String.format("%s/modpack/%s?build=%s", Constants.TECHNIC_API_URL, slug,
                        Constants.LAUNCHER_NAME.toLowerCase(Locale.ENGLISH)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClassWithThrow(TechnicModpack.class);
    }

    private static Object normalizeSolderUrl(String solderUrl) {
        if (solderUrl.endsWith("/")) {
            return solderUrl.substring(0, solderUrl.length() - 1);
        }

        return solderUrl;
    }

    public static TechnicSolderModpack getSolderModpackBySlug(String solderUrl, String slug) {
        return Download.build().setUrl(String.format("%s/modpack/%s", normalizeSolderUrl(solderUrl), slug))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(TechnicSolderModpack.class);
    }

    public static TechnicSolderModpackManifest getSolderModpackManifest(String solderUrl, String slug, String build) {
        return Download.build().setUrl(String.format("%s/modpack/%s/%s", normalizeSolderUrl(solderUrl), slug, build))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(TechnicSolderModpackManifest.class);
    }
}
