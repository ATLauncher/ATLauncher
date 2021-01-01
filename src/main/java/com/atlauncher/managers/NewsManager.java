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
package com.atlauncher.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.News;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class NewsManager {

    /**
     * Get the News for the Launcher
     *
     * @return The News items
     */
    public static List<News> getNews() {
        return Data.NEWS;
    }

    /**
     * Loads the languages for use in the Launcher
     */
    public static void loadNews() {
        PerformanceManager.start();
        LogManager.debug("Loading news");
        Data.NEWS.clear();
        try {
            java.lang.reflect.Type type = new TypeToken<List<News>>() {
            }.getType();
            File fileDir = FileSystem.JSON.resolve("newnews.json").toFile();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), StandardCharsets.UTF_8));

            Data.NEWS.addAll(Gsons.DEFAULT.fromJson(in, type));
            in.close();
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading news");
        PerformanceManager.end();
    }

    /**
     * Get the News for the Launcher in HTML for display on the news panel.
     *
     * @return The HTML for displaying on the News Panel
     */
    public static String getNewsHTML() {
        StringBuilder news = new StringBuilder("<html>");

        for (News newsItem : Data.NEWS) {
            news.append(newsItem.getHTML()).append("<hr/>");
        }

        // remove the last <hr/>
        news = new StringBuilder(news.substring(0, news.length() - 5));
        news.append("</html>");

        return news.toString();
    }
}
