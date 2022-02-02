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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeCategoryForGame;
import com.atlauncher.data.curseforge.CurseForgeCoreApiResponse;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the CurseForge API.
 */
public class CurseForgeApi {
    public static List<CurseForgeProject> searchCurseForge(int sectionId, String query, int page, int modLoaderType,
            String sort) {
        return searchCurseForge(null, sectionId, query, page, modLoaderType, sort);
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
            int modLoaderType, String sort, Integer categoryId) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        try {
            if (usingCoreApi) {
                url = String.format(
                        "%s/mods/search?gameId=432&modLoaderType=%d&classId=%s&searchFilter=%s&sortField=%s&sortOrder=desc&pageSize=%d&index=%d",
                        Constants.CURSEFORGE_CORE_API_URL, modLoaderType, sectionId,
                        URLEncoder.encode(query, StandardCharsets.UTF_8.name()),
                        sort.replace(" ", ""),
                        Constants.CURSEFORGE_PAGINATION_SIZE, page * Constants.CURSEFORGE_PAGINATION_SIZE);
            } else {
                url = String.format(
                        "%s/addon/search?gameId=432&modLoaderType=%d&sectionId=%s&searchFilter=%s&sort=%s&sortDescending=true&pageSize=%d&index=%d",
                        Constants.CURSEFORGE_API_URL, modLoaderType, sectionId,
                        URLEncoder.encode(query, StandardCharsets.UTF_8.name()), sort.replace(" ", ""),
                        Constants.CURSEFORGE_PAGINATION_SIZE, page * Constants.CURSEFORGE_PAGINATION_SIZE);
            }

            if (gameVersion != null) {
                url += "&gameVersion=" + gameVersion;
            }

            if (categoryId != null) {
                url += "&categoryId=" + categoryId;
            }

            Download download = Download.build()
                    .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .setUrl(url);

            if (usingCoreApi) {
                java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeProject>>>() {
                }.getType();

                CurseForgeCoreApiResponse<List<CurseForgeProject>> response = download.asType(type);

                return response.data;
            } else {
                java.lang.reflect.Type type = new TypeToken<List<CurseForgeProject>>() {
                }.getType();

                return download.asType(type);
            }
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
            int modLoaderType, String sort) {
        return searchCurseForge(gameVersion, sectionId, query, page, modLoaderType, sort, null);
    }

    public static List<CurseForgeProject> searchWorlds(String gameVersion, String query, int page, String sort) {
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_WORLDS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseForgeProject> searchResourcePacks(String query, int page, String sort) {
        return searchCurseForge(Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseForgeProject> searchMods(String gameVersion, String query, int page, String sort) {
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseForgeProject> searchModPacks(String query, int page, String sort, String categoryId) {
        Integer categoryIdParam = categoryId == null ? null : Integer.parseInt(categoryId);

        return searchCurseForge(null, Constants.CURSEFORGE_MODPACKS_SECTION_ID, query, page, 0, sort,
                categoryIdParam);
    }

    public static List<CurseForgeProject> searchModPacks(String query, int page, String sort) {
        return searchCurseForge(Constants.CURSEFORGE_MODPACKS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseForgeProject> searchModsForFabric(String gameVersion, String query, int page, String sort) {
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
                Constants.CURSEFORGE_FABRIC_MODLOADER_ID, sort);
    }

    public static List<CurseForgeProject> searchModsForForge(String gameVersion, String query, int page, String sort) {
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
                Constants.CURSEFORGE_FORGE_MODLOADER_ID, sort);
    }

    public static List<CurseForgeFile> getFilesForProject(int projectId) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        if (usingCoreApi) {
            url = String.format("%s/mods/%d/files?pageSize=1000", Constants.CURSEFORGE_CORE_API_URL, projectId);
        } else {
            url = String.format("%s/addon/%d/files", Constants.CURSEFORGE_API_URL, projectId);
        }

        Download download = Download.build().setUrl(url)
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeFile>>>() {
            }.getType();

            CurseForgeCoreApiResponse<List<CurseForgeFile>> response = download.asType(type);

            return response.data;
        } else {
            java.lang.reflect.Type type = new TypeToken<List<CurseForgeFile>>() {
            }.getType();

            return download.asType(type);
        }
    }

    public static CurseForgeFile getFileForProject(int projectId, int fileId) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        if (usingCoreApi) {
            url = String.format("%s/mods/%d/files/%d", Constants.CURSEFORGE_CORE_API_URL, projectId, fileId);
        } else {
            url = String.format("%s/addon/%d/file/%d", Constants.CURSEFORGE_API_URL, projectId, fileId);
        }

        Download download = Download.build().setUrl(url)
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeFile>>() {
            }.getType();

            CurseForgeCoreApiResponse<CurseForgeFile> response = download.asType(type);

            return response.data;
        } else {
            return download.asClass(CurseForgeFile.class);
        }
    }

