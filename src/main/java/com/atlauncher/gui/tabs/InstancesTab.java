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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.dialogs.AddCurseForgePackDialog;
import com.atlauncher.gui.dialogs.AddFTBPackDialog;
import com.atlauncher.gui.dialogs.ImportInstanceDialog;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.network.Analytics;

import org.mini2Dx.gettext.GetText;

public class InstancesTab extends JPanel implements Tab, RelocalizationListener {
    private static final long serialVersionUID = -969812552965390610L;
    private JButton clearButton;
    private JTextField searchField;
    private JButton searchButton;

    private String searchText = null;

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
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton importButton = new JButton(GetText.tr("Import"));
        importButton.addActionListener(e -> new ImportInstanceDialog());

        JButton addCurseForgePackButton = new JButton(GetText.tr("Add CurseForge Pack"));
        addCurseForgePackButton.addActionListener(e -> new AddCurseForgePackDialog());

        JButton addFTBPackButton = new JButton(GetText.tr("Add FTB Pack"));
        addFTBPackButton.addActionListener(e -> new AddFTBPackDialog());

        searchField = new JTextField(16);
        if (keepFilters) {
            searchField.setText(this.searchText);
        }
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    Analytics.sendEvent(searchField.getText(), "Search", "Instance");
                    reload();
                }
            }
        });
        searchField.setMaximumSize(new Dimension(190, 23));

        searchButton = new JButton(GetText.tr("Search"));
        searchButton.addActionListener(e -> {
            Analytics.sendEvent(searchField.getText(), "Search", "Instance");
            reload();
        });

        clearButton = new JButton(GetText.tr("Clear"));
        clearButton.addActionListener(e -> {
            searchField.setText("");
            reload();
        });

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        topPanel.add(importButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(addCurseForgePackButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(addFTBPackButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(searchButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(clearButton);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel();
        panel.setName("instancesPanel");
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.fill = GridBagConstraints.BOTH;

        InstanceManager.getInstancesSorted().forEach(instance -> {
            if (keepFilters) {
                boolean showInstance = true;

                if (searchText != null) {
                    if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                            .matcher(instance.launcher.name).find()) {
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

        if (panel.getComponentCount() == 0) {
            nilCard = new NilCard(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
            panel.add(nilCard, gbc);
        }

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(currentPosition));
    }

    public void reload() {
        this.currentPosition = scrollPane.getVerticalScrollBar().getValue();
        this.searchText = searchField.getText();
        if (this.searchText.isEmpty()) {
            this.searchText = null;
        }
        removeAll();
        loadContent(true);
        validate();
        repaint();
        searchField.requestFocus();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Instances");
    }

    @Override
    public void onRelocalization() {
        clearButton.setText(GetText.tr("Clear"));
        searchButton.setText(GetText.tr("Search"));

        if (nilCard != null) {
            nilCard.setMessage(GetText.tr("There are no instances to display.\n\nInstall one from the Packs tab."));
        }
    }
}
