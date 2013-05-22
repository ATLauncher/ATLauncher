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

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JWindow;

public class SplashScreen extends JWindow {
    private static final long serialVersionUID = 1L;
    protected final ImageIcon icon;

    public SplashScreen() {
        this.icon = Utils.getIconImage("/resources/SplashScreen.png");
        setLayout(null);
        JButton background = new JButton(icon);
        background.setBounds(0, 0, icon.getIconWidth(), icon.getIconHeight());
        background.setFocusable(false);
        background.setContentAreaFilled(false);
        background.setBorderPainted(false);
        background.setOpaque(false);
        add(background);
        setSize(icon.getIconWidth(), icon.getIconHeight());
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void close() {
        this.setVisible(false);
        this.dispose();
    }
}