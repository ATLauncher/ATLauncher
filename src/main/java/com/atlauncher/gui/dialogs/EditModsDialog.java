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
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.curse.CurseFingerprint;
import com.atlauncher.data.curse.CurseFingerprintedMod;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.gui.handlers.ModsJCheckBoxTransferHandler;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseApi;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

public class EditModsDialog extends JDialog {
    private static final long serialVersionUID = 7004414192679481818L;

    public Instance instance;
    public InstanceV2 instanceV2;

    private JList<ModsJCheckBox> disabledModsPanel, enabledModsPanel;
    private JButton checkForUpdatesButton;
    private JButton reinstallButton;
    private JButton enableButton;
    private JButton disableButton;
    private JButton removeButton;
    private JCheckBox selectAllEnabledModsCheckbox, selectAllDisabledModsCheckbox;
    private ArrayList<ModsJCheckBox> enabledMods, disabledMods;

    public EditModsDialog(Instance instance) {
        super(App.launcher.getParent(),
                // #. {0} is the name of the instance
                GetText.tr("Editing Mods For {0}", instance.getName()), ModalityType.APPLICATION_MODAL);
        this.instance = instance;
        setSize(550, 450);
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

        setupComponents();

        loadMods();

        setVisible(true);
    }

    public EditModsDialog(InstanceV2 instanceV2) {
        super(App.launcher.getParent(),
                // #. {0} is the name of the instance
                GetText.tr("Editing Mods For {0}", instanceV2.launcher.name), ModalityType.APPLICATION_MODAL);
        this.instanceV2 = instanceV2;
        setSize(550, 450);
        setLocationRelativeTo(App.launcher.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

        setupComponents();

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
            boolean usesCoreMods = false;
            try {
                usesCoreMods = MinecraftManager.getMinecraftVersion(
                        instanceV2 != null ? instanceV2.id : this.instance.getMinecraftVersion()).coremods;
            } catch (InvalidMinecraftVersion e1) {
                LogManager.logStackTrace(e1);
            }
            String[] modTypes;
            if (usesCoreMods) {
                modTypes = new String[] { "Mods Folder", "Inside Minecraft.jar", "CoreMods Mod", "Texture Pack",
                        "Shader Pack" };
            } else {
                modTypes = new String[] { "Mods Folder", "Inside Minecraft.jar", "Resource Pack", "Shader Pack" };
            }

            FileChooserDialog fcd = new FileChooserDialog(GetText.tr("Add Mod"), GetText.tr("Mod"), GetText.tr("Add"),
                    GetText.tr("Type of Mod"), modTypes, new String[] { "jar", "zip", "litemod" });

            if (fcd.wasClosed()) {
                return;
            }

            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Copying Mods"), 0,
                    GetText.tr("Copying Mods"));

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
                            type = com.atlauncher.data.Type.jar;
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
                            DisableableMod mod = new DisableableMod();
                            mod.disabled = true;
                            mod.userAdded = true;
                            mod.wasSelected = true;
                            mod.file = file.getName();
                            mod.type = type;
                            mod.optional = true;
                            mod.name = file.getName();
                            mod.version = "Unknown";
                            mod.description = null;

                            MCMod mcMod = Utils.getMCModForFile(file);
                            if (mcMod != null) {
                                mod.name = Optional.ofNullable(mcMod.name).orElse(file.getName());
                                mod.version = Optional.ofNullable(mcMod.version).orElse("Unknown");
                                mod.description = Optional.ofNullable(mcMod.description).orElse(null);
                            } else {
                                FabricMod fabricMod = Utils.getFabricModForFile(file);
                                if (fabricMod != null) {
                                    mod.name = Optional.ofNullable(fabricMod.name).orElse(file.getName());
                                    mod.version = Optional.ofNullable(fabricMod.version).orElse("Unknown");
                                    mod.description = Optional.ofNullable(fabricMod.description).orElse(null);
                                }
                            }

                            File copyTo = instanceV2 != null ? instanceV2.getRoot().resolve("disabledmods").toFile()
                                    : instance.getDisabledModsDirectory();

                            try {
                                long murmurHash = Hashing.murmur(file.toPath());

                                LogManager.debug("File " + file.getName() + " has murmur hash of " + murmurHash);

                                CurseFingerprint fingerprintResponse = CurseApi.checkFingerprint(murmurHash);

                                if (fingerprintResponse.exactMatches.size() == 1) {
                                    CurseFingerprintedMod foundMod = fingerprintResponse.exactMatches.get(0);

                                    // add Curse information
                                    mod.curseMod = CurseApi.getModById(foundMod.id);
                                    mod.curseModId = foundMod.id;
                                    mod.curseFile = foundMod.file;
                                    mod.curseFileId = foundMod.file.id;

                                    mod.name = mod.curseMod.name;
                                    mod.description = mod.curseMod.summary;

                                    LogManager.debug("Found matching mod from CurseForge called " + mod.curseMod.name
                                            + " with file named " + mod.curseFile.displayName);
                                }
                            } catch (IOException e1) {
                                LogManager.logStackTrace(e1);
                            }

                            if (Utils.copyFile(file, copyTo)) {
                                if (this.instanceV2 != null) {
                                    instanceV2.launcher.mods.add(mod);
                                } else {
                                    instance.getInstalledMods().add(mod);
                                    disabledMods.add(new ModsJCheckBox(mod, this));
                                }
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

        if (instanceV2 != null ? instanceV2.launcher.enableCurseIntegration
                : this.instance.hasEnabledCurseIntegration()) {
            JButton addCurseModButton = new JButton(GetText.tr("Add Curse Mod"));
            addCurseModButton.addActionListener(e -> {
                if (instanceV2 != null) {
                    new AddModsDialog(instanceV2);
                } else {
                    new AddModsDialog(instance);
                }

                loadMods();

                reloadPanels();

            });
            bottomPanel.add(addCurseModButton);

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

        JButton closeButton = new JButton(GetText.tr("Close"));
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
    }

    private void loadMods() {
        List<DisableableMod> mods = instanceV2 != null
                ? instanceV2.launcher.mods.stream().filter(DisableableMod::wasSelected).collect(Collectors.toList())
                : instance.getInstalledSelectedMods();
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
        if (instanceV2 != null ? instanceV2.launcher.enableCurseIntegration
                : this.instance.hasEnabledCurseIntegration()) {
            boolean hasSelectedAllCurseMods = (enabledMods.stream().anyMatch(AbstractButton::isSelected) && enabledMods
                    .stream().filter(AbstractButton::isSelected).allMatch(cb -> cb.getDisableableMod().isFromCurse()))
                    || (disabledMods.stream().anyMatch(AbstractButton::isSelected) && disabledMods.stream()
                            .filter(AbstractButton::isSelected).allMatch(cb -> cb.getDisableableMod().isFromCurse()));

            checkForUpdatesButton.setEnabled(hasSelectedAllCurseMods);
            reinstallButton.setEnabled(hasSelectedAllCurseMods);
        }

        removeButton.setEnabled((disabledMods.size() != 0 && disabledMods.stream().anyMatch(AbstractButton::isSelected))
                || (enabledMods.size() != 0 && enabledMods.stream().anyMatch(AbstractButton::isSelected)));
        enableButton.setEnabled(disabledMods.size() != 0 && disabledMods.stream().anyMatch(AbstractButton::isSelected));
        disableButton.setEnabled(enabledMods.size() != 0 && enabledMods.stream().anyMatch(AbstractButton::isSelected));

        selectAllEnabledModsCheckbox
                .setSelected(enabledMods.size() != 0 && enabledMods.stream().allMatch(AbstractButton::isSelected));
        selectAllDisabledModsCheckbox
                .setSelected(disabledMods.size() != 0 && disabledMods.stream().allMatch(AbstractButton::isSelected));
    }

    private void checkForUpdates() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>();
        mods.addAll(enabledMods);
        mods.addAll(disabledMods);

        ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Checking For Updates"), mods.size(),
                GetText.tr("Checking For Updates"));
        progressDialog.addThread(new Thread(() -> {
            for (ModsJCheckBox mod : mods) {
                if (mod.isSelected() && mod.getDisableableMod().isFromCurse()) {
                    if (this.instanceV2 != null) {
                        mod.getDisableableMod().checkForUpdate(instanceV2);
                    } else {
                        mod.getDisableableMod().checkForUpdate(instance);
                    }
                }
                progressDialog.doneTask();
            }

            progressDialog.close();
        }));
        progressDialog.start();

        DialogManager.okDialog().setTitle(GetText.tr("Checking For Updates Complete"))
                .setContent(GetText.tr("The selected mods have been updated (if available).")).show();

        reloadPanels();
    }

