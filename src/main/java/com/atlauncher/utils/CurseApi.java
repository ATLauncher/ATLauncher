/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.google.gson.reflect.TypeToken;

/**
 * Various utility methods for interacting with the Curse API.
 */
public class CurseApi {
    public static List<CurseMod> searchMods(String gameVersion, String query, int page, int categoryId) {
        try {
            String url = String.format(
                    "%s/addon/search?gameId=432&gameVersion=%s&categoryId=%d&sectionId=6&searchFilter=%s&sort=Popularity&sortDescending=true&pageSize=%d&index=%d",
                    Constants.CURSE_API_URL, gameVersion, categoryId,
                    URLEncoder.encode(query, StandardCharsets.UTF_8.name()), Constants.CURSE_PAGINATION_SIZE,
                    page * Constants.CURSE_PAGINATION_SIZE);

            Downloadable downloadable = new Downloadable(url, false);

            java.lang.reflect.Type type = new TypeToken<List<CurseMod>>() {
            }.getType();

            return Gsons.DEFAULT.fromJson(downloadable.getContents(), type);
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static List<CurseMod> searchMods(String gameVersion, String query, int page) {
        return searchMods(gameVersion, query, page, 0);
    }

    public static List<CurseMod> searchModsForFabric(String gameVersion, String query, int page) {
        return searchMods(gameVersion, query, page, Constants.CURSE_FABRIC_CATEGORY_ID);
    }

    public static List<CurseFile> getFilesForMod(int modId) {
        String url = String.format("%s/addon/%d/files", Constants.CURSE_API_URL, modId);

        Downloadable downloadable = new Downloadable(url, false);

        java.lang.reflect.Type type = new TypeToken<List<CurseFile>>() {
        }.getType();

        return Gsons.DEFAULT.fromJson(downloadable.getContents(), type);
    }
}
