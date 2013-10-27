/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;

import com.atlauncher.utils.Utils;

public class SplashScreen extends JWindow {

    private ImageIcon icon = Utils.getIconImage("/resources/SplashScreen.png");

    public SplashScreen() {
        setLayout(null);
        final JButton background = new JButton(icon);
        background.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        background.setFocusable(false);
        background.setContentAreaFilled(false);
        background.setBorderPainted(false);
        background.setOpaque(false);
        add(background);
        setSize(icon.getIconWidth(), icon.getIconHeight());
        setVisible(true);
        setLocationRelativeTo(null);

        final JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem forceQuit = new JMenuItem("Force Quit");
        forceQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        contextMenu.add(forceQuit);

        background.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    contextMenu.show(background, e.getX(), e.getY());
                }
            }
        });
    }

    /**
     * Closes and disposes of the splash screen
     */
    public void close() {
        this.setVisible(false);
        this.dispose();
    }
}