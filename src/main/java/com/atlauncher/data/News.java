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
package com.atlauncher.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.managers.LogManager;
import com.google.gson.annotations.SerializedName;

/**
 * News class contains a single news article.
 */
@Json
public class News {
    /**
     * The title of this news article.
     */
    private String title;

    /**
     * The content of this news article.
     */
    private String content;

    /**
     * The time the news item was created at.
     */
    @SerializedName("created_at")
    private String createdAt;

    private String getFormattedDate() {
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
        SimpleDateFormat formatter = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

        try {
            return formatter.format(iso8601Format.parse(this.createdAt));
        } catch (ParseException e) {
            LogManager.logStackTrace(e);
            return null;
        }
    }

    /**
     * Gets the HTML of this object.
     */
    public String getHTML() {
        return "<h2>" + this.title + " (" + this.getFormattedDate() + ")</h2>" + "<p>" + this.content + "</p>";
    }
}
