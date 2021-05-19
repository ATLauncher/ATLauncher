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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
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
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

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
        if (!isServer && InstanceManager.isInstance(nameField.getText())) {
            DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                    .text(GetText.tr("An instance already exists with that name.<br/><br/>Rename it and try again."))
                    .build()).setType(DialogManager.ERROR).show();
            return;
        } else if (!isServer && nameField.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Instance name is invalid. It must contain at least 1 letter or number."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return;
        } else if (isServer && ServerManager.isServer(nameField.getText())) {
            DialogManager.okDialog().setTitle(GetText.tr("Error")).setContent(new HTMLBuilder().center()
                    .text(GetText.tr("A server already exists with that name.<br/><br/>Rename it and try again."))
                    .build()).setType(DialogManager.ERROR).show();
            return;
        } else if (isServer && nameField.getText().replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Server name is invalid. It must contain at least 1 letter or number."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return;
        }

        final Pack pack = new Pack();
        pack.name = "Vanilla Minecraft";

        final PackVersion version = new PackVersion();
        version.version = selectedMinecraftVersion;

        try {
            version.minecraftVersion = MinecraftManager.getMinecraftVersion(selectedMinecraftVersion);
        } catch (InvalidMinecraftVersion e1) {
            LogManager.logStackTrace(e1);
            return;
        }

        String dialogTitle = // #. {0} is the name of the Minecraft version the user is installing
                isServer ? GetText.tr("Installing Minecraft {0} Server", selectedMinecraftVersion)
                        // #. {0} is the name of the Minecraft version the user is installing
                        : GetText.tr("Installing Minecraft {0}", selectedMinecraftVersion);

        final JDialog dialog = new JDialog(App.launcher.getParent(), dialogTitle, ModalityType.DOCUMENT_MODAL);
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setSize(300, 100);
        dialog.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        final JLabel doing = new JLabel(GetText.tr("Starting Install Process"));
        doing.setHorizontalAlignment(JLabel.CENTER);
        doing.setVerticalAlignment(JLabel.TOP);
        topPanel.add(doing);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar(0, 10000);
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);
        JProgressBar subProgressBar = new JProgressBar(0, 10000);
        bottomPanel.add(subProgressBar, BorderLayout.SOUTH);
        subProgressBar.setValue(0);
        subProgressBar.setVisible(false);

        dialog.add(topPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        LoaderVersion loaderVersion = null;

        final InstanceInstaller instanceInstaller = new InstanceInstaller(nameField.getText(), pack, version, false,
                isServer, false, null, false, loaderVersion, null, null, null, null, null) {

            protected void done() {
                Boolean success = false;
                int type;
                String text;
                String title;
                if (isCancelled()) {
                    type = DialogManager.ERROR;

                    if (isReinstall) {
                        // #. {0} is the pack name and {1} is the pack version
                        title = GetText.tr("{0} {1} Not Reinstalled", pack.getName(), version.version);

                        // #. {0} is the pack name and {1} is the pack version
                        text = GetText.tr("{0} {1} wasn't reinstalled.<br/><br/>Check error logs for more information.",
                                pack.getName(), version.version);
                    } else {
                        // #. {0} is the pack name and {1} is the pack version
                        title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                        // #. {0} is the pack name and {1} is the pack version
                        text = GetText.tr("{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
                                pack.getName(), version.version);

                        if (Files.exists(this.root) && Files.isDirectory(this.root)) {
                            FileUtils.deleteDirectory(this.root);
                        }
                    }
                } else {
                    type = DialogManager.INFO;

                    try {
                        success = get();
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        return;
                    } catch (ExecutionException e) {
                        LogManager.logStackTrace(e);
                    }

                    if (success) {
                        type = DialogManager.INFO;

                        // #. {0} is the pack name and {1} is the pack version
                        title = GetText.tr("{0} {1} Installed", pack.getName(), version.version);

                        if (isReinstall) {
                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr("{0} {1} has been reinstalled.", pack.getName(), version.version);
                        } else if (isServer) {
                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr("{0} {1} server has been installed.<br/><br/>Find it in the servers tab.",
                                    pack.getName(), version.version);
                        } else {
                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr("{0} {1} has been installed.<br/><br/>Find it in the instances tab.",
                                    pack.getName(), version.version);
                        }

                        if (isServer) {
                            App.launcher.reloadServersPanel();
                        } else {
                            App.launcher.reloadInstancesPanel();
                        }
                    } else {
                        if (isReinstall) {
                            // #. {0} is the pack name and {1} is the pack version
                            title = GetText.tr("{0} {1} Not Reinstalled", pack.getName(), version.version);

                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr(
                                    "{0} {1} wasn't reinstalled.<br/><br/>Check error logs for more information.",
                                    pack.getName(), version.version);
                        } else {
                            // #. {0} is the pack name and {1} is the pack version
                            title = GetText.tr("{0} {1} Not Installed", pack.getName(), version.version);

                            // #. {0} is the pack name and {1} is the pack version
                            text = GetText.tr(
                                    "{0} {1} wasn't installed.<br/><br/>Check error logs for more information.",
                                    pack.getName(), version.version);
                        }
                    }
                }

                if (this.curseForgeExtractedPath != null) {
                    FileUtils.deleteDirectory(this.curseForgeExtractedPath);
                }

                if (this.multiMCExtractedPath != null) {
                    FileUtils.deleteDirectory(this.multiMCExtractedPath);
                }

                dialog.dispose();

                DialogManager.okDialog().setTitle(title).setContent(new HTMLBuilder().center().text(text).build())
                        .setType(type).show();
            }
        };

        instanceInstaller.addPropertyChangeListener(evt -> {
            if ("progress" == evt.getPropertyName()) {
                if (progressBar.isIndeterminate()) {
                    progressBar.setIndeterminate(false);
                }
                double progress = 0.0;
                if (evt.getNewValue() instanceof Double) {
                    progress = (Double) evt.getNewValue();
                } else if (evt.getNewValue() instanceof Integer) {
                    progress = ((Integer) evt.getNewValue()) * 100.0;
                }
                if (progress > 100.0) {
                    progress = 100.0;
                }
                progressBar.setValue((int) Math.round(progress * 100.0));
            } else if ("subprogress" == evt.getPropertyName()) {
                if (!subProgressBar.isVisible()) {
                    subProgressBar.setVisible(true);
                }
                if (subProgressBar.isIndeterminate()) {
                    subProgressBar.setIndeterminate(false);
                }
                double progress;
                String paint = null;
                if (evt.getNewValue() instanceof Double) {
                    progress = (Double) evt.getNewValue();
                } else if (evt.getNewValue() instanceof Integer) {
                    progress = ((Integer) evt.getNewValue()) * 100.0;
                } else {
                    String[] parts = (String[]) evt.getNewValue();
                    progress = Double.parseDouble(parts[0]);
                    paint = parts[1];
                }
                if (progress >= 100.0) {
                    progress = 100.0;
                }
                if (progress < 0.0) {
                    if (subProgressBar.isStringPainted()) {
                        subProgressBar.setStringPainted(false);
                    }
                    subProgressBar.setVisible(false);
                } else {
                    if (!subProgressBar.isStringPainted()) {
                        subProgressBar.setStringPainted(true);
                    }
                    if (paint != null) {
                        subProgressBar.setString(paint);
                    }
                }
                if (paint == null && progress > 0.0) {
                    subProgressBar.setString(String.format("%.2f%%", progress));
                }
                subProgressBar.setValue((int) Math.round(progress * 100.0));
            } else if ("subprogressint" == evt.getPropertyName()) {
                if (subProgressBar.isStringPainted()) {
                    subProgressBar.setStringPainted(false);
                }
                if (!subProgressBar.isVisible()) {
                    subProgressBar.setVisible(true);
                }
                if (!subProgressBar.isIndeterminate()) {
                    subProgressBar.setIndeterminate(true);
                }
            } else if ("doing" == evt.getPropertyName()) {
                String doingText = (String) evt.getNewValue();
                doing.setText(doingText);
            }

        });
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                instanceInstaller.cancel(true);
            }
        });
        instanceInstaller.execute();
        dialog.setVisible(true);

    }

    @Override
    public String getTitle() {
        return GetText.tr("Vanilla Packs");
    }
}
