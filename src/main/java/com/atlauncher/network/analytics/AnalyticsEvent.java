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
package com.atlauncher.network.analytics;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsEvent {
    public String name;
    public Map<String, Object> payload;
    public long timestamp;

    public AnalyticsEvent(String name, Map<String, Object> payload) {
        this.name = name;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public static AnalyticsEvent forScreenView(String title) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("screen_name", title);

        return new AnalyticsEvent("screen_view", payload);
    }

    public static AnalyticsEvent forLinkClick(String url) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("url", url);

        return new AnalyticsEvent("link_click", payload);
    }
}
