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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.PackCard;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.PackManager;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class FeaturedPacksTab extends JPanel implements Tab, RelocalizationListener {
    private final JPanel contentPanel = new JPanel(new GridBagLayout());
    private final JScrollPane scrollPane;
    private NilCard nilCard;

    private final List<Pack> packs = new LinkedList<>();
    private final List<PackCard> cards = new LinkedList<>();

    public FeaturedPacksTab() {
        super(new BorderLayout());
        setName("featuredPacksPanel");
        this.contentPanel.setLayout(new GridBagLayout());

        scrollPane = new JScrollPane(this.contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        this.add(scrollPane, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        refresh();
    }

    private void addLoadingCard() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.contentPanel.add(new LoadingPanel(), gbc);
    }

    private void loadPacksToShow() {
        List<Pack> packs = App.settings.sortPacksAlphabetically ? PackManager.getPacksSortedAlphabetically(true)
                : PackManager.getPacksSortedPositionally(true);

        this.packs.clear();
        this.packs.addAll(packs.stream().filter(Pack::canInstall).collect(Collectors.toList()));
    }

    private void addPackCards() {
        this.packs.stream().skip(this.cards.size()).limit(10).forEach(pack -> this.cards.add(new PackCard(pack)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
    }

    private void load(boolean keep) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        int count = 0;
        for (PackCard card : this.cards) {
            if (keep) {
                this.contentPanel.add(card, gbc);
                gbc.gridy++;
                count++;
            }
        }

        if (count == 0) {
            nilCard = new NilCard(GetText.tr("There are no packs to display.\n\nPlease check back another time."));
            this.contentPanel.add(nilCard, gbc);
        }
    }

    public void reload() {
        this.contentPanel.removeAll();
        load(true);
        revalidate();
        repaint();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(0);
            }
        });
    }

    public void refresh() {
        this.cards.clear();
        addLoadingCard();
        loadPacksToShow();
        addPackCards();
        reload();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Featured Packs");
    }

    @Override
    public void onRelocalization() {
        if (nilCard != null) {
            nilCard.setMessage(GetText.tr("There are no packs to display.\n\nPlease check back another time."));
        }
    }
}
