/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.Color;
import java.awt.Font;

import com.atlauncher.utils.Utils;

public class DefaultTheme extends Theme {

    private static final Color BASE_COLOUR = new Color(40, 45, 50);
    private static final Color SELECTION_COLOUR = new Color(0, 136, 204);
    private static final Color BUTTON_COLOUR = new Color(0, 0, 0);
    private static final Color TEXT_COLOUR = new Color(255, 255, 255);
    private static final Color HOVER_BORDER_COLOUR = new Color(80, 170, 107);
    private static final Color MOD_SELECTION_BACKGROUND_COLOUR = new Color(50, 55, 60);
    private static final Color MOD_INFO_QUESTION_MARK_COLOUR = new Color(50, 55, 60);
    private static final Color NORMAL_INSTANCE_TEXT_COLOUR = new Color(255, 255, 255);
    private static final Color CORRUPTED_INSTANCE_TEXT_COLOUR = new Color(255, 0, 0);
    private static final Font DEFAULT_FONT = Utils.makeFont("SansSerif").deriveFont(Utils.getBaseFontSize());
    private static final Font TABS_FONT = Utils.makeFont("Oswald-Regular").deriveFont((float) 34);

    public DefaultTheme() {
        super(BASE_COLOUR, SELECTION_COLOUR, BUTTON_COLOUR, TEXT_COLOUR, HOVER_BORDER_COLOUR,
                MOD_SELECTION_BACKGROUND_COLOUR, MOD_INFO_QUESTION_MARK_COLOUR,
                NORMAL_INSTANCE_TEXT_COLOUR, CORRUPTED_INSTANCE_TEXT_COLOUR, DEFAULT_FONT,
                TABS_FONT);
    }

}
