package com.atlauncher.gui.theme;

import java.awt.Font;

import com.atlauncher.App;
import com.atlauncher.utils.Resources;

public class ThemeFonts {
    public String normal;
    public String tab;

    public Font getNormalFont() {
        return Resources.makeFont(App.THEME.fonts.normal);
    }

    public Font getTabFont() {
        return Resources.makeFont(App.THEME.fonts.tab);
    }
}
