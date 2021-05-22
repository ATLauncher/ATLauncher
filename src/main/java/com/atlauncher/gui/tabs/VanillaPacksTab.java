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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.utils.Utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class VanillaPacksTab extends JPanel implements Tab {
    private List<VersionManifestVersionType> minecraftVersionTypeFilters = new ArrayList<>(
            Arrays.asList(VersionManifestVersionType.RELEASE));
    private String selectedMinecraftVersion = null;

    private JTextField nameField = new JTextField(32);
    private boolean nameFieldDirty = false;

    private JTextArea descriptionField = new JTextArea(2, 40);
    private boolean descriptionFieldDirty = false;

    private JTable minecraftVersionTable;
    private DefaultTableModel minecraftVersionTableModel;

    public VanillaPacksTab() {
        super(new BorderLayout());
        setName("vanillaPacksPanel");

        setupMainPanel();
        setupBottomPanel();
    }

    private void setupMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

        JLabel nameLabel = new JLabel(GetText.tr("Instance Name") + ":");
        mainPanel.add(nameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            private void checkDirty(DocumentEvent e) {
                if (selectedMinecraftVersion == null) {
                    return;
                }

                String currentValue = nameField.getText();

                // if the name is the same as the default is, then we're not dirty
                nameFieldDirty = !currentValue.equals(String.format("Minecraft %s", selectedMinecraftVersion));
            }
        });
        mainPanel.add(nameField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabel descriptionLabel = new JLabel(GetText.tr("Description") + ":");
        mainPanel.add(descriptionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JScrollPane descriptionScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setPreferredSize(new Dimension(450, 80));
        descriptionScrollPane.setViewportView(descriptionField);

        descriptionField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkDirty(e);
            }

            private void checkDirty(DocumentEvent e) {
                if (selectedMinecraftVersion == null) {
                    return;
                }

                String currentValue = descriptionField.getText();

                // if the description is the same as the default is, then we're not dirty
                descriptionFieldDirty = !currentValue.equals(String.format("Minecraft %s", selectedMinecraftVersion));
            }
        });
        mainPanel.add(descriptionScrollPane, gbc);

        // Minecraft Version
        gbc.gridx = 0;
        gbc.gridy += 2;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.NORTHEAST;

        JPanel minecraftVersionPanel = new JPanel();
        minecraftVersionPanel.setLayout(new BoxLayout(minecraftVersionPanel, BoxLayout.Y_AXIS));

        JLabel minecraftVersionLabel = new JLabel(GetText.tr("Minecraft Version") + ":");
        minecraftVersionPanel.add(minecraftVersionLabel);

        minecraftVersionPanel.add(Box.createVerticalStrut(20));

        JPanel minecraftVersionFilterPanel = new JPanel();
        minecraftVersionFilterPanel.setLayout(new BoxLayout(minecraftVersionFilterPanel, BoxLayout.Y_AXIS));

        JLabel minecraftVersionFilterLabel = new JLabel(GetText.tr("Filter"));
        minecraftVersionFilterLabel.setFont(App.THEME.getBoldFont());
        minecraftVersionFilterPanel.add(minecraftVersionFilterLabel);

        JCheckBox minecraftVersionReleasesFilterCheckbox = new JCheckBox(GetText.tr("Releases"));
        minecraftVersionReleasesFilterCheckbox.setSelected(true);
        minecraftVersionReleasesFilterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (minecraftVersionReleasesFilterCheckbox.isSelected()) {
                    minecraftVersionTypeFilters.add(VersionManifestVersionType.RELEASE);
                } else {
                    minecraftVersionTypeFilters.remove(VersionManifestVersionType.RELEASE);
                }

                reloadMinecraftVersionsTable();
            }
        });
        minecraftVersionFilterPanel.add(minecraftVersionReleasesFilterCheckbox);

        JCheckBox minecraftVersionSnapshotsFilterCheckbox = new JCheckBox(GetText.tr("Snapshots"));
        minecraftVersionSnapshotsFilterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (minecraftVersionSnapshotsFilterCheckbox.isSelected()) {
                    minecraftVersionTypeFilters.add(VersionManifestVersionType.SNAPSHOT);
                } else {
                    minecraftVersionTypeFilters.remove(VersionManifestVersionType.SNAPSHOT);
                }

                reloadMinecraftVersionsTable();
            }
        });
        minecraftVersionFilterPanel.add(minecraftVersionSnapshotsFilterCheckbox);

        JCheckBox minecraftVersionBetasFilterCheckbox = new JCheckBox(GetText.tr("Betas"));
        minecraftVersionBetasFilterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (minecraftVersionBetasFilterCheckbox.isSelected()) {
                    minecraftVersionTypeFilters.add(VersionManifestVersionType.OLD_BETA);
                } else {
                    minecraftVersionTypeFilters.remove(VersionManifestVersionType.OLD_BETA);
                }

                reloadMinecraftVersionsTable();
            }
        });
        minecraftVersionFilterPanel.add(minecraftVersionBetasFilterCheckbox);

        JCheckBox minecraftVersionAlphasFilterCheckbox = new JCheckBox(GetText.tr("Alphas"));
        minecraftVersionAlphasFilterCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (minecraftVersionAlphasFilterCheckbox.isSelected()) {
                    minecraftVersionTypeFilters.add(VersionManifestVersionType.OLD_ALPHA);
                } else {
                    minecraftVersionTypeFilters.remove(VersionManifestVersionType.OLD_ALPHA);
                }

                reloadMinecraftVersionsTable();
            }
        });
        minecraftVersionFilterPanel.add(minecraftVersionAlphasFilterCheckbox);

        minecraftVersionPanel.add(minecraftVersionFilterPanel);

        mainPanel.add(minecraftVersionPanel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JScrollPane minecraftVersionScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        minecraftVersionScrollPane.setPreferredSize(new Dimension(450, 300));

        setupMinecraftVersionsTable();
        reloadMinecraftVersionsTable();
        minecraftVersionScrollPane.setViewportView(minecraftVersionTable);

        mainPanel.add(minecraftVersionScrollPane, gbc);

        // Enable User Lock
        gbc.gridx = 0;
        gbc.gridy += 2;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabel enableUserLockLabel = new JLabelWithHover(GetText.tr("Enable User Lock") + "?",
                Utils.getIconImage(App.THEME.getIconPath("question")),
                new HTMLBuilder().center().text(GetText.tr(
                        "Enabling the user lock setting will lock this instance to only be played<br/>by the person installing this instance (you) and will not show the instance to anyone else."))
                        .build());
        mainPanel.add(enableUserLockLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JCheckBox enableUserLock = new JCheckBox();
        mainPanel.add(enableUserLock, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupMinecraftVersionsTable() {
        minecraftVersionTableModel = new DefaultTableModel(new String[][] {},
                new String[] { GetText.tr("Version"), GetText.tr("Released"), GetText.tr("Type") }) {

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };
        minecraftVersionTable = new JTable(minecraftVersionTableModel);

        ListSelectionModel sm = minecraftVersionTable.getSelectionModel();
        sm.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                int minIndex = lsm.getMinSelectionIndex();
                int maxIndex = lsm.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (lsm.isSelectedIndex(i)) {
                        selectedMinecraftVersion = (String) minecraftVersionTableModel.getValueAt(i, 0);
                        selectedMinecraftVersionChanged(selectedMinecraftVersion);
                    }
                }
            }
        });

        TableColumnModel cm = minecraftVersionTable.getColumnModel();
        cm.getColumn(0).setResizable(false);

        cm.getColumn(1).setResizable(false);
        cm.getColumn(1).setMaxWidth(200);

        cm.getColumn(2).setResizable(false);
        cm.getColumn(2).setMaxWidth(200);

        minecraftVersionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        minecraftVersionTable.setShowVerticalLines(false);
    }

    private void selectedMinecraftVersionChanged(String selectedMinecraftVersion) {
        String defaultValue = String.format("Minecraft %s", selectedMinecraftVersion);

        if (!nameFieldDirty) {
            nameField.setText(defaultValue);
        }

        if (!descriptionFieldDirty) {
            descriptionField.setText(defaultValue);
        }
    }

    private void reloadMinecraftVersionsTable() {
        // remove all rows
        int rowCount = minecraftVersionTableModel.getRowCount();
        if (rowCount > 0) {
            for (int i = rowCount - 1; i >= 0; i--) {
                minecraftVersionTableModel.removeRow(i);
            }
        }

        List<VersionManifestVersion> minecraftVersions = MinecraftManager
                .getFilteredMinecraftVersions(minecraftVersionTypeFilters);

        DateTimeFormatter fmt = DateTimeFormat.forPattern(App.settings.dateFormat);
        minecraftVersions.stream().forEach(mv -> {
            minecraftVersionTableModel.addRow(new String[] { mv.id,
                    fmt.print(ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime)), mv.type.toString() });
        });

        // figure out which row to select
        int newSelectedRow = 0;
        if (selectedMinecraftVersion != null) {
            Optional<VersionManifestVersion> versionToSelect = minecraftVersions.stream()
                    .filter(mv -> mv.id.equals(selectedMinecraftVersion)).findFirst();

            if (versionToSelect.isPresent()) {
                newSelectedRow = minecraftVersions.indexOf(versionToSelect.get());
            }
        }

        minecraftVersionTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);

        // refresh the table
        minecraftVersionTable.revalidate();
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton createServerButton = new JButton(GetText.tr("Create Server"));
        bottomPanel.add(createServerButton);
        createServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                install(true);
            }
        });

        JButton createInstanceButton = new JButton(GetText.tr("Create Instance"));
        bottomPanel.add(createInstanceButton);
        createInstanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                install(false);
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void install(boolean isServer) {
        Installable installable;
        try {
            installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(selectedMinecraftVersion));
            installable.instanceName = nameField.getText();
            installable.isReinstall = false;
            installable.isServer = isServer;

            installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Vanilla Packs");
    }
}
