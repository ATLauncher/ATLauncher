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
package com.atlauncher.gui.theme;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;

import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.utils.Resources;
import com.atlauncher.utils.Utils;

public final class Theme {
    /**
     * This is the default theme used by the launcher incase all else fails and it cannot read the theme from disk.
     */
    public static final Theme DEFAULT_THEME = new Theme(Constants.LAUNCHER_NAME, "RyanTheAllmighty", true, new Color(40, 45, 50)
            , new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 136, 204), new Color(100, 100, 200), new
            Color(80, 170, 107), new Color(50, 55, 60), new Color(50, 55, 60), new Color(30, 35, 40), new Color(255,
            255, 255), new Color(255, 0, 0), new Color(255, 255, 255), new Color(137, 194, 54), new Color(255, 255,
            76), new Color(238, 34, 34), new Color(255, 0, 255), "SansSerif", "SansSerif", "Oswald-Regular",
            "SansSerif");

    /**
     * This is the name of the theme.
     */
    private final String name;

    /**
     * This is the themes authors name.
     */
    private final String author;

    /**
     * This determines if the tabs should be shown on the right or not. If false the tabs will show on the left hand
     * side of the launcher rather than the default of being on the right hand side of the launcher.
     */
    private final boolean tabsOnRight;

    /**
     * This is the base colour.
     */
    private final Color baseColor;

    /**
     * This is the text colour.
     */
    private final Color textColor;

    /**
     * This is the button colour.
     */
    private final Color buttonColor;

    /**
     * This is the selection colour.
     */
    private final Color selectionColor;

    /**
     * This is the dropdown selection colour.
     */
    private final Color dropdownSelectionColor;

    /**
     * This is the hover boarder colour.
     */
    private final Color hoverBorderColor;

    /**
     * This is the mod selection background colour.
     */
    private final Color modSelectionBGColor;

    /**
     * This is the mod info colour.
     */
    private final Color modInfoColor;

    /**
     * This is the tab background colour.
     */
    private final Color tabBackgroundColor;

    /**
     * This is the normal instance colour.
     */
    private final Color normalInstanceColor;

    /**
     * This is the corrupted instance colour.
     */
    private final Color corruptedInstanceColor;

    /**
     * This is the console text colour.
     */
    private final Color consoleTextColor;

    /**
     * This is the console log info colour.
     */
    private final Color logInfoColor;

    /**
     * This is the console log warning colour.
     */
    private final Color logWarnColor;

    /**
     * This is the console log error colour.
     */
    private final Color logErrorColor;

    /**
     * This is the console log debug colour.
     */
    private final Color logDebugColor;

    /**
     * This is the default font.
     */
    private final String defaultFont;

    /**
     * This is the console font.
     */
    private final String consoleFont;

    /**
     * This is the tab font.
     */
    private final String tabFont;

    /**
     * This is the button font.
     */
    private final String buttonFont;

    /**
     * This is the constructor which takes in all the values for a theme. Used in creating the default theme.
     * @param name the name of the theme
     * @param author the author of the theme
     * @param tabsOnRight if tabs should be displayed on the right of the launcher
     * @param baseColor the base colour
     * @param textColor the text colour
     * @param buttonColor the button colour
     * @param selectionColor the selection colour
     * @param dropdownSelectionColor the dropdown selection colour
     * @param hoverBorderColor the hover boarder colour
     * @param modSelectionBGColor the mod selection background colour
     * @param modInfoColor the mod info colour
     * @param tabBackgroundColor the tab background colour
     * @param normalInstanceColor the normal instance colour
     * @param corruptedInstanceColor the corrupted instance colour
     * @param consoleTextColor the console text colour
     * @param logInfoTextColor the console log info colour
     * @param logWarnColor the console log warning colour
     * @param logErrorColor the console log error colour
     * @param logDebugColor the console log debug colour
     * @param defaultFont the default font
     * @param consoleFont the console font
     * @param tabFont the tab font
     * @param buttonFont the button font
     */
    private Theme(String name, String author, boolean tabsOnRight, Color baseColor, Color textColor, Color
            buttonColor, Color selectionColor, Color dropdownSelectionColor, Color hoverBorderColor, Color
            modSelectionBGColor, Color modInfoColor, Color tabBackgroundColor, Color normalInstanceColor, Color
            corruptedInstanceColor, Color consoleTextColor, Color logInfoTextColor, Color logWarnColor, Color
            logErrorColor, Color logDebugColor, String defaultFont, String consoleFont, String tabFont, String
            buttonFont) {

        this.name = name;
        this.author = author;
        this.tabsOnRight = tabsOnRight;
        this.baseColor = baseColor;
        this.textColor = textColor;
        this.buttonColor = buttonColor;
        this.selectionColor = selectionColor;
        this.dropdownSelectionColor = dropdownSelectionColor;
        this.hoverBorderColor = hoverBorderColor;
        this.modSelectionBGColor = modSelectionBGColor;
        this.modInfoColor = modInfoColor;
        this.tabBackgroundColor = tabBackgroundColor;
        this.normalInstanceColor = normalInstanceColor;
        this.corruptedInstanceColor = corruptedInstanceColor;
        this.consoleTextColor = consoleTextColor;
        this.logInfoColor = logInfoTextColor;
        this.logWarnColor = logWarnColor;
        this.logErrorColor = logErrorColor;
        this.logDebugColor = logDebugColor;
        this.defaultFont = defaultFont;
        this.consoleFont = consoleFont;
        this.tabFont = tabFont;
        this.buttonFont = buttonFont;
    }

    /**
     * Apply the themes values to the UIManager.
     */
    public void apply() {
        try {
            UIManager.put("control", this.baseColor);
            UIManager.put("text", this.textColor);
            UIManager.put("nimbusBase", this.buttonColor);
            UIManager.put("nimbusFocus", this.baseColor);
            UIManager.put("nimbusBorder", this.baseColor);
            UIManager.put("nimbusLightBackground", this.baseColor);
            UIManager.put("info", this.baseColor);
            UIManager.put("nimbusSelectionBackground", this.dropdownSelectionColor);
            UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(2, 5, 2, 5));
            UIManager.put("defaultFont", Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Button.font", Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Toaster.font", Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Toaster.bgColor", this.tabBackgroundColor);
            UIManager.put("Toaster.msgColor", this.consoleTextColor);
            UIManager.put("Toaster.borderColor", this.hoverBorderColor);
            UIManager.put("Toaster.opacity", 0.75F);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the default font.
     *
     * @return the default font
     */
    public Font getDefaultFont() {
        if (this.defaultFont == null) {
            LogManager.error("The default font for the theme you're using is corrupt!");
            return DEFAULT_THEME.getDefaultFont();
        }
        return Resources.makeFont(this.defaultFont);
    }

    /**
     * Gets the console font.
     *
     * @return the console font
     */
    public Font getConsoleFont() {
        if (this.consoleFont == null) {
            LogManager.error("The console font for the theme you're using is corrupt!");
            return DEFAULT_THEME.getConsoleFont();
        }
        return Resources.makeFont(this.consoleFont);
    }

    /**
     * Gets the tab font.
     *
     * @return the tab font
     */
    public Font getTabFont() {
        if (this.tabFont == null) {
            LogManager.error("The tab font for the theme you're using is corrupt!");
            return DEFAULT_THEME.getTabFont();
        }
        return Resources.makeFont(this.tabFont);
    }

    /**
     * Gets the button font.
     *
     * @return the button font
     */
    public Font getButtonFont() {
        if (this.buttonFont == null) {
            LogManager.error("The button font for the theme you're using is corrupt!");
            return DEFAULT_THEME.getButtonFont();
        }
        return Resources.makeFont(this.buttonFont);
    }

    /**
     * Gets if the tabs on the launcher should be shown on the right or not.
     *
     * @return true if the tabs should show on the right or false if they should show on the left
     */
    public boolean tabsOnRight() {
        return this.tabsOnRight;
    }

    /**
     * Gets the console text colour.
     *
     * @return console text colour
     */
    public Color getConsoleTextColor() {
        if (this.consoleTextColor == null) {
            LogManager.error("The console text colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getConsoleTextColor();
        }
        return this.consoleTextColor;
    }

    /**
     * Gets the selection colour.
     *
     * @return selection colour
     */
    public Color getSelectionColor() {
        if (this.selectionColor == null) {
            LogManager.error("The selection colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getSelectionColor();
        }
        return this.selectionColor;
    }

    /**
     * Gets the hover border colour.
     *
     * @return hover border colour
     */
    public Color getHoverBorderColor() {
        if (this.hoverBorderColor == null) {
            LogManager.error("The border hover colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getHoverBorderColor();
        }
        return this.hoverBorderColor;
    }

    /**
     * Gets the mod info colour.
     *
     * @return mod info colour
     */
    public Color getModInfoColor() {
        if (this.modInfoColor == null) {
            LogManager.error("The mod info colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getModInfoColor();
        }
        return this.modInfoColor;
    }

    /**
     * Gets the base colour.
     *
     * @return base colour
     */
    public Color getBaseColor() {
        if (this.baseColor == null) {
            LogManager.error("The base colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getBaseColor();
        }
        return this.baseColor;
    }

    /**
     * Gets the corrupted instance colour.
     *
     * @return corrupted instance colour
     */
    public Color getCorruptedInstanceColor() {
        if (this.corruptedInstanceColor == null) {
            LogManager.error("The corrupted instance text colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getCorruptedInstanceColor();
        }
        return this.corruptedInstanceColor;
    }

    /**
     * Gets the normal instance colour.
     *
     * @return normal instance colour
     */
    public Color getNormalInstanceColor() {
        if (this.normalInstanceColor == null) {
            LogManager.error("The normal instance text colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getNormalInstanceColor();
        }
        return this.normalInstanceColor;
    }

    /**
     * Gets the mod selection background colour.
     *
     * @return mod selection background colour
     */
    public Color getModSelectionBackgroundColor() {
        if (this.modSelectionBGColor == null) {
            LogManager.error("The mod selection background colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getModSelectionBackgroundColor();
        }
        return this.modSelectionBGColor;
    }

    /**
     * Gets the tab background colour.
     *
     * @return tab background colour
     */
    public Color getTabBackgroundColor() {
        if (this.tabBackgroundColor == null) {
            LogManager.error("The tab background colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getTabBackgroundColor();
        }
        return this.tabBackgroundColor;
    }

    /**
     * Gets the log info colour.
     *
     * @return log info colour
     */
    public Color getLogInfoColor() {
        if (this.logInfoColor == null) {
            LogManager.error("The log info colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getLogInfoColor();
        }
        return this.logInfoColor;
    }

    /**
     * Gets the log error colour.
     *
     * @return log error colour
     */
    public Color getLogErrorColor() {
        if (this.logErrorColor == null) {
            LogManager.error("The log error colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getLogErrorColor();
        }
        return this.logErrorColor;
    }

    /**
     * Gets the log warning colour.
     *
     * @return log warning colour
     */
    public Color getLogWarnColor() {
        if (this.logWarnColor == null) {
            LogManager.error("The log warning colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getLogWarnColor();
        }
        return this.logWarnColor;
    }

    /**
     * Gets the log debug colour.
     *
     * @return log debug colour
     */
    public Color getLogDebugColor() {
        if (this.logDebugColor == null) {
            LogManager.error("The log debug colour for the theme you're using is corrupt!");
            return DEFAULT_THEME.getLogDebugColor();
        }
        return this.logDebugColor;
    }

    /**
     * Returns the name and author of this theme.
     *
     * @return the name and author of this theme
     */
    @Override
    public String toString() {
        if (this.name == null || this.name.isEmpty() || this.author == null || this.author.isEmpty()) {
            LogManager.error("The name and/or author for the theme you're using is corrupt!");
            return "Unknown by Unknown";
        }
        return this.name + " by " + this.author;
    }
}
