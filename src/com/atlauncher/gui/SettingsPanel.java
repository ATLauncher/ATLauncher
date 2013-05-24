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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.data.Language;
import com.atlauncher.data.Server;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel {

    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton saveButton;

    private JLabel languageLabel;
    private JComboBox<Language> language;

    private JLabel downloadServerLabel;
    private JComboBox<Server> downloadServer;

    private JLabel memoryLabel;
    private JComboBox<String> memory;

    private JPanel windowSizePanel;
    private JLabel windowSizeLabel;
    private JTextField widthField;
    private JTextField heightField;

    private JLabel javaParametersLabel;
    private JTextField javaParametersField;

    private JLabel enableConsoleLabel;
    private JCheckBox enableConsole;

    private JLabel enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JLabel enableLoggingLabel;
    private JCheckBox enableLogging;

    private final Insets LABEL_INSETS = new Insets(3, 0, 3, 10);
    private final Insets FIELD_INSETS = new Insets(3, 0, 3, 0);
    private final Insets LABEL_INSETS_SMALL = new Insets(0, 0, 0, 10);
    private final Insets FIELD_INSETS_SMALL = new Insets(0, 0, 0, 0);

    public SettingsPanel() {
        setLayout(new BorderLayout());
        topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Language

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel("Language:");
        topPanel.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<Language>();
        Language[] languages = LauncherFrame.settings.getLanguages();
        for (int i = 0; i < languages.length; i++) {
            language.addItem(languages[i]);
        }
        topPanel.add(language, gbc);

        // Download Server

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        downloadServerLabel = new JLabel("Download Server:");
        topPanel.add(downloadServerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        downloadServer = new JComboBox<Server>();
        Server[] servers = LauncherFrame.settings.getServers();
        for (int i = 0; i < servers.length; i++) {
            downloadServer.addItem(servers[i]);
        }
        topPanel.add(downloadServer, gbc);

        // Memory Settings

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        memoryLabel = new JLabel("Memory/RAM:");
        topPanel.add(memoryLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        memory = new JComboBox<String>();
        String[] memoryOptions = LauncherFrame.settings.getMemoryOptions();
        for(int i=0;i<memoryOptions.length;i++){
            memory.addItem(memoryOptions[i]);
        }
        topPanel.add(memory, gbc);

        // Window Size

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = LABEL_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        windowSizeLabel = new JLabel("Window Size:");
        topPanel.add(windowSizeLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS_SMALL;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        windowSizePanel = new JPanel();
        windowSizePanel.setLayout(new FlowLayout());
        widthField = new JTextField(4);
        heightField = new JTextField(4);
        windowSizePanel.add(widthField);
        windowSizePanel.add(new JLabel("x"));
        windowSizePanel.add(heightField);
        topPanel.add(windowSizePanel, gbc);
        windowSizePanel.setPreferredSize(new Dimension(137, windowSizePanel
                .getPreferredSize().height));
        windowSizeLabel.setPreferredSize(new Dimension(windowSizeLabel
                .getPreferredSize().width,
                windowSizePanel.getPreferredSize().height));

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersLabel = new JLabel("Java Parameters:");
        topPanel.add(javaParametersLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        javaParametersField = new JTextField(20);
        topPanel.add(javaParametersField, gbc);

        // Enable Console

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableConsoleLabel = new JLabel("Enable Console?");
        topPanel.add(enableConsoleLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableConsole = new JCheckBox();
        topPanel.add(enableConsole, gbc);

        // Enable Leaderboards

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabel("Enable Leaderboards?");
        topPanel.add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        topPanel.add(enableLeaderboards, gbc);

        // Enable Logging

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLoggingLabel = new JLabel("Enable Logging?");
        topPanel.add(enableLoggingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLogging = new JCheckBox();
        topPanel.add(enableLogging, gbc);

        // End Components

        bottomPanel = new JPanel();
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Settings Saved!");
            }
        });
        bottomPanel.add(saveButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

}