    public static CurseForgeProject getProjectById(int projectId) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        if (usingCoreApi) {
            url = String.format("%s/mods/%d", Constants.CURSEFORGE_CORE_API_URL, projectId);
        } else {
            url = String.format("%s/addon/%d", Constants.CURSEFORGE_API_URL, projectId);
        }

        Download download = Download.build().setUrl(url)
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeProject>>() {
            }.getType();

            CurseForgeCoreApiResponse<CurseForgeProject> response = download.asType(type);

            return response.data;
        } else {
            return download.asClass(CurseForgeProject.class);
        }
    }

    public static Map<Integer, CurseForgeProject> getProjectsAsMap(int[] addonIds) {
        try {
            return getProjects(addonIds).stream().distinct().collect(Collectors.toMap(p -> p.id, p -> p));
        } catch (Throwable t) {
            LogManager.logStackTrace("Error trying to get CurseForge projects as map", t);
        }

        return null;
    }

    public static List<CurseForgeProject> getProjects(int[] projectIds) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        Download download = Download.build();

        if (usingCoreApi) {
            url = String.format("%s/mods", Constants.CURSEFORGE_CORE_API_URL);

            Map<String, int[]> body = new HashMap<>();
            body.put("modIds", projectIds);

            download = download
                    .post(RequestBody.create(Gsons.DEFAULT.toJson(body),
                            MediaType.get("application/json; charset=utf-8")));
        } else {
            url = String.format("%s/addon", Constants.CURSEFORGE_API_URL);

            download = download
                    .post(RequestBody.create(Gsons.DEFAULT.toJson(projectIds),
                            MediaType.get("application/json; charset=utf-8")));
        }

        download = download.setUrl(url)
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeProject>>>() {
            }.getType();

            CurseForgeCoreApiResponse<List<CurseForgeProject>> response = download.asType(type);

            return response.data;
        } else {
            java.lang.reflect.Type type = new TypeToken<List<CurseForgeProject>>() {
            }.getType();

            return download.asType(type);
        }
    }

    public static List<CurseForgeFile> getFiles(int[] fileIds) {
        Map<String, int[]> body = new HashMap<>();
        body.put("fileIds", fileIds);

        Download download = Download.build()
                .post(RequestBody.create(Gsons.DEFAULT.toJson(
                        body),
                        MediaType.get("application/json; charset=utf-8")))
                .setUrl(String.format("%s/mods/files", Constants.CURSEFORGE_CORE_API_URL))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeFile>>>() {
        }.getType();

        CurseForgeCoreApiResponse<List<CurseForgeFile>> response = download.asType(type);

        return response.data;
    }

    public static CurseForgeFingerprint checkFingerprint(long murmurHash) {
        Long[] hashes = { murmurHash };
        return checkFingerprints(hashes);
    }

    public static CurseForgeFingerprint checkFingerprints(Long[] murmurHashes) {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        Download download = Download.build();

        if (usingCoreApi) {
            url = String.format("%s/fingerprints", Constants.CURSEFORGE_CORE_API_URL);

            Map<String, Long[]> body = new HashMap<>();
            body.put("fingerprints", murmurHashes);

            download = download
                    .post(RequestBody.create(Gsons.DEFAULT.toJson(body),
                            MediaType.get("application/json; charset=utf-8")));
        } else {
            url = String.format("%s/fingerprint", Constants.CURSEFORGE_API_URL);
        }

        download = download.setUrl(url)
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<CurseForgeFingerprint>>() {
            }.getType();

            CurseForgeCoreApiResponse<CurseForgeFingerprint> response = download.asType(type);

            return response.data;
        } else {
            return download.asClass(CurseForgeFingerprint.class);
        }
    }

    public static List<CurseForgeCategoryForGame> getCategories() {
        boolean usingCoreApi = ConfigManager.getConfigItem("platforms.curseforge.useCoreApi", false);
        String url;

        if (usingCoreApi) {
            url = String.format("%s/categories?gameId=432", Constants.CURSEFORGE_CORE_API_URL);
        } else {
            url = String.format("%s/category?gameId=432", Constants.CURSEFORGE_API_URL);
        }

        Download download = Download.build().setUrl(url)
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        if (usingCoreApi) {
            java.lang.reflect.Type type = new TypeToken<CurseForgeCoreApiResponse<List<CurseForgeCategoryForGame>>>() {
            }.getType();

            CurseForgeCoreApiResponse<List<CurseForgeCategoryForGame>> response = download.asType(type);

            return response.data;
        } else {
            java.lang.reflect.Type type = new TypeToken<List<CurseForgeCategoryForGame>>() {
            }.getType();

            return download.asType(type);
        }
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForModpacks() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        return categories.stream().filter(c -> c.classId != null && c.classId == 4471)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }

    public static List<CurseForgeCategoryForGame> getCategoriesForMods() {
        List<CurseForgeCategoryForGame> categories = getCategories();

        return categories.stream().filter(c -> c.classId != null && c.classId == 6)
                .sorted(Comparator.comparing(c -> c.name)).collect(Collectors.toList());
    }
}
