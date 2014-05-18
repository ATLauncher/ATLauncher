/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.theme;

public class LoadableTheme {

    private String THEME_NAME;
    private String AUTHORS_NAME;

    private int[] BASE_COLOUR;
    private int[] TAB_BACKGROUND_COLOUR;
    private int[] SELECTION_COLOUR;
    private int[] DROP_DOWN_SELECTION_COLOUR;
    private int[] BUTTON_COLOUR;
    private int[] TEXT_COLOUR;
    private int[] HOVER_BORDER_COLOUR;
    private int[] MOD_SELECTION_BACKGROUND_COLOUR;
    private int[] MOD_INFO_QUESTION_MARK_COLOUR;
    private int[] NORMAL_INSTANCE_TEXT_COLOUR;
    private int[] CORRUPTED_INSTANCE_TEXT_COLOUR;
    private int[] CONSOLE_TEXT_COLOUR;
    private int[] LOG_INFO_COLOUR;
    private int[] LOG_WARN_COLOUR;
    private int[] LOG_ERROR_COLOUR;

    private String DEFAULT_FONT;
    private String CONSOLE_FONT;
    private String TAB_FONT;
    private String SETTINGS_TAB_FONT;
    private String BUTTON_FONT;

    private boolean SHOW_TABS_ON_RIGHT;

    public Theme createTheme() {
        return new Theme(THEME_NAME, AUTHORS_NAME, BASE_COLOUR, TAB_BACKGROUND_COLOUR, SELECTION_COLOUR,
                DROP_DOWN_SELECTION_COLOUR, BUTTON_COLOUR, TEXT_COLOUR, HOVER_BORDER_COLOUR,
                MOD_SELECTION_BACKGROUND_COLOUR, MOD_INFO_QUESTION_MARK_COLOUR,
                NORMAL_INSTANCE_TEXT_COLOUR, CORRUPTED_INSTANCE_TEXT_COLOUR, CONSOLE_TEXT_COLOUR,
                LOG_INFO_COLOUR, LOG_WARN_COLOUR, LOG_ERROR_COLOUR, DEFAULT_FONT, CONSOLE_FONT,
                TAB_FONT, SETTINGS_TAB_FONT, BUTTON_FONT, SHOW_TABS_ON_RIGHT);
    }

}
