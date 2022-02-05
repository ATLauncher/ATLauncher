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
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.CurseForgePackCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.utils.CurseForgeApi;

import org.mini2Dx.gettext.GetText;

public class CurseForgePacksPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort, String search,
            int page) {
        List<CurseForgeProject> packs = CurseForgeApi.searchModPacks(search, page - 1, sort, category,
                minecraftVersion);

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

        List<CurseForgePackCard> cards = packs.stream().map(p -> new CurseForgePackCard(p))
                .collect(Collectors.toList());

        contentPanel.removeAll();

        for (CurseForgePackCard card : cards) {
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort, String search,
            int page) {
        List<CurseForgeProject> packs = CurseForgeApi.searchModPacks(search, page - 1, sort, category,
                minecraftVersion);

        if (packs != null) {
            for (CurseForgeProject pack : packs) {
                contentPanel.add(new CurseForgePackCard(pack), gbc);
                gbc.gridy++;
            }
        }
    }

    @Override
    public String getPlatformName() {
        return "CurseForge";
    }

    @Override
    public String getAnalyticsCategory() {
        return "CurseForgePack";
    }

    @Override
    public boolean hasCategories() {
        return true;
    }

    @Override
    public Map<String, String> getCategoryFields() {
        Map<String, String> categoryFields = new LinkedHashMap<>();

        CurseForgeApi.getCategoriesForModpacks().stream().forEach(c -> categoryFields.put(String.valueOf(
                c.id), c.name));

        return categoryFields;
    }

    @Override
    public boolean hasSort() {
        return true;
    }

    @Override
    public Map<String, String> getSortFields() {
        Map<String, String> sortFields = new LinkedHashMap<>();

        sortFields.put("Popularity", GetText.tr("Popularity"));
        sortFields.put("LastUpdated", GetText.tr("Last Updated"));
        sortFields.put("TotalDownloads", GetText.tr("Total Downloads"));

        return sortFields;
    }

    @Override
    public boolean supportsMinecraftVersionFiltering() {
        return true;
    }

    @Override
    public List<VersionManifestVersionType> getSupportedMinecraftVersionTypesForFiltering() {
        List<VersionManifestVersionType> supportedTypes = new ArrayList<>();

        supportedTypes.add(VersionManifestVersionType.RELEASE);

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
    public String getPlatformMessage() {
        return ConfigManager.getConfigItem("platforms.curseforge.message", null);
    }
}