    private void reinstall() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>();
        mods.addAll(enabledMods);
        mods.addAll(disabledMods);

        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected() && mod.getDisableableMod().isFromCurse()) {
                if (this.instanceV2 != null) {
                    mod.getDisableableMod().reinstall(instanceV2);
                } else {
                    mod.getDisableableMod().reinstall(instance);
                }
            }
        }
        reloadPanels();
    }

    private void enableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>(disabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                if (this.instanceV2 != null) {
                    mod.getDisableableMod().enable(instanceV2);
                } else {
                    mod.getDisableableMod().enable(instance);
                }
            }
        }
        reloadPanels();
    }

    private void disableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                if (this.instanceV2 != null) {
                    mod.getDisableableMod().disable(instanceV2);
                } else {
                    mod.getDisableableMod().disable(instance);
                }
            }
        }
        reloadPanels();
    }

    private void removeMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                if (this.instanceV2 != null) {
                    this.instanceV2.launcher.mods.remove(mod.getDisableableMod());
                    Utils.delete((mod.getDisableableMod().isDisabled()
                            ? mod.getDisableableMod().getDisabledFile(this.instanceV2)
                            : mod.getDisableableMod().getFile(this.instanceV2)));
                } else {
                    instance.removeInstalledMod(mod.getDisableableMod());
                }
                enabledMods.remove(mod);
            }
        }
        mods = new ArrayList<>(disabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                if (this.instanceV2 != null) {
                    this.instanceV2.launcher.mods.remove(mod.getDisableableMod());
                    Utils.delete((mod.getDisableableMod().isDisabled()
                            ? mod.getDisableableMod().getDisabledFile(this.instanceV2)
                            : mod.getDisableableMod().getFile(this.instanceV2)));
                } else {
                    instance.removeInstalledMod(mod.getDisableableMod());
                }
                disabledMods.remove(mod);
            }
        }
        reloadPanels();
    }

    public void reloadPanels() {
        if (this.instanceV2 != null) {
            this.instanceV2.save();
        } else {
            InstanceManager.saveInstances();
        }

        enabledModsPanel.removeAll();
        disabledModsPanel.removeAll();
        loadMods();
        checkBoxesChanged();
        enabledModsPanel.repaint();
        disabledModsPanel.repaint();
    }

}
