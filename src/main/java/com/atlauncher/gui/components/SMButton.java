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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolTip;

import com.atlauncher.gui.HoverLineBorder;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class SMButton extends JButton {
    private static final Cursor hand = new Cursor(Cursor.HAND_CURSOR);

    public SMButton(ImageIcon i, String tooltip) {
        super(i);
        this.setToolTipText(tooltip);
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setContentAreaFilled(false);
        this.setCursor(hand);
    }

    public SMButton(String i, String t) {
        this(Utils.getIconImage(i), t);
    }

    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBorder(new HoverLineBorder());
        return tip;
    }
}
