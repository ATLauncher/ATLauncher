/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.data.Mod;
import com.atlauncher.workers.InstanceInstaller;

public class ModsChooser extends JDialog {

    private InstanceInstaller installer;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private ArrayList<ModsJCheckBox> modCheckboxes;
    private ArrayList<ModDescriptionJLabel> modLabels;

    private boolean wasClosed = false;

    public ModsChooser(InstanceInstaller installerr) {
        super(App.settings.getParent(), App.settings.getLocalizedString("instance.selectmods"),
                ModalityType.APPLICATION_MODAL);
        this.installer = installerr;
        setSize(550, 450);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                wasClosed = true;
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

        JSplitPane labels = new JSplitPane();
        labels.setDividerLocation(275);
        labels.setDividerSize(0);
        labels.setBorder(null);
        labels.setEnabled(false);
        split.setRightComponent(labels);

        JLabel topLabelLeft = new JLabel(App.settings.getLocalizedString("instance.requiredmods"));
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setLeftComponent(topLabelLeft);

        JLabel topLabelRight = new JLabel(App.settings.getLocalizedString("instance.optionalmods"));
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

        selectAllButton = new JButton();

        if (installer.hasRecommendedMods()) {
            selectAllButton.setText(App.settings.getLocalizedString("instance.selectrecommended"));
        } else {
            selectAllButton.setText(App.settings.getLocalizedString("instance.selectall"));
        }

        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if ((installer.isServer() ? check.getMod().isServerOptional() : check.getMod()
                            .isOptional()) && check.getMod().isRecommeneded()) {
                        if (check.getMod().hasGroup()) {
                            if (check.getMod().isRecommeneded()
                                    && installer.isOnlyRecommendedInGroup(check.getMod())) {
                                check.setSelected(true);
                                check.setEnabled(true);
                                sortOutMods(check);
                            }
                        } else {
                            check.setSelected(true);
                            check.setEnabled(true);
                            sortOutMods(check);
                        }
                    }
                }
            }
        });
        bottomPanel.add(selectAllButton);

        clearAllButton = new JButton(App.settings.getLocalizedString("instance.clearall"));
        clearAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if ((installer.isServer() ? check.getMod().isServerOptional() : check.getMod()
                            .isOptional())) {
                        check.setSelected(false);
                        ArrayList<Mod> linkedMods = modsToChange(check.getMod());
                        for (Mod mod : linkedMods) {
                            for (ModsJCheckBox check1 : modCheckboxes) {
                                if (check1.getMod() == mod) {
                                    check1.setEnabled(false);
                                }
                            }
                        }
                    }
                }
            }
        });
        bottomPanel.add(clearAllButton);

        JButton installButton = new JButton(App.settings.getLocalizedString("common.install"));
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
            if (installer.isServer() && !mod.installOnServer()) {
                continue;
            }
            ModsJCheckBox checkBox = null;
            ModDescriptionJLabel label = null;
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(mod.getName());
            if ((installer.isServer() ? mod.isServerOptional() : mod.isOptional())) {
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
                    if ((installer.isServer() ? linkedMod.isServerOptional() : linkedMod
                            .isOptional())) {
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
                if (mod.isHidden() || mod.isLibrary()) {
                    checkBox.setVisible(false);
                    if (!mod.getDescription().isEmpty()) {
                        label.setVisible(false);
                    }
                } else {
                    count1++;
                }
            } else {
                checkBox = new ModsJCheckBox(mod);
                checkBox.setBounds(0, (count2 * 20), nameSize + 23, 20);
                if (!mod.getDescription().isEmpty()) {
                    label = new ModDescriptionJLabel(mod.getDescription());
                    label.setBounds(nameSize + 24, (count2 * 20), 12, 20);
                }
                checkBox.setSelected(true);
                checkBox.setEnabled(false);

                if (mod.isHidden()) {
                    checkBox.setVisible(false);
                    if (!mod.getDescription().isEmpty()) {
                        label.setVisible(false);
                    }
                } else {
                    count2++;
                }
            }
            if (installer.wasModInstalled(mod.getName())) {
                if ((installer.isServer() ? mod.isServerOptional() : mod.isOptional())) {
                    checkBox.setSelected(true);
                    checkBox.setEnabled(true);
                }
            }
            checkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ModsJCheckBox a = (ModsJCheckBox) e.getSource();
                    sortOutMods(a);
                }
            });
            modCheckboxes.add(checkBox);
            modLabels.add(label);
        }
        for (int i = 0; i < modCheckboxes.size(); i++) {
            ModsJCheckBox checkBox = modCheckboxes.get(i);
            ModDescriptionJLabel label = modLabels.get(i);
            if ((installer.isServer() ? checkBox.getMod().isServerOptional() : checkBox.getMod()
                    .isOptional())) {
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

    private ArrayList<Mod> modsInGroup(Mod mod) {
        return installer.getGroupedMods(mod);
    }

    private ArrayList<Mod> modsDependancies(Mod mod) {
        return installer.getModsDependancies(mod);
    }

    private ArrayList<Mod> dependedMods(Mod mod) {
        return installer.dependedMods(mod);
    }

    private boolean hasADependancy(Mod mod) {
        return installer.hasADependancy(mod);
    }

    public void sortOutMods(ModsJCheckBox a) {
        if (a.isSelected()) {
            ArrayList<Mod> linkedMods = modsToChange(a.getMod());
            for (Mod mod : linkedMods) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.getMod() == mod) {
                        check.setEnabled(true);
                    }
                }
            }
            if (a.getMod().hasGroup()) {
                ArrayList<Mod> groupMods = modsInGroup(a.getMod());
                for (Mod mod : groupMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            }
            if (a.getMod().hasDepends()) {
                ArrayList<Mod> dependsMods = modsDependancies(a.getMod());
                for (Mod mod : dependsMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            check.setSelected(true);
                        }
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
            if (hasADependancy(a.getMod())) {
                ArrayList<Mod> dependedMods = dependedMods(a.getMod());
                for (Mod mod : dependedMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            } else if (a.getMod().hasDepends()) {
                ArrayList<Mod> dependsMods = modsDependancies(a.getMod());
                for (Mod mod : dependsMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            if (check.getMod().isLibrary()) {
                                check.setSelected(false);
                            }
                        }
                    }
                }
            }
        }
    }

    public ArrayList<Mod> getSelectedMods() {
        if (wasClosed) {
            return null;
        }
        ArrayList<Mod> mods = new ArrayList<Mod>();
        for (ModsJCheckBox check : modCheckboxes) {
            if (check.isSelected()) {
                mods.add(check.getMod());
            }
        }
        return mods;
    }

    public boolean wasClosed() {
        return this.wasClosed;
    }

}
