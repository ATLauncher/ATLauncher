/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2026 ATLauncher
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
package com.atlauncher.gui.tabs.news;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.atlauncher.App;
import com.formdev.flatlaf.util.UIScale;

final class NewsStyle {
    private NewsStyle() {}

    static int scale(int value) {
        return UIScale.scale(value);
    }

    static Font font(int size, boolean bold) {
        Font base = App.THEME == null ? UIManager.getFont("Label.font") : App.THEME.getNormalFont();
        return base.deriveFont(bold ? Font.BOLD : Font.PLAIN, (float) scale(size));
    }

    static Color background() {
        return UIManager.getColor("Panel.background");
    }

    static Color foreground() {
        return UIManager.getColor("Label.foreground");
    }

    static boolean dark() {
        Color color = background();
        return (color.getRed() * .2126 + color.getGreen() * .7152 + color.getBlue() * .0722) < 128;
    }

    static Color accent() {
        Color accent = UIManager.getColor("News.linkColor");
        return accent == null ? foreground() : accent;
    }

    static Color card() {
        return dark() ? blend(foreground(), background(), .025f) : blend(Color.WHITE, background(), .85f);
    }

    static Color border() {
        return blend(foreground(), card(), dark() ? .07f : .12f);
    }

    static Color muted() {
        return blend(foreground(), background(), .60f);
    }

    static Color body() {
        return blend(foreground(), background(), .85f);
    }

    static Color blend(Color first, Color second, float amount) {
        return new Color(Math.round(first.getRed() * amount + second.getRed() * (1 - amount)),
            Math.round(first.getGreen() * amount + second.getGreen() * (1 - amount)),
            Math.round(first.getBlue() * amount + second.getBlue() * (1 - amount)));
    }

    static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    static JPanel stack() {
        JPanel panel = panel(null);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font(size, bold));
        label.setForeground(color);
        return label;
    }

    static void add(JPanel panel, Component child) {
        if (child instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) child).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        panel.add(child);
    }

    static void gap(JPanel panel, int size) {
        panel.add(Box.createVerticalStrut(scale(size)));
    }
}
