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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthCategory;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthProjectType;
import com.atlauncher.data.modrinth.ModrinthSearchResult;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.network.DownloadException;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the Modrinth API.
 */
public class ModrinthApi {
    private static Download getDownloadBuilder() {
        Download downloadBuilder = Download.build();

        if (App.settings.modrinthApiKey != null && !App.settings.modrinthApiKey.isEmpty()) {
            downloadBuilder.header("Authorization", App.settings.modrinthApiKey);
        }

        return downloadBuilder;
    }

    public static ModrinthSearchResult searchModrinth(List<String> gameVersions, String query, int page, String index,
            List<List<String>> categories, ModrinthProjectType projectType) {
        try {
            List<List<String>> facets = new ArrayList<>();

            String url = String.format(Locale.ENGLISH, "%s/search?limit=%d&offset=%d&query=%s&index=%s",
                    Constants.MODRINTH_API_URL,
                    Constants.MODRINTH_PAGINATION_SIZE, page * Constants.MODRINTH_PAGINATION_SIZE,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), index);

            if (gameVersions != null && gameVersions.size() != 0) {
                facets.add(
                        gameVersions.stream().map(gv -> String.format("versions:%s", gv)).collect(Collectors.toList()));
            }

            if (categories != null) {
                categories.forEach(c -> {
                    facets
                            .add(c.stream().map(s -> String.format("categories:%s", s)).collect(Collectors.toList()));
                });
            }

            if (projectType != null) {
                List<String> projectTypeFacets = new ArrayList<>();
                projectTypeFacets
                        .add(String.format("project_type:%s", projectType.toString().toLowerCase(Locale.ENGLISH)));
                facets.add(projectTypeFacets);
            }

            if (facets.size() != 0) {
                url += String.format("&facets=%s", Gsons.DEFAULT_SLIM.toJson(facets));
            }

            return getDownloadBuilder().cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .setUrl(url).asClass(ModrinthSearchResult.class);
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static ModrinthSearchResult searchResourcePacks(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? null : Arrays.asList(Arrays.asList(category));

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.RESOURCEPACK);
    }

