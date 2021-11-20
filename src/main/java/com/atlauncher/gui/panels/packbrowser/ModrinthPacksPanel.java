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
package com.atlauncher.gui.panels.packbrowser;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ModrinthPacksPanel extends PackBrowserPlatformPanel {
    @Override
    protected void loadPacks(JPanel contentPanel, Integer category, String sort, String search, int page) {
        contentPanel.removeAll();
        contentPanel.add(new JLabel("modrinth"));
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, Integer category, String sort, String search, int page) {
    }

    @Override
    public String getPlatformName() {
        return "Modrinth";
    }

    @Override
    public String getAnalyticsCategory() {
        return "ModrinthPack";
    }

    @Override
    public boolean hasCategories() {
        return false;
    }

    @Override
    public Map<Integer, String> getCategoryFields() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public Map<String, String> getSortFields() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean hasPagination() {
        return true;
    }
}
