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
import java.awt.GridLayout;
import java.awt.Window;
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
import javax.swing.JScrollPane;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileDependency;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.card.CurseForgeFileDependencyCard;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class CurseForgeProjectFileSelectorDialog extends JDialog {
    private int filesLength = 0;
    private final CurseForgeProject mod;
    private Instance instance;
    private Integer installedFileId = null;

    private final JPanel dependenciesPanel = new JPanel(new FlowLayout());
    private JButton addButton;
    private JLabel versionsLabel;
    private JLabel installedJLabel;
    private JComboBox<CurseForgeFile> filesDropdown;
    private final List<CurseForgeFile> files = new ArrayList<>();

    public CurseForgeProjectFileSelectorDialog(CurseForgeProject mod, Instance instance) {
        this(App.launcher.getParent(), mod, instance);
    }

    public CurseForgeProjectFileSelectorDialog(Window parent, CurseForgeProject mod, Instance instance) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;

        setupComponents();
    }

    public CurseForgeProjectFileSelectorDialog(Window parent, CurseForgeProject mod, Instance instance,
            int installedFileId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;
        this.installedFileId = installedFileId;

        setupComponents();
    }

    private void setupComponents() {
        Analytics.sendScreenView("CurseForge Project File Selector Dialog");

        // #. {0} is the name of the mod we're installing
        setTitle(GetText.tr("Installing {0}", mod.name));

        setSize(550, 200);
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
        JPanel filesPanel = new JPanel(new FlowLayout());
        filesPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

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
            CurseForgeFile file = (CurseForgeFile) filesDropdown.getSelectedItem();

            ProgressDialog progressDialog = new ProgressDialog<>(
                    // #. {0} is the name of the mod we're installing
                    GetText.tr("Installing {0}", file.displayName), false);
            progressDialog.addThread(new Thread(() -> {
                Analytics.sendEvent(mod.name + " - " + file.displayName, "AddFile", "CurseForgeMod");
                instance.addFileFromCurse(mod, file, progressDialog);

                progressDialog.close();
            }));
            progressDialog.start();
        });

        filesDropdown.addActionListener(e -> {
            CurseForgeFile selectedFile = (CurseForgeFile) filesDropdown.getSelectedItem();

            dependenciesPanel.setVisible(false);

            // this file has dependencies
            if (selectedFile.dependencies.size() != 0) {
                // check to see which required ones we don't already have
                List<CurseForgeFileDependency> dependencies = selectedFile.dependencies.stream()
                        .filter(dependency -> dependency.isRequired() && instance.launcher.mods.stream()
                                .noneMatch(installedMod -> installedMod.isFromCurseForge()
                                        && installedMod.getCurseForgeModId() == dependency.addonId))
                        .collect(Collectors.toList());

                if (dependencies.size() != 0) {
                    dependenciesPanel.removeAll();

                    dependencies.forEach(dependency -> dependenciesPanel
                            .add(new CurseForgeFileDependencyCard(this, dependency, instance)));

                    dependenciesPanel.setLayout(new GridLayout(dependencies.size() < 2 ? 1 : dependencies.size() / 2,
                            (dependencies.size() / 2) + 1));

                    setSize(550, 450);
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
            LoaderVersion loaderVersion = this.instance.launcher.loaderVersion;

            Stream<CurseForgeFile> curseForgeFilesStream = CurseForgeApi.getFilesForProject(mod.id).stream()
                    .sorted(Comparator.comparingInt((CurseForgeFile file) -> file.id).reversed());

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                curseForgeFilesStream = curseForgeFilesStream.filter(
                        file -> mod.categorySection.gameCategoryId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID
                                || file.gameVersion.contains(this.instance.id));
            } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                try {
                    List<String> minecraftVersionsToSearch = MinecraftManager
                            .getMajorMinecraftVersions(this.instance.id).stream().map(mv -> mv.version)
                            .collect(Collectors.toList());

                    curseForgeFilesStream = curseForgeFilesStream
                            .filter(v -> v.gameVersion.stream().anyMatch(gv -> minecraftVersionsToSearch.contains(gv)));
                } catch (InvalidMinecraftVersion e) {
                    LogManager.logStackTrace(e);
                }
            }

            files.addAll(curseForgeFilesStream.collect(Collectors.toList()));

            // ensures that font width is taken into account
            for (CurseForgeFile file : files) {
                filesLength = Math.max(filesLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(file.displayName) + 100);
            }

            // try to filter out non compatable mods (Forge on Fabric and vice versa)
            if (App.settings.addModRestriction == AddModRestriction.NONE) {
                files.forEach(version -> filesDropdown.addItem(version));
            } else {
                files.stream().filter(version -> {
                    String fileName = version.fileName.toLowerCase();
                    String displayName = version.displayName.toLowerCase();

                    if (loaderVersion != null && loaderVersion.isFabric()) {
                        return !displayName.contains("-forge-") && !displayName.contains("(forge)")
                                && !displayName.contains("[forge") && !fileName.contains("forgemod");
                    }

                    if (loaderVersion != null && !loaderVersion.isFabric()) {
                        // if it's Forge, and the gameVersion has "Fabric" then exclude it
                        return version.gameVersion.contains("Fabric")
                                || (!displayName.toLowerCase().contains("-fabric-") && !displayName.contains("(fabric)")
                                        && !displayName.contains("[fabric") && !fileName.contains("fabricmod"));
                    }

                    return true;
                }).forEach(version -> filesDropdown.addItem(version));
            }

            if (filesDropdown.getItemCount() == 0) {
                DialogManager.okDialog().setParent(CurseForgeProjectFileSelectorDialog.this).setTitle("No files found")
                        .setContent("No files found for this mod").setType(DialogManager.ERROR).show();
                dispose();
            }

            if (this.installedFileId != null) {
                CurseForgeFile installedFile = files.stream().filter(f -> f.id == this.installedFileId).findFirst()
                        .orElse(null);

                if (installedFile != null) {
                    filesDropdown.setSelectedItem(installedFile);

                    // #. {0} is the name of the file that the user already has installed
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
