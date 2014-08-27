/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.gui.CustomLineBorder;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.border.Border;

public class JLabelWithHover extends JLabel {
    private static final long serialVersionUID = -4371080285355832166L;
    private static final Border HOVER_BORDER = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);

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
        tip.setBorder(HOVER_BORDER);
        return tip;
    }
}
