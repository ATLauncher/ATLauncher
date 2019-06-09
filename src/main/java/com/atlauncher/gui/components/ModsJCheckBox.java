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
package com.atlauncher.gui.components;

import javax.swing.JCheckBox;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.utils.Utils;

/**
 * This class extends {@link JCheckBox} and overrides the need to use JCheckBox in the {@link ModsChooser}, {@link
 * ModsChooser} and {@link EditModsDialog}, providing specific functionality for those two components. Mainly providing
 * a hover tooltip for a mods description, as well as giving pack developers a way to colour mod's names.
 */
public class ModsJCheckBox extends JCheckBox {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -4560260483416099547L;

    /**
     * The mod this object will use to display it's data. Will be type {@link Mod}, {@link com.atlauncher.data.json.Mod}
     * or {@link DisableableMod}.
     */
    private Object mod;

    /**
     * Static object for the {@link Border} to show around the tooltips for mods with descriptions.
     */
    private static final Border HOVER_BORDER = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);

    /**
     * Constructor for use in the {@link ModsChooser} dialog with new JSON format.
     *
     * @param mod The mod this object is displaying data for
     */
    public ModsJCheckBox(Mod mod) {
        super(mod.getName());
        if (mod.hasColour() && mod.getCompiledColour() != null) {
            setForeground(mod.getCompiledColour());
        }
        this.mod = mod;
        if (mod.hasDescription()) {
            this.setToolTipText("<html>" + Utils.splitMultilinedString(mod.getDescription(), 100, "<br/>") + "</html>");
        }
    }

    /**
     * Constructor for use in the {@link EditModsDialog} dialog.
     *
     * @param mod The mod this object is displaying data for
     */
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

    /**
     * Gets the {@link Mod} object associated with this.
     *
     * @return The mod for this object
     */
    public Mod getMod() {
        return (Mod) this.mod;
    }

    /**
     * Gets the {@link DisableableMod} object associated with this.
     *
     * @return The mod for this object
     */
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
