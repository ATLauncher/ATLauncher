/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Mod;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.JsonModsChooser;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JToolTip;
import javax.swing.border.Border;

/**
 * This class extends {@link JCheckBox} and overrides the need to use JCheckBox in the
 * {@link ModsChooser}, {@link JsonModsChooser} and {@link EditModsDialog}, providing specific
 * functionality for those two components. Mainly providing a hover tooltip for a mods description,
 * as well as giving pack developers a way to colour mod's names. Alternatively can be used to
 * display categories.
 */
public class ModsJCheckBox extends JCheckBox{
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -4560260483416099547L;

    /**
     * The mod this object will use to display it's data. Will be type {@link Mod},
     * {@link com.atlauncher.data.json.Mod} or {@link DisableableMod}.
     */
    private Object mod;

    /**
     * If this object is classed as a category or not.
     */
    private boolean isCategory = false;

    /**
     * The name of the category this object represents, if any.
     */
    private String categoryName = null;

    /**
     * Static object for the {@link Border} to show around the tooltips for mods with descriptions.
     */
    private static final Border HOVER_BORDER = new CustomLineBorder(5,
            App.THEME.getHoverBorderColor(), 2);

    /**
     * Constructor for use in the {@link ModsChooser} dialog.
     *
     * @param mod The mod this object is displaying data for
     */
    public ModsJCheckBox(Mod mod){
        super(mod.getName());
        if(mod.hasColour()){
            setForeground(mod.getColour());
        }
        this.mod = mod;
        if(mod.getDescription() != null && !mod.getDescription().isEmpty()){
            this.setToolTipText("<html>"
                    + Utils.splitMultilinedString(mod.getDescription(), 100, "<br/>") + "</html>");
        }
    }

    /**
     * Constructor for use in the {@link ModsChooser} dialog with new JSON format.
     *
     * @param mod The mod this object is displaying data for
     */
    public ModsJCheckBox(com.atlauncher.data.json.Mod mod){
        super(mod.getName());
        if(mod.hasColour() && mod.getCompiledColour() != null){
            setForeground(mod.getCompiledColour());
        }
        this.mod = mod;
        if(mod.hasDescription()){
            this.setToolTipText("<html>"
                    + Utils.splitMultilinedString(mod.getDescription(), 100, "<br/>") + "</html>");
        }
    }

    /**
     * Constructor for use in the {@link EditModsDialog} dialog.
     *
     * @param mod The mod this object is displaying data for
     */
    public ModsJCheckBox(DisableableMod mod){
        super(mod.getName());
        if(mod.hasColour()){
            setForeground(mod.getColour());
        }
        this.mod = mod;
        if(mod.getDescription() != null && !mod.getDescription().isEmpty()){
            this.setToolTipText(mod.getDescription());
        }
    }

    /**
     * Constructor used for displaying categories in the {@link ModsChooser} dialog.
     *
     * @param categoryName The name of the category to show
     */
    public ModsJCheckBox(String categoryName){
        super(categoryName);
        this.isCategory = true;
    }

    /**
     * Checks if this object is a category or not.
     *
     * @return true if this object represents a category
     */
    public boolean isCategory(){
        return this.isCategory;
    }

    /**
     * Gets the categories name.
     *
     * @return The categories name
     */
    public String getCategoryName(){
        return this.categoryName;
    }

    /**
     * Gets the {@link Mod} object associated with this.
     *
     * @return The mod for this object
     */
    public Mod getMod(){
        return (Mod) this.mod;
    }

    /**
     * Gets the {@link com.atlauncher.data.json.Mod} object associated with this.
     *
     * @return The mod for this object
     */
    public com.atlauncher.data.json.Mod getJsonMod(){
        return (com.atlauncher.data.json.Mod) this.mod;
    }

    /**
     * Gets the {@link DisableableMod} object associated with this.
     *
     * @return The mod for this object
     */
    public DisableableMod getDisableableMod(){
        return (DisableableMod) this.mod;
    }

    @Override
    public JToolTip createToolTip(){
        JToolTip tip = super.createToolTip();
        tip.setBorder(HOVER_BORDER);
        return tip;
    }

}
