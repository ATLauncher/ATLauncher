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

import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.packbrowser.ATLauncherPackCard;
import com.atlauncher.managers.PackManager;

import org.mini2Dx.gettext.GetText;

public class ATLauncherPacksPanel extends PackBrowserPlatformPanel {
    private final List<Pack> packs = new LinkedList<>();
    private final List<ATLauncherPackCard> cards = new LinkedList<>();

    private void loadPacksToShow(String searchText) {
        List<Pack> packs = App.settings.sortPacksAlphabetically ? PackManager.getPacksSortedAlphabetically(false)
                : PackManager.getPacksSortedPositionally(false);

        this.packs.addAll(packs.stream().filter(Pack::canInstall).filter(pack -> {
            if (!searchText.isEmpty()) {
                return (pack.getDescription() != null
                        && Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                                .matcher(pack.getDescription()).find())
                        || Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE).matcher(pack.getName())
                                .find();
            }

            return true;
        }).collect(Collectors.toList()));
    }

    @Override
    protected void loadPacks(JPanel contentPanel, Integer category, String sort, String search, int page) {
        contentPanel.removeAll();
        this.packs.clear();
        this.cards.clear();
        loadPacksToShow(search);

        loadMorePacks(contentPanel, category, sort, search, page);
    }

    @Override
    public void loadMorePacks(JPanel contentPanel, Integer category, String sort, String search, int page) {
        this.packs.stream().skip(this.cards.size()).limit(10)
                .forEach(pack -> this.cards.add(new ATLauncherPackCard(pack)));

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
            contentPanel.add(
                    new NilCard(GetText.tr("There are no packs to display.\n\nPlease check back another time.")), gbc);
        }
    }

    @Override
    public String getPlatformName() {
        return "ATLauncher";
    }

    @Override
    public String getAnalyticsCategory() {
        return "Pack";
    }

    @Override
    public boolean hasCategories() {
        return false;
    }

    @Override
    public Map<Integer, String> getCategoryFields() {
        return new HashMap<>();
    }

    @Override
    public boolean hasSort() {
        return false;
    }

    @Override
    public Map<String, String> getSortFields() {
        return new HashMap<>();
    }

    @Override
    public boolean hasPagination() {
        // already loaded in all the cards possible, so don't navigate
        if (this.packs.size() != 0 && this.cards.size() != 0 && this.packs.size() == this.cards.size()) {
            return false;
        }

        return true;
    }
}
