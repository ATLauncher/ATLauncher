/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.theme;

import java.awt.Color;
import java.awt.Font;

import com.atlauncher.utils.Utils;

public class Theme {

    private String themeName;
    private String authorsName;

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

    public Theme(String themeName, String authorsName, int[] baseColour, int[] tabBackgroundColour,
            int[] selectionColour, int[] dropDownSelectionColour, int[] buttonColour,
            int[] textColour, int[] hoverBorderColour, int[] modSelectionBackgroundColour,
            int[] modInfoQuestionMarkColour, int[] normalInstanceTextColour,
            int[] corruptedInstanceTextColour, int[] consoleTextColour, int[] logInfoTextColour,
            int[] logWarnTextColour, int[] logErrorTextColour, String defaultFont,
            String consoleFont, String tabFont, String settingsTabFont, String buttonFont,
            boolean showTabsOnRight) {
        this.themeName = themeName;
        this.authorsName = authorsName;
        this.baseColour = Utils.getColourFromTheme(baseColour);
        this.tabBackgroundColour = Utils.getColourFromTheme(tabBackgroundColour);
        this.selectionColour = Utils.getColourFromTheme(selectionColour);
        this.dropDownSelectionColour = Utils.getColourFromTheme(dropDownSelectionColour);
        this.buttonColour = Utils.getColourFromTheme(buttonColour);
        this.textColour = Utils.getColourFromTheme(textColour);
        this.hoverBorderColour = Utils.getColourFromTheme(hoverBorderColour);
        this.modSelectionBackgroundColour = Utils.getColourFromTheme(modSelectionBackgroundColour);
        this.modInfoQuestionMarkColour = Utils.getColourFromTheme(modInfoQuestionMarkColour);
        this.normalInstanceTextColour = Utils.getColourFromTheme(normalInstanceTextColour);
        this.corruptedInstanceTextColour = Utils.getColourFromTheme(corruptedInstanceTextColour);
        this.consoleTextColour = Utils.getColourFromTheme(consoleTextColour);
        this.logInfoTextColour = Utils.getColourFromTheme(logInfoTextColour);
        this.logWarnTextColour = Utils.getColourFromTheme(logWarnTextColour);
        this.logErrorTextColour = Utils.getColourFromTheme(logErrorTextColour);
        this.defaultFont = Utils.makeFont(defaultFont).deriveFont(Utils.getBaseFontSize());
        this.consoleFont = Utils.makeFont(consoleFont).deriveFont(Utils.getBaseFontSize());
        this.tabFont = Utils.makeFont(tabFont).deriveFont((float) 34);
        this.settingsTabFont = Utils.makeFont(settingsTabFont).deriveFont((float) 17);
        this.buttonFont = Utils.makeFont(consoleFont).deriveFont(Utils.getBaseFontSize());
        this.showTabsOnRight = showTabsOnRight;
    }

    public String getThemeName() {
        return this.themeName;
    }

    public String getAuthorsName() {
        return this.authorsName;
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
