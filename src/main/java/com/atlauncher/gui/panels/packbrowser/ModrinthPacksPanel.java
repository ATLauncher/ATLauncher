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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.modrinth.ModrinthSearchHit;
import com.atlauncher.data.modrinth.ModrinthSearchResult;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.ModrinthPackCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.utils.ModrinthApi;

import org.apache.commons.text.WordUtils;
import org.mini2Dx.gettext.GetText;

public class ModrinthPacksPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    @Override
    protected void loadPacks(JPanel contentPanel, String category, String sort, String search, int page) {
        ModrinthSearchResult searchResult = ModrinthApi.searchModPacks(search, page - 1, sort);

        if (searchResult.hits.size() == 0) {
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

        List<ModrinthPackCard> cards = searchResult.hits.stream().map(p -> new ModrinthPackCard(p))
                .collect(Collectors.toList());

        contentPanel.removeAll();

        for (ModrinthPackCard card : cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String category, String sort, String search, int page) {
        ModrinthSearchResult searchResult = ModrinthApi.searchModPacks(search, page - 1, sort);

        for (ModrinthSearchHit pack : searchResult.hits) {
            contentPanel.add(new ModrinthPackCard(pack), gbc);
            gbc.gridy++;
        }
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
        return true;
    }

    @Override
    public Map<String, String> getCategoryFields() {
        Map<String, String> categoryFields = new LinkedHashMap<>();

        ModrinthApi.getCategoriesForModpacks().stream()
                .forEach(c -> categoryFields.put(c.name, WordUtils.capitalizeFully(c.name)));

        return categoryFields;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public Map<String, String> getSortFields() {
        Map<String, String> sortFields = new LinkedHashMap<>();

        sortFields.put("relevance", GetText.tr("Relevance"));
        sortFields.put("downloads", GetText.tr("Downloads"));
        sortFields.put("follows", GetText.tr("Follows"));
        sortFields.put("newest", GetText.tr("Newest"));
        sortFields.put("updated", GetText.tr("Updated"));

        return sortFields;
    }

    @Override
    public boolean hasPagination() {
        return true;
    }

    @Override
    public String getPlatformMessage() {
        return ConfigManager.getConfigItem("platforms.modrinth.message", null);
    }
}
