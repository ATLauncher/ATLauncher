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
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.gui.handlers.ModsJCheckBoxTransferHandler;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Utils;

public class EditModsDialog extends JDialog {
    private static final long serialVersionUID = 7004414192679481818L;

    public Instance instance;

    private JList<ModsJCheckBox> disabledModsPanel, enabledModsPanel;
    private JButton checkForUpdatesButton;
    private JButton reinstallButton;
    private JButton enableButton;
    private JButton disableButton;
    private JButton removeButton;
    private JButton refreshMetadataButton;
    private JCheckBox selectAllEnabledModsCheckbox, selectAllDisabledModsCheckbox;
    private ArrayList<ModsJCheckBox> enabledMods, disabledMods;

    public EditModsDialog(Instance instance) {
        super(App.launcher.getParent(),
                // #. {0} is the name of the instance
                GetText.tr("Editing Mods For {0}", instance.launcher.name), ModalityType.DOCUMENT_MODAL);
        this.instance = instance;
        setSize(550, 450);
        setMinimumSize(new Dimension(550, 450));
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

        setupComponents();

        instance.scanMissingMods();

        loadMods();

        setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Edit Mods Dialog");

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerSize(0);
        split.setBorder(null);
        split.setEnabled(false);
        add(split, BorderLayout.NORTH);

        JSplitPane labelsTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        labelsTop.setDividerSize(0);
        labelsTop.setBorder(null);
        labelsTop.setEnabled(false);
        split.setLeftComponent(labelsTop);

        JSplitPane labels = new JSplitPane();
        labels.setDividerLocation(275);
        labels.setDividerSize(0);
        labels.setBorder(null);
        labels.setEnabled(false);
        split.setRightComponent(labels);

        JPanel topLeftPanel = new JPanel(new FlowLayout());

        JLabel topLabelLeft = new JLabel(GetText.tr("Enabled Mods"));
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        topLeftPanel.add(topLabelLeft);

        selectAllEnabledModsCheckbox = new JCheckBox();
        selectAllEnabledModsCheckbox.addActionListener(e -> {
            boolean selected = selectAllEnabledModsCheckbox.isSelected();

            enabledMods.forEach(em -> em.setSelected(selected));
        });
        topLeftPanel.add(selectAllEnabledModsCheckbox);

        labels.setLeftComponent(topLeftPanel);

        JPanel topRightPanel = new JPanel(new FlowLayout());

        JLabel topLabelRight = new JLabel(GetText.tr("Disabled Mods"));
        topLabelRight.setHorizontalAlignment(SwingConstants.CENTER);
        topRightPanel.add(topLabelRight);

        selectAllDisabledModsCheckbox = new JCheckBox();
        selectAllDisabledModsCheckbox.addActionListener(e -> {
            boolean selected = selectAllDisabledModsCheckbox.isSelected();

            disabledMods.forEach(dm -> dm.setSelected(selected));
        });
        topRightPanel.add(selectAllDisabledModsCheckbox);

        labels.setRightComponent(topRightPanel);

        JSplitPane modsInPack = new JSplitPane();
        modsInPack.setDividerLocation(275);
        modsInPack.setDividerSize(0);
        modsInPack.setBorder(null);
        modsInPack.setEnabled(false);
        add(modsInPack, BorderLayout.CENTER);

        disabledModsPanel = new JList<>();
        disabledModsPanel.setLayout(null);
        disabledModsPanel.setBackground(UIManager.getColor("Mods.modSelectionColor"));
        disabledModsPanel.setDragEnabled(true);
        disabledModsPanel.setTransferHandler(new ModsJCheckBoxTransferHandler(this, true));

        JScrollPane scroller1 = new JScrollPane(disabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        enabledModsPanel = new JList<>();
        enabledModsPanel.setLayout(null);
        enabledModsPanel.setBackground(UIManager.getColor("Mods.modSelectionColor"));
        enabledModsPanel.setDragEnabled(true);
        enabledModsPanel.setTransferHandler(new ModsJCheckBoxTransferHandler(this, false));

        JScrollPane scroller2 = new JScrollPane(enabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        JPanel bottomPanel = new JPanel(new WrapLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton(GetText.tr("Add Mod"));
        addButton.addActionListener(e -> {
            String[] modTypes = new String[] { "Mods Folder", "Resource Pack", "Shader Pack", "Inside Minecraft.jar" };

            FileChooserDialog fcd = new FileChooserDialog(this, GetText.tr("Add Mod"), GetText.tr("Mod"),
                    GetText.tr("Add"), GetText.tr("Type of Mod"), modTypes);

            if (fcd.wasClosed()) {
                return;
            }

            final ProgressDialog<Object> progressDialog = new ProgressDialog<>(GetText.tr("Copying Mods"), 0,
                    GetText.tr("Copying Mods"), this);

            progressDialog.addThread(new Thread(() -> {
                ArrayList<File> files = fcd.getChosenFiles();
                if (files != null && !files.isEmpty()) {
                    boolean reload = false;
                    for (File file : files) {
                        String typeTemp = fcd.getSelectorValue();
                        com.atlauncher.data.Type type = null;
                        if (typeTemp.equalsIgnoreCase("Mods Folder")) {
                            type = com.atlauncher.data.Type.mods;
                        } else if (typeTemp.equalsIgnoreCase("Inside Minecraft.jar")) {
                            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Add As Mod?"))
                                    .setContent(new HTMLBuilder().text(GetText.tr(
                                            "Adding as Inside Minecraft.jar is usually not what you want and will likely cause issues.<br/><br/>If you're adding mods this is usually not correct. Do you want to add this as a mod instead?"))
                                            .build())
                                    .setType(DialogManager.WARNING).show();

                            if (ret != 0) {
                                type = com.atlauncher.data.Type.jar;
                            } else {
                                type = com.atlauncher.data.Type.mods;
                            }
                        } else if (typeTemp.equalsIgnoreCase("CoreMods Mod")) {
                            type = com.atlauncher.data.Type.coremods;
                        } else if (typeTemp.equalsIgnoreCase("Texture Pack")) {
                            type = com.atlauncher.data.Type.texturepack;
                        } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                            type = com.atlauncher.data.Type.resourcepack;
                        } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                            type = com.atlauncher.data.Type.shaderpack;
                        }
                        if (type != null) {
                            DisableableMod mod = DisableableMod.generateMod(file, type,
                                    App.settings.enableAddedModsByDefault);
                            File copyTo = App.settings.enableAddedModsByDefault ? mod.getFile(instance)
                                    : mod.getDisabledFile(instance);

                            if (copyTo.exists()) {
                                LogManager.warn("The file " + file.getName() + " already exists. Not adding!");
                                continue;
                            }

                            if (!copyTo.getParentFile().exists()) {
                                copyTo.getParentFile().mkdirs();
                            }

                            if (Utils.copyFile(file, copyTo, true)) {
                                instance.launcher.mods.add(mod);
                                reload = true;
                            }
                        }
                    }
                    if (reload) {
                        reloadPanels();
                    }
                }
                progressDialog.close();
            }));

            progressDialog.start();
        });
        bottomPanel.add(addButton);

        if (instance.launcher.enableCurseForgeIntegration) {
            if (ConfigManager.getConfigItem("platforms.curseforge.modsEnabled", true)
                    || (ConfigManager.getConfigItem("platforms.modrinth.modsEnabled", true)
                            && this.instance.launcher.loaderVersion != null)) {
                JButton browseMods = new JButton(GetText.tr("Browse Mods"));
                browseMods.addActionListener(e -> {
                    new AddModsDialog(this, instance);

                    loadMods();

                    reloadPanels();
                });
                bottomPanel.add(browseMods);
            }

            checkForUpdatesButton = new JButton(GetText.tr("Check For Updates"));
            checkForUpdatesButton.addActionListener(e -> checkForUpdates());
            checkForUpdatesButton.setEnabled(false);
            bottomPanel.add(checkForUpdatesButton);

            reinstallButton = new JButton(GetText.tr("Reinstall"));
            reinstallButton.addActionListener(e -> reinstall());
            reinstallButton.setEnabled(false);
            bottomPanel.add(reinstallButton);
        }

        enableButton = new JButton(GetText.tr("Enable Selected"));
        enableButton.addActionListener(e -> enableMods());
        enableButton.setEnabled(false);
        bottomPanel.add(enableButton);

        disableButton = new JButton(GetText.tr("Disable Selected"));
        disableButton.addActionListener(e -> disableMods());
        disableButton.setEnabled(false);
        bottomPanel.add(disableButton);

        removeButton = new JButton(GetText.tr("Remove Selected"));
        removeButton.addActionListener(e -> removeMods());
        removeButton.setEnabled(false);
        bottomPanel.add(removeButton);

        refreshMetadataButton = new JButton(GetText.tr("Refresh Metadata"));
        refreshMetadataButton.addActionListener(e -> refreshMetadata());
        refreshMetadataButton.setEnabled(false);
        bottomPanel.add(refreshMetadataButton);
    }

    private void loadMods() {
        List<DisableableMod> mods = instance.launcher.mods.stream().filter(DisableableMod::wasSelected)
                .filter(m -> !m.skipped && m.type != com.atlauncher.data.Type.worlds)
                .sorted(Comparator.comparing(m -> m.name)).collect(Collectors.toList());
        enabledMods = new ArrayList<>();
        disabledMods = new ArrayList<>();
        int dCount = 0;
        int eCount = 0;

        for (DisableableMod mod : mods) {
            ModsJCheckBox checkBox;
            int nameSize = getFontMetrics(App.THEME.getNormalFont()).stringWidth(mod.getName());

            checkBox = new ModsJCheckBox(mod, this);
            if (mod.isDisabled()) {
                checkBox.setBounds(0, (dCount * 20), Math.max(nameSize + 23, 250), 20);
                disabledMods.add(checkBox);
                dCount++;
            } else {
                checkBox.setBounds(0, (eCount * 20), Math.max(nameSize + 23, 250), 20);
                enabledMods.add(checkBox);
                eCount++;
            }
        }

        for (ModsJCheckBox checkBox : enabledMods) {
            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    checkBoxesChanged();
                }
            });
            enabledModsPanel.add(checkBox);
        }
        for (ModsJCheckBox checkBox : disabledMods) {
            checkBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
                    checkBoxesChanged();
                }
            });
            disabledModsPanel.add(checkBox);
        }
        enabledModsPanel.setPreferredSize(new Dimension(0, enabledMods.size() * 20));
        disabledModsPanel.setPreferredSize(new Dimension(0, disabledMods.size() * 20));
    }

    private void checkBoxesChanged() {
        if (instance.launcher.enableCurseForgeIntegration) {
            boolean hasSelectedACurseForgeOrModrinthMod = (enabledMods.stream().anyMatch(AbstractButton::isSelected)
                    && enabledMods.stream().filter(AbstractButton::isSelected)
                            .anyMatch(cb -> cb.getDisableableMod().isUpdatable()))
                    || (disabledMods.stream().anyMatch(AbstractButton::isSelected) && disabledMods.stream()
                            .filter(AbstractButton::isSelected).anyMatch(cb -> cb.getDisableableMod().isUpdatable()));

            checkForUpdatesButton.setEnabled(hasSelectedACurseForgeOrModrinthMod);
            reinstallButton.setEnabled(hasSelectedACurseForgeOrModrinthMod);
        }

        removeButton.setEnabled((!disabledMods.isEmpty() && disabledMods.stream().anyMatch(AbstractButton::isSelected))
                || (!enabledMods.isEmpty() && enabledMods.stream().anyMatch(AbstractButton::isSelected)));
        enableButton.setEnabled(!disabledMods.isEmpty() && disabledMods.stream().anyMatch(AbstractButton::isSelected));
        disableButton.setEnabled(!enabledMods.isEmpty() && enabledMods.stream().anyMatch(AbstractButton::isSelected));
        refreshMetadataButton
                .setEnabled(!enabledMods.isEmpty() && enabledMods.stream().anyMatch(AbstractButton::isSelected));

        selectAllEnabledModsCheckbox
                .setSelected(!enabledMods.isEmpty() && enabledMods.stream().allMatch(AbstractButton::isSelected));
        selectAllDisabledModsCheckbox
                .setSelected(!disabledMods.isEmpty() && disabledMods.stream().allMatch(AbstractButton::isSelected));
    }

    private void checkForUpdates() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>();
        mods.addAll(enabledMods);
        mods.addAll(disabledMods);

        ProgressDialog<Void> progressDialog = new ProgressDialog<>(GetText.tr("Checking For Updates"), mods.size(),
                GetText.tr("Checking For Updates"), this);
        progressDialog.addThread(new Thread(() -> {
            for (ModsJCheckBox mod : mods) {
                if (mod.isSelected() && mod.getDisableableMod().isUpdatable()) {
                    mod.getDisableableMod().checkForUpdate(progressDialog, instance);
                }
                progressDialog.doneTask();
            }

            progressDialog.close();
        }));
        progressDialog.start();

        DialogManager.okDialog().setTitle(GetText.tr("Checking For Updates Complete"))
                .setContent(GetText.tr("The selected mods have been checked for updates.")).show();

        reloadPanels();
    }

    private void reinstall() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>();
        mods.addAll(enabledMods);
        mods.addAll(disabledMods);

        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected() && mod.getDisableableMod().isUpdatable()) {
                mod.getDisableableMod().reinstall(this, instance);
            }
        }
        reloadPanels();
    }

    private void enableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>(disabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                mod.getDisableableMod().enable(instance);
            }
        }
        reloadPanels();
    }

    private void disableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                mod.getDisableableMod().disable(instance);
            }
        }
        reloadPanels();
    }

    private void removeMods() {
        int ret = DialogManager.yesNoDialog(false)
                .setTitle(GetText.tr("Delete Selected Mods?"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "This will delete the selected mods from the instance.<br/><br/>Are you sure you want to do this?"))
                        .build())
                .setType(DialogManager.WARNING).show();

        if (ret == 0) {
            ArrayList<ModsJCheckBox> mods = new ArrayList<>(enabledMods);
            for (ModsJCheckBox mod : mods) {
                if (mod.isSelected()) {
                    this.instance.launcher.mods.remove(mod.getDisableableMod());
                    FileUtils.delete(
                            (mod.getDisableableMod().isDisabled()
                                    ? mod.getDisableableMod().getDisabledFile(this.instance)
                                    : mod.getDisableableMod().getFile(this.instance)).toPath(),
                            true);
                    enabledMods.remove(mod);
                }
            }
            mods = new ArrayList<>(disabledMods);
            for (ModsJCheckBox mod : mods) {
                if (mod.isSelected()) {
                    this.instance.launcher.mods.remove(mod.getDisableableMod());
                    FileUtils.delete(
                            (mod.getDisableableMod().isDisabled()
                                    ? mod.getDisableableMod().getDisabledFile(this.instance)
                                    : mod.getDisableableMod().getFile(this.instance)).toPath(),
                            true);
                    disabledMods.remove(mod);
                }
            }
            reloadPanels();
        }
    }

    private void refreshMetadata() {
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Refreshing Metadata"), 0,
                GetText.tr("Refreshing Metadata"),
                "Aborting refreshing metadata");
        dialog.addThread(new Thread(() -> {

            List<ModsJCheckBox> modsToRefresh = new ArrayList<>();
            modsToRefresh
                    .addAll(enabledMods.parallelStream().filter(ModsJCheckBox::isSelected)
                            .collect(Collectors.toList()));
            modsToRefresh
                    .addAll(disabledMods.parallelStream().filter(ModsJCheckBox::isSelected)
                            .collect(Collectors.toList()));

            // TODO: Generalise this, cause fuck me I've copy pasted this like 10 times now
            if (!App.settings.dontCheckModsOnCurseForge) {
                Map<Long, ModsJCheckBox> murmurHashes = new HashMap<>();

                modsToRefresh.stream()
                        .filter(mjc -> mjc.getDisableableMod().getFile(instance.ROOT, instance.id) != null)
                        .forEach(mjc -> {
                            try {
                                long hash = Hashing
                                        .murmur(mjc.getDisableableMod().getFile(instance.ROOT, instance.id).toPath());
                                murmurHashes.put(hash, mjc);
                            } catch (Throwable t) {
                                LogManager.logStackTrace(t);
                            }
                        });

                if (!murmurHashes.isEmpty()) {
                    CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                            .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

                    if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                        int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                                .toArray();

                        if (projectIdsFound.length != 0) {
                            Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                                    .getProjectsAsMap(projectIdsFound);

                            if (foundProjects != null) {
                                fingerprintResponse.exactMatches.stream().filter(em -> em != null && em.file != null
                                        && murmurHashes.containsKey(em.file.packageFingerprint)).forEach(foundMod -> {
                                            DisableableMod dm = murmurHashes.get(foundMod.file.packageFingerprint)
                                                    .getDisableableMod();

                                            // add CurseForge information
                                            dm.curseForgeProjectId = foundMod.id;
                                            dm.curseForgeFile = foundMod.file;
                                            dm.curseForgeFileId = foundMod.file.id;

                                            CurseForgeProject curseForgeProject = foundProjects.get(foundMod.id);

                                            if (curseForgeProject != null) {
                                                dm.curseForgeProject = curseForgeProject;
                                                dm.name = curseForgeProject.name;
                                                dm.description = curseForgeProject.summary;
                                            }

                                            LogManager.debug("Found matching mod from CurseForge called "
                                                    + dm.curseForgeFile.displayName);
                                        });
                            }
                        }
                    }
                }
            }

            if (!App.settings.dontCheckModsOnModrinth) {
                Map<String, ModsJCheckBox> sha1Hashes = new HashMap<>();

                modsToRefresh.stream()
                        .filter(mjc -> mjc.getDisableableMod().getFile(instance.ROOT, instance.id) != null)
                        .forEach(mjc -> {
                            try {
                                sha1Hashes.put(
                                        Hashing.sha1(
                                                mjc.getDisableableMod().getFile(instance.ROOT, instance.id).toPath())
                                                .toString(),
                                        mjc);
                            } catch (Throwable t) {
                                LogManager.logStackTrace(t);
                            }
                        });

                if (!sha1Hashes.isEmpty()) {
                    Set<String> keys = sha1Hashes.keySet();
                    Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                            .getVersionsFromSha1Hashes(keys.toArray(new String[keys.size()]));

                    if (modrinthVersions != null && !modrinthVersions.isEmpty()) {
                        String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                .toArray(String[]::new);

                        if (projectIdsFound.length != 0) {
                            Map<String, ModrinthProject> foundProjects = ModrinthApi.getProjectsAsMap(projectIdsFound);

                            if (foundProjects != null) {
                                for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                    ModrinthVersion version = entry.getValue();
                                    ModrinthProject project = foundProjects.get(version.projectId);

                                    if (project != null) {
                                        DisableableMod dm = sha1Hashes.get(entry.getKey()).getDisableableMod();

                                        // add Modrinth information
                                        dm.modrinthProject = project;
                                        dm.modrinthVersion = version;
                                        dm.name = project.title;
                                        dm.description = project.description;

                                        LogManager
                                                .debug(String.format(
                                                        "Found matching mod from Modrinth called %s with file %s",
                                                        project.title, version.name));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            instance.save();

            dialog.close();
        }));
        dialog.start();

        reloadPanels();
    }

    public void reloadPanels() {
        this.instance.save();

        enabledModsPanel.removeAll();
        disabledModsPanel.removeAll();
        loadMods();
        checkBoxesChanged();
        enabledModsPanel.repaint();
        disabledModsPanel.repaint();
    }

}
