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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ConsoleBottomBar extends JPanel {

    private JPanel leftSide;
    private JPanel rightSide;

    private JButton copyLog;
    private JButton uploadLog;
    private JButton facebookIcon;
    private JButton twitterIcon;
    private JButton redditIcon;

    public ConsoleBottomBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50)); // Make the bottom bar at least
                                                // 50 pixels high

        createButtons(); // Create the buttons
        setupActionListeners(); // Setup Action Listeners

        leftSide = new JPanel();
        rightSide = new JPanel();
        rightSide.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 5, 0, 0);
        leftSide.add(copyLog, gbc);
        gbc.gridx++;
        leftSide.add(uploadLog, gbc);

        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(0, 0, 0, 5);
        rightSide.add(facebookIcon, gbc);
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
        copyLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LauncherFrame.console.log("Copied Log To Clipboard");
                StringSelection text = new StringSelection(
                        LauncherFrame.console.getLog());
                Clipboard clipboard = Toolkit.getDefaultToolkit()
                        .getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        uploadLog.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String result = Utils.uploadPaste("ATLauncher Log", LauncherFrame.console.getLog());
                if (result.contains("http://paste.atlauncher.com")) {
                    LauncherFrame.console
                            .log("Log uploaded and copied to clipboard: " + result);
                    StringSelection text = new StringSelection(result);
                    Clipboard clipboard = Toolkit.getDefaultToolkit()
                            .getSystemClipboard();
                    clipboard.setContents(text, null);
                }else{
                    LauncherFrame.console
                            .log("Log failed to upload: " + result);
                }
            }
        });
        facebookIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LauncherFrame.console
                        .log("Opening Up ATLauncher Facebook Page");
                Utils.openBrowser("http://www.facebook.com/ATLauncher");
            }
        });
        redditIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LauncherFrame.console.log("Opening Up ATLauncher Reddit Page");
                Utils.openBrowser("http://www.reddit.com/r/ATLauncher");
            }
        });
        twitterIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LauncherFrame.console.log("Opening Up ATLauncher Twitter Page");
                Utils.openBrowser("http://www.twitter.com/ATLauncher");
            }
        });
    }

    /**
     * Creates the JButton's for use in the bar
     */
    private void createButtons() {
        copyLog = new JButton("Copy Log");
        uploadLog = new JButton("Upload Log");

        facebookIcon = new JButton(
                Utils.getIconImage("/resources/FacebookIcon.png"));
        facebookIcon.setBorder(BorderFactory.createEmptyBorder());
        facebookIcon.setContentAreaFilled(false);
        facebookIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        redditIcon = new JButton(
                Utils.getIconImage("/resources/RedditIcon.png"));
        redditIcon.setBorder(BorderFactory.createEmptyBorder());
        redditIcon.setContentAreaFilled(false);
        redditIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));

        twitterIcon = new JButton(
                Utils.getIconImage("/resources/TwitterIcon.png"));
        twitterIcon.setBorder(BorderFactory.createEmptyBorder());
        twitterIcon.setContentAreaFilled(false);
        twitterIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
