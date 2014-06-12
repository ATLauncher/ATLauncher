/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import javax.swing.JCheckBox;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Mod;
import com.atlauncher.gui.CustomLineBorder;

public class ModsJCheckBox extends JCheckBox {

    private static final long serialVersionUID = -4560260483416099547L;
    private Object mod;
    private boolean isCategory = false;
    private String categoryName = null;
    private static final Border HOVER_BORDER = new CustomLineBorder(5,
            App.THEME.getHoverBorderColor(), 2);

    public ModsJCheckBox(Mod mod) {
        super(mod.getName());
        if (mod.hasColour()) {
            setForeground(mod.getColour());
        }
        this.mod = mod;
        if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
            this.setToolTipText(mod.getDescription());
        }
    }

    public ModsJCheckBox(DisableableMod mod) {
        super(mod.getName());
        if (mod.hasColour()) {
            setForeground(mod.getColour());
        }
        this.mod = mod;
        if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
            this.setToolTipText(mod.getDescription());
        }
    }

    public ModsJCheckBox(String categoryName) {
        super(categoryName);
        this.isCategory = true;
    }

    public boolean isCategory() {
        return this.isCategory;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public Mod getMod() {
        return (Mod) this.mod;
    }

    public DisableableMod getDisableableMod() {
        return (DisableableMod) this.mod;
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBorder(HOVER_BORDER);
        return tip;
    }

}
