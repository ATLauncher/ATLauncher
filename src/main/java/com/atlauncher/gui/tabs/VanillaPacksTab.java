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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.utils.ComboItem;

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

    private JCheckBox minecraftVersionReleasesFilterCheckbox = new JCheckBox(GetText.tr("Releases"));
    private JCheckBox minecraftVersionSnapshotsFilterCheckbox = new JCheckBox(GetText.tr("Snapshots"));
    private JCheckBox minecraftVersionBetasFilterCheckbox = new JCheckBox(GetText.tr("Betas"));
    private JCheckBox minecraftVersionAlphasFilterCheckbox = new JCheckBox(GetText.tr("Alphas"));

    private JTable minecraftVersionTable;
    private DefaultTableModel minecraftVersionTableModel;

    private ButtonGroup loaderTypeButtonGroup = new ButtonGroup();
    private JRadioButton loaderTypeNoneRadioButton = new JRadioButton(GetText.tr("None"));
    private JRadioButton loaderTypeFabricRadioButton = new JRadioButton("Fabric");
    private JRadioButton loaderTypeForgeRadioButton = new JRadioButton("Forge");

    private JComboBox<ComboItem<LoaderVersion>> loaderVersionsDropDown = new JComboBox<>();

    private JButton createServerButton = new JButton(GetText.tr("Create Server"));
    private JButton createInstanceButton = new JButton(GetText.tr("Create Instance"));

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
        gbc.anchor = GridBagConstraints.EAST;

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
                LoaderType selectedLoader = getSelectedLoader();

                // if the name is the same as the default is, then we're not dirty
                nameFieldDirty = !(currentValue.equals(String.format("Minecraft %s", selectedMinecraftVersion))
                        || (selectedLoader != null && currentValue.equals(
                                String.format("Minecraft %s with %s", selectedMinecraftVersion, selectedLoader))));
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
                LoaderType selectedLoader = getSelectedLoader();

                // if the description is the same as the default is, then we're not dirty
                descriptionFieldDirty = !(currentValue.equals(String.format("Minecraft %s", selectedMinecraftVersion))
                        || (selectedLoader != null && currentValue.equals(
                                String.format("Minecraft %s with %s", selectedMinecraftVersion, selectedLoader))));
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

        // Loader Type
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.EAST;

        JLabel loaderTypeLabel = new JLabel(GetText.tr("Loader") + "?");
        mainPanel.add(loaderTypeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        loaderTypeButtonGroup.add(loaderTypeNoneRadioButton);
        loaderTypeButtonGroup.add(loaderTypeFabricRadioButton);
        loaderTypeButtonGroup.add(loaderTypeForgeRadioButton);

        JPanel loaderTypePanel = new JPanel(new FlowLayout());
        loaderTypePanel.add(loaderTypeNoneRadioButton);
        loaderTypePanel.add(loaderTypeFabricRadioButton);
        loaderTypePanel.add(loaderTypeForgeRadioButton);

        loaderTypeNoneRadioButton.addActionListener(e -> {
            selectedLoaderTypeChanged(null);
        });
        loaderTypeFabricRadioButton.addActionListener(e -> {
            selectedLoaderTypeChanged(LoaderType.FABRIC);
        });
        loaderTypeForgeRadioButton.addActionListener(e -> {
            selectedLoaderTypeChanged(LoaderType.FORGE);
        });

        loaderTypeNoneRadioButton.setSelected(true);

        mainPanel.add(loaderTypePanel, gbc);

        // Loader Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabel loaderVersionLabel = new JLabel(GetText.tr("Loader Version") + ":");
        mainPanel.add(loaderVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        loaderVersionsDropDown.setEnabled(false);
        loaderVersionsDropDown.addItem(new ComboItem<LoaderVersion>(null, GetText.tr("Select Loader First")));
        mainPanel.add(loaderVersionsDropDown, gbc);

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
                        selectedMinecraftVersionChanged((String) minecraftVersionTableModel.getValueAt(i, 0));
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

    private void selectedMinecraftVersionChanged(String newSelectedMinecraftVersion) {
        if (selectedMinecraftVersion != newSelectedMinecraftVersion) {
            selectedMinecraftVersion = newSelectedMinecraftVersion;
            String defaultValue = String.format("Minecraft %s", newSelectedMinecraftVersion);

            try {
                VersionManifestVersion version = MinecraftManager.getMinecraftVersion(newSelectedMinecraftVersion);
                createServerButton.setVisible(version.hasServer());
            } catch (InvalidMinecraftVersion ignored) {
                createServerButton.setVisible(false);
            }

            if (!nameFieldDirty) {
                nameField.setText(defaultValue);
            }

            if (!descriptionFieldDirty) {
                descriptionField.setText(defaultValue);
            }

            // refresh the loader versions if we have one selected
            LoaderType selectedLoaderType = getSelectedLoader();
            if (selectedLoaderType != null) {
                selectedLoaderTypeChanged(selectedLoaderType);
            }
        }
    }

    private LoaderType getSelectedLoader() {
        if (loaderTypeFabricRadioButton.isSelected()) {
            return LoaderType.FABRIC;
        }

        if (loaderTypeForgeRadioButton.isSelected()) {
            return LoaderType.FORGE;
        }

        return null;
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

        // update checkboxes so not all of them can be unchecked
        minecraftVersionReleasesFilterCheckbox.setEnabled(
                !(minecraftVersionReleasesFilterCheckbox.isSelected() && minecraftVersionTypeFilters.size() == 1));
        minecraftVersionSnapshotsFilterCheckbox.setEnabled(
                !(minecraftVersionSnapshotsFilterCheckbox.isSelected() && minecraftVersionTypeFilters.size() == 1));
        minecraftVersionBetasFilterCheckbox.setEnabled(
                !(minecraftVersionBetasFilterCheckbox.isSelected() && minecraftVersionTypeFilters.size() == 1));
        minecraftVersionAlphasFilterCheckbox.setEnabled(
                !(minecraftVersionAlphasFilterCheckbox.isSelected() && minecraftVersionTypeFilters.size() == 1));
    }

    private void selectedLoaderTypeChanged(LoaderType selectedLoader) {
        loaderVersionsDropDown.removeAllItems();
        loaderVersionsDropDown.setEnabled(false);

        if (selectedLoader == null) {
            // update the name and description fields if they're not dirty
            String defaultNameFieldValue = String.format("Minecraft %s", selectedMinecraftVersion);
            if (!nameFieldDirty) {
                nameField.setText(defaultNameFieldValue);
            }

            if (!descriptionFieldDirty) {
                descriptionField.setText(defaultNameFieldValue);
            }

            loaderVersionsDropDown.addItem(new ComboItem<LoaderVersion>(null, GetText.tr("Select Loader First")));
            return;
        }

        loaderVersionsDropDown.addItem(new ComboItem<LoaderVersion>(null, GetText.tr("Getting Loader Versions")));

        loaderTypeNoneRadioButton.setEnabled(false);
        loaderTypeFabricRadioButton.setEnabled(false);
        loaderTypeForgeRadioButton.setEnabled(false);
        loaderVersionsDropDown.setEnabled(false);
        createServerButton.setEnabled(false);
        createInstanceButton.setEnabled(false);

        Runnable r = () -> {
            List<LoaderVersion> loaderVersions = new ArrayList<>();

            if (selectedLoader == LoaderType.FABRIC) {
                loaderVersions.addAll(FabricLoader.getChoosableVersions(selectedMinecraftVersion));
            } else if (selectedLoader == LoaderType.FORGE) {
                loaderVersions.addAll(ForgeLoader.getChoosableVersions(selectedMinecraftVersion));
            }

            if (loaderVersions.size() == 0) {
                loaderVersionsDropDown.removeAllItems();
                loaderVersionsDropDown.addItem(new ComboItem<LoaderVersion>(null, GetText.tr("No Versions Found")));
                loaderTypeNoneRadioButton.setEnabled(true);
                loaderTypeFabricRadioButton.setEnabled(true);
                loaderTypeForgeRadioButton.setEnabled(true);
                createServerButton.setEnabled(true);
                createInstanceButton.setEnabled(true);
                return;
            }

            int loaderVersionLength = 0;

            // ensures that font width is taken into account
            for (LoaderVersion version : loaderVersions) {
                loaderVersionLength = Math.max(loaderVersionLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toString()) + 25);
            }

            loaderVersionsDropDown.removeAllItems();

            loaderVersions.forEach(version -> loaderVersionsDropDown
                    .addItem(new ComboItem<LoaderVersion>(version, version.toString())));

            // ensures that the dropdown is at least 200 px wide
            loaderVersionLength = Math.max(200, loaderVersionLength);

            // ensures that there is a maximum width of 400 px to prevent overflow
            loaderVersionLength = Math.min(400, loaderVersionLength);

            loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

            loaderTypeNoneRadioButton.setEnabled(true);
            loaderTypeFabricRadioButton.setEnabled(true);
            loaderTypeForgeRadioButton.setEnabled(true);
            loaderVersionsDropDown.setEnabled(true);
            createServerButton.setEnabled(true);
            createInstanceButton.setEnabled(true);

            // update the name and description fields if they're not dirty
            String defaultNameFieldValue = String.format("Minecraft %s with %s", selectedMinecraftVersion,
                    selectedLoader.toString());
            if (!nameFieldDirty) {
                nameField.setText(defaultNameFieldValue);
            }

            if (!descriptionFieldDirty) {
                descriptionField.setText(defaultNameFieldValue);
            }
        };

        new Thread(r).start();
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());

        bottomPanel.add(createServerButton);
        createServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                install(true);
            }
        });

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
            LoaderVersion selectedLoaderVersion = ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getSelectedItem())
                    .getValue();

            installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(selectedMinecraftVersion),
                    selectedLoaderVersion, descriptionField.getText());
            installable.instanceName = nameField.getText();
            installable.isReinstall = false;
            installable.isServer = isServer;

            boolean success = installable.startInstall();

            if (success) {
                nameFieldDirty = false;
                descriptionFieldDirty = false;

                loaderTypeNoneRadioButton.setSelected(true);
                selectedLoaderTypeChanged(null);

                minecraftVersionTable.setRowSelectionInterval(0, 0);
            }
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public String getTitle() {
        return GetText.tr("Vanilla Packs");
    }
}