    public static ModrinthSearchResult searchShaders(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? null : Arrays.asList(Arrays.asList(category));

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.SHADER);
    }

    public static ModrinthSearchResult searchModsForForge(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? Arrays.asList(Arrays.asList("forge"))
                : Arrays.asList(Arrays.asList(category), Arrays.asList("forge"));

        return searchModrinth(gameVersions, query, page, sort, categories,
                ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForForgeOrFabric(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? Arrays.asList(Arrays.asList("forge", "fabric"))
                : Arrays.asList(Arrays.asList(category), Arrays.asList("forge", "fabric"));

        return searchModrinth(gameVersions, query, page, sort, categories,
                ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForNeoForge(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = new ArrayList<>();

        if (category != null) {
            categories.add(Arrays.asList(category));
        }

        List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<String>());
        if (gameVersions.stream().anyMatch(gv -> neoForgeForgeCompatabilityVersions.contains(gv))) {
            categories.add(Arrays.asList("neoforge", "forge"));
        } else {
            categories.add(Arrays.asList("neoforge"));
        }

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForNeoForgeOrFabric(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = new ArrayList<>();

        if (category != null) {
            categories.add(Arrays.asList(category));
        }

        List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<String>());
        if (gameVersions.stream().anyMatch(gv -> neoForgeForgeCompatabilityVersions.contains(gv))) {
            categories.add(Arrays.asList("neoforge", "forge", "fabric"));
        } else {
            categories.add(Arrays.asList("neoforge", "fabric"));
        }

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForFabric(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? Arrays.asList(Arrays.asList("fabric"))
                : Arrays.asList(Arrays.asList(category), Arrays.asList("fabric"));

        return searchModrinth(gameVersions, query, page, sort, categories,
                ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForQuilt(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? Arrays.asList(Arrays.asList("quilt"))
                : Arrays.asList(Arrays.asList(category), Arrays.asList("quilt"));

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModsForQuiltOrFabric(List<String> gameVersions, String query, int page,
            String sort, String category) {
        List<List<String>> categories = category == null ? Arrays.asList(Arrays.asList("quilt", "fabric"))
                : Arrays.asList(Arrays.asList(category), Arrays.asList("quilt", "fabric"));

        return searchModrinth(gameVersions, query, page, sort, categories, ModrinthProjectType.MOD);
    }

    public static ModrinthSearchResult searchModPacks(String minecraftVersion, String query, int page, String sort,
            String category) {
        List<List<String>> categories = category == null ? null : Arrays.asList(Arrays.asList(category));

        return searchModrinth(minecraftVersion == null ? null : Arrays.asList(minecraftVersion), query, page, sort,
                categories, ModrinthProjectType.MODPACK);
    }

    public static ModrinthProject getProject(String projectId) {
        return getDownloadBuilder()
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

            List<String> loaders = new ArrayList<>();

            if (loaderVersion.isForge()) {
                loaders.add("forge");
            } else if (loaderVersion.isNeoForge()) {
                List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                        .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<String>());
                if (neoForgeForgeCompatabilityVersions.contains(minecraftVersion)) {
                    loaders.add("forge");
                }

                loaders.add("neoforge");
            } else if (loaderVersion.isFabric()) {
                loaders.add("fabric");
            } else if (loaderVersion.isQuilt()) {
                loaders.add("fabric");
                loaders.add("quilt");
            }

            queryParamsString += String.format("loaders=%s", Gsons.DEFAULT_SLIM.toJson(loaders));
        }

        return getDownloadBuilder()
                .setUrl(String.format("%s/project/%s/version%s", Constants.MODRINTH_API_URL, projectId,
                        queryParamsString))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }

    public static List<ModrinthCategory> getCategories() {
        java.lang.reflect.Type type = new TypeToken<List<ModrinthCategory>>() {
        }.getType();

        return getDownloadBuilder().setUrl(String.format("%s/tag/category", Constants.MODRINTH_API_URL))
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

    public static List<ModrinthCategory> getCategoriesForShaders() {
        List<ModrinthCategory> categories = getCategories();

        return categories.stream().filter(c -> c.projectType == ModrinthProjectType.SHADER)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<ModrinthCategory> getCategoriesForResourcePacks() {
        List<ModrinthCategory> categories = getCategories();

        return categories.stream().filter(c -> c.projectType == ModrinthProjectType.RESOURCEPACK)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static ModrinthVersion getVersionFromSha1Hash(String hash) {
        return getVersionFromHash(hash, "sha1");
    }

    public static ModrinthVersion getVersionFromSha512Hash(String hash) {
        return getVersionFromHash(hash, "sha512");
    }

    private static ModrinthVersion getVersionFromHash(String hash, String algorithm) {
        try {
            return getDownloadBuilder()
                    .setUrl(String.format("%s/version_file/%s?algorithm=%s", Constants.MODRINTH_API_URL, hash,
                            algorithm))
                    .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .asTypeWithThrow(ModrinthVersion.class);
        } catch (DownloadException e) {
            // 404 is fine from this endpoint, so anything else, log it
            if (e.statusCode != 404) {
                LogManager.logStackTrace(e);
            }

            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public static Map<String, ModrinthVersion> getVersionsFromSha1Hashes(String[] hashes) {
        return getVersionsFromHashes(hashes, "sha1");
    }

    public static Map<String, ModrinthVersion> getVersionsFromSha512Hashes(String[] hashes) {
        return getVersionsFromHashes(hashes, "sha512");
    }

    private static Map<String, ModrinthVersion> getVersionsFromHashes(String[] hashes, String algorithm) {
        if (hashes.length == 0) {
            return new HashMap<>();
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("hashes", hashes);
            body.put("algorithm", algorithm);

            java.lang.reflect.Type type = new TypeToken<Map<String, ModrinthVersion>>() {
            }.getType();

            return getDownloadBuilder()
                    .setUrl(String.format("%s/version_files", Constants.MODRINTH_API_URL))
                    .post(RequestBody.create(Gsons.DEFAULT_SLIM.toJson(body),
                            MediaType.get("application/json; charset=utf-8")))
                    .asTypeWithThrow(type);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public static List<ModrinthProject> getProjects(String[] projectIds) {
        java.lang.reflect.Type type = new TypeToken<List<ModrinthProject>>() {
        }.getType();

        return getDownloadBuilder()
                .setUrl(String.format("%s/projects?ids=%s", Constants.MODRINTH_API_URL,
                        Gsons.DEFAULT_SLIM.toJson(projectIds)))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asType(type);
    }

    public static Map<String, ModrinthProject> getProjectsAsMap(String[] projectIds) {
        try {
            List<ModrinthProject> projects = getProjects(projectIds);

            if (projects != null) {
                return projects.stream().distinct()
                        .collect(Collectors.toMap(p -> p.id, p -> p, (existing, replacement) -> existing));
            }
        } catch (Throwable t) {
            LogManager.logStackTrace("Error trying to get Modrinth projects as map", t);
        }

        return null;
    }
}
