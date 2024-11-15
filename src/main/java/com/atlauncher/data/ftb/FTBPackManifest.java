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
package com.atlauncher.data.ftb;

import java.util.List;
import java.util.Locale;

public class FTBPackManifest {
    public String synopsis;
    public String description;
    public List<FTBPackArt> art;
    public List<FTBPackLink> links;
    public List<FTBPackAuthor> authors;
    public List<FTBPackVersion> versions;
    public int installs;
    public int plays;
    public boolean featured;
    public int refreshed;
    public String notification;
    public FTBPackRating rating;
    public String status;
    public int id;
    public String name;
    public String type;
    public int updated;
    public List<FTBPackTag> tags;

    public boolean hasTag(String tag) {
        if (tags == null) {
            return false;
        }

        return tags.stream().map(t -> t.name).anyMatch(tag::equals);
    }

    public String getWebsiteUrl() {
        return String.format(Locale.ENGLISH, "https://feed-the-beast.com/modpacks/%d-%s", id, getSlug());
    }

    public String getSlug() {
        return name.replace("+", " Plus").toLowerCase(Locale.ROOT).replaceAll("\\W", "-").replaceAll("-{2,}", "-");
    }
}
