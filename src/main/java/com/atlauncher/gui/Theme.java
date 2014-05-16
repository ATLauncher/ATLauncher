/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.Color;
import java.awt.Font;

public class Theme {

    private Color baseColour;
    private Color tabBackgroundColour;
    private Color selectionColour;

    /**
     * The Colour used for buttons, checkboxes, tabs, scrollbars and closed dropdown boxes
     */
    private Color buttonColour;
    private Color textColour;
    private Color hoverBorderColour;
    private Color modSelectionBackgroundColour;
    private Color modInfoQuestionMarkColour;
    private Color normalInstanceTextColour;
    private Color corruptedInstanceTextColour;

    /**
     * The default font used in the Launcher pretty much everywhere
     */
    private Font defaultFont;

    /**
     * The font used to display the text on the tabs in the main frame (News, Packs, Instances etc)
     */
    private Font tabFont;

    private Font buttonFont;

    /**
     * If the tabs should display in their default position of on the Right or not
     */
    private boolean showTabsOnRight;

    public Theme(Color baseColour, Color tabBackgroundColour, Color selectionColour,
            Color buttonColour, Color textColour, Color hoverBorderColour,
            Color modSelectionBackgroundColour, Color modInfoQuestionMarkColour,
            Color normalInstanceTextColour, Color corruptedInstanceTextColour, Font defaultFont,
            Font tabFont, Font buttonFont, boolean showTabsOnRight) {
        this.baseColour = baseColour;
        this.tabBackgroundColour = tabBackgroundColour;
        this.selectionColour = selectionColour;
        this.buttonColour = buttonColour;
        this.textColour = textColour;
        this.hoverBorderColour = hoverBorderColour;
        this.modSelectionBackgroundColour = modSelectionBackgroundColour;
        this.modInfoQuestionMarkColour = modInfoQuestionMarkColour;
        this.normalInstanceTextColour = normalInstanceTextColour;
        this.corruptedInstanceTextColour = corruptedInstanceTextColour;
        this.defaultFont = defaultFont;
        this.tabFont = tabFont;
        this.buttonFont = buttonFont;
        this.showTabsOnRight = showTabsOnRight;
    }

    public Color getBaseColour() {
        return this.baseColour;
    }

    public Color getTabBackgroundColour() {
        return this.tabBackgroundColour;
    }

    public Color getSelectionColour() {
        return this.selectionColour;
    }

    public Color getButtonColour() {
        return this.buttonColour;
    }

    public Color getTextColour() {
        return this.textColour;
    }

    public Color getHoverBorderColour() {
        return this.hoverBorderColour;
    }

    public Color getModSelectionBackgroundColour() {
        return this.modSelectionBackgroundColour;
    }

    public Color getModInfoQuestionMarkColour() {
        return this.modInfoQuestionMarkColour;
    }

    public String getModInfoQuestionMarkColourHTML() {
        return "#" + Integer.toHexString(this.modInfoQuestionMarkColour.getRed()) + ""
                + Integer.toHexString(this.modInfoQuestionMarkColour.getGreen()) + ""
                + Integer.toHexString(this.modInfoQuestionMarkColour.getBlue());
    }

    public Color getNormalInstanceTextColour() {
        return this.normalInstanceTextColour;
    }

    public Color getCorruptedInstanceTextColour() {
        return this.corruptedInstanceTextColour;
    }

    public Font getDefaultFont() {
        return this.defaultFont;
    }

    public Font getTabsFont() {
        return this.tabFont;
    }

    public Font getButtonFont() {
        return this.buttonFont;
    }

    public boolean showTabsOnRight() {
        return this.showTabsOnRight;
    }

}
