/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.json.ModType;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditModsDialog extends JDialog {
    private static final long serialVersionUID = 7004414192679481818L;

    private Instance instance; // The instance this is for

    private JPanel bottomPanel, disabledModsPanel, enabledModsPanel;
    private JSplitPane split, labelsTop, labels, modsInPack;
    private JScrollPane scroller1, scroller2;
    private JButton addButton, enableButton, disableButton, removeButton, closeButton;
    private JLabel topLabelLeft, topLabelRight;
    private ArrayList<ModsJCheckBox> enabledMods, disabledMods;

    public EditModsDialog(final Instance instance) {
        super(App.settings.getParent(), Language.INSTANCE.localizeWithReplace("instance.editingmods", instance
                .getName()), ModalityType.APPLICATION_MODAL);
        this.instance = instance;
        setSize(550, 450);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

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

        scroller1 = new JScrollPane(disabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        enabledModsPanel = new JPanel();
        enabledModsPanel.setLayout(null);
        enabledModsPanel.setBackground(App.THEME.getModSelectionBackgroundColor());

        scroller2 = new JScrollPane(enabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        addButton = new JButton(Language.INSTANCE.localize("instance.addmod"));
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileChooserDialog fcd = new FileChooserDialog(Language.INSTANCE.localize("instance.addmod"), Language
                        .INSTANCE.localize("common.mod"), Language.INSTANCE.localize("common.add"), Language.INSTANCE
                        .localize("instance.typeofmod"), new String[]{"Mods Folder", "Inside Minecraft.jar",
                        "CoreMods Mod", "Texture Pack", "Resource Pack", "Shader Pack"}, new String[]{"jar", "zip",
                        "litemod"});
                ArrayList<File> files = fcd.getChosenFiles();
                if (files != null && files.size() >= 1) {
                    boolean reload = false;
                    for (File file : files) {
                        String typeTemp = fcd.getSelectorValue();
                        ModType type = null;
                        if (typeTemp.equalsIgnoreCase("Mods Folder")) {
                            type = ModType.mods;
                        } else if (typeTemp.equalsIgnoreCase("Inside Minecraft.jar")) {
                            type = ModType.jar;
                        } else if (typeTemp.equalsIgnoreCase("CoreMods Mod")) {
                            type = ModType.coremods;
                        } else if (typeTemp.equalsIgnoreCase("Texture Pack")) {
                            type = ModType.texturepack;
                        } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                            type = ModType.resourcepack;
                        } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                            type = ModType.shaderpack;
                        }
                        if (type != null) {
                            DisableableMod mod = new DisableableMod(file.getName(), "Custom", true, file.getName(),
                                    type, null, null, true, true);
                            if (FileUtils.copyFile(file.toPath(), instance.getDisabledModsDirectory())) {
                                instance.getInstalledMods().add(mod);
                                disabledMods.add(new ModsJCheckBox(mod));
                                reload = true;
                            }
                        }
                    }
                    if (reload) {
                        reloadPanels();
                    }
                }
            }
        });
        bottomPanel.add(addButton);

        enableButton = new JButton(Language.INSTANCE.localize("instance.enablemod"));
        enableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableMods();
            }
        });
        bottomPanel.add(enableButton);

        disableButton = new JButton(Language.INSTANCE.localize("instance.disablemod"));
        disableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disableMods();
            }
        });
        bottomPanel.add(disableButton);

        removeButton = new JButton(Language.INSTANCE.localize("instance.removemod"));
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeMods();
            }
        });
        bottomPanel.add(removeButton);

        closeButton = new JButton(Language.INSTANCE.localize("common.close"));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(closeButton);

        loadMods();

        setVisible(true);
    }

    private void loadMods() {
        List<DisableableMod> mods = instance.getInstalledMods();
        enabledMods = new ArrayList<ModsJCheckBox>();
        disabledMods = new ArrayList<ModsJCheckBox>();
        int dCount = 0;
        int eCount = 0;
        for (DisableableMod mod : mods) {
            ModsJCheckBox checkBox = null;
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(mod.getName());

            checkBox = new ModsJCheckBox(mod);
            if (mod.isDisabled()) {
                checkBox.setBounds(0, (dCount * 20), checkBox.getPreferredSize().width, 20);
                disabledMods.add(checkBox);
                dCount++;
            } else {
                checkBox.setBounds(0, (eCount * 20), checkBox.getPreferredSize().width, 20);
                enabledMods.add(checkBox);
                eCount++;
            }
        }
        for (int i = 0; i < enabledMods.size(); i++) {
            ModsJCheckBox checkBox = enabledMods.get(i);
            enabledModsPanel.add(checkBox);
        }
        for (int i = 0; i < disabledMods.size(); i++) {
            ModsJCheckBox checkBox = disabledMods.get(i);
            disabledModsPanel.add(checkBox);
        }
        enabledModsPanel.setPreferredSize(new Dimension(0, enabledMods.size() * 20));
        disabledModsPanel.setPreferredSize(new Dimension(0, disabledMods.size() * 20));
    }

    private void enableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<ModsJCheckBox>(disabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                mod.getDisableableMod().enable(instance);
            }
        }
        reloadPanels();
    }

    private void disableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<ModsJCheckBox>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                mod.getDisableableMod().disable(instance);
            }
        }
        reloadPanels();
    }

    private void removeMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<ModsJCheckBox>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                instance.removeInstalledMod(mod.getDisableableMod());
                enabledMods.remove(mod);
            }
        }
        mods = new ArrayList<ModsJCheckBox>(disabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                instance.removeInstalledMod(mod.getDisableableMod());
                disabledMods.remove(mod);
            }
        }
        reloadPanels();
    }

    private void reloadPanels() {
        InstanceManager.saveInstances();
        enabledModsPanel.removeAll();
        disabledModsPanel.removeAll();
        loadMods();
        enabledModsPanel.repaint();
        disabledModsPanel.repaint();
    }

}
