/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseFileDependency;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.gui.card.CurseFileDependencyCard;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseApi;

import org.mini2Dx.gettext.GetText;

public class CurseModFileSelectorDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
    private int filesLength = 0;
    private CurseMod mod;
    private Instance instance;
    private InstanceV2 instanceV2;
    private Integer installedFileId = null;

    private JPanel filesPanel;
    private JPanel dependenciesPanel = new JPanel(new FlowLayout());
    private JButton addButton;
    private JLabel versionsLabel;
    private JLabel installedJLabel;
    private JComboBox<CurseFile> filesDropdown;
    private List<CurseFile> files = new ArrayList<>();

    public CurseModFileSelectorDialog(CurseMod mod, Instance instance) {
        super(App.launcher.getParent(), ModalityType.APPLICATION_MODAL);

        this.mod = mod;
        this.instance = instance;

        setupComponents();
    }

    public CurseModFileSelectorDialog(CurseMod mod, Instance instance, int installedFileId) {
        super(App.launcher.getParent(), ModalityType.APPLICATION_MODAL);

        this.mod = mod;
        this.instance = instance;
        this.installedFileId = installedFileId;

        setupComponents();
    }

    public CurseModFileSelectorDialog(CurseMod mod, InstanceV2 instanceV2) {
        super(App.launcher.getParent(), ModalityType.APPLICATION_MODAL);

        this.mod = mod;
        this.instanceV2 = instanceV2;

        setupComponents();
    }

    public CurseModFileSelectorDialog(CurseMod mod, InstanceV2 instanceV2, int installedFileId) {
        super(App.launcher.getParent(), ModalityType.APPLICATION_MODAL);

        this.mod = mod;
        this.instanceV2 = instanceV2;
        this.installedFileId = installedFileId;

        setupComponents();
    }

    private void setupComponents() {
        Analytics.sendScreenView("Curse Mods File Selector Dialog");

        // #. {0} is the name of the mod we're installing
        setTitle(GetText.tr("Installing {0}", mod.name));

        setSize(500, 200);
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addButton = new JButton(GetText.tr("Add"));
        addButton.setEnabled(false);

        dependenciesPanel.setVisible(false);
        dependenciesPanel
                .setBorder(BorderFactory.createTitledBorder(GetText.tr("The below mods need to be installed")));

        // Top Panel Stuff
        JPanel top = new JPanel(new BorderLayout());
        // #. {0} is the name of the mod we're installing
        top.add(new JLabel(GetText.tr("Installing {0}", mod.name), JLabel.CENTER), BorderLayout.NORTH);

        installedJLabel = new JLabel("", JLabel.CENTER);
        top.add(installedJLabel, BorderLayout.SOUTH);

        // Middle Panel Stuff
        JPanel middle = new JPanel(new BorderLayout());

        // Middle Panel Stuff
        filesPanel = new JPanel(new FlowLayout());
        filesPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        versionsLabel = new JLabel(GetText.tr("Version To Install") + ": ");
        filesPanel.add(versionsLabel);

        filesDropdown = new JComboBox<>();
        filesDropdown.setEnabled(false);
        filesPanel.add(filesDropdown);

        JScrollPane scrollPane = new JScrollPane(dependenciesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(550, 250));

        middle.add(filesPanel, BorderLayout.NORTH);
        middle.add(scrollPane, BorderLayout.SOUTH);

        this.getFiles();

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addButton.addActionListener(e -> {
            CurseFile file = (CurseFile) filesDropdown.getSelectedItem();

            // #. {0} is the name of the mod we're installing
            final JDialog dialog = new JDialog(this, GetText.tr("Installing {0}", file.displayName),
                    ModalityType.DOCUMENT_MODAL);

            dialog.setLocationRelativeTo(this);
            dialog.setSize(300, 100);
            dialog.setResizable(false);

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            // #. {0} is the name of the mod we're installing
            final JLabel doing = new JLabel(GetText.tr("Installing {0}", file.displayName));
            doing.setHorizontalAlignment(JLabel.CENTER);
            doing.setVerticalAlignment(JLabel.TOP);
            topPanel.add(doing, BorderLayout.NORTH);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());

            JProgressBar progressBar = new JProgressBar(0, 100);
            bottomPanel.add(progressBar, BorderLayout.NORTH);
            progressBar.setIndeterminate(true);

            dialog.add(topPanel, BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            Runnable r = () -> {
                Analytics.sendEvent(mod.name + " - " + file.displayName, "AddFile", "CurseMod");
                if (this.instanceV2 != null) {
                    instanceV2.addFileFromCurse(mod, file);
                } else {
                    instance.addFileFromCurse(mod, file);
                }
                dialog.dispose();
                dispose();
            };

            new Thread(r).start();

            dialog.setVisible(true);
        });

        filesDropdown.addActionListener(e -> {
            CurseFile selectedFile = (CurseFile) filesDropdown.getSelectedItem();

            dependenciesPanel.setVisible(false);

            // this file has dependencies
            if (selectedFile.dependencies.size() != 0) {
                // check to see which required ones we don't already have
                List<CurseFileDependency> dependencies = selectedFile.dependencies.stream()
                        .filter(dependency -> dependency.isRequired() && (instanceV2 != null
                                ? instanceV2.launcher.mods.stream().noneMatch(installedMod -> installedMod.isFromCurse()
                            && installedMod.getCurseModId() == dependency.addonId)
                                : instance.getInstalledMods().stream().noneMatch(installedMod -> installedMod.isFromCurse()
                            && installedMod.getCurseModId() == dependency.addonId)))
                        .collect(Collectors.toList());

                if (dependencies.size() != 0) {
                    dependenciesPanel.removeAll();

                    if (this.instanceV2 != null) {
                        dependencies.forEach(dependency -> dependenciesPanel
                                .add(new CurseFileDependencyCard(dependency, instanceV2)));
                    } else {
                        dependencies.forEach(
                                dependency -> dependenciesPanel.add(new CurseFileDependencyCard(dependency, instance)));
                    }

                    dependenciesPanel.setLayout(new GridLayout(dependencies.size() < 2 ? 1 : dependencies.size() / 2,
                            (dependencies.size() / 2) + 1));

                    setSize(550, 400);
                    setLocationRelativeTo(App.launcher.getParent());

                    dependenciesPanel.setVisible(true);

                    scrollPane.repaint();
                    scrollPane.validate();
                }
            }
        });

        JButton cancel = new JButton(GetText.tr("Cancel"));
        cancel.addActionListener(e -> dispose());
        bottom.add(addButton);
        bottom.add(cancel);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    protected void getFiles() {
        versionsLabel.setVisible(true);
        filesDropdown.setVisible(true);

        Runnable r = () -> {
            LoaderVersion loaderVersion = (this.instanceV2 != null ? this.instanceV2.launcher.loaderVersion
                    : this.instance.getLoaderVersion());

            Stream<CurseFile> curseFilesStream = CurseApi.getFilesForMod(mod.id).stream()
                    .sorted(Comparator.comparingInt((CurseFile file) -> file.id).reversed());

            if (!App.settings.disableAddModRestrictions) {
                curseFilesStream = curseFilesStream.filter(file -> App.settings.disableAddModRestrictions
                        || mod.categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID
                        || file.gameVersion.contains(
                                this.instanceV2 != null ? this.instanceV2.id : this.instance.getMinecraftVersion()));
            }

            files.addAll(curseFilesStream.collect(Collectors.toList()));

            // ensures that font width is taken into account
            for (CurseFile file : files) {
                filesLength = Math.max(filesLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(file.displayName) + 100);
            }

            // try to filter out non compatable mods (Forge on Fabric and vice versa)
            if (App.settings.disableAddModRestrictions) {
                files.stream().forEach(version -> filesDropdown.addItem(version));
            } else {
                files.stream().filter(version -> {
                    String fileName = version.fileName.toLowerCase();
                    String displayName = version.displayName.toLowerCase();

                    if (loaderVersion != null && loaderVersion.isFabric()) {
                        return !displayName.contains("-forge-") && !displayName.contains("(forge)")
                                && !displayName.contains("[forge") && !fileName.contains("forgemod");
                    }

                    if (loaderVersion != null && !loaderVersion.isFabric()) {
                        return !displayName.toLowerCase().contains("-fabric-") && !displayName.contains("(fabric)")
                                && !displayName.contains("[fabric") && !fileName.contains("fabricmod");
                    }

                    return true;
                }).forEach(version -> filesDropdown.addItem(version));
            }

            if (filesDropdown.getItemCount() == 0) {
                DialogManager.okDialog().setParent(CurseModFileSelectorDialog.this).setTitle("No files found")
                        .setContent("No files found for this mod").setType(DialogManager.ERROR).show();
                dispose();
            }

            if (this.installedFileId != null) {
                CurseFile installedFile = files.stream().filter(f -> f.id == this.installedFileId).findFirst()
                        .orElse(null);

                if (installedFile != null) {
                    filesDropdown.setSelectedItem(installedFile);

                    // #. {0} is the name of the Curse mod that the user already has installed
                    installedJLabel.setText(GetText.tr("The version currently installed is {0}", installedFile));
                    installedJLabel.setVisible(true);
                }
            }

            // ensures that the dropdown is at least 200 px wide and has a maximum width of
            // 350 px to prevent overflow
            filesDropdown.setPreferredSize(new Dimension(Math.min(350, Math.max(200, filesLength)), 25));

            filesDropdown.setEnabled(true);
            versionsLabel.setVisible(true);
            filesDropdown.setVisible(true);
            addButton.setEnabled(true);
            filesDropdown.setEnabled(true);
        };

        new Thread(r).start();
    }
}
