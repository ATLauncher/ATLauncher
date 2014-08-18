package com.atlauncher.gui.theme;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.UIManager;

import com.atlauncher.utils.Resources;
import com.atlauncher.utils.Utils;

public final class Theme {
    public static final Theme DEFAULT_THEME = new Theme("ATLauncher", "RyanTheAllmighty", true,
            new Color(40, 45, 50), new Color(255, 255, 255), new Color(0, 0, 0), new Color(0, 136,
                    204), new Color(100, 100, 200), new Color(80, 170, 107), new Color(50, 55, 60),
            new Color(50, 55, 60), new Color(30, 35, 40), new Color(255, 255, 255), new Color(255,
                    0, 0), new Color(255, 255, 255), new Color(137, 194, 54), new Color(255, 255,
                    76), new Color(238, 34, 34), new Color(255, 0, 255), "SansSerif", "SansSerif",
            "Oswald-Regular", "SansSerif");

    // Meta
    private final String name;
    private final String author;

    // Flags
    private final boolean tabsOnRight;

    // Colors
    private final Color baseColor;
    private final Color textColor;
    private final Color buttonColor;
    private final Color selectionColor;
    private final Color dropdownSelectionColor;
    private final Color hoverBorderColor;
    private final Color modSelectionBGColor;
    private final Color modInfoColor;
    private final Color tabBackgroundColor;
    private final Color normalInstanceColor;
    private final Color corruptedInstanceColor;
    private final Color consoleTextColor;
    private final Color logInfoColor;
    private final Color logWarnColor;
    private final Color logErrorColor;
    private final Color logDebugColor;

    // Fonts
    private final String defaultFont, consoleFont, tabFont, buttonFont;

    private Theme(String name, String author, boolean tabsOnRight, Color baseColor,
            Color textColor, Color buttonColor, Color selectionColor, Color dropdownSelectionColor,
            Color hoverBorderColor, Color modSelectionBGColor, Color modInfoColor,
            Color tabBackgroundColor, Color normalInstanceColor, Color corruptedInstanceColor,
            Color consoleTextColor, Color logInfoTextColor, Color logWarnColor,
            Color logErrorColor, Color logDebugColor, String defaultFont, String consoleFont,
            String tabFont, String buttonFont) {
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
            UIManager.put("Table.focusCellHighlightBorder",
                    BorderFactory.createEmptyBorder(2, 5, 2, 5));
            UIManager.put("defaultFont",
                    Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Button.font",
                    Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Toaster.font",
                    Resources.makeFont(this.defaultFont).deriveFont(Utils.getBaseFontSize()));
            UIManager.put("Toaster.bgColor", this.tabBackgroundColor);
            UIManager.put("Toaster.msgColor", this.consoleTextColor);
            UIManager.put("Toaster.borderColor", this.hoverBorderColor);
            UIManager.put("Toaster.opacity", 0.75F);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Font getDefaultFont() {
        if (this.defaultFont == null) {
            return DEFAULT_THEME.getDefaultFont();
        }
        return Resources.makeFont(this.defaultFont);
    }

    public Font getConsoleFont() {
        if (this.consoleFont == null) {
            return DEFAULT_THEME.getConsoleFont();
        }
        return Resources.makeFont(this.consoleFont);
    }

    public Font getTabFont() {
        if (this.tabFont == null) {
            return DEFAULT_THEME.getTabFont();
        }
        return Resources.makeFont(this.tabFont);
    }

    public Font getButtonFont() {
        if (this.buttonFont == null) {
            return DEFAULT_THEME.getButtonFont();
        }
        return Resources.makeFont(this.buttonFont);
    }

    public boolean tabsOnRight() {
        return this.tabsOnRight;
    }

    public Color getConsoleTextColor() {
        if (this.consoleTextColor == null) {
            return DEFAULT_THEME.getConsoleTextColor();
        }
        return this.consoleTextColor;
    }

    public Color getSelectionColor() {
        if (this.selectionColor == null) {
            return DEFAULT_THEME.getSelectionColor();
        }
        return this.selectionColor;
    }

    public Color getHoverBorderColor() {
        if (this.hoverBorderColor == null) {
            return DEFAULT_THEME.getHoverBorderColor();
        }
        return this.hoverBorderColor;
    }

    public Color getModInfoColor() {
        if (this.modInfoColor == null) {
            return DEFAULT_THEME.getModInfoColor();
        }
        return this.modInfoColor;
    }

    public Color getBaseColor() {
        if (this.baseColor == null) {
            return DEFAULT_THEME.getBaseColor();
        }
        return this.baseColor;
    }

    public Color getCorruptedInstanceColor() {
        if (this.corruptedInstanceColor == null) {
            return DEFAULT_THEME.getCorruptedInstanceColor();
        }
        return this.corruptedInstanceColor;
    }

    public Color getNormalInstanceColor() {
        if (this.normalInstanceColor == null) {
            return DEFAULT_THEME.getNormalInstanceColor();
        }
        return this.normalInstanceColor;
    }

    public Color getModSelectionBackgroundColor() {
        if (this.modSelectionBGColor == null) {
            return DEFAULT_THEME.getModSelectionBackgroundColor();
        }
        return this.modSelectionBGColor;
    }

    public Color getTabBackgroundColor() {
        if (this.tabBackgroundColor == null) {
            return DEFAULT_THEME.getTabBackgroundColor();
        }
        return this.tabBackgroundColor;
    }

    public Color getLogInfoColor() {
        if (this.logInfoColor == null) {
            return DEFAULT_THEME.getLogInfoColor();
        }
        return this.logInfoColor;
    }

    public Color getLogErrorColor() {
        if (this.logErrorColor == null) {
            return DEFAULT_THEME.getLogErrorColor();
        }
        return this.logErrorColor;
    }

    public Color getLogWarnColor() {
        if (this.logWarnColor == null) {
            return DEFAULT_THEME.getLogWarnColor();
        }
        return this.logWarnColor;
    }

    public Color getLogDebugColor() {
        if (this.logDebugColor == null) {
            return DEFAULT_THEME.getLogDebugColor();
        }
        return this.logDebugColor;
    }

    @Override
    public String toString() {
        if (this.name == null || this.author == null) {
            return "Unknown by Unknown";
        }
        return this.name + " by " + this.author;
    }
}