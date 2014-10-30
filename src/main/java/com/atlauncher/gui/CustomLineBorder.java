/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.gui;

import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;

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
