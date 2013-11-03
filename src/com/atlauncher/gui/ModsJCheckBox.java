/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import javax.swing.JCheckBox;

import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Mod;

public class ModsJCheckBox extends JCheckBox {

    private Object mod;

    public ModsJCheckBox(Mod mod) {
        super(mod.getName());
        if (mod.hasColour()) {
            setForeground(mod.getColour());
        }
        this.mod = mod;
    }

    public ModsJCheckBox(DisableableMod mod) {
        super(mod.getName());
        if (mod.hasColour()) {
            setForeground(mod.getColour());
        }
        this.mod = mod;
    }

    public Mod getMod() {
        return (Mod) this.mod;
    }

    public DisableableMod getDisableableMod() {
        return (DisableableMod) this.mod;
    }

}
