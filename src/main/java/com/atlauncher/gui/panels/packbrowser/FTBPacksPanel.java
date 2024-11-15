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

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.FTBPackCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.utils.FTBApi;

public class FTBPacksPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    boolean hasMorePages = true;

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        List<FTBPackManifest> packs;

        if (search == null || search.isEmpty()) {
            packs = FTBApi.getModPacks(page, sort);
        } else {
            packs = FTBApi.searchModPacks(search, page);
        }

        hasMorePages = packs != null && packs.size() == Constants.CURSEFORGE_PAGINATION_SIZE;

        if (packs == null || packs.size() == 0) {
            contentPanel.removeAll();
            contentPanel.add(
                    new NilCard(new HTMLBuilder().text(GetText
                            .tr("There are no packs to display.<br/><br/>Try removing your search query and try again."))
                            .build()),
                    gbc);
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        List<FTBPackCard> cards = packs.stream().map(p -> new FTBPackCard(p)).collect(Collectors.toList());

        contentPanel.removeAll();

        for (FTBPackCard card : cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        List<FTBPackManifest> packs;

        if (search == null || search.isEmpty()) {
            packs = FTBApi.getModPacks(page, sort);
        } else {
            packs = FTBApi.searchModPacks(search, page);
        }

        hasMorePages = packs != null && packs.size() == Constants.FTB_PAGINATION_SIZE;

        if (packs != null) {
            for (FTBPackManifest pack : packs) {
                contentPanel.add(new FTBPackCard(pack), gbc);
                gbc.gridy++;
            }
        }
    }

    @Override
    public String getPlatformName() {
        return "FTB";
    }

    @Override
    public String getAnalyticsCategory() {
        return "FTBPack";
    }

    @Override
    public boolean supportsSearch() {
        return true;
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
        return true;
    }

    @Override
    public boolean supportsSortOrder() {
        return false;
    }

    @Override
    public Map<String, String> getSortFields() {
        Map<String, String> sortFields = new LinkedHashMap<>();

        sortFields.put("popular/plays", GetText.tr("Most Popular"));
        sortFields.put("popular/installs", GetText.tr("Most Installed"));
        sortFields.put("updated", GetText.tr("Recently Updated"));
        sortFields.put("featured", GetText.tr("Featured"));

        return sortFields;
    }

    @Override
    public Map<String, Boolean> getSortFieldsDefaultOrder() {
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
        return true;
    }

    @Override
    public boolean hasMorePages() {
        return hasMorePages;
    }

    public boolean supportsManualAdding() {
        return false;
    }

    public void addById(String id) {
    }

    @Override
    public String getPlatformMessage() {
        return ConfigManager.getConfigItem("platforms.ftb.message", null);
    }
}
