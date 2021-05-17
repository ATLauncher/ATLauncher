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

import com.atlauncher.App;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.Instance;
import com.atlauncher.data.modrinth.ModrinthMod;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.ModrinthApi;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ModrinthVersionSelectorDialog extends JDialog {
    private int filesLength = 0;
    private final ModrinthMod mod;
    private Instance instance;
    private String installedVersionId = null;

    private JButton addButton;
    private JLabel versionsLabel;
    private JLabel installedJLabel;
    private JComboBox<ModrinthVersion> versionsDropdown;
    private final List<ModrinthVersion> versions = new ArrayList<>();
    private List<ModrinthVersion> versionsData;

    public ModrinthVersionSelectorDialog(ModrinthMod mod, Instance instance) {
        this(App.launcher.getParent(), mod, instance);
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthMod mod, Instance instance) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthMod mod, List<ModrinthVersion> versions,
            Instance instance, String installedVersionId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.versionsData = versions;
        this.instance = instance;
        this.installedVersionId = installedVersionId;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthMod mod, Instance instance, String installedVersionId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;
        this.installedVersionId = installedVersionId;

        setupComponents();
    }

    private void setupComponents() {
        Analytics.sendScreenView("Modrinth Version Selector Dialog");

        // #. {0} is the name of the mod we're installing
        setTitle(GetText.tr("Installing {0}", mod.title));

        setSize(550, 200);
        setMinimumSize(new Dimension(550, 200));
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addButton = new JButton(GetText.tr("Add"));
        addButton.setEnabled(false);

        // Top Panel Stuff
        JPanel top = new JPanel(new BorderLayout());
        // #. {0} is the name of the mod we're installing
        top.add(new JLabel(GetText.tr("Installing {0}", mod.title), JLabel.CENTER), BorderLayout.NORTH);

        installedJLabel = new JLabel("", JLabel.CENTER);
        top.add(installedJLabel, BorderLayout.SOUTH);

        // Middle Panel Stuff
        JPanel middle = new JPanel(new BorderLayout());

        // Middle Panel Stuff
        JPanel filesPanel = new JPanel(new FlowLayout());
        filesPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        versionsLabel = new JLabel(GetText.tr("Version To Install") + ": ");
        filesPanel.add(versionsLabel);

        versionsDropdown = new JComboBox<>();
        versionsDropdown.setEnabled(false);
        filesPanel.add(versionsDropdown);

        middle.add(filesPanel, BorderLayout.NORTH);

        this.getFiles();

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addButton.addActionListener(e -> {
            ModrinthVersion version = (ModrinthVersion) versionsDropdown.getSelectedItem();

            ProgressDialog progressDialog = new ProgressDialog<>(
                    // #. {0} is the name of the mod we're installing
                    GetText.tr("Installing {0}", version.name), true, this);
            progressDialog.addThread(new Thread(() -> {
                Analytics.sendEvent(mod.title + " - " + version.name, "AddFile", "ModrinthMod");
                instance.addFileFromModrinth(mod, version, progressDialog);

                progressDialog.close();
            }));
            progressDialog.start();
            dispose();
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
        versionsDropdown.setVisible(true);

        Runnable r = () -> {
            if (this.versionsData == null) {
                this.versionsData = ModrinthApi.getVersions(mod.versions);
            }

            Stream<ModrinthVersion> modrinthVersionsStream = this.versionsData.stream()
                    .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed());

            if (App.settings.addModRestriction != AddModRestriction.NONE
                    && this.instance.launcher.loaderVersion != null) {
                modrinthVersionsStream = modrinthVersionsStream
                        .filter(v -> this.instance.launcher.loaderVersion.isFabric() ? v.loaders.contains("fabric")
                                : v.loaders.contains("forge"));
            }

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                modrinthVersionsStream = modrinthVersionsStream.filter(v -> v.gameVersions.contains(this.instance.id));
            } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                try {
                    List<String> minecraftVersionsToSearch = MinecraftManager
                            .getMajorMinecraftVersions(this.instance.id).stream().map(mv -> mv.id)
                            .collect(Collectors.toList());

                    modrinthVersionsStream = modrinthVersionsStream.filter(
                            v -> v.gameVersions.stream().anyMatch(gv -> minecraftVersionsToSearch.contains(gv)));
                } catch (InvalidMinecraftVersion e) {
                    LogManager.logStackTrace(e);
                }
            }

            versions.addAll(modrinthVersionsStream.collect(Collectors.toList()));

            // ensures that font width is taken into account
            for (ModrinthVersion version : versions) {
                filesLength = Math.max(filesLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.name) + 100);
            }

            versions.forEach(version -> versionsDropdown.addItem(version));

            if (versionsDropdown.getItemCount() == 0) {
                DialogManager.okDialog().setParent(ModrinthVersionSelectorDialog.this).setTitle("No versions found")
                        .setContent("No versions found for this mod").setType(DialogManager.ERROR).show();
                dispose();
            }

            if (this.installedVersionId != null) {
                ModrinthVersion installedFile = versions.stream()
                        .filter(f -> f.id.equalsIgnoreCase(this.installedVersionId)).findFirst().orElse(null);

                if (installedFile != null) {
                    versionsDropdown.setSelectedItem(installedFile);

                    // #. {0} is the name of the mod that the user already has installed
                    installedJLabel.setText(GetText.tr("The version currently installed is {0}", installedFile.name));
                    installedJLabel.setVisible(true);
                }
            }

            // ensures that the dropdown is at least 200 px wide and has a maximum width of
            // 350 px to prevent overflow
            versionsDropdown.setPreferredSize(new Dimension(Math.min(350, Math.max(200, filesLength)), 25));

            versionsDropdown.setEnabled(true);
            versionsLabel.setVisible(true);
            versionsDropdown.setVisible(true);
            addButton.setEnabled(true);
            versionsDropdown.setEnabled(true);
        };

        new Thread(r).start();
    }
}
