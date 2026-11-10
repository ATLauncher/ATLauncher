/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2026 ATLauncher
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

import java.util.Date;
import java.util.Objects;

public final class NewsItem {
    public final String id;
    public final String title;
    public final String content;
    public final Date createdAt;
    public final NewsSource source;

    /** Optional article and image links use an empty string when absent. */
    public final String url;
    public final String imageUrl;

    public NewsItem(String id, String title, String content, Date createdAt, NewsSource source, String url,
            String imageUrl) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.content = content == null ? "" : content;
        this.createdAt = new Date(Objects.requireNonNull(createdAt, "createdAt").getTime());
        this.source = Objects.requireNonNull(source, "source");
        this.url = url == null ? "" : url;
        this.imageUrl = imageUrl == null ? "" : imageUrl;
    }
}
