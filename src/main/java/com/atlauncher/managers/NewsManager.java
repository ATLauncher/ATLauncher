/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.Data;
import com.atlauncher.data.News;

import java.util.List;

public class NewsManager {
    public static List<News> getNews() {
        return Data.NEWS;
    }

    public static String getNewsHTML() {
        String news = "<html>";

        for (News newsItem : NewsManager.getNews()) {
            news += newsItem.getHTML() + "<hr/>";
        }

        news = news.substring(0, news.length() - 5);

        news += "</html>";
        return news;
    }
}
