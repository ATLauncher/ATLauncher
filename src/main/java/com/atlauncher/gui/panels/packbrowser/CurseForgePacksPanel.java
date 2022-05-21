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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.CurseForgePackCard;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;

import org.mini2Dx.gettext.GetText;

public class CurseForgePacksPanel extends PackBrowserPlatformPanel {
    GridBagConstraints gbc = new GridBagConstraints();

    boolean hasMorePages = true;

    @Override
    protected void loadPacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        List<CurseForgeProject> packs = CurseForgeApi.searchModPacks(search, page - 1, sort, sortDescending, category,
                minecraftVersion);

        hasMorePages = packs != null && packs.size() == Constants.CURSEFORGE_PAGINATION_SIZE;

        if (packs == null || packs.size() == 0) {
            hasMorePages = false;
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
    public void loadMorePacks(JPanel contentPanel, String minecraftVersion, String category, String sort,
            boolean sortDescending, String search, int page) {
        List<CurseForgeProject> packs = CurseForgeApi.searchModPacks(search, page - 1, sort, sortDescending, category,
                minecraftVersion);

        hasMorePages = packs != null && packs.size() == Constants.MODPACKS_CH_PAGINATION_SIZE;

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
    public boolean supportsSearch() {
        return true;
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
        sortFields.put("Name", GetText.tr("Name"));
        sortFields.put("Featured", GetText.tr("Featured"));

        return sortFields;
    }

    @Override
    public Map<String, Boolean> getSortFieldsDefaultOrder() {
        // Sort field / if in descending order
        Map<String, Boolean> sortFieldsOrder = new LinkedHashMap<>();

        sortFieldsOrder.put("Popularity", true);
        sortFieldsOrder.put("LastUpdated", true);
        sortFieldsOrder.put("TotalDownloads", true);
        sortFieldsOrder.put("Name", false);
        sortFieldsOrder.put("Featured", true);

        return sortFieldsOrder;
    }

    @Override
    public boolean supportsSortOrder() {
        return true;
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
    public boolean hasMorePages() {
        return hasMorePages;
    }

    public boolean supportsManualAdding() {
        return true;
    }

    public void addById(String id) {

        CurseForgeProject project = null;

        if (id.startsWith("https://www.curseforge.com/minecraft/modpacks")) {
            Pattern pattern = Pattern.compile(
                    "https:\\/\\/www\\.curseforge\\.com\\/minecraft\\/modpacks\\/([a-zA-Z0-9-]+)\\/?(?:download|files)?\\/?([0-9]+)?");
            Matcher matcher = pattern.matcher(id);

            if (!matcher.find() || matcher.groupCount() < 2) {
                LogManager.error("Cannot install as the url was not a valid CurseForge modpack url");
                return;
            }

            String packSlug = matcher.group(1);
            project = CurseForgeApi.getModPackBySlug(packSlug);
        } else {
            try {
                project = CurseForgeApi.getProjectById(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                project = CurseForgeApi.getModPackBySlug(id);
            }
        }

        if (project == null) {
            DialogManager.okDialog().setType(DialogManager.ERROR).setTitle(GetText.tr("Pack Not Found"))
                    .setContent(
                            GetText.tr(
                                    "A pack with that id/slug was not found. Please check the id/slug/url and try again."))
                    .show();
            return;
        }

        if (AccountManager.getSelectedAccount() == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(GetText.tr("Cannot create instance as you have no account selected."))
                    .setType(DialogManager.ERROR).show();
        } else {
            Analytics.sendEvent(project.name, "InstallManual", getAnalyticsCategory());
            Analytics.sendEvent(project.name, "Install", getAnalyticsCategory());
            new InstanceInstallerDialog(project);
        }
    }

    @Override
    public String getPlatformMessage() {
        return ConfigManager.getConfigItem("platforms.curseforge.message", null);
    }
}
