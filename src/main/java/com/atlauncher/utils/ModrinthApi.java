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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthCategory;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthProjectType;
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
    public static ModrinthSearchResult searchModrinth(List<String> gameVersions, String query, int page, String index,
            String category, ModrinthProjectType projectType) {
        try {
            List<List<String>> facets = new ArrayList<>();

            String url = String.format("%s/search?limit=%d&offset=%d&query=%s&index=%s", Constants.MODRINTH_API_URL,
                    Constants.MODRINTH_PAGINATION_SIZE, page * Constants.MODRINTH_PAGINATION_SIZE,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), index);

            if (gameVersions != null && gameVersions.size() != 0) {
                facets.add(
                        gameVersions.stream().map(gv -> String.format("versions:%s", gv)).collect(Collectors.toList()));
            }

            if (category != null) {
                List<String> categoryFacets = new ArrayList<>();
                categoryFacets.add(String.format("categories:%s", category));
                facets.add(categoryFacets);
            }

            if (projectType != null) {
                List<String> projectTypeFacets = new ArrayList<>();
                projectTypeFacets.add(String.format("project_type:%s", projectType.toString().toLowerCase()));
                facets.add(projectTypeFacets);
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
        return searchModrinth(gameVersions, query, page, sort, "forge", ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForFabric(List<String> gameVersions, String query, int page,
            String sort) {
        return searchModrinth(gameVersions, query, page, sort, "fabric", ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForQuilt(List<String> gameVersions, String query, int page,
            String sort) {
        return searchModrinth(gameVersions, query, page, sort, "quilt", ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModPacks(String minecraftVersion, String query, int page, String sort,
            String category) {
        return searchModrinth(minecraftVersion == null ? null : Arrays.asList(minecraftVersion), query, page, sort,
                category, ModrinthProjectType.MODPACK);
    }

    public static ModrinthProject getProject(String projectId) {
        return Download.build()
                .setUrl(String.format("%s/project/%s", Constants.MODRINTH_API_URL, projectId.replace("local-", "")))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(ModrinthProject.class);
    }

    public static List<ModrinthVersion> getVersions(String projectId) {
        return getVersions(projectId, null, null);
    }

    public static List<ModrinthVersion> getVersions(String projectId, String minecraftVersion,
            LoaderVersion loaderVersion) {
        java.lang.reflect.Type type = new TypeToken<List<ModrinthVersion>>() {
        }.getType();

        String queryParamsString = "";

        if (minecraftVersion != null) {
            if (queryParamsString.length() == 0) {
                queryParamsString += "?";
            }

            queryParamsString += String.format("game_versions=[\"%s\"]", minecraftVersion);
        }

        if (loaderVersion != null) {
            if (queryParamsString.length() == 0) {
                queryParamsString += "?";
            } else {
                queryParamsString += "&";
            }

            queryParamsString += String.format("loaders=[\"%s\"]", loaderVersion.isFabric() ? "fabric" : "forge");
        }

        return Download.build()
                .setUrl(String.format("%s/project/%s/version%s", Constants.MODRINTH_API_URL, projectId,
                        queryParamsString))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }

    public static List<ModrinthCategory> getCategories() {
        java.lang.reflect.Type type = new TypeToken<List<ModrinthCategory>>() {
        }.getType();

        return Download.build().setUrl(String.format("%s/tag/category", Constants.MODRINTH_API_URL))
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build()).asType(type);
    }

    public static List<ModrinthCategory> getCategoriesForModpacks() {
        List<ModrinthCategory> categories = getCategories();

        return categories.stream().filter(c -> c.projectType == ModrinthProjectType.MODPACK)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<ModrinthCategory> getCategoriesForMods() {
        List<ModrinthCategory> categories = getCategories();

        return categories.stream().filter(c -> c.projectType == ModrinthProjectType.MOD)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }
}
