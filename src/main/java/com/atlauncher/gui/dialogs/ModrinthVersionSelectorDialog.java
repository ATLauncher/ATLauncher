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
import java.awt.GridLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.AddModRestriction;
import com.atlauncher.data.Instance;
import com.atlauncher.data.modrinth.ModrinthDependency;
import com.atlauncher.data.modrinth.ModrinthDependencyType;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthProjectType;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.card.ModrinthProjectDependencyCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;

public class ModrinthVersionSelectorDialog extends JDialog {
    private int versionsLength = 0;
    private int filesLength = 0;
    private final ModrinthProject mod;
    private final Instance instance;
    private String installedVersionId = null;
    private boolean selectNewest = true;

    private final JPanel dependenciesPanel = new JPanel(new FlowLayout());
    private JScrollPane scrollPane;
    private JButton addButton;
    private JButton viewModButton;
    private JButton viewFileButton;
    private JLabel versionsLabel;
    private JLabel installedJLabel;
    private JComboBox<ModrinthVersion> versionsDropdown;
    private final List<ModrinthVersion> versions = new ArrayList<>();
    private List<ModrinthVersion> versionsData;

    private JLabel filesLabel;
    private JComboBox<ComboItem<ModrinthFile>> filesDropdown;

    public ModrinthVersionSelectorDialog(ModrinthProject mod, Instance instance) {
        this(App.launcher.getParent(), mod, instance);
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, Instance instance) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, List<ModrinthVersion> versions,
            Instance instance, String installedVersionId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.versionsData = versions;
        this.instance = instance;
        this.installedVersionId = installedVersionId;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, Instance instance,
            String installedVersionId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;
        this.installedVersionId = installedVersionId;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, Instance instance,
            String installedVersionId, boolean selectNewest) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instance = instance;
        this.installedVersionId = installedVersionId;
        this.selectNewest = selectNewest;

        setupComponents();
    }

    public void reloadDependenciesPanel() {
        if (versionsDropdown.getSelectedItem() == null) {
            return;
        }

        ModrinthVersion selectedFile = (ModrinthVersion) versionsDropdown.getSelectedItem();

        dependenciesPanel.setVisible(false);

        List<ModrinthDependency> dependencies = selectedFile.dependencies.stream().filter(d -> d.projectId != null)
                .collect(Collectors.toList());

        // this file has dependencies
        if (!dependencies.isEmpty()) {
            // check to see which required ones we don't already have
            List<ModrinthDependency> dependenciesNeeded = dependencies.stream()
                    .filter(dependency -> dependency.dependencyType == ModrinthDependencyType.REQUIRED)
                    .filter(dependency -> {
                        if (!dependency.projectId.equals(Constants.MODRINTH_FABRIC_MOD_ID)) {
                            return true;
                        }

                        // We shouldn't install Fabric API when using Sinytra Connector
                        return !instance.isForgeLikeAndHasInstalledSinytraConnector();
                    })
                    .filter(dependency -> instance.launcher.mods.stream()
                            .noneMatch(installedMod -> {
                                // don't show Modrinth dependency when grabbed from CurseForge
                                if (dependency.projectId.equals(Constants.MODRINTH_FABRIC_MOD_ID)
                                        && installedMod.isFromCurseForge()
                                        && installedMod
                                                .getCurseForgeFileId() == Constants.CURSEFORGE_FABRIC_MOD_ID) {
                                    return true;
                                }

                                // don't show Modrinth dependency when grabbed from CurseForge
                                if (dependency.projectId.equals(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID)
                                        && installedMod.isFromCurseForge()
                                        && installedMod
                                                .getCurseForgeFileId() == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID) {
                                    return true;
                                }

                                // don't show Fabric dependency when QSL is installed
                                if (dependency.projectId.equals(Constants.MODRINTH_FABRIC_MOD_ID)
                                        && installedMod.isFromModrinth()
                                        && installedMod.modrinthProject.id
                                                .equals(Constants.MODRINTH_QSL_MOD_ID)) {
                                    return true;
                                }

                                // don't show Modrinth dependency when grabbed from CurseForge
                                if (dependency.projectId.equals(Constants.MODRINTH_FORGIFIED_FABRIC_API_MOD_ID)
                                        && installedMod.isFromCurseForge()
                                        && installedMod
                                                .getCurseForgeFileId() == Constants.CURSEFORGE_FORGIFIED_FABRIC_API_MOD_ID) {
                                    return true;
                                }

                                return installedMod.isFromModrinth()
                                        && installedMod.modrinthProject.id.equals(dependency.projectId);
                            }))
                    .collect(Collectors.toList());

            if (!dependenciesNeeded.isEmpty()) {
                dependenciesPanel.removeAll();

                dependenciesNeeded.forEach(dependency -> dependenciesPanel
                        .add(new ModrinthProjectDependencyCard(this, dependency, instance)));

                dependenciesPanel
                        .setLayout(new GridLayout(dependenciesNeeded.size() < 2 ? 1 : dependenciesNeeded.size() / 2,
                                (dependenciesNeeded.size() / 2) + 1));

                setSize(550, 450);
                setLocationRelativeTo(App.launcher.getParent());

                dependenciesPanel.setVisible(true);

                scrollPane.repaint();
                scrollPane.validate();
            } else {
                setSize(550, 200);
            }
        } else {
            setSize(550, 200);
        }
    }

    private void setupComponents() {
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

        viewModButton = new JButton(GetText.tr("View Mod"));
        viewModButton.setEnabled(false);

        viewFileButton = new JButton(GetText.tr("View File"));
        viewFileButton.setEnabled(false);

        dependenciesPanel.setVisible(false);
        dependenciesPanel
                .setBorder(BorderFactory.createTitledBorder(GetText.tr("The below mods need to be installed")));

        // Top Panel Stuff
        JPanel top = new JPanel(new BorderLayout());
        // #. {0} is the name of the mod we're installing
        top.add(new JLabel(GetText.tr("Installing {0}", mod.title), JLabel.CENTER), BorderLayout.NORTH);

        installedJLabel = new JLabel("", JLabel.CENTER);
        top.add(installedJLabel, BorderLayout.SOUTH);

        // Middle Panel Stuff
        JPanel middle = new JPanel(new BorderLayout());

        // Middle Panel Stuff
        JPanel versionsPanel = new JPanel(new FlowLayout());
        versionsPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        versionsLabel = new JLabel(GetText.tr("Version To Install") + ": ");
        versionsPanel.add(versionsLabel);

        versionsDropdown = new JComboBox<>();
        ModrinthVersion loadingProject = new ModrinthVersion();
        loadingProject.name = GetText.tr("Loading");
        versionsDropdown.addItem(loadingProject);
        versionsDropdown.setEnabled(false);
        versionsPanel.add(versionsDropdown);

        JPanel filesPanel = new JPanel(new FlowLayout());
        filesPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        filesLabel = new JLabel(GetText.tr("File To Install") + ": ");
        filesPanel.add(filesLabel);
        filesDropdown = new JComboBox<>();
        filesPanel.add(filesDropdown);
        filesPanel.setVisible(false);

        scrollPane = new JScrollPane(dependenciesPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(550, 250));

        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.Y_AXIS));
        selectorPanel.add(versionsPanel);
        selectorPanel.add(filesPanel);

        middle.add(selectorPanel, BorderLayout.NORTH);
        middle.add(scrollPane, BorderLayout.SOUTH);

        this.getFiles();

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addButton.addActionListener(e -> {
            ModrinthVersion version = (ModrinthVersion) versionsDropdown.getSelectedItem();
            ModrinthFile file = filesDropdown.getSelectedItem() == null ? null
                    : ((ComboItem<ModrinthFile>) filesDropdown.getSelectedItem()).getValue();

            ProgressDialog<Object> progressDialog = new ProgressDialog<>(
                    // #. {0} is the name of the mod we're installing
                    GetText.tr("Installing {0}", version.name), true, this);
            progressDialog.addThread(new Thread(() -> {
                Analytics.trackEvent(AnalyticsEvent.forAddedMod(mod, version));
                instance.addFileFromModrinth(mod, version, file, progressDialog);

                progressDialog.close();
            }));
            progressDialog.start();
            dispose();
        });

        viewModButton.addActionListener(e -> OS.openWebBrowser(String.format("https://modrinth.com/mod/%s", mod.slug)));

        viewFileButton.addActionListener(e -> {
            ModrinthVersion version = (ModrinthVersion) versionsDropdown.getSelectedItem();

            OS.openWebBrowser(String.format("https://modrinth.com/mod/%s/version/%s", mod.slug, version.id));
        });

        versionsDropdown.addActionListener(e -> {
            ModrinthVersion version = (ModrinthVersion) versionsDropdown.getSelectedItem();

            if (version != null) {
                reloadDependenciesPanel();

                filesDropdown.removeAllItems();
                if (version.files.size() > 1) {
                    int selectedIndex = 0;

                    for (ModrinthFile file : version.files) {
                        filesDropdown.addItem(new ComboItem<>(file, file.toString()));

                        if (file.primary) {
                            selectedIndex = filesDropdown.getItemCount() - 1;
                        }

                        // ensures that font width is taken into account
                        filesLength = Math.max(filesLength,
                                getFontMetrics(App.THEME.getNormalFont()).stringWidth(file.toString()) + 100);
                    }

                    // ensures that the dropdown is at least 200 px wide and has a maximum width of
                    // 350 px to prevent overflow
                    filesDropdown.setPreferredSize(new Dimension(Math.min(350, Math.max(200, filesLength)), 25));
                    filesDropdown.setSelectedIndex(selectedIndex);
                }

                filesPanel.setVisible(version.files.size() > 1);
            }
        });

        JButton cancel = new JButton(GetText.tr("Cancel"));
        cancel.addActionListener(e -> dispose());
        bottom.add(addButton);
        bottom.add(viewModButton);
        bottom.add(viewFileButton);
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
                this.versionsData = ModrinthApi.getVersions(mod.id);
            }

            Stream<ModrinthVersion> modrinthVersionsStream = this.versionsData.stream()
                    .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed());

            if (App.settings.addModRestriction != AddModRestriction.NONE && this.instance.launcher.loaderVersion != null
                    && mod.projectType == ModrinthProjectType.MOD) {
                List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                        .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<String>());
                boolean hasNeoForgeVersion = this.versionsData.stream()
                        .anyMatch(v -> v.loaders.contains("neoforge")
                                || (neoForgeForgeCompatabilityVersions.contains(this.instance.id)
                                        && v.loaders.contains("forge")));
                boolean hasForgeVersion = this.versionsData.stream().anyMatch(v -> v.loaders.contains("forge"));
                modrinthVersionsStream = modrinthVersionsStream.filter(v -> {
                    if (v.loaders.contains("fabric") && (this.instance.launcher.loaderVersion.isFabric()
                            || this.instance.launcher.loaderVersion.isLegacyFabric()
                            || this.instance.launcher.loaderVersion.isQuilt()
                            || (this.instance.isForgeLikeAndHasInstalledSinytraConnector()
                                    && this.instance.launcher.loaderVersion.isForge() && !hasForgeVersion)
                            || (this.instance.isForgeLikeAndHasInstalledSinytraConnector()
                                    && this.instance.launcher.loaderVersion.isNeoForge() && !hasNeoForgeVersion))) {
                        return true;
                    }

                    if (v.loaders.contains("neoforge") && this.instance.launcher.loaderVersion.isNeoForge()) {
                        return true;
                    }

                    if (v.loaders.contains("forge") && (this.instance.launcher.loaderVersion.isForge()
                            || (this.instance.launcher.loaderVersion.isNeoForge()
                                    && neoForgeForgeCompatabilityVersions.contains(this.instance.id)))) {
                        return true;
                    }

                    return v.loaders.contains("quilt") && this.instance.launcher.loaderVersion.isQuilt();
                });
            }

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                modrinthVersionsStream = modrinthVersionsStream.filter(v -> v.gameVersions.contains(this.instance.id));
            } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                try {
                    List<String> minecraftVersionsToSearch = MinecraftManager
                            .getMajorMinecraftVersions(this.instance.id).stream().map(mv -> mv.id)
                            .collect(Collectors.toList());

                    modrinthVersionsStream = modrinthVersionsStream.filter(
                            v -> v.gameVersions.stream().anyMatch(minecraftVersionsToSearch::contains));
                } catch (InvalidMinecraftVersion e) {
                    LogManager.logStackTrace(e);
                }
            }

            versions.addAll(modrinthVersionsStream.collect(Collectors.toList()));

            // ensures that font width is taken into account
            for (ModrinthVersion version : versions) {
                versionsLength = Math.max(versionsLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.name) + 100);
            }

            versionsDropdown.removeAllItems();

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
                    if (!selectNewest) {
                        versionsDropdown.setSelectedItem(installedFile);
                    }

                    // #. {0} is the name of the mod that the user already has installed
                    installedJLabel.setText(GetText.tr("The version currently installed is {0}", installedFile.name));
                    installedJLabel.setVisible(true);
                }
            }

            // ensures that the dropdown is at least 200 px wide and has a maximum width of
            // 350 px to prevent overflow
            versionsDropdown.setPreferredSize(new Dimension(Math.min(350, Math.max(200, versionsLength)), 25));

            versionsDropdown.setEnabled(true);
            versionsLabel.setVisible(true);
            versionsDropdown.setVisible(true);
            addButton.setEnabled(true);
            viewModButton.setEnabled(true);
            viewFileButton.setEnabled(true);
        };

        new Thread(r).start();
    }
}
