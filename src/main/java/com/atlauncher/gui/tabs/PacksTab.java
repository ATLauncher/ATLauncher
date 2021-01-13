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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.TabChangeManager;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.PackCard;
import com.atlauncher.gui.dialogs.AddCurseForgePackDialog;
import com.atlauncher.gui.dialogs.AddFTBPackDialog;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.managers.PackManager;
import com.atlauncher.network.Analytics;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class PacksTab extends JPanel implements Tab, RelocalizationListener {
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel contentPanel = new JPanel(new GridBagLayout());
    private final JButton addButton = new JButton(GetText.tr("Add Pack"));
    private final JButton addCurseButton = new JButton(GetText.tr("Add CurseForge Pack"));
    private final JButton addFTBPackButton = new JButton(GetText.tr("Add FTB Pack"));
    private final JButton clearButton = new JButton(GetText.tr("Clear"));
    private final JButton expandAllButton = new JButton(GetText.tr("Expand All"));
    private final JButton collapseAllButton = new JButton(GetText.tr("Collapse All"));
    private final JTextField searchField = new JTextField(16);
    private final JButton searchButton = new JButton(GetText.tr("Search"));
    private final JScrollPane scrollPane;
    private NilCard nilCard;
    private final boolean isSystem;
    private final boolean isFeatured;
    private boolean loadingMore = false;

    private final List<Pack> packs = new LinkedList<>();
    private final List<PackCard> cards = new LinkedList<>();

    public PacksTab(boolean isFeatured, boolean isSystem) {
        super(new BorderLayout());
        setName(isSystem ? "vanillaPacksPanel" : (isSystem ? "featuredPacksPanel" : "packsPanel"));
        this.isFeatured = isFeatured;
        this.isSystem = isSystem;
        this.topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.contentPanel.setLayout(new GridBagLayout());

        searchField.setMaximumSize(new Dimension(190, 23));

        scrollPane = new JScrollPane(this.contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (!loadingMore && !isFeatured && !isSystem) {
                    int maxValue = scrollPane.getVerticalScrollBar().getMaximum()
                            - scrollPane.getVerticalScrollBar().getVisibleAmount();
                    int currentValue = scrollPane.getVerticalScrollBar().getValue();

                    if ((float) currentValue / (float) maxValue > 0.9f) {
                        loadMorePacks();
                    }
                }
            }
        });

        if (!this.isFeatured && !this.isSystem) {
            this.add(this.topPanel, BorderLayout.NORTH);
        }

        this.add(scrollPane, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        this.setupTopPanel();

        refresh();

        TabChangeManager.addListener(() -> searchField.setText(""));

        this.collapseAllButton.addActionListener(e -> {
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof PackCard) {
                    ((PackCard) comp).setCollapsed(true);
                }
            }
        });
        this.expandAllButton.addActionListener(e -> {
            for (Component comp : contentPanel.getComponents()) {
                if (comp instanceof PackCard) {
                    ((PackCard) comp).setCollapsed(false);
                }
            }
        });
        this.addCurseButton.addActionListener(e -> new AddCurseForgePackDialog());
        this.addFTBPackButton.addActionListener(e -> new AddFTBPackDialog());
        this.clearButton.addActionListener(e -> {
            searchField.setText("");
            refresh();
        });

        this.searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    Analytics.sendEvent(searchField.getText(), "Search", "Pack");
                    refresh();
                }
            }
        });

        this.searchButton.addActionListener(e -> {
            Analytics.sendEvent(searchField.getText(), "Search", "Pack");
            refresh();
        });
    }

    private void addLoadingCard() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.contentPanel.add(new LoadingPanel(), gbc);
    }

    private void setupTopPanel() {
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        topPanel.add(addCurseButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(addFTBPackButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(searchButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(clearButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(expandAllButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(collapseAllButton);
    }

    private void loadMorePacks() {
        loadingMore = true;

        addPackCards();
        load(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                loadingMore = false;
            }
        });
    }

    private void loadPacksToShow() {
        List<Pack> packs = App.settings.sortPacksAlphabetically
                ? PackManager.getPacksSortedAlphabetically(this.isFeatured, this.isSystem)
                : PackManager.getPacksSortedPositionally(this.isFeatured, this.isSystem);

        this.packs.clear();
        this.packs.addAll(packs.stream().filter(Pack::canInstall).filter(pack -> {
            String searchText = this.searchField.getText();

            if (!searchText.isEmpty()) {
                return Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                        .matcher(pack.getDescription()).find()
                        || Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE).matcher(pack.getName())
                                .find();
            }

            return true;
        }).collect(Collectors.toList()));
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
        return (this.isFeatured ? GetText.tr("Featured Packs")
                : (this.isSystem ? GetText.tr("Vanilla Packs") : GetText.tr("Packs")));
    }

    @Override
    public void onRelocalization() {
        addButton.setText(GetText.tr("Add Pack"));
        addCurseButton.setText(GetText.tr("Add CurseForge Pack"));
        addFTBPackButton.setText(GetText.tr("Add FTB Pack"));
        clearButton.setText(GetText.tr("Clear"));
        expandAllButton.setText(GetText.tr("Expand All"));
        collapseAllButton.setText(GetText.tr("Collapse All"));
        searchButton.setText(GetText.tr("Search"));

        if (nilCard != null) {
            nilCard.setMessage(GetText.tr("There are no packs to display.\n\nPlease check back another time."));
        }
    }
}
