/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

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
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class JavaSettingsTab extends AbstractSettingsTab {

    private JLabelWithHover memoryLabel;
    private JComboBox<String> memory;

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

    public JavaSettingsTab() {
        // Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        memoryLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.memory") + ":",
                HELP_ICON,
                (Utils.is64Bit() ? App.settings.getLocalizedString("settings.memoryhelp")
                        : "<html><center>"
                                + App.settings.getLocalizedString("settings.memoryhelp32bit",
                                        "<br/>") + "</center></html>"));
        add(memoryLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        memory = new JComboBox<String>();
        String[] memoryOptions = Utils.getMemoryOptions();
        for (int i = 0; i < memoryOptions.length; i++) {
            memory.addItem(memoryOptions[i]);
        }
        memory.setSelectedItem(App.settings.getMemory() + " MB");
        memory.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int selectedRam = Integer.parseInt(((String) memory.getSelectedItem()).replace(
                            " MB", ""));
                    if (selectedRam > 4096) {
                        JOptionPane.showMessageDialog(
                                App.settings.getParent(),
                                "<html><center>"
                                        + App.settings.getLocalizedString(
                                                "settings.toomuchramallocated", "<br/><br/>")
                                        + "</center></html>", App.settings
                                        .getLocalizedString("settings.help"),
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        add(memory, gbc);

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
                + ":", HELP_ICON, "<html><center>"
                + App.settings.getLocalizedString("settings.javapathhelp", "<br/>")
                + "</center></html>");
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
    }

    public boolean isValidJavaPath() {
        File jPath = new File(javaPath.getText(), "bin");
        if (!jPath.exists()) {
            JOptionPane.showMessageDialog(
                    App.settings.getParent(),
                    "<html><center>"
                            + App.settings.getLocalizedString("settings.javapathincorrect",
                                    "<br/><br/>") + "</center></html>",
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
                    "<html><center>"
                            + App.settings.getLocalizedString("settings.javaparametersincorrect",
                                    "<br/><br/>") + "</center></html>",
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public void save() {
        App.settings.setMemory(Integer.parseInt(((String) memory.getSelectedItem()).replace(" MB",
                "")));
        App.settings.setPermGen(Integer.parseInt(permGen.getText().replaceAll("[^0-9]", "")));
        App.settings
                .setWindowWidth(Integer.parseInt(widthField.getText().replaceAll("[^0-9]", "")));
        App.settings.setWindowHeight(Integer.parseInt(heightField.getText()
                .replaceAll("[^0-9]", "")));
        App.settings.setJavaPath(javaPath.getText());
        App.settings.setJavaParameters(javaParameters.getText());
        App.settings.setStartMinecraftMaximised(startMinecraftMaximised.isSelected());
    }
}
