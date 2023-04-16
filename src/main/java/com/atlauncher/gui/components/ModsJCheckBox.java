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

import javax.swing.JCheckBox;
import javax.swing.JToolTip;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.HoverLineBorder;
import com.atlauncher.gui.dialogs.ModsChooser;

/**
 * This class extends {@link JCheckBox} and overrides the need to use JCheckBox
 * in the {@link ModsChooser}, {@link ModsChooser} and {@link EditModsDialog},
 * providing specific functionality for those two components. Mainly providing a
 * hover tooltip for a mods description, as well as giving pack developers a way
 * to colour mod's names.
 */
@SuppressWarnings("serial")
public class ModsJCheckBox extends JCheckBox {
    /**
     * The mod this object will use to display it's data. Will be type {@link Mod}.
     */
    private final Mod mod;

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
            this.setToolTipText(new HTMLBuilder().text(mod.getDescription()).split(100).build());
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

    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBorder(new HoverLineBorder());
        return tip;
    }

}
