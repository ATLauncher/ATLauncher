/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.dialogs;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Language;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

public class JsonModsChooser extends JDialog {
    private static final long serialVersionUID = -5309108183485463434L;
    private InstanceInstaller installer;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private List<ModsJCheckBox> modCheckboxes;

    private boolean wasClosed = false;

    public JsonModsChooser(InstanceInstaller installerr) {
        super(App.settings.getParent(), Language.INSTANCE.localize("instance.selectmods"),
                ModalityType.APPLICATION_MODAL);
        this.installer = installerr;
        setSize(550, 450);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
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

        JLabel topLabelLeft = new JLabel(Language.INSTANCE.localize("instance.requiredmods"));
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setLeftComponent(topLabelLeft);

        JLabel topLabelRight = new JLabel(Language.INSTANCE.localize("instance.optionalmods"));
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
        checkBoxPanel1.setBackground(App.THEME.getModSelectionBackgroundColor());

        JScrollPane scroller1 = new JScrollPane(checkBoxPanel1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        JPanel checkBoxPanel2 = new JPanel();
        checkBoxPanel2.setLayout(null);
        checkBoxPanel2.setBackground(App.THEME.getModSelectionBackgroundColor());

        JScrollPane scroller2 = new JScrollPane(checkBoxPanel2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        selectAllButton = new JButton();

        if (installer.hasJsonRecommendedMods()) {
            selectAllButton.setText(Language.INSTANCE.localize("instance.selectrecommended"));
        } else {
            selectAllButton.setText(Language.INSTANCE.localize("instance.selectall"));
        }

        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if ((installer.isServer() ? check.getJsonMod().isServerOptional() : check.getJsonMod().isOptional
                            ())) {
                        if (check.getJsonMod().isRecommended()) {
                            if (check.getJsonMod().hasGroup()) {
                                if (check.getJsonMod().isRecommended() && installer.isOnlyRecommendedInGroup(check
                                        .getJsonMod())) {
                                    check.setSelected(true);
                                    check.setEnabled(true);
                                    sortOutMods(check);
                                } else if (installer.hasJsonRecommendedMods()) {
                                    check.setSelected(false);
                                }
                            } else {
                                check.setSelected(true);
                                check.setEnabled(true);
                                sortOutMods(check);
                            }
                        } else {
                            check.setSelected(false);
                        }
                    }
                }
            }
        });
        bottomPanel.add(selectAllButton);

        clearAllButton = new JButton(Language.INSTANCE.localize("instance.clearall"));
        clearAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.isCategory()) {
                        continue;
                    }
                    if ((installer.isServer() ? check.getJsonMod().isServerOptional() : check.getJsonMod().isOptional
                            ())) {
                        check.setSelected(false);
                        List<Mod> linkedMods = modsToChange(check.getJsonMod());
                        for (Mod mod : linkedMods) {
                            for (ModsJCheckBox check1 : modCheckboxes) {
                                if (check1.getJsonMod() == mod) {
                                    check1.setEnabled(false);
                                }
                            }
                        }
                    }
                }
            }
        });
        bottomPanel.add(clearAllButton);

        JButton installButton = new JButton(Language.INSTANCE.localize("common.install"));
        installButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(installButton);

        modCheckboxes = new ArrayList<ModsJCheckBox>();
        int count1 = 0;
        int count2 = 0;

        for (int i = 0; i < installer.getJsonMods().size(); ) {
            boolean skip = false;
            final Mod mod = installer.getJsonMods().get(i);
            if (installer.isServer() && !mod.installOnServer()) {
                continue;
            }
            ModsJCheckBox checkBox = null;
            if ((installer.isServer() ? mod.isServerOptional() : mod.isOptional())) {
                if (!skip) {
                    checkBox = new ModsJCheckBox(mod);
                    checkBox.setEnabled(true);
                    if (!mod.hasLinked()) {
                        checkBox.setBounds(0, (count1 * 20), checkBox.getPreferredSize().width, 20);
                    } else {
                        Mod linkedMod = installer.getJsonModByName(mod.getLinked());
                        if (linkedMod == null) {
                            LogManager.error("The mod " + mod.getName() + " tried to reference a linked mod " + mod
                                    .getLinked() + " which doesn't exist!");
                            installer.cancel(true);
                            return;
                        }
                        if ((installer.isServer() ? linkedMod.isServerOptional() : linkedMod.isOptional())) {
                            checkBox.setEnabled(false);
                            checkBox.setBounds(20, (count1 * 20), checkBox.getPreferredSize().width, 20);
                        } else {
                            checkBox.setBounds(0, (count1 * 20), checkBox.getPreferredSize().width, 20);
                        }
                        if (mod.isSelected()) {
                            checkBox.setEnabled(true);
                            checkBox.setSelected(true);
                            if (!linkedMod.isSelected()) {
                                boolean needToEnableChildren = false;
                                for (ModsJCheckBox checkbox : modCheckboxes) {
                                    if (checkbox.getJsonMod().getName().equalsIgnoreCase(mod.getLinked())) {
                                        checkbox.setSelected(true); // Select the checkbox
                                        needToEnableChildren = true;
                                        break;
                                    }
                                }
                                if (needToEnableChildren) {
                                    for (ModsJCheckBox checkbox : modCheckboxes) {
                                        if (checkbox.getJsonMod().getLinked().equalsIgnoreCase(mod.getLinked())) {
                                            checkbox.setEnabled(true);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (linkedMod.isSelected()) {
                                checkBox.setEnabled(true);
                            }
                        }
                    }
                    if (mod.isHidden() || mod.isLibrary()) {
                        checkBox.setVisible(false);
                    } else {
                        count1++;
                    }
                }

                if (mod.hasWarning()) {
                    final ModsJCheckBox finalCheckBox = checkBox;
                    checkBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (finalCheckBox.isSelected() && installer.getJsonVersion().hasWarningMessage(mod
                                    .getWarning())) {
                                String message = installer.getJsonVersion().getWarningMessage(mod.getWarning());

                                if (message != null) {
                                    String[] options = {App.settings.getLocalizedString("common.yes"),
                                            App.settings.getLocalizedString("common.no")};
                                    int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                                            "<html>" + message + "<br/>" +
                                                    Language.INSTANCE.localize("instance.warningsure") + "</html>",
                                            Language.INSTANCE.localize("instance.warning"),
                                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
                                            options[1]);
                                    if (ret != 0) {
                                        finalCheckBox.setSelected(false);
                                    }
                                }
                            }
                        }
                    });
                }
            } else {
                checkBox = new ModsJCheckBox(mod);
                checkBox.setBounds(0, (count2 * 20), checkBox.getPreferredSize().width, 20);
                checkBox.setSelected(true);
                checkBox.setEnabled(false);

                if (mod.isHidden() || mod.isLibrary()) {
                    checkBox.setVisible(false);
                } else {
                    count2++;
                }
            }
            if (!checkBox.isCategory()) {
                if (installer.isReinstall()) {
                    if (installer.wasModInstalled(mod.getName())) {
                        if ((installer.isServer() ? mod.isServerOptional() : mod.isOptional())) {
                            checkBox.setSelected(true);
                            checkBox.setEnabled(true);
                        }
                    }
                } else {
                    if ((installer.isServer() ? mod.isServerOptional() : mod.isOptional()) && mod.isSelected()) {
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
            }
            modCheckboxes.add(checkBox);
            if (!skip) {
                i++;
            }
        }
        for (int i = 0; i < modCheckboxes.size(); i++) {
            ModsJCheckBox checkBox = modCheckboxes.get(i);
            if (checkBox.isCategory()) {
                checkBoxPanel1.add(checkBox);
            } else if ((installer.isServer() ? checkBox.getJsonMod().isServerOptional() : checkBox.getJsonMod()
                    .isOptional())) {
                checkBoxPanel1.add(checkBox);
            } else {
                checkBoxPanel2.add(checkBox);
            }
        }
        checkBoxPanel1.setPreferredSize(new Dimension(0, count1 * 20));
        checkBoxPanel2.setPreferredSize(new Dimension(0, count2 * 20));
    }

    private List<Mod> modsToChange(Mod mod) {
        return installer.getJsonLinkedMods(mod);
    }

    private List<Mod> modsInGroup(Mod mod) {
        return installer.getGroupedMods(mod);
    }

    private List<Mod> modsDependancies(Mod mod) {
        return installer.getModsDependancies(mod);
    }

    private List<Mod> dependedMods(Mod mod) {
        return installer.dependedMods(mod);
    }

    private boolean hasADependancy(Mod mod) {
        return installer.hasADependancy(mod);
    }

    public void sortOutMods(ModsJCheckBox a) {
        if (a.isSelected()) {
            List<Mod> linkedMods = modsToChange(a.getJsonMod());
            for (Mod mod : linkedMods) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.getJsonMod() == mod) {
                        check.setEnabled(true);
                    }
                }
            }
            if (a.getJsonMod().hasGroup()) {
                List<Mod> groupMods = modsInGroup(a.getJsonMod());
                for (Mod mod : groupMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getJsonMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            }
            if (a.getJsonMod().hasDepends()) {
                List<Mod> dependsMods = modsDependancies(a.getJsonMod());
                for (Mod mod : dependsMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getJsonMod() == mod) {
                            check.setSelected(true);
                        }
                    }
                }
            }
        } else {
            List<Mod> linkedMods = modsToChange(a.getJsonMod());
            for (Mod mod : linkedMods) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.getJsonMod() == mod) {
                        check.setEnabled(false);
                        check.setSelected(false);
                    }
                }
            }
            if (hasADependancy(a.getJsonMod())) {
                List<Mod> dependedMods = dependedMods(a.getJsonMod());
                for (Mod mod : dependedMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getJsonMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            } else if (a.getJsonMod().hasDepends()) {
                List<Mod> dependsMods = modsDependancies(a.getJsonMod());
                for (Mod mod : dependsMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getJsonMod() == mod) {
                            if (check.getJsonMod().isLibrary()) {
                                check.setSelected(false);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Mod> getSelectedMods() {
        if (wasClosed) {
            return null;
        }
        List<Mod> mods = new ArrayList<Mod>();
        for (ModsJCheckBox check : modCheckboxes) {
            if (check.isSelected()) {
                mods.add(check.getJsonMod());
            }
        }
        return mods;
    }

    public boolean wasClosed() {
        return this.wasClosed;
    }

}
