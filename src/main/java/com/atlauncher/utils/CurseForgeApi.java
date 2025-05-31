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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeCategoryForGame;
import com.atlauncher.data.curseforge.CurseForgeCoreApiResponse;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.NetworkClient;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the CurseForge API.
 */
public class CurseForgeApi {
    private final static Headers REQUEST_HEADERS = Headers.of("x-api-key", Constants.CURSEFORGE_CORE_API_KEY);

    public static List<CurseForgeProject> searchCurseForge(int sectionId, String query, int page,
        List<Integer> modLoaderTypes,
        String sort) {
        return searchCurseForge(null, sectionId, query, page, modLoaderTypes, sort);
    }

    public static List<CurseForgeProject> searchCurseForge(int sectionId, String query, int page,
        List<Integer> modLoaderTypes,
        String sort, Integer categoryId) {
        return searchCurseForge(null, sectionId, query, page, modLoaderTypes, sort, true, categoryId);
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
        List<Integer> modLoaderTypes, String sort, Integer categoryId) {
        return searchCurseForge(gameVersion, sectionId, query, page, modLoaderTypes, sort, true, categoryId);
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
        List<Integer> modLoaderTypes, String sort, boolean sortDescending, Integer categoryId) {
        try {
            String url = String.format(Locale.ENGLISH,
                "%s/mods/search?gameId=432&classId=%s&searchFilter=%s&sortField=%s&sortOrder=%s&pageSize=%d&index=%d",
                Constants.CURSEFORGE_CORE_API_URL, sectionId,
                URLEncoder.encode(query, StandardCharsets.UTF_8.name()),
                sort.replace(" ", ""),
                sortDescending ? "desc" : "asc",
                Constants.CURSEFORGE_PAGINATION_SIZE, page * Constants.CURSEFORGE_PAGINATION_SIZE);

            if (modLoaderTypes != null && !modLoaderTypes.isEmpty()) {
                url += "&modLoaderTypes=" + Gsons.DEFAULT.toJson(modLoaderTypes);
            }

            if (gameVersion != null) {
                url += "&gameVersion=" + gameVersion;
            }

            if (categoryId != null) {
                url += "&categoryId=" + categoryId;
            }

            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeProject>>>() {
            }
                .getType();

            CurseForgeCoreApiResponse<List<CurseForgeProject>> response = NetworkClient.getCached(
                url,
                REQUEST_HEADERS,
                type, new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

            if (response == null || response.data.isEmpty()) {
                LogManager.warn("CurseForge API returned no results for " + url);
                Network.removeUrlFromCache(url);
            }

            if (response != null) {
                return response.data;
            }
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
        List<Integer> modLoaderTypes, String sort) {
        return searchCurseForge(gameVersion, sectionId, query, page, modLoaderTypes, sort, null);
    }

    public static List<CurseForgeProject> searchWorlds(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_WORLDS_SECTION_ID, query, page, null, sort,
            categoryIdParam);
    }

    public static List<CurseForgeProject> searchResourcePacks(String query, int page, String sort, String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        return searchCurseForge(Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID, query, page, null, sort,
            categoryIdParam);
    }

    public static List<CurseForgeProject> searchShaderPacks(String query, int page, String sort, String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        return searchCurseForge(Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID, query, page, null, sort,
            categoryIdParam);
    }

    public static List<CurseForgeProject> searchMods(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page, null, sort,
            categoryIdParam);
    }

