package com.atlauncher.gui;

import javax.swing.JCheckBox;

import com.atlauncher.data.Mod;

public class ModsJCheckBox extends JCheckBox {

    private Mod mod;

    public ModsJCheckBox(Mod mod) {
        super(mod.getName());
        if(mod.hasColour()){
            setForeground(mod.getColour());
        }
        this.mod = mod;
    }

    public Mod getMod() {
        return this.mod;
    }

}
