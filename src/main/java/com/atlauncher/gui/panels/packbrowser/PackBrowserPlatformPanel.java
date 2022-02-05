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
package com.atlauncher.gui.panels.packbrowser;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.gui.panels.LoadingPanel;

import org.mini2Dx.gettext.GetText;

public abstract class PackBrowserPlatformPanel extends JPanel {
    public abstract String getPlatformName();

    public abstract String getPlatformMessage();

    public abstract String getAnalyticsCategory();

    public abstract boolean hasCategories();

    public abstract Map<String, String> getCategoryFields();

    public abstract boolean hasSort();

    public abstract Map<String, String> getSortFields();

    public abstract boolean supportsMinecraftVersionFiltering();

    public abstract List<VersionManifestVersionType> getSupportedMinecraftVersionTypesForFiltering();

    public abstract List<VersionManifestVersion> getSupportedMinecraftVersionsForFiltering();

    public abstract boolean hasPagination();

    protected abstract void loadPacks(JPanel contentPanel, String minecraftVersion,
            String category, String sort, String search, int page);

    public abstract void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            String search, int page);

    public void load(JPanel contentPanel, String minecraftVersion, String category, String sort, String search,
            int page) {
        // remove all components on the content panel
        contentPanel.removeAll();

        // add in a loading state
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPanel.add(new LoadingPanel(GetText.tr("Loading {0} Packs...", getPlatformName())), gbc);

        // load in the packs
        loadPacks(contentPanel, minecraftVersion, category, sort, search, page);
    }

    public PackBrowserPlatformPanel() {
        super();

        setLayout(new BorderLayout());
    }
}