    public static List<CurseForgeProject> searchPlugins(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        // CurseForge plugins don't seem to update their supported versions often, so we have to just get all
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_PLUGINS_SECTION_ID, query, page, null, sort,
            categoryIdParam);
    }

    public static List<CurseForgeProject> searchModPacks(String query, int page, String sort, boolean sortDescending,
        String categoryId,
        String minecraftVersion) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        return searchCurseForge(minecraftVersion, Constants.CURSEFORGE_MODPACKS_SECTION_ID, query, page, null, sort,
            sortDescending, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForFabric(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);
        List<Integer> modLoaderTypes = Collections.singletonList(Constants.CURSEFORGE_FABRIC_MODLOADER_ID);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForQuilt(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);
        List<Integer> modLoaderTypes = Arrays.asList(Constants.CURSEFORGE_FABRIC_MODLOADER_ID,
            Constants.CURSEFORGE_QUILT_MODLOADER_ID);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForForge(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);
        List<Integer> modLoaderTypes = Collections.singletonList(Constants.CURSEFORGE_FORGE_MODLOADER_ID);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForForgeOrFabric(String gameVersion, String query, int page,
        String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);
        List<Integer> modLoaderTypes = Arrays.asList(Constants.CURSEFORGE_FORGE_MODLOADER_ID,
            Constants.CURSEFORGE_FABRIC_MODLOADER_ID);

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForNeoForge(String gameVersion, String query, int page, String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        List<Integer> modLoaderTypes = new ArrayList<>();
        modLoaderTypes.add(Constants.CURSEFORGE_NEOFORGE_MODLOADER_ID);

        List<String> neoForgeForgeCompatabilityVersions = ConfigManager
            .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<>());
        if (neoForgeForgeCompatabilityVersions.contains(gameVersion)) {
            modLoaderTypes.add(Constants.CURSEFORGE_FORGE_MODLOADER_ID);
        }

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeProject> searchModsForNeoForgeOrFabric(String gameVersion, String query, int page,
        String sort,
        String categoryId) {
        Integer categoryIdParam = Utils.getSafeIntegerFromString(categoryId);

        List<Integer> modLoaderTypes = new ArrayList<>();
        modLoaderTypes.add(Constants.CURSEFORGE_NEOFORGE_MODLOADER_ID);
        modLoaderTypes.add(Constants.CURSEFORGE_FABRIC_MODLOADER_ID);

        List<String> neoForgeForgeCompatabilityVersions = ConfigManager
            .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<>());
        if (neoForgeForgeCompatabilityVersions.contains(gameVersion)) {
            modLoaderTypes.add(Constants.CURSEFORGE_FORGE_MODLOADER_ID);
        }

        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
            modLoaderTypes, sort, categoryIdParam);
    }

    public static List<CurseForgeFile> getFilesForProject(int projectId) {
        String url = String.format(Locale.ENGLISH, "%s/mods/%d/files?pageSize=1000", Constants.CURSEFORGE_CORE_API_URL,
            projectId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeFile>>>() {
        }.getType();

        CurseForgeCoreApiResponse<List<CurseForgeFile>> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (response == null || response.data.isEmpty()) {
            LogManager.warn("CurseForge API returned no files for project " + projectId);
            Network.removeUrlFromCache(url);
        }

        if (response != null) {
            return response.data;
        }

        return null;
    }

    public static CurseForgeFile getFileForProject(int projectId, int fileId) {
        String url = String.format(Locale.ENGLISH, "%s/mods/%d/files/%d", Constants.CURSEFORGE_CORE_API_URL, projectId,
            fileId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeFile>>() {
        }.getType();

        CurseForgeCoreApiResponse<CurseForgeFile> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (response != null) {
            return response.data;
        }

        Network.removeUrlFromCache(url);
        return null;
    }

    public static String getProjectDescription(int projectId) {
        String url = String.format(Locale.ENGLISH, "%s/mods/%d/description?raw=true", Constants.CURSEFORGE_CORE_API_URL,
            projectId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<String>>() {
        }.getType();

        CurseForgeCoreApiResponse<String> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (response != null) {
            return response.data;
        }

        Network.removeUrlFromCache(url);
        return null;
    }

    public static String getChangelogForProjectFile(int projectId, int fileId) {
        String url = String.format("%s/mods/%d/files/%d/changelog", Constants.CURSEFORGE_CORE_API_URL, projectId,
            fileId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<String>>() {
        }.getType();

        CurseForgeCoreApiResponse<String> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (response != null) {
            try {
                Pattern pattern = Pattern.compile("\"/linkout\\?remoteUrl=(.*?)\"");
                Matcher matcher = pattern.matcher(response.data);
                StringBuffer buffer = new StringBuffer();
                while (matcher.find()) {
                    String decoded = URLDecoder.decode(URLDecoder.decode(matcher.group(1), "UTF-8"), "UTF-8");
                    matcher.appendReplacement(buffer, "\"" + Matcher.quoteReplacement(decoded) + "\"");
                }
                matcher.appendTail(buffer);

                return buffer.toString();
            } catch (UnsupportedEncodingException e) {
                return response.data.replaceAll("/(\\/linkout\\?remoteUrl)/g",
                    "https://www.curseforge.com/linkout?remoteUrl");
            }
        }

        Network.removeUrlFromCache(url);
        return null;
    }

    public static CurseForgeProject getProjectById(int projectId) {
        return getProjectById(Integer.toString(projectId));
    }

    public static CurseForgeProject getProjectById(String projectId) {
        String url = String.format(Locale.ENGLISH, "%s/mods/%s", Constants.CURSEFORGE_CORE_API_URL, projectId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeProject>>() {
        }.getType();

        CurseForgeCoreApiResponse<CurseForgeProject> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (response != null) {
            return response.data;
        }

        Network.removeUrlFromCache(url);
        return null;
    }

    public static CurseForgeProject getModBySlug(String slug) {
        return getProjectBySlug(slug, Constants.CURSEFORGE_MODS_SECTION_ID);
    }

    public static CurseForgeProject getModPackBySlug(String slug) {
        return getProjectBySlug(slug, Constants.CURSEFORGE_MODPACKS_SECTION_ID);
    }

    private static CurseForgeProject getProjectBySlug(String slug, int classId) {
        String url = String.format(Locale.ENGLISH, "%s/mods/search?gameId=432&slug=%s&classId=%s",
            Constants.CURSEFORGE_CORE_API_URL,
            slug, classId);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeProject>>>() {
        }.getType();

        CurseForgeCoreApiResponse<List<CurseForgeProject>> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (response == null || response.data.isEmpty()) {
            LogManager.warn("CurseForge API returned no results for " + url);
            Network.removeUrlFromCache(url);
        }

        if (response != null && !response.data.isEmpty()) {
            return response.data.get(0);
        }

        return null;
    }

    public static Map<Integer, CurseForgeProject> getProjectsAsMap(int[] addonIds) {
        if (addonIds == null || addonIds.length == 0) {
            return Collections.emptyMap();
        }

        try {
            List<CurseForgeProject> projects = getProjects(addonIds);

            if (projects != null) {
                return projects.stream().distinct()
                    .collect(Collectors.toMap(p -> p.id, p -> p, (existing, replacement) -> existing));
            }
        } catch (Throwable t) {
            LogManager.logStackTrace("Error trying to get CurseForge projects as map", t);
        }

        return null;
    }

    public static List<CurseForgeProject> getProjects(int[] projectIds) {
        if (projectIds == null || projectIds.length == 0) {
            return Collections.emptyList();
        }

        String url = String.format("%s/mods", Constants.CURSEFORGE_CORE_API_URL);

        Map<String, int[]> body = new HashMap<>();
        body.put("modIds", projectIds);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeProject>>>() {
        }.getType();

        CurseForgeCoreApiResponse<List<CurseForgeProject>> response = NetworkClient.post(
            url,
            REQUEST_HEADERS,
            RequestBody.create(Gsons.DEFAULT.toJson(body),
                MediaType.get("application/json; charset=utf-8")),
            type);

        if (response != null) {
            return response.data;
        }

        return null;
    }

    public static List<CurseForgeFile> getFiles(int[] fileIds) {
        if (fileIds == null || fileIds.length == 0) {
            return Collections.emptyList();
        }

        Map<String, int[]> body = new HashMap<>();
        body.put("fileIds", fileIds);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeFile>>>() {
        }.getType();

        CurseForgeCoreApiResponse<List<CurseForgeFile>> response = NetworkClient.post(
            String.format("%s/mods/files", Constants.CURSEFORGE_CORE_API_URL),
            REQUEST_HEADERS,
            RequestBody.create(Gsons.DEFAULT.toJson(
                    body),
                MediaType.get("application/json; charset=utf-8")),
            type);

        if (response != null) {
            return response.data;
        }

        return null;
    }

    public static CurseForgeFingerprint checkFingerprints(Long[] murmurHashes) {
        String url = String.format("%s/fingerprints/%d", Constants.CURSEFORGE_CORE_API_URL,
            Constants.CURSEFORGE_MINECRAFT_GAME_ID);

        Map<String, Long[]> body = new HashMap<>();
        body.put("fingerprints", murmurHashes);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeFingerprint>>() {
        }.getType();

        CurseForgeCoreApiResponse<CurseForgeFingerprint> response = NetworkClient.post(
            url,
            REQUEST_HEADERS,
            RequestBody.create(Gsons.DEFAULT.toJson(body),
                MediaType.get("application/json; charset=utf-8")),
            type);

        if (response != null) {
            return response.data;
        }

        return null;
    }

    public static List<CurseForgeCategoryForGame> getCategories() {
        String url = String.format("%s/categories?gameId=432", Constants.CURSEFORGE_CORE_API_URL);

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeCategoryForGame>>>() {
        }
            .getType();

        CurseForgeCoreApiResponse<List<CurseForgeCategoryForGame>> response = NetworkClient.getCached(
            url,
            REQUEST_HEADERS,
            type, new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (response == null || response.data.isEmpty()) {
            LogManager.warn("CurseForge API returned no results for " + url);
            Network.removeUrlFromCache(url);
        }

        if (response != null) {
            return response.data;
        }

        return null;
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForModpacks() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream().filter(c -> c.classId != null && c.classId == 4471)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForMods() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream().filter(c -> c.classId != null && c.classId == Constants.CURSEFORGE_MODS_SECTION_ID)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForPlugins() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream()
            .filter(c -> c.classId != null && c.classId == Constants.CURSEFORGE_PLUGINS_SECTION_ID)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForResourcePacks() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream()
            .filter(c -> c.classId != null && c.classId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForShaderPacks() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream()
            .filter(c -> c.classId != null && c.classId == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForWorlds() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        if (categories == null) {
            return new ArrayList<>();
        }

        return categories.stream().filter(c -> c.classId != null && c.classId == Constants.CURSEFORGE_WORLDS_SECTION_ID)
            .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }
}
