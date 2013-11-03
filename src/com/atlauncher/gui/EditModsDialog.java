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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.utils.Utils;

public class EditModsDialog extends JDialog {

    private Instance instance; // The instance this is for

    private JPanel bottomPanel, disabledModsPanel, enabledModsPanel;
    private JSplitPane split, labelsTop, labels, modsInPack;
    private JScrollPane scroller1, scroller2;
    private JButton enableButton, disableButton, closeButton;
    private JLabel topLabelLeft, topLabelRight;
    private ArrayList<ModsJCheckBox> enabledMods, disabledMods;

    public EditModsDialog(Instance instance) {
        super(App.settings.getParent(), App.settings.getLocalizedString("instance.editingmods",
                instance.getName()), ModalityType.APPLICATION_MODAL);
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

        topLabelLeft = new JLabel(App.settings.getLocalizedString("instance.enabledmods"));
        topLabelLeft.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setLeftComponent(topLabelLeft);

        topLabelRight = new JLabel(App.settings.getLocalizedString("instance.disabledmods"));
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
        disabledModsPanel.setBackground(new Color(50, 55, 60));

        scroller1 = new JScrollPane(disabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller1.getVerticalScrollBar().setUnitIncrement(16);
        scroller1.setPreferredSize(new Dimension(275, 350));
        modsInPack.setRightComponent(scroller1);

        enabledModsPanel = new JPanel();
        enabledModsPanel.setLayout(null);
        enabledModsPanel.setBackground(new Color(50, 55, 60));

        scroller2 = new JScrollPane(enabledModsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller2.getVerticalScrollBar().setUnitIncrement(16);
        scroller2.setPreferredSize(new Dimension(275, 350));
        modsInPack.setLeftComponent(scroller2);

        bottomPanel = new JPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        enableButton = new JButton(App.settings.getLocalizedString("instance.enablemod"));
        enableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableMods();
            }
        });
        bottomPanel.add(enableButton);

        disableButton = new JButton(App.settings.getLocalizedString("instance.disablemod"));
        disableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disableMods();
            }
        });
        bottomPanel.add(disableButton);

        closeButton = new JButton(App.settings.getLocalizedString("common.close"));
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
        ArrayList<DisableableMod> mods = instance.getInstalledMods();
        enabledMods = new ArrayList<ModsJCheckBox>();
        disabledMods = new ArrayList<ModsJCheckBox>();
        int count = 0;
        int dCount = 0;
        int eCount = 0;
        for (DisableableMod mod : mods) {
            ModsJCheckBox checkBox = null;
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(mod.getName());

            checkBox = new ModsJCheckBox(mod);
            if (mod.isDisabled()) {
                checkBox.setBounds(0, (dCount++ * 20), nameSize + 23, 20);
                disabledMods.add(checkBox);
            } else {
                checkBox.setBounds(0, (eCount++ * 20), nameSize + 23, 20);
                enabledMods.add(checkBox);
            }
        }
        for (int i = 0; i < enabledMods.size(); i++) {
            JCheckBox checkBox = enabledMods.get(i);
            enabledModsPanel.add(checkBox);
        }
        for (int i = 0; i < disabledMods.size(); i++) {
            JCheckBox checkBox = disabledMods.get(i);
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
                disabledMods.remove(mod);
                enabledMods.add(mod);
            }
        }
        App.settings.saveInstances();
        reloadPanels();
    }

    private void disableMods() {
        ArrayList<ModsJCheckBox> mods = new ArrayList<ModsJCheckBox>(enabledMods);
        for (ModsJCheckBox mod : mods) {
            if (mod.isSelected()) {
                mod.getDisableableMod().disable(instance);
                enabledMods.remove(mod);
                disabledMods.add(mod);
            }
        }
        App.settings.saveInstances();
        reloadPanels();
    }

    private void reloadPanels() {
        int count = 0;
        enabledModsPanel.removeAll();
        for (int i = 0; i < enabledMods.size(); i++) {
            ModsJCheckBox checkBox = enabledMods.get(i);
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(
                    checkBox.getDisableableMod().getName());
            checkBox.setBounds(0, (count++ * 20), nameSize + 23, 20);
            if (checkBox.isSelected()) {
                checkBox.setSelected(false);
            }
            enabledModsPanel.add(checkBox);
        }

        count = 0;
        disabledModsPanel.removeAll();
        for (int i = 0; i < disabledMods.size(); i++) {
            ModsJCheckBox checkBox = disabledMods.get(i);
            int nameSize = getFontMetrics(Utils.getFont()).stringWidth(
                    checkBox.getDisableableMod().getName());
            checkBox.setBounds(0, (count++ * 20), nameSize + 23, 20);
            if (checkBox.isSelected()) {
                checkBox.setSelected(false);
            }
            disabledModsPanel.add(checkBox);
        }

        enabledModsPanel.setPreferredSize(new Dimension(0, enabledMods.size() * 20));
        disabledModsPanel.setPreferredSize(new Dimension(0, disabledMods.size() * 20));
        enabledModsPanel.repaint();
        disabledModsPanel.repaint();
    }

}
