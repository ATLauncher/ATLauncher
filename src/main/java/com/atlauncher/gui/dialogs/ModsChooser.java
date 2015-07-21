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
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Language;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.components.ModsJCheckBox;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.reflect.TypeToken;
import io.github.asyncronous.toast.Toaster;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModsChooser extends JDialog {
    private static final long serialVersionUID = -5309108183485463434L;
    private InstanceInstaller installer;
    private JButton useShareCode;
    private JButton selectAllButton;
    private JButton clearAllButton;
    private JButton installButton;
    private List<ModsJCheckBox> modCheckboxes;
    private List<ModsJCheckBox> sortedOut;

    private boolean wasClosed = false;

    public ModsChooser(InstanceInstaller installerr) {
        super(App.settings.getParent(), Language.INSTANCE.localize("instance.selectmods"), ModalityType
                .APPLICATION_MODAL);
        this.installer = installerr;
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

        JScrollPane scroller1 = new JScrollPane(checkBoxPanel1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        modsInPack.setRightComponent(scroller1);

        JPanel checkBoxPanel2 = new JPanel();
        checkBoxPanel2.setLayout(null);
        checkBoxPanel2.setBackground(App.THEME.getModSelectionBackgroundColor());

        JScrollPane scroller2 = new JScrollPane(checkBoxPanel2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        modsInPack.setLeftComponent(scroller2);

        JPanel bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        useShareCode = new JButton();
        useShareCode.setText(Language.INSTANCE.localize("instance.usesharecode"));
        useShareCode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ret = JOptionPane.showInputDialog(null, Language.INSTANCE.localize("instance.entersharecode"),
                        Language.INSTANCE.localize("instance.sharecode"), JOptionPane.QUESTION_MESSAGE);

                if (ret != null) {
                    applyShareCode(ret);
                }
            }
        });
        bottomPanel.add(useShareCode);

        selectAllButton = new JButton();

        if (installer.hasRecommendedMods()) {
            selectAllButton.setText(Language.INSTANCE.localize("instance.selectrecommended"));
        } else {
            selectAllButton.setText(Language.INSTANCE.localize("instance.selectall"));
        }

        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if ((installer.isServer() ? check.getMod().isServerOptional() : check.getMod().isOptional())) {
                        if (check.getMod().isRecommended()) {
                            if (check.getMod().hasGroup()) {
                                if (check.getMod().isRecommended() && installer.isOnlyRecommendedInGroup(check.getMod
                                        ())) {
                                    check.setSelected(true);
                                    check.setEnabled(true);
                                    sortOutMods(check);
                                } else if (installer.hasRecommendedMods()) {
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
                    if ((installer.isServer() ? check.getMod().isServerOptional() : check.getMod().isOptional())) {
                        check.setSelected(false);
                        List<Mod> linkedMods = modsToChange(check.getMod());
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

        installButton = new JButton(Language.INSTANCE.localize("common.install"));
        installButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        bottomPanel.add(installButton);

        modCheckboxes = new ArrayList<ModsJCheckBox>();
        int count1 = 0;
        int count2 = 0;

        for (int i = 0; i < installer.getMods().size(); ) {
            boolean skip = false;
            final Mod mod = installer.getMods().get(i);
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
                        Mod linkedMod = installer.getModByName(mod.getLinked());
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
                                    if (checkbox.getMod().getName().equalsIgnoreCase(mod.getLinked())) {
                                        checkbox.setSelected(true); // Select the checkbox
                                        needToEnableChildren = true;
                                        break;
                                    }
                                }
                                if (needToEnableChildren) {
                                    for (ModsJCheckBox checkbox : modCheckboxes) {
                                        if (checkbox.getMod().getLinked().equalsIgnoreCase(mod.getLinked())) {
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
                                    String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE
                                            .localize("common.no")};
                                    int ret = JOptionPane.showOptionDialog(App.settings.getParent(), "<html>" +
                                                    message + "<br/>" +
                                                    Language.INSTANCE.localize("instance.warningsure") + "</html>",
                                            Language.INSTANCE.localize("instance.warning"), JOptionPane
                                                    .DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options,
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
                    sortOutMods(a, true);
                }
            });
            modCheckboxes.add(checkBox);
            if (!skip) {
                i++;
            }
        }
        for (int i = 0; i < modCheckboxes.size(); i++) {
            ModsJCheckBox checkBox = modCheckboxes.get(i);
            if ((installer.isServer() ? checkBox.getMod().isServerOptional() : checkBox.getMod().isOptional())) {
                checkBoxPanel1.add(checkBox);
            } else {
                checkBoxPanel2.add(checkBox);
            }
        }

        sortedOut = new ArrayList<ModsJCheckBox>();
        for (ModsJCheckBox cb : this.modCheckboxes) {
            if (cb.getMod().isOptional() && cb.isSelected()) {
                sortOutMods(cb);
            }
        }

        checkBoxPanel1.setPreferredSize(new Dimension(0, count1 * 20));
        checkBoxPanel2.setPreferredSize(new Dimension(0, count2 * 20));

        setSize(calculateWidth(), 450);
        scroller1.setPreferredSize(new Dimension(calculateWidth() / 2, 350));
        scroller2.setPreferredSize(new Dimension(calculateWidth() / 2, 350));
    }

    private int calculateWidth() {
        int width = 30;

        width += useShareCode.getPreferredSize().width;
        width += selectAllButton.getPreferredSize().width;
        width += clearAllButton.getPreferredSize().width;
        width += installButton.getPreferredSize().width;

        if (width < 550) {
            width = 550;
        }

        return width;
    }

    public void applyShareCode(String code) {
        try {
            String data = installer.getShareCodeData(code);

            if (data == null) {
                Toaster.instance().popError(Language.INSTANCE.localize("instance.invalidsharecode"));
                return;
            }

            java.lang.reflect.Type type = new TypeToken<Map<String, List<Map<String, String>>>>() {
            }.getType();

            Map<String, List<Map<String, String>>> mods = Gsons.DEFAULT.fromJson(data, type);

            if (mods == null) {
                Toaster.instance().popError(Language.INSTANCE.localize("instance.invalidsharecode"));
                return;
            }

            List<Map<String, String>> optionalMods = mods.get("optional");

            if (optionalMods == null || optionalMods.size() == 0) {
                Toaster.instance().popError(Language.INSTANCE.localize("instance.invalidsharecode"));
                return;
            }

            for (ModsJCheckBox checkbox : this.modCheckboxes) {
                if (!checkbox.getMod().isOptional()) {
                    continue;
                }

                boolean found = false;

                for (Map<String, String> mod : optionalMods) {
                    if (mod.get("name").equalsIgnoreCase(checkbox.getMod().getName())) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    checkbox.setSelected(true);
                }
            }
        } catch (Exception e) {
            LogManager.error("Invalid share code!");
            Toaster.instance().popError(Language.INSTANCE.localize("instance.invalidsharecode"));
        }
    }

    private List<Mod> modsToChange(Mod mod) {
        return installer.getLinkedMods(mod);
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
        this.sortOutMods(a, false);
    }

    public void sortOutMods(ModsJCheckBox a, boolean firstGo) {
        if (firstGo) {
            sortedOut = new ArrayList<ModsJCheckBox>();
        }

        if (a.isSelected()) {
            List<Mod> linkedMods = modsToChange(a.getMod());
            for (Mod mod : linkedMods) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.getMod() == mod) {
                        check.setEnabled(true);
                    }
                }
            }
            if (a.getMod().hasGroup()) {
                List<Mod> groupMods = modsInGroup(a.getMod());
                for (Mod mod : groupMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            }
            if (a.getMod().hasDepends()) {
                List<Mod> dependsMods = modsDependancies(a.getMod());
                for (Mod mod : dependsMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod && !sortedOut.contains(check)) {
                            sortedOut.add(check);
                            check.setSelected(true);
                            sortOutMods(check);
                        }
                    }
                }
            }
        } else {
            List<Mod> linkedMods = modsToChange(a.getMod());
            for (Mod mod : linkedMods) {
                for (ModsJCheckBox check : modCheckboxes) {
                    if (check.getMod() == mod) {
                        check.setEnabled(false);
                        check.setSelected(false);
                    }
                }
            }
            if (hasADependancy(a.getMod())) {
                List<Mod> dependedMods = dependedMods(a.getMod());
                for (Mod mod : dependedMods) {
                    for (ModsJCheckBox check : modCheckboxes) {
                        if (check.getMod() == mod) {
                            check.setSelected(false);
                        }
                    }
                }
            } else if (a.getMod().hasDepends()) {
                List<Mod> dependsMods = modsDependancies(a.getMod());
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

    public List<Mod> getSelectedMods() {
        if (wasClosed) {
            return null;
        }
        List<Mod> mods = new ArrayList<Mod>();
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
