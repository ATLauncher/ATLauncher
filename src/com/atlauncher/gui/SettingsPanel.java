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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.atlauncher.data.Language;
import com.atlauncher.data.Server;

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

        helpIcon = Utils.getIconImage("/resources/Help.png");

        // Language
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        languageLabel = new JLabel("Language:");
        languageLabel.setIcon(helpIcon);
        languageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                "This specifies the Language used by the Launcher", "Help",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        topPanel.add(languageLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        language = new JComboBox<Language>();
        for (Language languagee : LauncherFrame.settings.getLanguages()) {
            language.addItem(languagee);
        }
        language.setSelectedItem(LauncherFrame.settings.getLanguage());
        topPanel.add(language, gbc);

        // Download Server
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        downloadServerLabel = new JLabel("Download Server:");
        downloadServerLabel.setIcon(helpIcon);
        downloadServerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(
                                LauncherFrame.settings.getParent(),
                                "The server to download files from. Keep on Auto for best results.",
                                "Help", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
        topPanel.add(downloadServerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        server = new JComboBox<Server>();
        for (Server serverr : LauncherFrame.settings.getServers()) {
            server.addItem(serverr);
        }
        server.setSelectedItem(LauncherFrame.settings.getServer());
        topPanel.add(server, gbc);

        // Memory Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        memoryLabel = new JLabel("Memory/RAM:");
        memoryLabel.setIcon(helpIcon);
        memoryLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        if (Utils.is64Bit()) {
                            JOptionPane.showMessageDialog(
                                    LauncherFrame.settings.getParent(),
                                    "<html><center>The amount of RAM to use when launching Minecraft.<br/>"
                                            + "<br/>You can only allocate up to 1GB of RAM as you "
                                            + "don't have<br/>a 64 bit system or you don't have Java "
                                            + "64 bit version installed</center></html>", "Help",
                                    JOptionPane.PLAIN_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                    "The amount of RAM to use when launching Minecraft.", "Help",
                                    JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                }
            }
        });
        topPanel.add(memoryLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        memory = new JComboBox<String>();
        String[] memoryOptions = Utils.getMemoryOptions();
        for (int i = 0; i < memoryOptions.length; i++) {
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
        windowSizeLabel.setIcon(helpIcon);
        windowSizeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                "The size that the Minecraft window should open as", "Help",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
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
        windowSizePanel.setPreferredSize(new Dimension(137,
                windowSizePanel.getPreferredSize().height));
        windowSizeLabel.setPreferredSize(new Dimension(windowSizeLabel.getPreferredSize().width,
                windowSizePanel.getPreferredSize().height));

        // Java Paramaters

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        javaParametersLabel = new JLabel("Java Parameters:");
        javaParametersLabel.setIcon(helpIcon);
        javaParametersLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                "Extra Java command line paramaters can be added here", "Help",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
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
        enableConsoleLabel.setIcon(helpIcon);
        enableConsoleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                "If you want the console to be visible when opening the Launcher",
                                "Help", JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
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
        enableLeaderboardsLabel.setIcon(helpIcon);
        enableLeaderboardsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(LauncherFrame.settings.getParent(),
                                "If you want to participate in the Leaderboards", "Help",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
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
        enableLoggingLabel.setIcon(helpIcon);
        enableLoggingLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getX() < 16 && e.getY() < 16) {
                        JOptionPane.showMessageDialog(
                                LauncherFrame.settings.getParent(),
                                "<html><center>The Launcher sends back anonymous usage and error "
                                        + "logs<br/>to our servers in order to make the Launcher and Packs"
                                        + "<br/>better. If you don't want this to happen then simply<br/>"
                                        + "disable this option</center></html>", "Help",
                                JOptionPane.PLAIN_MESSAGE);
                    }
                }
            }
        });
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
                LauncherFrame.settings.setLanguage((Language) language.getSelectedItem());
                LauncherFrame.settings.setServer((Server) server.getSelectedItem());
                LauncherFrame.settings.saveProperties();
                LauncherFrame.settings.getConsole().log("Settings Saved!");
            }
        });
        bottomPanel.add(saveButton);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

}
