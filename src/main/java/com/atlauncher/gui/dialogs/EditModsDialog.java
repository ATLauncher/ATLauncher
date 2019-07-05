/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.Language;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.utils.Utils;

public class EditModsDialog extends JDialog {
    private static final long serialVersionUID = 7004414192679481818L;

    private Instance instance;
    private InstanceV2 instanceV2;

    private JPanel bottomPanel, disabledModsPanel, enabledModsPanel;
    private JSplitPane split, labelsTop, labels, modsInPack;
    private JScrollPane scroller1, scroller2;
    private JButton addButton, enableButton, disableButton, removeButton, closeButton;
    private JLabel topLabelLeft, topLabelRight;
    private ArrayList<ModsJCheckBox> enabledMods, disabledMods;

    public EditModsDialog(Instance instance) {
        super(App.settings.getParent(),
                Language.INSTANCE.localizeWithReplace("instance.editingmods", instance.getName()),
                ModalityType.APPLICATION_MODAL);
        this.instance = instance;
        setSize(550, 450);
        setLocationRelativeTo(App.settings.getParent());
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
        super(App.settings.getParent(),
                Language.INSTANCE.localizeWithReplace("instance.editingmods", instanceV2.launcher.name),
                ModalityType.APPLICATION_MODAL);
        this.instanceV2 = instanceV2;
        setSize(550, 450);
        setLocationRelativeTo(App.settings.getParent());
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
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerSize(0);
        split.setBorder(null);
        split.setEnabled(false);
        add(split, BorderLayout.NORTH);

        labelsTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        labelsTop.setDividerSize(0);
        labelsTop.setBorder(null);
        labelsTop.setEnabled(false);
        split.setLeftComponent(labelsTop);

        labels = new JSplitPane();
        labels.setDividerLocation(275);
        labels.setDividerSize(0);
        labels.setBorder(null);
        labels.setEnabled(false);
        split.setRightComponent(labels);

        topLabelLeft = new JLabel(Language.INSTANCE.localize("instance.enabledmods"));
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setLeftComponent(topLabelLeft);

        topLabelRight = new JLabel(Language.INSTANCE.localize("instance.disabledmods"));
        topLabelRight.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setRightComponent(topLabelRight);

        modsInPack = new JSplitPane();
        modsInPack.setDividerLocation(275);
        modsInPack.setDividerSize(0);
        modsInPack.setBorder(null);
        modsInPack.setEnabled(false);
        add(modsInPack, BorderLayout.CENTER);

        disabledModsPanel = new JPanel();
        disabledModsPanel.setLayout(null);
        disabledModsPanel.setBackground(App.THEME.getModSelectionBackgroundColor());

        scroller1 = new JScrollPane(disabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        enabledModsPanel = new JPanel();
        enabledModsPanel.setLayout(null);
        enabledModsPanel.setBackground(App.THEME.getModSelectionBackgroundColor());

        scroller2 = new JScrollPane(enabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        addButton = new JButton(Language.INSTANCE.localize("instance.addmod"));
        addButton.addActionListener(e -> {
            if (instanceV2 != null ? instanceV2.launcher.enableCurseIntegration
                    : this.instance.hasEnabledCurseIntegration()) {
                if (instanceV2 != null) {
                    new AddModsDialog(instanceV2);
                } else {
                    new AddModsDialog(instance);
                }

                loadMods();

                reloadPanels();

                return;
            }

            boolean usesCoreMods = false;
            try {
                usesCoreMods = App.settings
                        .getMinecraftVersion(instanceV2 != null ? instanceV2.id : this.instance.getMinecraftVersion())
                        .usesCoreMods();
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

            FileChooserDialog fcd = new FileChooserDialog(Language.INSTANCE.localize("instance.addmod"),
                    Language.INSTANCE.localize("common.mod"), Language.INSTANCE.localize("common.add"),
                    Language.INSTANCE.localize("instance.typeofmod"), modTypes,
                    new String[] { "jar", "zip", "litemod" });
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
                        DisableableMod mod = new DisableableMod(file.getName(), "Custom", true, file.getName(), type,
                                null, null, true, true);
                        if (Utils.copyFile(file,
                                instanceV2 != null ? instanceV2.getRoot().resolve("disabledmods").toFile()
                                        : instance.getDisabledModsDirectory())) {

                            if (this.instanceV2 != null) {
                                instanceV2.launcher.mods.add(mod);
                            } else {
                                instance.getInstalledMods().add(mod);
                                disabledMods.add(new ModsJCheckBox(mod));
                            }
                            reload = true;
                        }
                    }
                }
                if (reload) {
                    reloadPanels();
                }
            }
        });
        bottomPanel.add(addButton);

        enableButton = new JButton(Language.INSTANCE.localize("instance.enablemod"));
        enableButton.addActionListener(e -> enableMods());
        bottomPanel.add(enableButton);

        disableButton = new JButton(Language.INSTANCE.localize("instance.disablemod"));
        disableButton.addActionListener(e -> disableMods());
        bottomPanel.add(disableButton);

        removeButton = new JButton(Language.INSTANCE.localize("instance.removemod"));
        removeButton.addActionListener(e -> removeMods());
        bottomPanel.add(removeButton);

        closeButton = new JButton(Language.INSTANCE.localize("common.close"));
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
    }

    private void loadMods() {
        List<DisableableMod> mods = instanceV2 != null
                ? instanceV2.launcher.mods.stream().filter(m -> m.wasSelected()).collect(Collectors.toList())
                : instance.getInstalledSelectedMods();
        enabledMods = new ArrayList<>();
        disabledMods = new ArrayList<>();
        int dCount = 0;
        int eCount = 0;
        for (DisableableMod mod : mods) {
            ModsJCheckBox checkBox = null;
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(mod.getName());

            checkBox = new ModsJCheckBox(mod);
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
            enabledModsPanel.add(checkBox);
        }
        for (ModsJCheckBox checkBox : disabledMods) {
            disabledModsPanel.add(checkBox);
        }
        enabledModsPanel.setPreferredSize(new Dimension(0, enabledMods.size() * 20));
        disabledModsPanel.setPreferredSize(new Dimension(0, disabledMods.size() * 20));
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

    private void reloadPanels() {
        if (this.instanceV2 != null) {
            this.instanceV2.save();
        } else {
            App.settings.saveInstances();
        }

        enabledModsPanel.removeAll();
        disabledModsPanel.removeAll();
        loadMods();
        enabledModsPanel.repaint();
        disabledModsPanel.repaint();
    }

}
