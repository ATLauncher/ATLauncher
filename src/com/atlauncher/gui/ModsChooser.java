/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import com.atlauncher.data.Mod;
import com.atlauncher.workers.PackInstaller;

public class ModsChooser extends JDialog {

    private PackInstaller installer;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private ArrayList<ModsJCheckBox> modCheckboxes;
    private ArrayList<ModDescriptionJLabel> modLabels;

    public ModsChooser(PackInstaller installerr) {
        super(LauncherFrame.settings.getParent(), "Select Mods To Install",
                ModalityType.APPLICATION_MODAL);
        this.installer = installerr;
        setSize(550, 450);
        setLocationRelativeTo(LauncherFrame.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                dispose();
            }
        });

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

        JLabel topLabelTop = new JLabel("Select Configuration: ");
        topLabelTop.setHorizontalAlignment(SwingConstants.CENTER);
        labelsTop.setLeftComponent(topLabelTop);

        JComboBox<String> configs = new JComboBox<String>();
        configs.addItem("Custom Configuration");
        configs.setSelectedIndex(0);
        labelsTop.setRightComponent(configs);

        JSplitPane labels = new JSplitPane();
        labels.setDividerLocation(275);
        labels.setDividerSize(0);
        labels.setBorder(null);
        labels.setEnabled(false);
        split.setRightComponent(labels);

        JLabel topLabelLeft = new JLabel("Required Mods");
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setLeftComponent(topLabelLeft);

        JLabel topLabelRight = new JLabel("Optional Mods");
        topLabelRight.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setRightComponent(topLabelRight);

        JSplitPane modsInPack = new JSplitPane();
        modsInPack.setDividerLocation(275);
        modsInPack.setDividerSize(0);
        modsInPack.setBorder(null);
        modsInPack.setEnabled(false);
        add(modsInPack, BorderLayout.CENTER);

        JPanel checkBoxPanel1 = new JPanel();
        checkBoxPanel1.setLayout(null);
        checkBoxPanel1.setBackground(new Color(50, 55, 60));

        JScrollPane scroller1 = new JScrollPane(checkBoxPanel1,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        JPanel checkBoxPanel2 = new JPanel();
        checkBoxPanel2.setLayout(null);
        checkBoxPanel2.setBackground(new Color(50, 55, 60));

        JScrollPane scroller2 = new JScrollPane(checkBoxPanel2,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        selectAllButton = new JButton("Select All");

        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if(check.getMod().isOptional()){
                        check.setSelected(true);
                        check.setEnabled(true);
                    }
                }
            }
        });
        bottomPanel.add(selectAllButton);

        clearAllButton = new JButton("Clear All");
        clearAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if(check.getMod().isOptional()){
                        check.setSelected(false);
                        ArrayList<Mod> linkedMods = modsToChange(check.getMod());
                        for (Mod mod : linkedMods) {
                            for (ModsJCheckBox check1 : modCheckboxes) {
                                if (check1.getMod() == mod) {
                                    check1.setEnabled(false);
                                    check1.setSelected(false);
                                }
                            }
                        }
                    }
                }
            }
        });
        bottomPanel.add(clearAllButton);

        JButton installButton = new JButton("Install");
        installButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(installButton);

        modCheckboxes = new ArrayList<ModsJCheckBox>();
        modLabels = new ArrayList<ModDescriptionJLabel>();
        int count1 = 0;
        int count2 = 0;

        for (Mod mod : installer.getMods()) {
            ModsJCheckBox checkBox = null;
            ModDescriptionJLabel label = null;
            int nameSize = getFontMetrics(getFont()).stringWidth(mod.getName());
            if (mod.isOptional()) {
                checkBox = new ModsJCheckBox(mod);
                checkBox.setEnabled(true);
                if (mod.getLinked().isEmpty()) {
                    checkBox.setBounds(0, (count1 * 20), nameSize + 23, 20);
                    if (!mod.getDescription().isEmpty()) {
                        label = new ModDescriptionJLabel(mod.getDescription());
                        label.setBounds(nameSize + 24, (count1 * 20), 12, 20);
                    }
                } else {
                    Mod linkedMod = installer.getModByName(mod.getLinked());
                    if (linkedMod.isOptional()) {
                        checkBox.setEnabled(false);
                        checkBox.setBounds(20, (count1 * 20), nameSize + 23, 20);
                        if (!mod.getDescription().isEmpty()) {
                            label = new ModDescriptionJLabel(mod.getDescription());
                            label.setBounds(nameSize + 44, (count1 * 20), 12, 20);
                        }
                    } else {
                        checkBox.setBounds(0, (count1 * 20), nameSize + 23, 20);
                        if (!mod.getDescription().isEmpty()) {
                            label = new ModDescriptionJLabel(mod.getDescription());
                            label.setBounds(nameSize + 24, (count1 * 20), 12, 20);
                        }
                    }
                }
                count1++;
            } else {
                checkBox = new ModsJCheckBox(mod);
                checkBox.setBounds(0, (count2 * 20), nameSize + 23, 20);
                if (!mod.getDescription().isEmpty()) {
                    label = new ModDescriptionJLabel(mod.getDescription());
                    label.setBounds(nameSize + 24, (count2 * 20), 12, 20);
                }
                checkBox.setSelected(true);
                checkBox.setEnabled(false);
                count2++;
            }
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ModsJCheckBox a = (ModsJCheckBox) e.getSource();
                    if (a.isSelected()) {
                        ArrayList<Mod> linkedMods = modsToChange(a.getMod());
                        for (Mod mod : linkedMods) {
                            for (ModsJCheckBox check : modCheckboxes) {
                                if (check.getMod() == mod) {
                                    check.setEnabled(true);
                                }
                            }
                        }
                    } else {
                        ArrayList<Mod> linkedMods = modsToChange(a.getMod());
                        for (Mod mod : linkedMods) {
                            for (ModsJCheckBox check : modCheckboxes) {
                                if (check.getMod() == mod) {
                                    check.setEnabled(false);
                                    check.setSelected(false);
                                }
                            }
                        }
                    }
                }
            });
            modCheckboxes.add(checkBox);
            modLabels.add(label);
        }
        for (int i = 0; i < modCheckboxes.size(); i++) {
            ModsJCheckBox checkBox = modCheckboxes.get(i);
            ModDescriptionJLabel label = modLabels.get(i);
            if (checkBox.getMod().isOptional()) {
                if (!checkBox.getMod().getDescription().isEmpty()) {
                    checkBoxPanel1.add(label);
                }
                checkBoxPanel1.add(checkBox);
            } else {
                if (!checkBox.getMod().getDescription().isEmpty()) {
                    checkBoxPanel2.add(label);
                }
                checkBoxPanel2.add(checkBox);
            }
        }
        checkBoxPanel1.setPreferredSize(new Dimension(0, count1 * 20));
        checkBoxPanel2.setPreferredSize(new Dimension(0, count2 * 20));
    }

    private ArrayList<Mod> modsToChange(Mod mod) {
        return installer.getLinkedMods(mod);
    }

    public ArrayList<Mod> getSelectedMods() {
        ArrayList<Mod> mods = new ArrayList<Mod>();
        for (ModsJCheckBox check : modCheckboxes) {
            if (check.isSelected()) {
                mods.add(check.getMod());
            }
        }
        return mods;
    }

}
