/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.WindowUtils;

@SuppressWarnings("serial")
public class InstanceExportDialog extends JDialog {
    private final Instance instance;
    private final List<String> overrides = new ArrayList<>();

    private final JPanel topPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel();

    final ImageIcon HELP_ICON = Utils.getIconImage(App.THEME.getIconPath("question"));

    final GridBagConstraints gbc = new GridBagConstraints();

    public InstanceExportDialog(Instance instance) {
        // #. {0} is the name of the instance we're exporting
        super(App.launcher.getParent(), GetText.tr("Export {0}", instance.launcher.name), ModalityType.DOCUMENT_MODAL);
        this.instance = instance;

        setupComponents();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        WindowUtils.resizeForContent(this);

        setVisible(true);
    }

    private void setupComponents() {
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        topPanel.setLayout(new GridBagLayout());

        // Name
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover nameLabel = new JLabelWithHover(GetText.tr("Name") + ":", HELP_ICON,
                GetText.tr("The name of the instance"));
        topPanel.add(nameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField name = new JTextField(30);
        name.setText(Optional.ofNullable(instance.launcher.lastExportName).orElse(instance.launcher.name));
        topPanel.add(name, gbc);

        // Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover versionLabel = new JLabelWithHover(GetText.tr("Version") + ":", HELP_ICON,
                GetText.tr("The version of this instance"));
        topPanel.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField version = new JTextField(30);
        version.setText(Optional.ofNullable(instance.launcher.lastExportVersion).orElse(instance.launcher.version));
        topPanel.add(version, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover authorLabel = new JLabelWithHover(GetText.tr("Author") + ":", HELP_ICON,
                GetText.tr("Your name"));
        topPanel.add(authorLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JTextField author = new JTextField(30);
        author.setText(Optional.ofNullable(instance.launcher.lastExportAuthor)
                .orElse(AccountManager.getSelectedAccount() == null ? ""
                        : AccountManager.getSelectedAccount().minecraftUsername));
        topPanel.add(author, gbc);

        // Format
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        JLabelWithHover formatLabel = new JLabelWithHover(GetText.tr("Format") + ":", HELP_ICON,
                GetText.tr("Which format to export this instance as"));
        topPanel.add(formatLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        final JComboBox<ComboItem<InstanceExportFormat>> format = new JComboBox<>();
        format.addItem(new ComboItem<>(InstanceExportFormat.CURSEFORGE, "CurseForge"));
        format.addItem(new ComboItem<>(InstanceExportFormat.MODRINTH, "Modrinth"));
        format.addItem(new ComboItem<>(InstanceExportFormat.CURSEFORGE_AND_MODRINTH, "CurseForge & Modrinth"));
        format.addItem(new ComboItem<>(InstanceExportFormat.MULTIMC, "MultiMC"));
        topPanel.add(format, gbc);

        for (int i = 0; i < format.getItemCount(); i++) {
            ComboItem<InstanceExportFormat> item = format.getItemAt(i);

            if (item.getValue() == App.settings.defaultExportFormat) {
                format.setSelectedIndex(i);
                break;
            }
        }

        // Export File
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;
        JLabelWithHover saveToLabel = new JLabelWithHover(GetText.tr("Save To") + ":", HELP_ICON,
                GetText.tr("Select the folder you wish to export the instance to"));
        topPanel.add(saveToLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel saveToPanel = new JPanel();
        saveToPanel.setLayout(new BoxLayout(saveToPanel, BoxLayout.X_AXIS));

        final JTextField saveTo = new JTextField(25);
        saveTo.setText(Optional.ofNullable(instance.launcher.lastExportSaveTo)
                .orElse(instance.getRoot().toAbsolutePath().toString()));

        JButton browseButton = new JButton(GetText.tr("Browse"));
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(saveTo.getText()));
            chooser.setDialogTitle(GetText.tr("Select path to save to"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                saveTo.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        saveToPanel.add(saveTo);
        saveToPanel.add(Box.createHorizontalStrut(5));
        saveToPanel.add(browseButton);

        topPanel.add(saveToPanel, gbc);

        // Overrides
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover overridesLabel = new JLabelWithHover(GetText.tr("Folders To Export") + ":", HELP_ICON,
                GetText.tr("Select the folders you wish to include for this export"));
        topPanel.add(overridesLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        JPanel overridesPanel = new JPanel();
        overridesPanel.setLayout(new BoxLayout(overridesPanel, BoxLayout.Y_AXIS));
        overridesPanel.setBorder(BorderFactory.createEmptyBorder(0, -3, 0, 0));

        // get all files ignoring ATLauncher specific things as well as naughtys
        File[] files = instance.getRoot().toFile()
                .listFiles(pathname -> !pathname.getName().equalsIgnoreCase("disabledmods")
                        && !pathname.getName().equalsIgnoreCase("instance.json")
                        && !pathname.getName().equalsIgnoreCase(".fabric")
                        && !pathname.getName().equalsIgnoreCase(".quilt"));

        for (File filename : files) {
            // skip any folders with no files inside
            if (filename.isDirectory() && filename.listFiles().length == 0) {
                continue;
            }

            JCheckBox checkBox = new JCheckBox(filename.getName());

            checkBox.addItemListener(e -> {
                if (checkBox.isSelected()) {
                    overrides.add(checkBox.getText());
                } else {
                    overrides.remove(checkBox.getText());
                }
            });

            if (filename.getName().equalsIgnoreCase("config") || filename.getName().equalsIgnoreCase("mods")
                    || filename.getName().equalsIgnoreCase("oresources")
                    || filename.getName().equalsIgnoreCase("resourcepacks")
                    || filename.getName().equalsIgnoreCase("shaderpacks")
                    || filename.getName().equalsIgnoreCase("resources")
                    || filename.getName().equalsIgnoreCase("scripts")) {
                checkBox.setSelected(true);
            }

            overridesPanel.add(checkBox);
        }

        JScrollPane overridesScrollPanel = new JScrollPane(overridesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED) {
            {
                this.getVerticalScrollBar().setUnitIncrement(8);
            }
        };
        overridesScrollPanel.setPreferredSize(new Dimension(350, 200));

        topPanel.add(overridesScrollPanel, gbc);

        // bottom panel
        bottomPanel.setLayout(new FlowLayout());

        JButton exportButton = new JButton(GetText.tr("Export"));
        exportButton.addActionListener(arg0 -> {
            instance.scanMissingMods(this);

            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Exporting Instance"), 0,
                    GetText.tr("Exporting Instance. Please wait..."), null, this);

            dialog.addThread(new Thread(() -> {
                InstanceExportFormat exportFormat = ((ComboItem<InstanceExportFormat>) format.getSelectedItem())
                        .getValue();

                if (instance.export(name.getText(), version.getText(), author.getText(),
                        exportFormat, saveTo.getText(), overrides)) {
                    instance.launcher.lastExportName = name.getText();
                    instance.launcher.lastExportVersion = version.getText();
                    instance.launcher.lastExportAuthor = author.getText();
                    instance.launcher.lastExportSaveTo = saveTo.getText();
                    instance.save();

                    App.TOASTER.pop(GetText.tr("Exported Instance Successfully"));
                    String safePathName = name.getText().replaceAll("[\\\"?:*<>|]", "");
                    if (exportFormat == InstanceExportFormat.CURSEFORGE_AND_MODRINTH) {
                        OS.openFileExplorer(Paths.get(saveTo.getText()));
                    } else {
                        OS.openFileExplorer(
                                Paths.get(saveTo.getText())
                                        .resolve(String.format("%s.%s", safePathName,
                                                (exportFormat == InstanceExportFormat.MODRINTH ? "mrpack" : "zip"))),
                                true);
                    }
                } else {
                    App.TOASTER.popError(GetText.tr("Failed to export instance. Check the console for details"));
                }
                dialog.close();
                close();
            }));

            dialog.start();
        });
        bottomPanel.add(exportButton);

        JButton cancelButton = new JButton(GetText.tr("Cancel"));
        cancelButton.addActionListener(arg0 -> {
            close();
        });
        bottomPanel.add(cancelButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
