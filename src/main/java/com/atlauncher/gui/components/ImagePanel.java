/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import javax.swing.JPanel;

@SuppressWarnings("serial")
public final class ImagePanel extends JPanel {
    private static final Cursor HAND = new Cursor(Cursor.HAND_CURSOR);

    private volatile Image image;

    public ImagePanel(Image image) {
        this.image = image;
        this.setCursor(HAND);
        this.setPreferredSize(new Dimension(Math.min(image.getWidth(null), 300), Math.min(image.getWidth(null), 150)));
    }

    public void setImage(Image img) {
        this.image = img;
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(this.image, 0, (this.getHeight() - 150) / 2, 300, 150, null);
    }
}
