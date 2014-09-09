/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.utils.Utils;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;

public class SplashScreen extends JWindow {
    private static final BufferedImage img = Utils.getImage("SplashScreen");
    private final ContextMenu CONTEXT_MENU = new ContextMenu();

    public SplashScreen() {
        this.setLayout(null);
        this.setSize(img.getWidth(), img.getHeight());
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    CONTEXT_MENU.show(SplashScreen.this, e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    /**
     * Closes and disposes of the splash screen
     */
    public void close() {
        this.setVisible(false);
        this.dispose();
    }

    private final class ContextMenu extends JPopupMenu {
        private final JMenuItem FORCE_QUIT = new JMenuItem("Force Quit");

        public ContextMenu() {
            super();

            this.FORCE_QUIT.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            this.add(this.FORCE_QUIT);
        }
    }
}