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
import com.atlauncher.data.ModManagement;
import com.atlauncher.data.Server;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
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
    private final ModrinthProject project;
    private final ModManagement instanceOrServer;
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

    public ModrinthVersionSelectorDialog(ModrinthProject mod, ModManagement instanceOrServer) {
        this(App.launcher.getParent(), mod, instanceOrServer);
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, ModManagement instanceOrServer) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.project = mod;
        this.instanceOrServer = instanceOrServer;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, List<ModrinthVersion> versions,
        ModManagement instanceOrServer, String installedVersionId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.project = mod;
        this.versionsData = versions;
        this.instanceOrServer = instanceOrServer;
        this.installedVersionId = installedVersionId;

        setupComponents();
    }

    public ModrinthVersionSelectorDialog(Window parent, ModrinthProject mod, ModManagement instanceOrServer,
        String installedVersionId, boolean selectNewest) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.project = mod;
        this.instanceOrServer = instanceOrServer;
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
                    return instanceOrServer instanceof Server
                        || !((Instance) instanceOrServer).isForgeLikeAndHasInstalledSinytraConnector();
                })
                .filter(dependency -> instanceOrServer.getMods().stream()
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
                    .add(new ModrinthProjectDependencyCard(this, dependency, instanceOrServer)));

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
        setTitle(GetText.tr("Installing {0}", project.title));

        setSize(550, 200);
        setMinimumSize(new Dimension(550, 200));
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addButton = new JButton(GetText.tr("Add"));
        addButton.setEnabled(false);

        viewModButton = new JButton(GetText.tr("View Mod"));
        if (instanceOrServer.getLoaderVersion() != null && ((instanceOrServer.getLoaderVersion().isPaper()
            && (project.projectType == ModrinthProjectType.PLUGIN || project.loaders.contains("paper")
            || project.loaders.contains("bukkit") || project.loaders.contains("spigot")))
            || (instanceOrServer.getLoaderVersion().isPurpur()
            && (project.projectType == ModrinthProjectType.PLUGIN || project.loaders.contains("purpur")
            || project.loaders.contains("paper")
            || project.loaders.contains("bukkit") || project.loaders.contains("spigot"))))) {
            viewModButton.setText(GetText.tr("View Plugin"));
        }
        viewModButton.setEnabled(false);

        viewFileButton = new JButton(GetText.tr("View File"));
        viewFileButton.setEnabled(false);

        dependenciesPanel.setVisible(false);
        dependenciesPanel
            .setBorder(BorderFactory.createTitledBorder(GetText.tr("The below mods need to be installed")));

        // Top Panel Stuff
        JPanel top = new JPanel(new BorderLayout());
        // #. {0} is the name of the mod we're installing
        top.add(new JLabel(GetText.tr("Installing {0}", project.title), JLabel.CENTER), BorderLayout.NORTH);

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

            ProgressDialog<Void> progressDialog = new ProgressDialog<>(
                // #. {0} is the name of the mod we're installing
                GetText.tr("Installing {0}", version.name), true, this);
            progressDialog.addThread(new Thread(() -> {
                Analytics.trackEvent(AnalyticsEvent.forAddedMod(project, version));
                instanceOrServer.addFileFromModrinth(project, version, file, progressDialog);
                progressDialog.close();
            }));
            progressDialog.start();
            dispose();
        });

        viewModButton
            .addActionListener(e -> OS.openWebBrowser(String.format("https://modrinth.com/mod/%s", project.slug)));

        viewFileButton.addActionListener(e -> {
            ModrinthVersion version = (ModrinthVersion) versionsDropdown.getSelectedItem();

            OS.openWebBrowser(String.format("https://modrinth.com/mod/%s/version/%s", project.slug, version.id));
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
    }

    protected void getFiles() {
        versionsLabel.setVisible(true);
        versionsDropdown.setVisible(true);

        Runnable r = () -> {
            if (this.versionsData == null) {
                this.versionsData = ModrinthApi.getVersions(project.id);
            }

            if (this.versionsData == null) {
                DialogManager.okDialog().setParent(ModrinthVersionSelectorDialog.this).setTitle("No versions found")
                    .setContent("No versions found for this mod").setType(DialogManager.ERROR).show();
                dispose();
                return;
            }

            Stream<ModrinthVersion> modrinthVersionsStream = this.versionsData.stream()
                .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed());

            LoaderVersion loaderVersion = instanceOrServer.getLoaderVersion();
            String minecraftVersion = instanceOrServer.getMinecraftVersion();

            if (App.settings.addModRestriction != AddModRestriction.NONE && loaderVersion != null
                && project.projectType == ModrinthProjectType.MOD) {
                List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                    .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<>());
                boolean hasNeoForgeVersion = this.versionsData.stream()
                    .anyMatch(v -> v.loaders.contains("neoforge")
                        || (neoForgeForgeCompatabilityVersions.contains(minecraftVersion)
                        && v.loaders.contains("forge")));
                boolean hasForgeVersion = this.versionsData.stream().anyMatch(v -> v.loaders.contains("forge"));
                modrinthVersionsStream = modrinthVersionsStream.filter(v -> {
                    if (instanceOrServer instanceof Instance && v.loaders.contains("fabric")
                        && (loaderVersion.isFabric()
                        || loaderVersion.isLegacyFabric()
                        || loaderVersion.isQuilt()
                        || (((Instance) instanceOrServer).isForgeLikeAndHasInstalledSinytraConnector()
                        && loaderVersion.isForge() && !hasForgeVersion)
                        || (((Instance) instanceOrServer).isForgeLikeAndHasInstalledSinytraConnector()
                        && loaderVersion.isNeoForge() && !hasNeoForgeVersion))) {
                        return true;
                    }

                    if (v.loaders.contains("neoforge") && loaderVersion.isNeoForge()) {
                        return true;
                    }

                    if ((v.loaders.contains("paper") || v.loaders.contains("bukkit") || v.loaders.contains("spigot"))
                        && loaderVersion.isPaper()) {
                        return true;
                    }

                    if ((v.loaders.contains("purpur") || v.loaders.contains("paper") || v.loaders.contains("bukkit")
                        || v.loaders.contains("spigot"))
                        && loaderVersion.isPurpur()) {
                        return true;
                    }

                    if (v.loaders.contains("forge") && (loaderVersion.isForge()
                        || (loaderVersion.isNeoForge()
                        && neoForgeForgeCompatabilityVersions.contains(minecraftVersion)))) {
                        return true;
                    }

                    return v.loaders.contains("quilt") && loaderVersion.isQuilt();
                });
            }

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                modrinthVersionsStream = modrinthVersionsStream.filter(v -> v.gameVersions.contains(minecraftVersion));
            } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                try {
                    List<String> minecraftVersionsToSearch = MinecraftManager
                        .getMajorMinecraftVersions(minecraftVersion).stream().map(mv -> mv.id)
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
