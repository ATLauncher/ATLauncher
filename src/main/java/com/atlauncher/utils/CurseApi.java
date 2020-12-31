/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.util.concurrent.TimeUnit;

import com.atlauncher.Gsons;
import com.atlauncher.data.Constants;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseFingerprint;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the Curse API.
 */
public class CurseApi {
    public static List<CurseMod> searchCurse(int sectionId, String query, int page, int categoryId, String sort) {
        return searchCurse(null, sectionId, query, page, categoryId, sort);
    }

    public static List<CurseMod> searchCurse(String gameVersion, int sectionId, String query, int page, int categoryId,
            String sort) {
        try {
            String url = String.format(
                    "%s/addon/search?gameId=432&categoryId=%d&sectionId=%s&searchFilter=%s&sort=%s&sortDescending=true&pageSize=%d&index=%d",
                    Constants.CURSE_API_URL, categoryId, sectionId,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), sort.replace(" ", ""),
                    Constants.CURSE_PAGINATION_SIZE, page * Constants.CURSE_PAGINATION_SIZE);

            if (gameVersion != null) {
                url += "&gameVersion=" + gameVersion;
            }

            java.lang.reflect.Type type = new TypeToken<List<CurseMod>>() {
            }.getType();

            return Download.build().cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                    .setUrl(url).asType(type);
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static List<CurseMod> searchCurse(String gameVersion, int sectionId, String query, int page,
            int categoryId) {
        return searchCurse(gameVersion, sectionId, query, page, categoryId, "Popularity");
    }

    public static List<CurseMod> searchWorlds(String gameVersion, String query, int page, String sort) {
        return searchCurse(gameVersion, Constants.CURSE_WORLDS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseMod> searchResourcePacks(String query, int page, String sort) {
        return searchCurse(Constants.CURSE_RESOURCE_PACKS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseMod> searchMods(String gameVersion, String query, int page, String sort) {
        return searchCurse(gameVersion, Constants.CURSE_MODS_SECTION_ID, query, page, 0, sort);
    }

    public static List<CurseMod> searchModsForFabric(String gameVersion, String query, int page, String sort) {
        return searchCurse(gameVersion, Constants.CURSE_MODS_SECTION_ID, query, page,
                Constants.CURSE_FABRIC_CATEGORY_ID, sort);
    }

    public static List<CurseFile> getFilesForMod(int modId) {
        java.lang.reflect.Type type = new TypeToken<List<CurseFile>>() {
        }.getType();

        return Download.build().setUrl(String.format("%s/addon/%d/files", Constants.CURSE_API_URL, modId))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asType(type);
    }

    public static CurseFile getFileForMod(int modId, int fileId) {
        return Download.build().setUrl(String.format("%s/addon/%d/file/%d", Constants.CURSE_API_URL, modId, fileId))
                .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build()).asClass(CurseFile.class);
    }

    public static CurseMod getModById(int modId) {
        return Download.build().setUrl(String.format("%s/addon/%d", Constants.CURSE_API_URL, modId))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build()).asClass(CurseMod.class);
    }

    public static CurseFingerprint checkFingerprint(long murmurHash) {
        long[] hashes = { murmurHash };
        return checkFingerprints(hashes);
    }

    public static CurseFingerprint checkFingerprints(long[] murmurHashes) {
        return Download.build()
                .post(RequestBody.create(Gsons.DEFAULT.toJson(murmurHashes),
                        MediaType.get("application/json; charset=utf-8")))
                .setUrl(String.format("%s/fingerprint", Constants.CURSE_API_URL))
                .cached(new CacheControl.Builder().maxStale(10, TimeUnit.MINUTES).build())
                .asClass(CurseFingerprint.class);
    }
}
