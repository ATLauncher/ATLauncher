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
    private Color dropDownSelectionColour;

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
    private Color consoleTextColour;
    private Color logInfoTextColour;
    private Color logWarnTextColour;
    private Color logErrorTextColour;

    /**
     * The default font used in the Launcher pretty much everywhere
     */
    private Font defaultFont;

    /**
     * The font to use in the Console frame
     */
    private Font consoleFont;

    /**
     * The font used to display the text on the tabs in the main frame (News, Packs, Instances etc)
     */
    private Font tabFont;

    /**
     * The font used to display the text on the tabs in the settings frame
     */
    private Font settingsTabFont;

    private Font buttonFont;

    /**
     * If the tabs should display in their default position of on the Right or not
     */
    private boolean showTabsOnRight;

    public Theme(Color baseColour, Color tabBackgroundColour, Color selectionColour,
            Color dropDownSelectionColour, Color buttonColour, Color textColour,
            Color hoverBorderColour, Color modSelectionBackgroundColour,
            Color modInfoQuestionMarkColour, Color normalInstanceTextColour,
            Color corruptedInstanceTextColour, Color consoleTextColour, Color logInfoTextColour,
            Color logWarnTextColour, Color logErrorTextColour, Font defaultFont, Font consoleFont,
            Font tabFont, Font settingsTabFont, Font buttonFont, boolean showTabsOnRight) {
        this.baseColour = baseColour;
        this.tabBackgroundColour = tabBackgroundColour;
        this.selectionColour = selectionColour;
        this.dropDownSelectionColour = dropDownSelectionColour;
        this.buttonColour = buttonColour;
        this.textColour = textColour;
        this.hoverBorderColour = hoverBorderColour;
        this.modSelectionBackgroundColour = modSelectionBackgroundColour;
        this.modInfoQuestionMarkColour = modInfoQuestionMarkColour;
        this.normalInstanceTextColour = normalInstanceTextColour;
        this.corruptedInstanceTextColour = corruptedInstanceTextColour;
        this.consoleTextColour = consoleTextColour;
        this.logInfoTextColour = logInfoTextColour;
        this.logWarnTextColour = logWarnTextColour;
        this.logErrorTextColour = logErrorTextColour;
        this.defaultFont = defaultFont;
        this.consoleFont = consoleFont;
        this.tabFont = tabFont;
        this.settingsTabFont = settingsTabFont;
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

    public Color getDropDownSelectionColour() {
        return this.dropDownSelectionColour;
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

    public Color getConsoleTextColour() {
        return this.consoleTextColour;
    }

    public Color getLogInfoTextColour() {
        return this.logInfoTextColour;
    }

    public String getLogInfoTextColourHTML() {
        return "#" + Integer.toHexString(this.logInfoTextColour.getRed()) + ""
                + Integer.toHexString(this.logInfoTextColour.getGreen()) + ""
                + Integer.toHexString(this.logInfoTextColour.getBlue());
    }

    public Color getLogWarnTextColour() {
        return this.logWarnTextColour;
    }

    public String getLogWarnTextColourHTML() {
        return "#" + Integer.toHexString(this.logWarnTextColour.getRed()) + ""
                + Integer.toHexString(this.logWarnTextColour.getGreen()) + ""
                + Integer.toHexString(this.logWarnTextColour.getBlue());
    }

    public Color getLogErrorTextColour() {
        return this.logErrorTextColour;
    }

    public String getLogErrorTextColourHTML() {
        return "#" + Integer.toHexString(this.logErrorTextColour.getRed()) + ""
                + Integer.toHexString(this.logErrorTextColour.getGreen()) + ""
                + Integer.toHexString(this.logErrorTextColour.getBlue());
    }

    public Font getDefaultFont() {
        return this.defaultFont;
    }

    public Font getConsoleFont() {
        return this.consoleFont;
    }

    public Font getTabsFont() {
        return this.tabFont;
    }

    public Font getSettingsTabsFont() {
        return this.settingsTabFont;
    }

    public Font getButtonFont() {
        return this.buttonFont;
    }

    public boolean showTabsOnRight() {
        return this.showTabsOnRight;
    }

}
