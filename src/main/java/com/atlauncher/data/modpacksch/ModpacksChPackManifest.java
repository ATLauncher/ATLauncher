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
package com.atlauncher.data.modpacksch;

import java.util.List;
import java.util.Locale;

public class ModpacksChPackManifest {
    public String synopsis;
    public String description;
    public List<ModpacksChPackArt> art;
    public List<ModpacksChPackLink> links;
    public List<ModpacksChPackAuthor> authors;
    public List<ModpacksChPackVersion> versions;
    public int installs;
    public int plays;
    public boolean featured;
    public int refreshed;
    public String notification;
    public ModpacksChPackRating rating;
    public String status;
    public int id;
    public String name;
    public String type;
    public int updated;
    public List<ModpacksChPackTag> tags;

    public boolean hasTag(String tag) {
        return tags.stream().map(t -> t.name).anyMatch(tag::equals);
    }

    public String getWebsiteUrl() {
        return String.format("https://feed-the-beast.com/modpack/%s", getSlug());
    }

    public String getSlug() {
        return name.replace("+", " Plus").toLowerCase(Locale.ROOT).replaceAll("\\W", "_").replaceAll("_{2,}", "_");
    }
}
