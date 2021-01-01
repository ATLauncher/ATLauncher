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
package com.atlauncher.gui.card;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.atlauncher.data.json.Mod;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public final class ModCard extends JPanel {
    public final Mod mod;

    public ModCard(final Mod mod) {
        Dimension dim = new Dimension(this.getPreferredSize().width, (int) (this.getPreferredSize().height * 1.5));
        this.setPreferredSize(dim);
        this.mod = mod;
        if (this.mod.hasWebsite()) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (ModCard.this.mod.hasWebsite()) {
                    OS.openWebBrowser(mod.getWebsite());
                }
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.drawString(this.mod.getName(), 10, 10);
        g2.setColor(this.mod.isOptional() ? Color.GREEN : Color.RED);
        g2.drawString(this.mod.isOptional() ? "Optional" : "Required",
                g2.getFontMetrics().stringWidth(this.mod.getName()) + g2.getFontMetrics().charWidth('M') * 2, 10);
    }
}
