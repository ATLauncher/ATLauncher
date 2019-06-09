/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

import javax.swing.border.LineBorder;

public class CustomLineBorder extends LineBorder {
    private int insets = 0;

    public CustomLineBorder(int insets, Color color) {
        super(color);
        this.insets = insets;
    }

    public CustomLineBorder(int insets, Color color, int thickness) {
        super(color, thickness);
        this.insets = insets;
    }

    public CustomLineBorder(int insets, Color color, int thickness, boolean rounded) {
        super(color, thickness, rounded);
        this.insets = insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return new Insets(this.insets, this.insets, this.insets, this.insets);
    }
}
