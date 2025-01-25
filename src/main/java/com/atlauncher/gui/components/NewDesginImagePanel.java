/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.gui.components;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.atlauncher.managers.LogManager;

public final class NewDesginImagePanel extends JPanel {
    private static final Cursor HAND = new Cursor(Cursor.HAND_CURSOR);
    private static final int DEFAULT_WIDTH = 100, DEFAULT_HEIGHT = 100;

    private volatile Image image;

    /**
     * @param imageToLoad Deferred image loading
     */
    public NewDesginImagePanel(@Nonnull Callable<Image> imageToLoad) {
        this.setCursor(HAND);
        setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        // Launch a separate thread to load the image
        new Thread(() -> {
            try {
                setImage(imageToLoad.call());
            } catch (Exception e) {
                LogManager.error(e.getMessage());
            }
        }).start();
    }

    public void setImage(@Nonnull Image img) {
        this.image = img;

        this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));

        // Repaint on the event thread
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g;
            // crpp the image if not suqare or 1:1
            if (image.getWidth(null) != image.getHeight(null)) {
                g2.drawImage(this.image, 0, 0, 100, 100, 0, 0, 100, image.getHeight(null), null);
            } else {
                g2.drawImage(this.image, 0, 0, 100, 100, null);
            }
        }
    }
}
