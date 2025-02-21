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
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
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
import com.atlauncher.data.ModManagement;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileDependency;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.card.CurseForgeFileDependencyCard;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.OS;

public class CurseForgeProjectFileSelectorDialog extends JDialog {
    private int filesLength = 0;
    private final CurseForgeProject mod;
    private final ModManagement instanceOrServer;
    private Integer installedFileId = null;
    private boolean selectNewest = true;

    private final JPanel dependenciesPanel = new JPanel(new FlowLayout());
    private JScrollPane scrollPane;
    private JButton addButton;
    private JButton viewModButton;
    private JButton viewFileButton;
    private JLabel versionsLabel;
    private JLabel installedJLabel;
    private JComboBox<CurseForgeFile> filesDropdown;
    private final List<CurseForgeFile> files = new ArrayList<>();

    public CurseForgeProjectFileSelectorDialog(CurseForgeProject mod, ModManagement instanceOrServer) {
        this(App.launcher.getParent(), mod, instanceOrServer);
    }

    public CurseForgeProjectFileSelectorDialog(Window parent, CurseForgeProject mod, ModManagement instanceOrServer) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instanceOrServer = instanceOrServer;

        setupComponents();
    }

    public CurseForgeProjectFileSelectorDialog(Window parent, CurseForgeProject mod, ModManagement instanceOrServer,
            int installedFileId) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instanceOrServer = instanceOrServer;
        this.installedFileId = installedFileId;

        setupComponents();
    }

    public CurseForgeProjectFileSelectorDialog(Window parent, CurseForgeProject mod, ModManagement instanceOrServer,
            int installedFileId, boolean selectNewest) {
        super(parent, ModalityType.DOCUMENT_MODAL);

        this.mod = mod;
        this.instanceOrServer = instanceOrServer;
        this.installedFileId = installedFileId;
        this.selectNewest = selectNewest;

        setupComponents();
    }

    private void setupComponents() {
        // #. {0} is the name of the mod we're installing
        setTitle(GetText.tr("Installing {0}", mod.name));

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
        CurseForgeFile loadingProject = new CurseForgeFile();
        loadingProject.displayName = GetText.tr("Loading");
        filesDropdown.addItem(loadingProject);
        filesDropdown.setEnabled(false);
        filesPanel.add(filesDropdown);

        scrollPane = new JScrollPane(dependenciesPanel);
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

            ProgressDialog<Void> progressDialog = new ProgressDialog<>(
                    // #. {0} is the name of the mod we're installing
                    GetText.tr("Installing {0}", file.displayName), false, this);
            progressDialog.addThread(new Thread(() -> {
                Analytics.trackEvent(AnalyticsEvent.forAddedMod(mod, file));
                instanceOrServer.addFileFromCurseForge(mod, file, progressDialog);

                progressDialog.close();
            }));
            progressDialog.start();
            dispose();
        });

        viewModButton.addActionListener(e -> OS.openWebBrowser(mod.getWebsiteUrl()));

        viewFileButton.addActionListener(e -> {
            CurseForgeFile file = (CurseForgeFile) filesDropdown.getSelectedItem();

            OS.openWebBrowser(String.format(Locale.ENGLISH, "%s/files/%d", mod.getWebsiteUrl(), file.id));
        });

        filesDropdown.addActionListener(e -> reloadDependenciesPanel());

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

    public void reloadDependenciesPanel() {
        if (filesDropdown.getSelectedItem() == null) {
            return;
        }

        CurseForgeFile selectedFile = (CurseForgeFile) filesDropdown.getSelectedItem();

        dependenciesPanel.setVisible(false);

        // this file has dependencies
        if (!selectedFile.dependencies.isEmpty()) {
            // check to see which required ones we don't already have
            List<CurseForgeFileDependency> dependencies = selectedFile.dependencies.stream()
                    .filter(dependency -> dependency.isRequired())
                    .filter(dependency -> {
                        if (dependency.modId != Constants.CURSEFORGE_FABRIC_MOD_ID) {
                            return true;
                        }

                        // We shouldn't install Fabric API when using Sinytra Connector
                        return !instanceOrServer.isForgeLikeAndHasInstalledSinytraConnector();
                    })
                    .filter(dependency -> instanceOrServer.getMods().stream()
                            .noneMatch(installedMod -> {
                                if (instanceOrServer.getLoaderVersion().isQuilt()
                                        && dependency.modId == Constants.CURSEFORGE_FABRIC_MOD_ID) {
                                    // if on Quilt and the dependency is Fabric API, then don't show it if user
                                    // already has QSL installed
                                    return instanceOrServer.getMods().parallelStream().anyMatch(m -> m.isFromModrinth()
                                            && m.modrinthProject.id.equals(Constants.MODRINTH_QSL_MOD_ID));
                                }

                                // don't show CurseForge dependency when grabbed from Modrinth
                                if (dependency.modId == Constants.CURSEFORGE_FABRIC_MOD_ID
                                        && installedMod.isFromModrinth()
                                        && installedMod.modrinthProject.id.equals(Constants.MODRINTH_FABRIC_MOD_ID)) {
                                    return true;
                                }

                                // don't show CurseForge dependency when grabbed from Modrinth
                                if (dependency.modId == Constants.CURSEFORGE_LEGACY_FABRIC_MOD_ID
                                        && installedMod.isFromModrinth()
                                        && installedMod.modrinthProject.id
                                                .equals(Constants.MODRINTH_LEGACY_FABRIC_MOD_ID)) {
                                    return true;
                                }

                                // don't show CurseForge dependency when grabbed from Modrinth
                                if (dependency.modId == Constants.CURSEFORGE_FORGIFIED_FABRIC_API_MOD_ID
                                        && installedMod.isFromModrinth()
                                        && installedMod.modrinthProject.id
                                                .equals(Constants.MODRINTH_FORGIFIED_FABRIC_API_MOD_ID)) {
                                    return true;
                                }

                                return installedMod.isFromCurseForge()
                                        && installedMod.getCurseForgeModId() == dependency.modId;
                            }))
                    .collect(Collectors.toList());

            if (!dependencies.isEmpty()) {
                dependenciesPanel.removeAll();

                dependencies.forEach(dependency -> dependenciesPanel
                        .add(new CurseForgeFileDependencyCard(this, dependency, instanceOrServer)));

                dependenciesPanel.setLayout(new GridLayout(dependencies.size() < 2 ? 1 : dependencies.size() / 2,
                        (dependencies.size() / 2) + 1));

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

    protected void getFiles() {
        versionsLabel.setVisible(true);
        filesDropdown.setVisible(true);

        Runnable r = () -> {
            LoaderVersion loaderVersion = instanceOrServer.getLoaderVersion();

            List<CurseForgeFile> projectFiles = CurseForgeApi.getFilesForProject(mod.id);
            Stream<CurseForgeFile> curseForgeFilesStream = projectFiles.stream()
                    .sorted(Comparator.comparingInt((CurseForgeFile file) -> file.id).reversed());

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                curseForgeFilesStream = curseForgeFilesStream.filter(
                        file -> mod.getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID
                                || mod.getRootCategoryId() == Constants.CURSEFORGE_PLUGINS_SECTION_ID
                                || file.gameVersions.contains(instanceOrServer.getMinecraftVersion()));
            } else if (App.settings.addModRestriction == AddModRestriction.LAX) {
                try {
                    List<String> minecraftVersionsToSearch = MinecraftManager
                            .getMajorMinecraftVersions(instanceOrServer.getMinecraftVersion()).stream().map(mv -> mv.id)
                            .collect(Collectors.toList());

                    curseForgeFilesStream = curseForgeFilesStream
                            .filter(v -> v.gameVersions.stream()
                                    .anyMatch(minecraftVersionsToSearch::contains));
                } catch (InvalidMinecraftVersion e) {
                    LogManager.logStackTrace(e);
                }
            }

            List<String> neoForgeForgeCompatabilityVersions = ConfigManager
                    .getConfigItem("loaders.neoforge.forgeCompatibleMinecraftVersions", new ArrayList<>());
            boolean hasNeoForgeVersion = projectFiles.stream()
                    .anyMatch(v -> v.gameVersions.contains("NeoForge")
                            || (neoForgeForgeCompatabilityVersions.contains(instanceOrServer.getMinecraftVersion())
                                    && v.gameVersions.contains("Forge")));
            boolean hasForgeVersion = projectFiles.stream().anyMatch(v -> v.gameVersions.contains("Forge"));

            // filter out files not for our loader (if browsing mods)
            if (mod.getRootCategoryId() == Constants.CURSEFORGE_MODS_SECTION_ID) {
                curseForgeFilesStream = curseForgeFilesStream.filter(cf -> {
                    if (cf.gameVersions.contains("Fabric") && loaderVersion != null
                            && (loaderVersion.isFabric() || loaderVersion.isLegacyFabric()
                                    || loaderVersion.isQuilt()
                                    || (instanceOrServer.isForgeLikeAndHasInstalledSinytraConnector()
                                            && instanceOrServer.getLoaderVersion().isForge() && !hasForgeVersion)
                                    || (instanceOrServer.isForgeLikeAndHasInstalledSinytraConnector()
                                            && instanceOrServer.getLoaderVersion().isNeoForge()
                                            && !hasNeoForgeVersion))) {
                        return true;
                    }

                    if (cf.gameVersions.contains("NeoForge") && loaderVersion != null
                            && loaderVersion.isNeoForge()) {
                        return true;
                    }

                    if (cf.gameVersions.contains("Forge") && loaderVersion != null
                            && (loaderVersion.isForge()
                                    || (loaderVersion.isNeoForge()
                                            && neoForgeForgeCompatabilityVersions
                                                    .contains(instanceOrServer.getMinecraftVersion())))) {
                        return true;
                    }

                    if (cf.gameVersions.contains("Quilt") && loaderVersion != null
                            && loaderVersion.isQuilt()) {
                        return true;
                    }

                    // if there's no loaders, assume the mod is untagged so we should show it
                    return !cf.gameVersions.contains("Fabric") && !cf.gameVersions.contains("NeoForge")
                            && !cf.gameVersions.contains("Forge") && !cf.gameVersions.contains("Quilt");
                });
            }

            files.addAll(curseForgeFilesStream.collect(Collectors.toList()));

            // ensures that font width is taken into account
            for (CurseForgeFile file : files) {
                filesLength = Math.max(filesLength,
                        getFontMetrics(App.THEME.getNormalFont()).stringWidth(file.displayName) + 100);
            }

            filesDropdown.removeAllItems();

            // try to filter out non compatible mods (Forge on Fabric and vice versa) if no
            // loader gameVersions are set
            if (App.settings.addModRestriction == AddModRestriction.NONE) {
                files.forEach(version -> filesDropdown.addItem(version));
            } else {
                files.stream().filter(version -> {
                    if (!version.gameVersions.contains("Forge") && !version.gameVersions.contains("Fabric")) {
                        String fileName = version.fileName.toLowerCase(Locale.ENGLISH);
                        String displayName = version.displayName.toLowerCase(Locale.ENGLISH);

                        if (loaderVersion != null && loaderVersion.isFabric()) {
                            return !displayName.contains("-forge-") && !displayName.contains("(forge)")
                                    && !displayName.contains("[forge") && !fileName.contains("forgemod");
                        }

                        if (loaderVersion != null && !loaderVersion.isFabric()) {
                            // if it's Forge, and the gameVersion has "Fabric" then exclude it
                            return version.gameVersions.contains("Fabric")
                                    || (!displayName.toLowerCase(Locale.ENGLISH).contains("-fabric-")
                                            && !displayName.contains("(fabric)")
                                            && !displayName.contains("[fabric") && !fileName.contains("fabricmod"));
                        }
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
                    if (!selectNewest) {
                        filesDropdown.setSelectedItem(installedFile);
                    }

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
            viewModButton.setEnabled(true);
            viewFileButton.setEnabled(true);
        };

        new Thread(r).start();
    }
}
