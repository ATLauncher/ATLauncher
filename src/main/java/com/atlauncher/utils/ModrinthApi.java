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
package com.atlauncher.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.modrinth.ModrinthMod;
import com.atlauncher.data.modrinth.ModrinthSearchResult;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;

/**
 * Various utility methods for interacting with the Modrinth API.
 */
public class ModrinthApi {
    public static ModrinthSearchResult searchModrinth(String query, int page, String sort) {
        return searchModrinth(null, query, page, sort, null);
    }

    public static ModrinthSearchResult searchModrinth(List<String> gameVersions, String query, int page, String index,
            String category) {
        try {
            List<List<String>> facets = new ArrayList<>();

            String url = String.format("%s/mod?limit=%d&offset=%d&query=%s&index=%s", Constants.MODRINTH_API_URL,
                    Constants.MODRINTH_PAGINATION_SIZE, page * Constants.MODRINTH_PAGINATION_SIZE,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), index);

            if (gameVersions != null) {
                facets.add(
                        gameVersions.stream().map(gv -> String.format("versions:%s", gv)).collect(Collectors.toList()));
            }

            if (category != null) {
                List<String> categoryFacets = new ArrayList<>();
                categoryFacets.add(String.format("categories:%s", category));
                facets.add(categoryFacets);
            }

            if (facets.size() != 0) {
                url += String.format("&facets=%s", Gsons.DEFAULT.toJson(facets));
            }

            return Download.build().cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .setUrl(url).asClass(ModrinthSearchResult.class);
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static ModrinthSearchResult searchModsForForge(List<String> gameVersions, String query, int page,
            String sort) {
        return searchModrinth(gameVersions, query, page, sort, "forge");
    }

    public static ModrinthSearchResult searchModsForFabric(List<String> gameVersions, String query, int page,
            String sort) {
        return searchModrinth(gameVersions, query, page, sort, "fabric");
    }

    public static ModrinthMod getMod(String modId) {
        return Download.build()
                .setUrl(String.format("%s/mod/%s", Constants.MODRINTH_API_URL, modId.replace("local-", "")))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asClass(ModrinthMod.class);
    }

    public static List<ModrinthVersion> getVersions(List<String> versions) {
        java.lang.reflect.Type type = new TypeToken<List<ModrinthVersion>>() {
        }.getType();

        return Download.build()
                .setUrl(String.format("%s/versions?ids=%s", Constants.MODRINTH_API_URL, Gsons.DEFAULT.toJson(versions)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }
}
