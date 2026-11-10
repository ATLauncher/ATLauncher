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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NewsFeedState {
    public final NewsSource source;
    public final List<NewsItem> items;
    public final boolean initialized;
    public final boolean loading;
    public final boolean hasMore;
    public final String error;

    public NewsFeedState(NewsSource source, List<NewsItem> items, boolean initialized, boolean loading,
            boolean hasMore, String error) {
        this.source = source;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
        this.initialized = initialized;
        this.loading = loading;
        this.hasMore = hasMore;
        this.error = error;
    }
}
