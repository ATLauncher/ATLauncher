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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class BottomBar extends JPanel {

    private JPanel leftSide;
    private JPanel rightSide;

    private JButton facebookIcon;
    private JButton twitterIcon;
    private JButton redditIcon;

    public BottomBar() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(0, 50)); // Make the bottom bar at least
                                                // 50 pixels high

        leftSide = new JPanel();
        rightSide = new JPanel();
        rightSide.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        createIcons();

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

    private void createIcons() {
        facebookIcon = new JButton(
                Utils.getIconImage("/resources/FacebookIcon.png"));
        facebookIcon.setBorder(BorderFactory.createEmptyBorder());
        facebookIcon.setContentAreaFilled(false);
        facebookIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        facebookIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser("http://www.facebook.com/ATLauncher");
            }
        });

        redditIcon = new JButton(
                Utils.getIconImage("/resources/RedditIcon.png"));
        redditIcon.setBorder(BorderFactory.createEmptyBorder());
        redditIcon.setContentAreaFilled(false);
        redditIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        redditIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser("http://www.reddit.com/r/ATLauncher");
            }
        });

        twitterIcon = new JButton(
                Utils.getIconImage("/resources/TwitterIcon.png"));
        twitterIcon.setBorder(BorderFactory.createEmptyBorder());
        twitterIcon.setContentAreaFilled(false);
        twitterIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        twitterIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser("http://www.twitter.com/ATLauncher");
            }
        });
    }
}
