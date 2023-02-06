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
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.graphql.UnifiedModPackSearchQuery;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.UnifiedPackSearchCard;
import com.atlauncher.network.GraphqlClient;

public class UnifiedPacksSearchPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        if (search.isEmpty() || search.length() <= 1) {
            contentPanel.removeAll();
            contentPanel.add(
                    new NilCard(new HTMLBuilder().text(GetText
                            .tr("To find a pack, search for one at the top<br/><br/>Alternatively choose a modpack platform on the left hand side."))
                            .build()),
                    gbc);
            return;
        }

        UnifiedModPackSearchQuery.Data response = GraphqlClient
                .callAndWait(new UnifiedModPackSearchQuery(search));

        if (response == null || response.unifiedModPackSearch() == null
                || response.unifiedModPackSearch().size() == 0) {
            contentPanel.removeAll();
            contentPanel.add(
                    new NilCard(new HTMLBuilder().text(GetText
                            .tr("There are no packs to display.<br/><br/>Try another search query or choose a platform on the left hand side."))
                            .build()),
                    gbc);
            return;
        }

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        List<UnifiedPackSearchCard> cards = response.unifiedModPackSearch().stream()
                .map(p -> new UnifiedPackSearchCard(p))
                .collect(Collectors.toList());

        contentPanel.removeAll();

        for (UnifiedPackSearchCard card : cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        // no pagination
    }

    @Override
    public String getPlatformName() {
        return "UnifiedModPackSearch";
    }

    @Override
    public String getAnalyticsCategory() {
        return "Pack";
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
        return false;
    }

    @Override
    public Map<String, String> getSortFields() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<String, Boolean> getSortFieldsDefaultOrder() {
        return new LinkedHashMap<>();
    }

    @Override
    public boolean supportsSortOrder() {
        return false;
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

    public boolean supportsManualAdding() {
        return false;
    }

    public void addById(String id) {
    }

    @Override
    public boolean hasPagination() {
        return false;
    }

    @Override
    public boolean hasMorePages() {
        return false;
    }

    @Override
    public String getPlatformMessage() {
        return null;
    }
}
