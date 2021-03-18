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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
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
    public static List<CurseForgeProject> searchCurseForge(int sectionId, String query, int page, int categoryId,
            String sort) {
        return searchCurseForge(null, sectionId, query, page, categoryId, sort);
    }

    public static List<CurseForgeProject> searchCurseForge(String gameVersion, int sectionId, String query, int page,
            int categoryId, String sort) {
        try {
            String url = String.format(
                    "%s/addon/search?gameId=432&categoryId=%d&sectionId=%s&searchFilter=%s&sort=%s&sortDescending=true&pageSize=%d&index=%d",
                    Constants.CURSEFORGE_API_URL, categoryId, sectionId,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), sort.replace(" ", ""),
                    Constants.CURSEFORGE_PAGINATION_SIZE, page * Constants.CURSEFORGE_PAGINATION_SIZE);

            if (gameVersion != null) {
                url += "&gameVersion=" + gameVersion;
            }

            java.lang.reflect.Type type = new TypeToken<List<CurseForgeProject>>() {
            }.getType();

            return Download.build().cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .setUrl(url).asType(type);
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
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

    public static List<CurseForgeProject> searchModPacks(String query, int page, String sort) {
        return searchCurseForge(Constants.CURSEFORGE_MODPACKS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseForgeProject> searchModsForFabric(String gameVersion, String query, int page, String sort) {
        return searchCurseForge(gameVersion, Constants.CURSEFORGE_MODS_SECTION_ID, query, page,
                Constants.CURSEFORGE_FABRIC_CATEGORY_ID, sort);
    }

    public static List<CurseForgeFile> getFilesForProject(int projectId) {
        java.lang.reflect.Type type = new TypeToken<List<CurseForgeFile>>() {
        }.getType();

        return Download.build().setUrl(String.format("%s/addon/%d/files", Constants.CURSEFORGE_API_URL, projectId))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }

    public static CurseForgeFile getFileForProject(int modId, int fileId) {
        return Download.build()
                .setUrl(String.format("%s/addon/%d/file/%d", Constants.CURSEFORGE_API_URL, modId, fileId))
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build()).asClass(CurseForgeFile.class);
    }

    public static CurseForgeProject getProjectById(int modId) {
        return Download.build().setUrl(String.format("%s/addon/%d", Constants.CURSEFORGE_API_URL, modId))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(CurseForgeProject.class);
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
        java.lang.reflect.Type type = new TypeToken<List<CurseForgeProject>>() {
        }.getType();

        return Download.build()
                .post(RequestBody.create(Gsons.DEFAULT.toJson(projectIds),
                        MediaType.get("application/json; charset=utf-8")))
                .setUrl(String.format("%s/addon", Constants.CURSEFORGE_API_URL))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }

    public static CurseForgeFingerprint checkFingerprint(long murmurHash) {
        Long[] hashes = { murmurHash };
        return checkFingerprints(hashes);
    }

    public static CurseForgeFingerprint checkFingerprints(Long[] murmurHashes) {
        return Download.build()
                .post(RequestBody.create(Gsons.DEFAULT.toJson(murmurHashes),
                        MediaType.get("application/json; charset=utf-8")))
                .setUrl(String.format("%s/fingerprint", Constants.CURSEFORGE_API_URL))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(CurseForgeFingerprint.class);
    }
}
