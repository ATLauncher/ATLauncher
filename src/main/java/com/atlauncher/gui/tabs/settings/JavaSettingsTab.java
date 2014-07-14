/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs.settings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab {
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

    private final String[] MEMORY_OPTIONS = Utils.getMemoryOptions();

    public JavaSettingsTab() {
        // Initial Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        initialMemoryLabelWarning = new JLabelWithHover(WARNING_ICON,
                App.settings.getLocalizedString("settings.32bitmemorywarning"), RESTART_BORDER);

        initialMemoryLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.initialmemory") + ":", HELP_ICON,
                App.settings.getLocalizedString("settings.initialmemoryhelp"));

        initialMemoryPanel = new JPanel();
        initialMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        initialMemoryPanel.add(initialMemoryLabelWarning);
        initialMemoryPanel.add(initialMemoryLabel);

        add(initialMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        initialMemory = new JComboBox<String>();
        for (String option : MEMORY_OPTIONS) {
            initialMemory.addItem(option);
        }
        initialMemory.setSelectedItem(App.settings.getInitialMemory() + " MB");
        initialMemory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedRam = Integer.parseInt(((String) initialMemory.getSelectedItem())
                            .replace(" MB", ""));
                }
            }
        });
        add(initialMemory, gbc);

        // Maximum Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

        maximumMemoryLabelWarning = new JLabelWithHover(WARNING_ICON,
                App.settings.getLocalizedString("settings.32bitmemorywarning"), RESTART_BORDER);

        maximumMemoryLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.maximummemory") + ":", HELP_ICON,
                App.settings.getLocalizedString("settings.maximummemoryhelp"));

        maximumMemoryPanel = new JPanel();
        maximumMemoryPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        maximumMemoryPanel.add(maximumMemoryLabelWarning);
        maximumMemoryPanel.add(maximumMemoryLabel);

        add(maximumMemoryPanel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        maximumMemory = new JComboBox<String>();
        for (String option : MEMORY_OPTIONS) {
            maximumMemory.addItem(option);
        }
        maximumMemory.setSelectedItem(App.settings.getMaximumMemory() + " MB");
        maximumMemory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedRam = Integer.parseInt(((String) maximumMemory.getSelectedItem())
                            .replace(" MB", ""));
                    if (selectedRam > 4096) {
                        JOptionPane.showMessageDialog(
                                App.settings.getParent(),
                                "<html>"
                                        + App.settings.getLocalizedString(
                                                "settings.toomuchramallocated", "<br/><br/>")
                                        + "</html>", App.settings
                                        .getLocalizedString("settings.help"),
                                JOptionPane.PLAIN_MESSAGE);
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
        permGenLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.permgen")
                + ":", HELP_ICON, App.settings.getLocalizedString("settings.permgenhelp"));
        add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        permGen = new JTextField(4);
        permGen.setText(App.settings.getPermGen() + "");
        add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        windowSizeLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.windowsize") + ":", HELP_ICON,
                App.settings.getLocalizedString("settings.windowsizehelp"));
        add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        widthField = new JTextField(4);
        widthField.setText(App.settings.getWindowWidth() + "");
        heightField = new JTextField(4);
        heightField.setText(App.settings.getWindowHeight() + "");
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
        commonScreenSizes.setPreferredSize(new Dimension(
                commonScreenSizes.getPreferredSize().width + 10, commonScreenSizes
                        .getPreferredSize().height));
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
        javaPathLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.javapath")
                + ":", HELP_ICON, "<html>"
                + App.settings.getLocalizedString("settings.javapathhelp", "<br/>") + "</html>");
        add(javaPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathPanel = new JPanel();
        javaPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        javaPath = new JTextField(20);
        javaPath.setText(App.settings.getJavaPath());
        javaPathResetButton = new JButton(App.settings.getLocalizedString("settings.javapathreset"));
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
        javaParametersLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.javaparameters") + ":", HELP_ICON,
                App.settings.getLocalizedString("settings.javaparametershelp"));
        add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        javaParameters = new JTextField(20);
        javaParameters.setText(App.settings.getJavaParameters());
        javaParametersResetButton = new JButton(
                App.settings.getLocalizedString("settings.javapathreset"));
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
        startMinecraftMaximisedLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.startminecraftmaximised") + "?",
                HELP_ICON, App.settings.getLocalizedString("settings.startminecraftmaximisedhelp"));
        add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        startMinecraftMaximised = new JCheckBox();
        if (App.settings.startMinecraftMaximised()) {
            startMinecraftMaximised.setSelected(true);
        }
        add(startMinecraftMaximised, gbc);

        // Save Custom Mods

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        saveCustomModsLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.savecustommods") + "?", HELP_ICON,
                App.settings.getLocalizedString("settings.savecustommodshelp"));
        add(saveCustomModsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        saveCustomMods = new JCheckBox();
        if (App.settings.saveCustomMods()) {
            saveCustomMods.setSelected(true);
        }
        add(saveCustomMods, gbc);
    }

    public boolean isValidJavaPath() {
        File jPath = new File(javaPath.getText(), "bin");
        if (!jPath.exists()) {
            JOptionPane.showMessageDialog(
                    App.settings.getParent(),
                    "<html>"
                            + App.settings.getLocalizedString("settings.javapathincorrect",
                                    "<br/><br/>") + "</html>",
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean isValidJavaParamaters() {
        if (javaParameters.getText().contains("-Xms") || javaParameters.getText().contains("-Xmx")
                || javaParameters.getText().contains("-XX:PermSize")
                || javaParameters.getText().contains("-XX:MetaspaceSize")) {
            JOptionPane.showMessageDialog(
                    App.settings.getParent(),
                    "<html>"
                            + App.settings.getLocalizedString("settings.javaparametersincorrect",
                                    "<br/><br/>") + "</html>",
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public void save() {
        App.settings.setInitialMemory(Integer.parseInt(((String) maximumMemory.getSelectedItem())
                .replace(" MB", "")));
        App.settings.setMaximumMemory(Integer.parseInt(((String) initialMemory.getSelectedItem())
                .replace(" MB", "")));
        App.settings.setPermGen(Integer.parseInt(permGen.getText().replaceAll("[^0-9]", "")));
        App.settings
                .setWindowWidth(Integer.parseInt(widthField.getText().replaceAll("[^0-9]", "")));
        App.settings.setWindowHeight(Integer.parseInt(heightField.getText()
                .replaceAll("[^0-9]", "")));
        App.settings.setJavaPath(javaPath.getText());
        App.settings.setJavaParameters(javaParameters.getText());
        App.settings.setStartMinecraftMaximised(startMinecraftMaximised.isSelected());
        App.settings.setSaveCustomMods(saveCustomMods.isSelected());
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("settings.javatab");
    }
}
