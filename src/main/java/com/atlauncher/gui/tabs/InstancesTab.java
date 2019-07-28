/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.InstanceV2Card;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.network.Analytics;

import org.mini2Dx.gettext.GetText;

public class InstancesTab extends JPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = -969812552965390610L;
    private JPanel topPanel;
    private JButton clearButton;
    private JTextField searchBox;
    private JButton searchButton;
    private JCheckBox hasUpdate;
    private JLabel hasUpdateLabel;

    private String searchText = null;
    private boolean isUpdate = false;

    private JPanel panel;
    private JScrollPane scrollPane;
    private int currentPosition = 0;

    private NilCard nilCard;

    public InstancesTab() {
        setLayout(new BorderLayout());
        loadContent(false);
        RelocalizationManager.addListener(this);
    }

    public void loadContent(boolean keepFilters) {
        topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        clearButton = new JButton(GetText.tr("Clear"));
        clearButton.addActionListener(e -> {
            searchBox.setText("");
            hasUpdate.setSelected(false);
            reload();
        });
        topPanel.add(clearButton);

        searchBox = new JTextField(16);
        if (keepFilters) {
            searchBox.setText(this.searchText);
        }
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    Analytics.sendEvent(searchBox.getText(), "Search", "Instance");
                    reload();
                }
            }
        });
        topPanel.add(searchBox);

        searchButton = new JButton(GetText.tr("Search"));
        searchButton.addActionListener(e -> {
            Analytics.sendEvent(searchBox.getText(), "Search", "Instance");
            reload();
        });
        topPanel.add(searchButton);

        hasUpdate = new JCheckBox();
        hasUpdate.setSelected(isUpdate);
        hasUpdate.addActionListener(e -> reload());
        topPanel.add(hasUpdate);

        hasUpdateLabel = new JLabel(GetText.tr("Has Update"));
        topPanel.add(hasUpdateLabel);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        App.settings.getInstancesSorted().stream().filter(Instance::canPlay).forEach(instance -> {
            if (keepFilters) {
                boolean showInstance = true;

                if (searchText != null) {
                    if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                            .matcher(instance.getName()).find()) {
                        showInstance = false;
                    }
                }

                if (isUpdate) {
                    if (!instance.hasUpdate()) {
                        showInstance = false;
                    }
                }

                if (showInstance) {
                    panel.add(new InstanceCard(instance), gbc);
                    gbc.gridy++;
                }
            } else {
                panel.add(new InstanceCard(instance), gbc);
                gbc.gridy++;
            }
        });

        App.settings.getInstancesV2Sorted().stream().forEach(instance -> {
            if (keepFilters) {
                boolean showInstance = true;

                if (searchText != null) {
                    if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                            .matcher(instance.launcher.name).find()) {
                        showInstance = false;
                    }
                }

                if (isUpdate) {
                    if (!instance.hasUpdate()) {
                        showInstance = false;
                    }
                }

                if (showInstance) {
                    panel.add(new InstanceV2Card(instance), gbc);
                    gbc.gridy++;
                }
            } else {
                panel.add(new InstanceV2Card(instance), gbc);
                gbc.gridy++;
            }
        });

        if (panel.getComponentCount() == 0) {
            nilCard = new NilCard(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
            panel.add(nilCard, gbc);
        }

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(currentPosition));
    }

    public void reload() {
        this.currentPosition = scrollPane.getVerticalScrollBar().getValue();
        this.searchText = searchBox.getText();
        this.isUpdate = hasUpdate.isSelected();
        if (this.searchText.isEmpty()) {
            this.searchText = null;
        }
        removeAll();
        loadContent(true);
        validate();
        repaint();
        searchBox.requestFocus();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Instances");
    }

    @Override
    public void onRelocalization() {
        clearButton.setText(GetText.tr("Clear"));
        searchButton.setText(GetText.tr("Search"));
        hasUpdateLabel.setText(GetText.tr("Has Update"));

        if (nilCard != null) {
            nilCard.setMessage(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
        }
    }
}
