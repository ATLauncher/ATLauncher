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
package com.atlauncher.gui.borders;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class IconTitledBorder extends TitledBorder {
    private Icon icon;

    public IconTitledBorder(String title, Font font, Icon icon) {
        super(title);
        setTitleFont(font);
        this.icon = icon == null ? null : resizeIcon(icon, 20);
    }

    private Icon resizeIcon(Icon icon, int newHeight) {
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        int newWidth = (int) ((float) newHeight * iconWidth / iconHeight);

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(((ImageIcon) icon).getImage(), 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return new ImageIcon(resizedImage);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x, y, width, height);

        if (icon != null) {
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();

            FontMetrics fm = g.getFontMetrics();

            int titleWidth = fm.stringWidth(getTitle());

            int iconX = titleWidth + 10;
            int iconY = 1;

            g.setColor(UIManager.getColor("Panel.background"));
            g.fillRect(iconX - 2, iconY, iconWidth + 4, iconHeight);

            icon.paintIcon(c, g, iconX, iconY);
        }
    }
}
