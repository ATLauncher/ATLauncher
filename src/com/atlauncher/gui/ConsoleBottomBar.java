/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class ConsoleBottomBar extends JPanel {

    private JPanel leftSide;
    private JPanel rightSide;

    private JButton clear;
    private JButton copyLog;
    private JButton uploadLog;
    private JButton killMinecraft;
    private JButton facebookIcon;
    private JButton githubIcon;
    private JButton twitterIcon;
    private JButton redditIcon;

    public ConsoleBottomBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50)); // Make the bottom bar at least 50 pixels high

        createButtons(); // Create the buttons
        setupActionListeners(); // Setup Action Listeners

        leftSide = new JPanel();
        leftSide.setLayout(new GridBagLayout());
        rightSide = new JPanel();
        rightSide.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 5, 0, 0);
        leftSide.add(clear, gbc);
        gbc.gridx++;
        leftSide.add(copyLog, gbc);
        gbc.gridx++;
        leftSide.add(uploadLog, gbc);
        gbc.gridx++;
        leftSide.add(killMinecraft, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        rightSide.add(facebookIcon, gbc);
        gbc.gridx++;
        rightSide.add(githubIcon, gbc);
        gbc.gridx++;
        rightSide.add(redditIcon, gbc);
        gbc.gridx++;
        rightSide.add(twitterIcon, gbc);

        add(leftSide, BorderLayout.WEST);
        add(rightSide, BorderLayout.EAST);
    }

    /**
     * Sets up the action listeners on the buttons
     */
    private void setupActionListeners() {
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.clearConsole();
                App.settings.log("Console Cleared");
            }
        });
        copyLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Copied Log To Clipboard");
                StringSelection text = new StringSelection(App.settings.getLog());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        uploadLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = Utils.uploadLog();
                if (result.contains("%PASTECHECKURL%")) {
                    App.settings.log("Log uploaded and link copied to clipboard: " + result);
                    StringSelection text = new StringSelection(result);
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(text, null);
                } else {
                    App.settings
                            .log("Log failed to upload: " + result, LogMessageType.error, false);
                }
            }
        });
        killMinecraft.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), "<html><center>"
                        + App.settings.getLocalizedString("console.killsure", "<br/><br/>")
                        + "</center>" + "</html>", App.settings.getLocalizedString("console.kill"),
                        JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    App.settings.killMinecraft();
                    killMinecraft.setVisible(false);
                }
            }
        });
        facebookIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up ATLauncher Facebook Page");
                Utils.openBrowser("http://www.facebook.com/ATLauncher");
            }
        });
        githubIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up ATLauncher GitHub Page");
                Utils.openBrowser("https://github.com/RyanTheAllmighty/ATLauncher");
            }
        });
        redditIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up ATLauncher Reddit Page");
                Utils.openBrowser("http://www.reddit.com/r/ATLauncher");
            }
        });
        twitterIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                App.settings.log("Opening Up ATLauncher Twitter Page");
                Utils.openBrowser("http://www.twitter.com/ATLauncher");
            }
        });
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        clear = new JButton("Clear");
        copyLog = new JButton("Copy Log");
        uploadLog = new JButton("Upload Log");

        killMinecraft = new JButton("Kill Minecraft");
        killMinecraft.setVisible(false);

        facebookIcon = new JButton(Utils.getIconImage("/resources/FacebookIcon.png"));
        facebookIcon.setBorder(BorderFactory.createEmptyBorder());
        facebookIcon.setContentAreaFilled(false);
        facebookIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        githubIcon = new JButton(Utils.getIconImage("/resources/GitHubIcon.png"));
        githubIcon.setBorder(BorderFactory.createEmptyBorder());
        githubIcon.setContentAreaFilled(false);
        githubIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        redditIcon = new JButton(Utils.getIconImage("/resources/RedditIcon.png"));
        redditIcon.setBorder(BorderFactory.createEmptyBorder());
        redditIcon.setContentAreaFilled(false);
        redditIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        twitterIcon = new JButton(Utils.getIconImage("/resources/TwitterIcon.png"));
        twitterIcon.setBorder(BorderFactory.createEmptyBorder());
        twitterIcon.setContentAreaFilled(false);
        twitterIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void showKillMinecraft() {
        killMinecraft.setVisible(true);
    }

    public void hideKillMinecraft() {
        killMinecraft.setVisible(false);
    }

    public void setupLanguage() {
        clear.setText(App.settings.getLocalizedString("console.clear"));
        copyLog.setText(App.settings.getLocalizedString("console.copy"));
        uploadLog.setText(App.settings.getLocalizedString("console.upload"));
        killMinecraft.setText(App.settings.getLocalizedString("console.kill"));
    }
}
