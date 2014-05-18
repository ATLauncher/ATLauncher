/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.theme;

public class DefaultTheme {

    private final String THEME_NAME = "ATLauncher";
    private final String AUTHORS_NAME = "RyanTheAllmighty";

    private final int[] BASE_COLOUR = { 40, 45, 50 };
    private final int[] TAB_BACKGROUND_COLOUR = { 30, 35, 40 };
    private final int[] SELECTION_COLOUR = { 0, 136, 204 };
    private final int[] DROP_DOWN_SELECTION_COLOUR = { 100, 100, 200 };
    private final int[] BUTTON_COLOUR = { 0, 0, 0 };
    private final int[] TEXT_COLOUR = { 255, 255, 255 };
    private final int[] HOVER_BORDER_COLOUR = { 80, 170, 107 };
    private final int[] MOD_SELECTION_BACKGROUND_COLOUR = { 50, 55, 60 };
    private final int[] MOD_INFO_QUESTION_MARK_COLOUR = { 50, 55, 60 };
    private final int[] NORMAL_INSTANCE_TEXT_COLOUR = { 255, 255, 255 };
    private final int[] CORRUPTED_INSTANCE_TEXT_COLOUR = { 255, 0, 0 };
    private final int[] CONSOLE_TEXT_COLOUR = { 255, 255, 255 };
    private final int[] LOG_INFO_COLOUR = { 137, 194, 54 };
    private final int[] LOG_WARN_COLOUR = { 255, 255, 76 };
    private final int[] LOG_ERROR_COLOUR = { 238, 34, 34 };

    private final String DEFAULT_FONT = "SansSerif";
    private final String CONSOLE_FONT = "SansSerif";
    private final String TAB_FONT = "Oswald-Regular";
    private final String SETTINGS_TAB_FONT = "Oswald-Regular";
    private final String BUTTON_FONT = "SansSerif";

    private final boolean SHOW_TABS_ON_RIGHT = true;

    public Theme createTheme() {
        return new Theme(THEME_NAME, AUTHORS_NAME, BASE_COLOUR, TAB_BACKGROUND_COLOUR,
                SELECTION_COLOUR, DROP_DOWN_SELECTION_COLOUR, BUTTON_COLOUR, TEXT_COLOUR,
                HOVER_BORDER_COLOUR, MOD_SELECTION_BACKGROUND_COLOUR,
                MOD_INFO_QUESTION_MARK_COLOUR, NORMAL_INSTANCE_TEXT_COLOUR,
                CORRUPTED_INSTANCE_TEXT_COLOUR, CONSOLE_TEXT_COLOUR, LOG_INFO_COLOUR,
                LOG_WARN_COLOUR, LOG_ERROR_COLOUR, DEFAULT_FONT, CONSOLE_FONT, TAB_FONT,
                SETTINGS_TAB_FONT, BUTTON_FONT, SHOW_TABS_ON_RIGHT);
    }
}
