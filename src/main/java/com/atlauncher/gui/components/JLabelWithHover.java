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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.gui.HoverLineBorder;

@SuppressWarnings("serial")
public class JLabelWithHover extends JLabel {
    public JLabelWithHover(Icon icon, String tooltipText, Border border) {
        super();
        super.setIcon(icon);
        super.setToolTipText(tooltipText);
        super.setBorder(border);
    }

    public JLabelWithHover(String label, Icon icon, String tooltipText) {
        super(label);
        super.setIcon(icon);
        super.setToolTipText(tooltipText);
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBorder(new HoverLineBorder());
        return tip;
    }
}
