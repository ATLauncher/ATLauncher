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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

public class DropDownButton extends JButton {
    private final JPopupMenu popupMenu;
    private final boolean isSeparated;

    public DropDownButton(String label, JPopupMenu popupMenu) {
        this(label, popupMenu, false, null);
    }

    public DropDownButton(String label, JPopupMenu popupMenu, boolean isSeparated, MouseListener l) {
        super(label);
        this.popupMenu = popupMenu;
        this.isSeparated = isSeparated;

        setPreferredSize(new Dimension(getPreferredSize().width + 16, getPreferredSize().height));
        setMargin(new Insets(0, 0, 0, 12));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isSeparated && e.getX() < getWidth() - 24 && l != null) {
                    l.mousePressed(e);
                } else {
                    popupMenu.setPreferredSize(new Dimension(Math.max(getWidth(), popupMenu.getPreferredSize().width),
                            popupMenu.getPreferredSize().height));
                    popupMenu.show(DropDownButton.this, 0, getHeight());
                }
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(Utils.getIconImage(App.THEME.getIconPath((popupMenu.isShowing() ? "expanded" : "collapsed")))
                .getImage(), getWidth() - 20, ((getHeight() - 12) / 2), null);

        if (isSeparated) {
            g.setColor(UIManager.getColor("Button.borderColor"));
            g.drawLine(getWidth() - 24, 4, getWidth() - 24, getHeight() - 4);
        }
    }
}
