/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import javax.swing.border.LineBorder;

public class CustomLineBorder extends LineBorder{
    private int insets = 0;

    public CustomLineBorder(int insets, Color color){
        super(color);
        this.insets = insets;
    }

    public CustomLineBorder(int insets, Color color, int thickness){
        super(color, thickness);
        this.insets = insets;
    }

    public CustomLineBorder(int insets, Color color, int thickness, boolean rounded){
        super(color, thickness, rounded);
        this.insets = insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets){
        return new Insets(this.insets, this.insets, this.insets, this.insets);
    }
}
