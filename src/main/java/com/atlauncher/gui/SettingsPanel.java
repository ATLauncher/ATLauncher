/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Server;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {

    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton saveButton;
    private ImageIcon helpIcon;

    private JLabel languageLabel;
    private JComboBox<Language> language;

    private JLabel downloadServerLabel;
    private JComboBox<Server> server;

    private JLabel forgeLoggingLevelLabel;
    private JComboBox<String> forgeLoggingLevel;

    private JLabel memoryLabel;
    private JComboBox<String> memory;

    private JLabel permGenLabel;
    private JTextField permGen;

    private JPanel windowSizePanel;
    private JLabel windowSizeLabel;
    private JTextField widthField;
    private JTextField heightField;
    private JComboBox<String> commonScreenSizes;

    private JPanel javaPathPanel;
    private JLabel javaPathLabel;
    private JTextField javaPath;
    private JButton javaPathResetButton;

    private JPanel javaParametersPanel;
    private JLabel javaParametersLabel;
    private JTextField javaParameters;
    private JButton javaParametersResetButton;

    private JLabel startMinecraftMaximisedLabel;
    private JCheckBox startMinecraftMaximised;

    private JLabel advancedBackupLabel;
    private JCheckBox advancedBackup;

    private JLabel sortPacksAlphabeticallyLabel;
    private JCheckBox sortPacksAlphabetically;

    private JLabel keepLauncherOpenLabel;
    private JCheckBox keepLauncherOpen;

    private JLabel enableConsoleLabel;
    private JCheckBox enableConsole;

    private JLabel enableTrayIconLabel;
    private JCheckBox enableTrayIcon;

    private JLabel enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JLabel enableLoggingLabel;
    private JCheckBox enableLogs;

    private JLabel enableOpenEyeReportingLabel;
    private JCheckBox enableOpenEyeReporting;

    private final Insets LABEL_INSETS = new Insets(3, 0, 3, 10);
    private final Insets FIELD_INSETS = new Insets(3, 0, 3, 0);
    private final Insets LABEL_INSETS_SMALL = new Insets(0, 0, 0, 10);
    private final Insets FIELD_INSETS_SMALL = new Insets(0, 0, 0, 0);

    public SettingsPanel() {
        setLayout(new BorderLayout());
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        helpIcon = Utils.getIconImage("/assets/image/Help.png");

        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel(App.settings.getLocalizedString("settings.language") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        languageLabel.setIcon(helpIcon);
        languageLabel.setToolTipText(App.settings.getLocalizedString("settings.languagehelp"));
        topPanel.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<Language>();
        for (Language languagee : App.settings.getLanguages()) {
            language.addItem(languagee);
        }
        language.setSelectedItem(App.settings.getLanguage());
        topPanel.add(language, gbc);

        // Forge Logging Level
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        forgeLoggingLevelLabel = new JLabel(
                App.settings.getLocalizedString("settings.forgelogginglevel") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        forgeLoggingLevelLabel.setIcon(helpIcon);
        forgeLoggingLevelLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.forgelogginglevelhelp", "<br/><br/>")
                + "</center></html>");
        topPanel.add(forgeLoggingLevelLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        forgeLoggingLevel = new JComboBox<String>();
        forgeLoggingLevel.addItem("SEVERE");
        forgeLoggingLevel.addItem("WARNING");
        forgeLoggingLevel.addItem("INFO");
        forgeLoggingLevel.addItem("CONFIG");
        forgeLoggingLevel.addItem("FINE");
        forgeLoggingLevel.addItem("FINER");
        forgeLoggingLevel.addItem("FINEST");
        forgeLoggingLevel.setSelectedItem(App.settings.getForgeLoggingLevel());
        topPanel.add(forgeLoggingLevel, gbc);

        // Download Server
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        downloadServerLabel = new JLabel(App.settings.getLocalizedString("settings.downloadserver")
                + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        downloadServerLabel.setIcon(helpIcon);
        downloadServerLabel.setToolTipText(App.settings
                .getLocalizedString("settings.downloadserverhelp"));
        topPanel.add(downloadServerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        server = new JComboBox<Server>();
        for (Server serverr : App.settings.getServers()) {
            if (serverr.isUserSelectable()) {
                server.addItem(serverr);
            }
        }
        server.setSelectedItem(App.settings.getOriginalServer());
        topPanel.add(server, gbc);

        // Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        memoryLabel = new JLabel(App.settings.getLocalizedString("settings.memory") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        memoryLabel.setIcon(helpIcon);
        if (Utils.is64Bit())
            memoryLabel.setToolTipText(App.settings.getLocalizedString("settings.memoryhelp"));
        else
            memoryLabel.setToolTipText("<html><center>"
                    + App.settings.getLocalizedString("settings.memoryhelp32bit", "<br/>")
                    + "</center></html>");
        topPanel.add(memoryLabel, gbc);

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
        topPanel.add(memory, gbc);

        // Perm Gen Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        permGenLabel = new JLabel(App.settings.getLocalizedString("settings.permgen") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        permGenLabel.setIcon(helpIcon);
        permGenLabel.setToolTipText(App.settings.getLocalizedString("settings.permgenhelp"));
        topPanel.add(permGenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        permGen = new JTextField(4);
        permGen.setText(App.settings.getPermGen() + "");
        topPanel.add(permGen, gbc);

        // Window Size
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        windowSizeLabel = new JLabel(App.settings.getLocalizedString("settings.windowsize") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        windowSizeLabel.setIcon(helpIcon);
        windowSizeLabel.setToolTipText(App.settings.getLocalizedString("settings.windowsizehelp"));
        topPanel.add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new FlowLayout());
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
        topPanel.add(windowSizePanel, gbc);
        windowSizeLabel.setPreferredSize(new Dimension(windowSizeLabel.getPreferredSize().width,
                windowSizePanel.getPreferredSize().height));

        // Java Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathLabel = new JLabel(App.settings.getLocalizedString("settings.javapath") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        javaPathLabel.setIcon(helpIcon);
        javaPathLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.javapathhelp", "<br/>")
                + "</center></html>");
        topPanel.add(javaPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaPathPanel = new JPanel();
        javaPathPanel.setLayout(new FlowLayout());
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
        topPanel.add(javaPathPanel, gbc);
        javaPathLabel.setPreferredSize(new Dimension(javaPathLabel.getPreferredSize().width,
                javaPathPanel.getPreferredSize().height));

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersLabel = new JLabel(App.settings.getLocalizedString("settings.javaparameters")
                + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        javaParametersLabel.setIcon(helpIcon);
        javaParametersLabel.setToolTipText(App.settings
                .getLocalizedString("settings.javaparametershelp"));
        topPanel.add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersPanel = new JPanel();
        javaParametersPanel.setLayout(new FlowLayout());
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
        topPanel.add(javaParametersPanel, gbc);
        javaParametersLabel.setPreferredSize(new Dimension(
                javaParametersLabel.getPreferredSize().width, javaParametersPanel
                        .getPreferredSize().height));

        // Start Minecraft Maximised

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        startMinecraftMaximisedLabel = new JLabel(
                App.settings.getLocalizedString("settings.startminecraftmaximised") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        startMinecraftMaximisedLabel.setIcon(helpIcon);
        startMinecraftMaximisedLabel.setToolTipText(App.settings
                .getLocalizedString("settings.startminecraftmaximisedhelp"));
        topPanel.add(startMinecraftMaximisedLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        startMinecraftMaximised = new JCheckBox();
        if (App.settings.startMinecraftMaximised()) {
            startMinecraftMaximised.setSelected(true);
        }
        topPanel.add(startMinecraftMaximised, gbc);

        // Advanced Backup

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        advancedBackupLabel = new JLabel(App.settings.getLocalizedString("settings.advancedbackup")
                + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        advancedBackupLabel.setIcon(helpIcon);
        advancedBackupLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.advancedbackuphelp", "<br/>")
                + "</center></html>");
        topPanel.add(advancedBackupLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        advancedBackup = new JCheckBox();
        if (App.settings.isAdvancedBackupsEnabled()) {
            advancedBackup.setSelected(true);
        }
        topPanel.add(advancedBackup, gbc);

        // Sort Packs Alphabetically

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        sortPacksAlphabeticallyLabel = new JLabel(
                App.settings.getLocalizedString("settings.sortpacksalphabetically") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        sortPacksAlphabeticallyLabel.setIcon(helpIcon);
        sortPacksAlphabeticallyLabel.setToolTipText(App.settings
                .getLocalizedString("settings.sortpacksalphabeticallyhelp"));
        topPanel.add(sortPacksAlphabeticallyLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        sortPacksAlphabetically = new JCheckBox();
        if (App.settings.sortPacksAlphabetically()) {
            sortPacksAlphabetically.setSelected(true);
        }
        topPanel.add(sortPacksAlphabetically, gbc);

        // Keep Launcher Open

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        keepLauncherOpenLabel = new JLabel(
                App.settings.getLocalizedString("settings.keeplauncheropen") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        keepLauncherOpenLabel.setIcon(helpIcon);
        keepLauncherOpenLabel.setToolTipText(App.settings
                .getLocalizedString("settings.keeplauncheropenhelp"));
        topPanel.add(keepLauncherOpenLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        keepLauncherOpen = new JCheckBox();
        if (App.settings.keepLauncherOpen()) {
            keepLauncherOpen.setSelected(true);
        }
        topPanel.add(keepLauncherOpen, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableConsoleLabel = new JLabel(App.settings.getLocalizedString("settings.console") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableConsoleLabel.setIcon(helpIcon);
        enableConsoleLabel.setToolTipText(App.settings.getLocalizedString("settings.consolehelp"));
        topPanel.add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        if (App.settings.enableConsole()) {
            enableConsole.setSelected(true);
        }
        topPanel.add(enableConsole, gbc);

        // Enable Tray Icon

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableTrayIconLabel = new JLabel(App.settings.getLocalizedString("settings.traymenu") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableTrayIconLabel.setIcon(helpIcon);
        enableTrayIconLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.traymenuhelp", "<br/>")
                + "</center></html>");
        topPanel.add(enableTrayIconLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableTrayIcon = new JCheckBox();
        if (App.settings.enableTrayIcon()) {
            enableTrayIcon.setSelected(true);
        }
        topPanel.add(enableTrayIcon, gbc);

        // Enable Leaderboards

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabel(
                App.settings.getLocalizedString("settings.leaderboards") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableLeaderboardsLabel.setIcon(helpIcon);
        enableLeaderboardsLabel.setToolTipText(App.settings
                .getLocalizedString("settings.leaderboardshelp"));
        topPanel.add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        if (App.settings.enableLeaderboards()) {
            enableLeaderboards.setSelected(true);
        }
        if (!App.settings.enableLogs()) {
            enableLeaderboards.setEnabled(false);
        }
        topPanel.add(enableLeaderboards, gbc);

        // Enable Logging

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLoggingLabel = new JLabel(App.settings.getLocalizedString("settings.logging") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableLoggingLabel.setIcon(helpIcon);
        enableLoggingLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.logginghelp", "<br/>")
                + "</center></html>");
        topPanel.add(enableLoggingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLogs = new JCheckBox();
        enableLogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!enableLogs.isSelected()) {
                    enableOpenEyeReporting.setSelected(false);
                    enableOpenEyeReporting.setEnabled(false);
                    enableLeaderboards.setSelected(false);
                    enableLeaderboards.setEnabled(false);
                } else {
                    enableOpenEyeReporting.setSelected(true);
                    enableOpenEyeReporting.setEnabled(true);
                    enableLeaderboards.setSelected(true);
                    enableLeaderboards.setEnabled(true);
                }
            }
        });
        if (App.settings.enableLogs()) {
            enableLogs.setSelected(true);
        }
        topPanel.add(enableLogs, gbc);

        // Enable OpenEye Reporting

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableOpenEyeReportingLabel = new JLabel(
                App.settings.getLocalizedString("settings.openeye") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, new Color(80, 170, 107), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableOpenEyeReportingLabel.setIcon(helpIcon);
        enableOpenEyeReportingLabel.setToolTipText("<html><center>"
                + Utils.splitMultilinedString(
                        App.settings.getLocalizedString("settings.openeyehelp"), 80, "<br/>")
                + "</center></html>");
        topPanel.add(enableOpenEyeReportingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableOpenEyeReporting = new JCheckBox();
        if (!App.settings.enableLogs()) {
            enableOpenEyeReporting.setEnabled(false);
        }
        if (App.settings.enableOpenEyeReporting()) {
            enableOpenEyeReporting.setSelected(true);
        }
        topPanel.add(enableOpenEyeReporting, gbc);

        // End Components

        bottomPanel = new JPanel();
        saveButton = new JButton(App.settings.getLocalizedString("common.save"));
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                File jPath = new File(javaPath.getText(), "bin");
                if (!jPath.exists()) {
                    JOptionPane.showMessageDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("settings.javapathincorrect",
                                            "<br/><br/>") + "</center></html>",
                            App.settings.getLocalizedString("settings.help"),
                            JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                if (javaParameters.getText().contains("-Xms")
                        || javaParameters.getText().contains("-Xmx")
                        || javaParameters.getText().contains("-XX:PermSize")) {
                    JOptionPane.showMessageDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString(
                                            "settings.javaparametersincorrect", "<br/><br/>")
                                    + "</center></html>", App.settings
                                    .getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
                    return;
                }
                boolean reboot = false;
                boolean reloadPacksPanel = false;
                if (language.getSelectedItem() != App.settings.getLanguage()) {
                    reboot = true;
                }
                if (sortPacksAlphabetically.isSelected() != App.settings.sortPacksAlphabetically()) {
                    reloadPacksPanel = true;
                }
                App.settings.setLanguage((Language) language.getSelectedItem());
                App.settings.setServer((Server) server.getSelectedItem());
                App.settings.setForgeLoggingLevel((String) forgeLoggingLevel.getSelectedItem());
                App.settings.setMemory(Integer.parseInt(((String) memory.getSelectedItem())
                        .replace(" MB", "")));
                App.settings.setPermGen(Integer
                        .parseInt(permGen.getText().replaceAll("[^0-9]", "")));
                App.settings.setWindowWidth(Integer.parseInt(widthField.getText().replaceAll(
                        "[^0-9]", "")));
                App.settings.setWindowHeight(Integer.parseInt(heightField.getText().replaceAll(
                        "[^0-9]", "")));
                App.settings.setJavaPath(javaPath.getText());
                App.settings.setJavaParameters(javaParameters.getText());
                App.settings.setStartMinecraftMaximised(startMinecraftMaximised.isSelected());
                App.settings.setAdvancedBackups(advancedBackup.isSelected());
                App.settings.setSortPacksAlphabetically(sortPacksAlphabetically.isSelected());
                App.settings.setKeepLauncherOpen(keepLauncherOpen.isSelected());
                App.settings.setEnableConsole(enableConsole.isSelected());
                App.settings.setEnableTrayIcon(enableTrayIcon.isSelected());
                App.settings.setEnableLeaderboards(enableLeaderboards.isSelected());
                App.settings.setEnableLogs(enableLogs.isSelected());
                App.settings.setEnableOpenEyeReporting(enableOpenEyeReporting.isSelected());
                App.settings.saveProperties();
                App.settings.log("Settings Saved!");
                if (reboot) {
                    App.settings.restartLauncher();
                }
                if (reloadPacksPanel) {
                    App.settings.reloadPacksPanel();
                }
                String[] options = { App.settings.getLocalizedString("common.ok") };
                JOptionPane.showOptionDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("settings.saved"),
                        App.settings.getLocalizedString("settings.saved"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options,
                        options[0]);
            }
        });
        bottomPanel.add(saveButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}