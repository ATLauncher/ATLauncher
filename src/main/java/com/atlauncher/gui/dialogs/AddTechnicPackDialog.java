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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.technic.TechnicModpackSlim;
import com.atlauncher.gui.card.TechnicPackCard;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.panels.NoTechnicPacksPanel;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.TechnicApi;

import org.mini2Dx.gettext.GetText;

// TODO: this, AddFTBPackDialog and AddModDialog are all very similar and can probably be refactored to be shared
@SuppressWarnings("serial")
public final class AddTechnicPackDialog extends JDialog {
    private final JPanel contentPanel = new JPanel(new GridLayout(Constants.TECHNIC_PAGINATION_SIZE / 2, 2));
    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JTextField searchField = new JTextField(16);
    private final JButton searchButton = new JButton(GetText.tr("Search"));
    private JLabel platformMessageLabel = null;

    private JScrollPane jscrollPane;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public AddTechnicPackDialog() {
        super(App.launcher.getParent(), GetText.tr("Add Technic Pack"), ModalityType.DOCUMENT_MODAL);

        this.setPreferredSize(new Dimension(620, 500));
        this.setMinimumSize(new Dimension(620, 500));
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        String platformMessage = ConfigManager.getConfigItem("platforms.technic.message", null);
        if (platformMessage != null) {
            platformMessageLabel = new JLabel(new HTMLBuilder().center().text(platformMessage).build());
        }

        setupComponents();

        this.loadDefaultPacks();

        this.pack();
        this.setLocationRelativeTo(App.launcher.getParent());
        this.setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Add Technic Pack Dialog");

        this.topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchButtonsPanel.add(this.searchField);
        searchButtonsPanel.add(this.searchButton);

        this.topPanel.add(searchButtonsPanel, BorderLayout.NORTH);

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(this.topPanel, BorderLayout.NORTH);
        mainPanel.add(this.jscrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        if (platformMessageLabel != null) {
            platformMessageLabel.setForeground(Color.YELLOW);
            bottomPanel.add(platformMessageLabel, BorderLayout.NORTH);
        }

        this.add(mainPanel, BorderLayout.CENTER);
        this.add(bottomPanel, BorderLayout.SOUTH);

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

    private void getPacks() {
        setLoading(true);

        String query = searchField.getText();

        new Thread(() -> {
            if (query.isEmpty()) {
                setPacks(TechnicApi.getTrendingModpacks().modpacks);
            } else {
                setPacks(TechnicApi.searchModpacks(query).modpacks);
            }

            setLoading(false);
        }).start();
    }

    private void loadDefaultPacks() {
        getPacks();
    }

    private void searchForPacks() {
        String query = searchField.getText();

        Analytics.sendEvent(query, "Search", "TechnicPack");

        getPacks();
    }

    private void setPacks(List<TechnicModpackSlim> modpacks) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        if (modpacks.size() == 0) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoTechnicPacksPanel(!this.searchField.getText().isEmpty()), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new WrapLayout());

            modpacks.forEach(modpack -> {
                contentPanel.add(new TechnicPackCard(modpack, e -> {
                    Analytics.sendEvent(modpack.name, "Add", "TechnicPack");
                    new InstanceInstallerDialog(this, modpack);
                }), gbc);
                gbc.gridy++;
            });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }
}
