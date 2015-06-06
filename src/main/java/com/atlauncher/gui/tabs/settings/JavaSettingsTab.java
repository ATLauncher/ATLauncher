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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab {
    private final String[] MEMORY_OPTIONS = Utils.getMemoryOptions();
    private JLabelWithHover initialMemoryLabel;
    private JComboBox<String> initialMemory;
    private JLabelWithHover initialMemoryLabelWarning;
    private JPanel initialMemoryPanel;
    private JLabelWithHover maximumMemoryLabel;
    private JComboBox<String> maximumMemory;
    private JLabelWithHover maximumMemoryLabelWarning;
    private JPanel maximumMemoryPanel;
    private JLabelWithHover permGenLabel;
    private JTextField permGen;
    private JPanel windowSizePanel;
    private JLabelWithHover windowSizeLabel;
    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> commonScreenSizes;
    private JPanel javaPathPanel;
    private JLabelWithHover javaPathLabel;
    private JTextField javaPath;
    private JButton javaPathResetButton;
    private JPanel javaParametersPanel;
    private JLabelWithHover javaParametersLabel;
    private JTextField javaParameters;
    private JButton javaParametersResetButton;
    private JLabelWithHover startMinecraftMaximisedLabel;
    private JCheckBox startMinecraftMaximised;
    private JLabelWithHover saveCustomModsLabel;
    private JCheckBox saveCustomMods;

    public JavaSettingsTab() {
        EventHandler.EVENT_BUS.subscribe(this);
        // Initial Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON, "<html>" + Utils.splitMultilinedString
                (LanguageManager.localize("settings.32bitmemorywarning"), 80, "<br/>") + "</html>", RESTART_BORDER);

        initialMemoryLabel = new JLabelWithHover(LanguageManager.localize("settings.initialmemory") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(LanguageManager.localize("settings" + "" +
                ".initialmemoryhelp"), 80, "<br/>") + "</html>");

        initialMemoryPanel = new JPanel();
        initialMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!Utils.is64Bit()) {
            initialMemoryPanel.add(initialMemoryLabelWarning);
        }
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        initialMemory = new JComboBox<String>();
        initialMemory.addItem("64 MB");
        initialMemory.addItem("128 MB");
        initialMemory.addItem("256 MB");
        for (String option : MEMORY_OPTIONS) {
            initialMemory.addItem(option);
        }
        initialMemory.setSelectedItem(SettingsManager.getInitialMemory() + " MB");
        initialMemory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedRam = Integer.parseInt(((String) initialMemory.getSelectedItem()).replace(" MB", ""));
                    int maxRam = Integer.parseInt(((String) maximumMemory.getSelectedItem()).replace(" MB", ""));
                    if (selectedRam > maxRam) {
                        JOptionPane.showMessageDialog(App.frame, "<html>" + LanguageManager
                                .localizeWithReplace("settings.initialmemorytoohigh", "<br/><br/>") + "</html>",
                                LanguageManager.localize("settings.help"), JOptionPane.PLAIN_MESSAGE);
                        initialMemory.setSelectedItem("256 MB");
                    }
                }
            }
        });
        add(initialMemory, gbc);

        // Maximum Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        maximumMemoryLabelWarning = new JLabelWithHover(WARNING_ICON, "<html>" + Utils.splitMultilinedString
                (LanguageManager.localize("settings.32bitmemorywarning"), 80, "<br/>") + "</html>", RESTART_BORDER);

        maximumMemoryLabel = new JLabelWithHover(LanguageManager.localize("settings.maximummemory") + ":", HELP_ICON,
                "<html>" + Utils.splitMultilinedString(LanguageManager.localize("settings" + "" +
                ".maximummemoryhelp"), 80, "<br/>") + "</html>");

        maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        if (!Utils.is64Bit()) {
            maximumMemoryPanel.add(maximumMemoryLabelWarning);
        }
        maximumMemoryPanel.add(maximumMemoryLabel);

        add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        maximumMemory = new JComboBox<String>();
        for (String option : MEMORY_OPTIONS) {
            maximumMemory.addItem(option);
        }
        maximumMemory.setSelectedItem(SettingsManager.getMaximumMemory() + " MB");
        maximumMemory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedRam = Integer.parseInt(((String) maximumMemory.getSelectedItem()).replace(" MB", ""));
                    if (selectedRam > 4096) {
                        JOptionPane.showMessageDialog(App.frame, "<html>" + LanguageManager
                                .localizeWithReplace("settings.toomuchramallocated", "<br/><br/>") + "</html>",
                                LanguageManager.localize("settings.help"), JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        add(maximumMemory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        permGenLabel = new JLabelWithHover(LanguageManager.localize("settings.permgen") + ":", HELP_ICON, LanguageManager.localize("settings.permgenhelp"));
        add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        permGen = new JTextField(4);
        permGen.setText(SettingsManager.getPermGen() + "");
        add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        windowSizeLabel = new JLabelWithHover(LanguageManager.localize("settings.windowsize") + ":", HELP_ICON,
                LanguageManager.localize("settings.windowsizehelp"));
        add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        widthField = new JTextField(4);
        widthField.setText(SettingsManager.getWindowWidth() + "");
        heightField = new JTextField(4);
        heightField.setText(SettingsManager.getWindowHeight() + "");
        commonScreenSizes = new JComboBox<String>();
        commonScreenSizes.addItem("Select An Option");
        commonScreenSizes.addItem("854x480");
        if (Utils.getMaximumWindowWidth() >= 1280 && Utils.getMaximumWindowHeight() >= 720) {
            commonScreenSizes.addItem("1280x720");
        }
        if (Utils.getMaximumWindowWidth() >= 1600 && Utils.getMaximumWindowHeight() >= 900) {
            commonScreenSizes.addItem("1600x900");
        }
        if (Utils.getMaximumWindowWidth() >= 1920 && Utils.getMaximumWindowHeight() >= 1080) {
            commonScreenSizes.addItem("1920x1080");
        }
        commonScreenSizes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) commonScreenSizes.getSelectedItem();
                if (selected.contains("x")) {
                    String[] parts = selected.split("x");
                    widthField.setText(parts[0]);
                    heightField.setText(parts[1]);
                }
            }
        });
        commonScreenSizes.setPreferredSize(new Dimension(commonScreenSizes.getPreferredSize().width + 10,
                commonScreenSizes.getPreferredSize().height));
        windowSizePanel.add(widthField);
        windowSizePanel.add(new JLabel("x"));
        windowSizePanel.add(heightField);
        windowSizePanel.add(commonScreenSizes);
        add(windowSizePanel, gbc);

        // Java Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathLabel = new JLabelWithHover(LanguageManager.localize("settings.javapath") + ":", HELP_ICON, "<html>"
                + LanguageManager.localizeWithReplace("settings.javapathhelp", "<br/>") + "</html>");
        add(javaPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathPanel = new JPanel();
        javaPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        javaPath = new JTextField(20);
        javaPath.setText(SettingsManager.getJavaPath());
        javaPathResetButton = new JButton(LanguageManager.localize("settings.javapathreset"));
        javaPathResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javaPath.setText(Utils.getJavaHome());
            }
        });
        javaPathPanel.add(javaPath);
        javaPathPanel.add(javaPathResetButton);
        add(javaPathPanel, gbc);

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersLabel = new JLabelWithHover(LanguageManager.localize("settings.javaparameters") + ":",
                HELP_ICON, LanguageManager.localize("settings.javaparametershelp"));
        add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        javaParameters = new JTextField(20);
        javaParameters.setText(SettingsManager.getJavaParameters());
        javaParametersResetButton = new JButton(LanguageManager.localize("settings.javapathreset"));
        javaParametersResetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                javaParameters.setText("");
            }
        });
        javaParametersPanel.add(javaParameters);
        javaParametersPanel.add(javaParametersResetButton);
        add(javaParametersPanel, gbc);

        // Start Minecraft Maximised

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        startMinecraftMaximisedLabel = new JLabelWithHover(LanguageManager.localize("settings" + "" +
                ".startminecraftmaximised") + "?", HELP_ICON, LanguageManager.localize("settings" + "" +
                ".startminecraftmaximisedhelp"));
        add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        startMinecraftMaximised = new JCheckBox();
        if (SettingsManager.startMinecraftMaximised()) {
            startMinecraftMaximised.setSelected(true);
        }
        add(startMinecraftMaximised, gbc);

        // Save Custom Mods

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        saveCustomModsLabel = new JLabelWithHover(LanguageManager.localize("settings.savecustommods") + "?",
                HELP_ICON, LanguageManager.localize("settings.savecustommodshelp"));
        add(saveCustomModsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        saveCustomMods = new JCheckBox();
        if (SettingsManager.saveCustomMods()) {
            saveCustomMods.setSelected(true);
        }
        add(saveCustomMods, gbc);
    }

    public boolean isValidJavaPath() {
        File jPath = new File(javaPath.getText(), "bin");
        if (!jPath.exists()) {
            JOptionPane.showMessageDialog(App.frame, "<html>" + LanguageManager.localizeWithReplace
                    ("settings.javapathincorrect", "<br/><br/>") + "</html>", LanguageManager.localize("settings" +
                    ".help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isValidJavaParamaters() {
        if (javaParameters.getText().contains("-Xms") || javaParameters.getText().contains("-Xmx") || javaParameters
                .getText().contains("-XX:PermSize") || javaParameters.getText().contains("-XX:MetaspaceSize")) {
            JOptionPane.showMessageDialog(App.frame, "<html>" + LanguageManager.localizeWithReplace
                    ("settings.javaparametersincorrect", "<br/><br/>") + "</html>", LanguageManager.localize
                    ("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public void save() {
        SettingsManager.setInitialMemory(Integer.parseInt(((String) initialMemory.getSelectedItem()).replace(" MB", "")));
        SettingsManager.setMaximumMemory(Integer.parseInt(((String) maximumMemory.getSelectedItem()).replace(" MB", "")));
        SettingsManager.setPermGen(Integer.parseInt(permGen.getText().replaceAll("[^0-9]", "")));
        SettingsManager.setWindowWidth(Integer.parseInt(widthField.getText().replaceAll("[^0-9]", "")));
        SettingsManager.setWindowHeight(Integer.parseInt(heightField.getText().replaceAll("[^0-9]", "")));
        SettingsManager.setJavaPath(javaPath.getText());
        SettingsManager.setJavaParameters(javaParameters.getText());
        SettingsManager.setStartMinecraftMaximised(startMinecraftMaximised.isSelected());
        SettingsManager.setSaveCustomMods(saveCustomMods.isSelected());
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("settings.javatab");
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.initialMemoryLabelWarning.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager
                .localize("settings.32bitmemorywarning"), 80, "<br/>") + "</html>");

        this.initialMemoryLabel.setText(LanguageManager.localize("settings.initialmemory") + ":");
        this.initialMemoryLabel.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager.localize
                ("settings" + ".initialmemoryhelp"), 80, "<br/>") + "</html>");

        this.maximumMemoryLabelWarning.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager
                .localize("settings.32bitmemorywarning"), 80, "<br/>") + "</html>");

        this.maximumMemoryLabel.setText(LanguageManager.localize("settings.maximummemory") + ":");
        this.maximumMemoryLabel.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager.localize
                ("settings" + "" +
                ".maximummemoryhelp"), 80, "<br/>") + "</html>");


        this.permGenLabel.setText(LanguageManager.localize("settings.permgen") + ":");
        this.permGenLabel.setToolTipText(LanguageManager.localize("settings.permgenhelp"));

        this.windowSizeLabel.setText(LanguageManager.localize("settings.windowsize") + ":");
        this.windowSizeLabel.setToolTipText(LanguageManager.localize("settings.windowsizehelp"));

        this.javaPathLabel.setText(LanguageManager.localize("settings.javapath") + ":");
        this.javaPathLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings.javapathhelp",
                "<br/>") + "</html>");

        this.javaPathResetButton.setText(LanguageManager.localize("settings.javapathreset"));

        this.javaParametersLabel.setText(LanguageManager.localize("settings.javaparameters") + ":");
        this.javaParametersLabel.setToolTipText(LanguageManager.localize("settings.javaparametershelp"));

        this.javaParametersResetButton.setText(LanguageManager.localize("settings.javapathreset"));

        this.startMinecraftMaximisedLabel.setText(LanguageManager.localize("settings" + "" +
                ".startminecraftmaximised") + "?");
        this.startMinecraftMaximisedLabel.setToolTipText(LanguageManager.localize("settings" + "" +
                ".startminecraftmaximisedhelp"));

        this.saveCustomModsLabel.setText(LanguageManager.localize("settings.savecustommods") + "?");
        this.saveCustomModsLabel.setToolTipText(LanguageManager.localize("settings.savecustommodshelp"));
    }
}
