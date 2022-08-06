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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.atlauncher.strings.Noun;
import org.joda.time.format.ISODateTimeFormat;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Pack;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.ATLauncherPackCard;
import com.atlauncher.managers.PackManager;

public class ATLauncherFeaturedPacksPanel extends PackBrowserPlatformPanel {
    private final List<Pack> packs = new LinkedList<>();
    private final List<ATLauncherPackCard> cards = new LinkedList<>();

    private void loadPacksToShow(String minecraftVersion, String sort, boolean sortDescending, String searchText) {
        List<Pack> packs = PackManager.getPacksSortedPositionally(true, false);

        this.packs.addAll(packs.stream().filter(Pack::canInstall).filter(pack -> {
            if (minecraftVersion != null) {
                return pack.versions.stream().anyMatch(pv -> pv.minecraftVersion.id.equals(minecraftVersion));
            }

            return true;
        }).collect(Collectors.toList()));
    }

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        contentPanel.removeAll();
        this.packs.clear();
        this.cards.clear();
        loadPacksToShow(minecraftVersion, sort, sortDescending, search);

        loadMorePacks(contentPanel, minecraftVersion, category, sort, sortDescending, search, page);
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search,
            int page) {
        this.packs.stream().forEach(pack -> this.cards.add(new ATLauncherPackCard(pack, true)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        int count = 0;
        for (ATLauncherPackCard card : this.cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
            count++;
        }

        if (count == 0) {
            contentPanel.add(new NilCard(GetText.tr("There are no packs to display.")), gbc);
        }
    }

    @Override
    public Noun getPlatformName() {
        return Noun.ATLAUNCHER_FEAT;
    }

    @Override
    public String getAnalyticsCategory() {
        return "FeaturedPack";
    }

    @Override
    public boolean supportsSearch() {
        return false;
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
        Map<String, String> sortFields = new LinkedHashMap<>();

        return sortFields;
    }

    @Override
    public Map<String, Boolean> getSortFieldsDefaultOrder() {
        // Sort field / if in descending order
        Map<String, Boolean> sortFieldsOrder = new LinkedHashMap<>();

        return sortFieldsOrder;
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
        List<VersionManifestVersion> minecraftVersions = new ArrayList<>();

        PackManager.getPacks().stream().forEach(p -> {
            minecraftVersions
                    .addAll(p.versions.stream().map(v -> v.minecraftVersion).distinct().collect(Collectors.toList()));
        });

        return minecraftVersions.stream().distinct().sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
        }).reversed()).collect(Collectors.toList());
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
