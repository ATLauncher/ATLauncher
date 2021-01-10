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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.modpacksch.ModpacksChPackList;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.gui.card.FTBPackCard;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.panels.NoFTBPacksPanel;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ComboItem;

import org.mini2Dx.gettext.GetText;

import okhttp3.CacheControl;

@SuppressWarnings("serial")
public final class AddFTBPackDialog extends JDialog {
    private final JPanel contentPanel = new JPanel();
    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(16);
    private final JButton searchButton = new JButton(GetText.tr("Search"));
    private final JComboBox<ComboItem<String>> sortComboBox = new JComboBox<ComboItem<String>>();

    private JScrollPane jscrollPane;
    private JButton nextButton;
    private JButton prevButton;
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private int page = 1;

    public AddFTBPackDialog() {
        super(App.launcher.getParent(), GetText.tr("Add FTB Pack"), ModalityType.DOCUMENT_MODAL);

        this.setPreferredSize(new Dimension(620, 500));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        sortComboBox.addItem(new ComboItem<>("popular/plays", GetText.tr("Most Popular")));
        sortComboBox.addItem(new ComboItem<>("popular/installs", GetText.tr("Most Installed")));
        sortComboBox.addItem(new ComboItem<>("updated", GetText.tr("Recently Updated")));
        sortComboBox.addItem(new ComboItem<>("featured", GetText.tr("Featured")));

        setupComponents();

        this.loadDefaultPacks();

        this.pack();
        this.setLocationRelativeTo(App.launcher.getParent());
        this.setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Add FTB Pack Dialog");

        this.topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchButtonsPanel.add(this.searchField);
        searchButtonsPanel.add(this.searchButton);
        searchButtonsPanel.add(new JLabel(GetText.tr("Sort") + ":"));
        searchButtonsPanel.add(this.sortComboBox);

        this.topPanel.add(searchButtonsPanel, BorderLayout.NORTH);

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(this.topPanel, BorderLayout.NORTH);
        mainPanel.add(this.jscrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());

        prevButton = new JButton("<<");
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> goToPreviousPage());

        nextButton = new JButton(">>");
        nextButton.setEnabled(false);
        nextButton.addActionListener(e -> goToNextPage());

        bottomPanel.add(prevButton);
        bottomPanel.add(nextButton);

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

        this.sortComboBox.addActionListener(e -> {
            searchForPacks();
        });

        this.searchField.addActionListener(e -> searchForPacks());

        this.searchButton.addActionListener(e -> searchForPacks());
    }

    private void setLoading(boolean loading) {
        if (loading) {
            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new LoadingPanel(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private void goToPreviousPage() {
        if (page > 1) {
            page -= 1;
        }

        Analytics.sendEvent(page, "Previous", "Navigation", "FTBPack");

        getPacks();
    }

    private void goToNextPage() {
        if (contentPanel.getComponentCount() != 0) {
            page += 1;
        }

        Analytics.sendEvent(page, "Next", "Navigation", "FTBPack");

        getPacks();
    }

    @SuppressWarnings("unchecked")
    private void getPacks() {
        setLoading(true);
        prevButton.setEnabled(false);
        nextButton.setEnabled(false);

        String query = searchField.getText();

        new Thread(() -> {
            ModpacksChPackList packList;

            if (query.isEmpty()) {
                packList = com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/modpack/%s/250", Constants.MODPACKS_CH_API_URL,
                                ((ComboItem<String>) sortComboBox.getSelectedItem()).getValue()))
                        .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                        .asClass(ModpacksChPackList.class);
            } else {
                packList = com.atlauncher.network.Download.build()
                        .setUrl(String.format("%s/modpack/search/%d?term=%s", Constants.MODPACKS_CH_API_URL,
                                Constants.MODPACKS_CH_PAGINATION_SIZE, query))
                        .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                        .asClass(ModpacksChPackList.class);
            }

            List<Integer> packsToShow = packList.packs.stream().skip((page - 1) * Constants.MODPACKS_CH_PAGINATION_SIZE)
                    .limit(Constants.MODPACKS_CH_PAGINATION_SIZE).collect(Collectors.toList());

            List<ModpacksChPackManifest> packs = packsToShow.parallelStream()
                    .map(packId -> com.atlauncher.network.Download.build()
                            .setUrl(String.format("%s/modpack/%s", Constants.MODPACKS_CH_API_URL, packId))
                            .cached(new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build())
                            .asClass(ModpacksChPackManifest.class))
                    .collect(Collectors.toList());

            setPacks(packs);

            setLoading(false);
        }).start();
    }

    private void loadDefaultPacks() {
        getPacks();
    }

    private void searchForPacks() {
        String query = searchField.getText();

        Analytics.sendEvent(query, "Search", "FTBPack");

        getPacks();
    }

    private void setPacks(List<ModpacksChPackManifest> packs) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        prevButton.setEnabled(page > 1);
        nextButton.setEnabled(packs.size() == Constants.MODPACKS_CH_PAGINATION_SIZE);

        if (packs.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoFTBPacksPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new WrapLayout());

            packs.forEach(ftbPack -> {
                contentPanel.add(new FTBPackCard(this, ftbPack), gbc);
                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }
}
