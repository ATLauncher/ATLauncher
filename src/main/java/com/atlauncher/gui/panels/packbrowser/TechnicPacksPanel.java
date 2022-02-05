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

import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.technic.TechnicModpackSlim;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.TechnicPackCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.utils.TechnicApi;

import org.mini2Dx.gettext.GetText;

public class TechnicPacksPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort, String search,
            int page) {
        List<TechnicModpackSlim> packs;

        if (search == null || search.isEmpty()) {
            packs = TechnicApi.getTrendingModpacks().modpacks;
        } else {
            packs = TechnicApi.searchModpacks(search).modpacks;
        }

        if (packs == null || packs.size() == 0) {
            contentPanel.removeAll();
            contentPanel.add(
                    new NilCard(GetText
                            .tr("There are no packs to display.\n\nTry removing your search query and try again.")),
                    gbc);
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        List<TechnicPackCard> cards = packs.stream().map(p -> new TechnicPackCard(p)).collect(Collectors.toList());

        contentPanel.removeAll();

        for (TechnicPackCard card : cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort, String search,
            int page) {
        // no pagination on api
    }

    @Override
    public String getPlatformName() {
        return "Technic";
    }

    @Override
    public String getAnalyticsCategory() {
        return "TechnicPack";
    }

    @Override
    public boolean hasCategories() {
        return false;
    }

    @Override
    public Map<String, String> getCategoryFields() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean hasSort() {
        return false;
    }

    @Override
    public Map<String, String> getSortFields() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean supportsMinecraftVersionFiltering() {
        return false;
    }

    @Override
    public List<VersionManifestVersionType> getSupportedMinecraftVersionTypesForFiltering() {
        List<VersionManifestVersionType> supportedTypes = new ArrayList<>();

        return supportedTypes;
    }

    @Override
    public List<VersionManifestVersion> getSupportedMinecraftVersionsForFiltering() {
        List<VersionManifestVersion> supportedTypes = new ArrayList<>();

        return supportedTypes;
    }

    @Override
    public boolean hasPagination() {
        return false;
    }

    @Override
    public String getPlatformMessage() {
        return ConfigManager.getConfigItem("platforms.technic.message", null);
    }
}
