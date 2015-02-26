/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui;

import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * The splash screen which shows when the launcher is started up and is loading it's stuff.
 */
public class SplashScreen extends JWindow {
    private static final BufferedImage img = Utils.getImage("SplashScreen");
    private final ContextMenu CONTEXT_MENU = new ContextMenu();

    public SplashScreen() {
        this.setLayout(null);
        this.setSize(img.getWidth(), img.getHeight());
        this.setLocationRelativeTo(null);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    CONTEXT_MENU.show(SplashScreen.this, e.getX(), e.getY());
                }
            }
        });
        this.setAlwaysOnTop(false);
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
    }

    /**
     * Closes and disposes of the splash screen.
     */
    public void close() {
        this.setVisible(false);
        this.dispose();
    }

    /**
     * The context menu which is shows on right click for the splash screen image, giving a force quit option.
     */
    private final class ContextMenu extends JPopupMenu {
        private final JMenuItem FORCE_QUIT = new JMenuItem(Language.INSTANCE.localize("common.forcequit"));

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