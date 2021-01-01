/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.constants;

import java.awt.Insets;

public class UIConstants {
    public static final int SPACING_SMALL = 3;
    public static final int SPACING_LARGE = 5;

    public static final Insets LABEL_INSETS = new Insets(SPACING_LARGE, 0, SPACING_LARGE, SPACING_LARGE * 2);
    public static final Insets FIELD_INSETS = new Insets(SPACING_LARGE, 0, SPACING_LARGE, 0);

    public static final Insets LABEL_INSETS_SMALL = new Insets(SPACING_SMALL, 0, SPACING_SMALL, SPACING_LARGE * 2);
    public static final Insets FIELD_INSETS_SMALL = new Insets(SPACING_SMALL, 0, SPACING_SMALL, 0);

    // CheckBoxes has 4 margin on it, so we negate that here so it aligns up without
    // the need to remove that margin from all CheckBox components
    public static final Insets CHECKBOX_FIELD_INSETS = new Insets(SPACING_LARGE, -SPACING_SMALL, SPACING_LARGE, 0);
    public static final Insets CHECKBOX_FIELD_INSETS_SMALL = new Insets(SPACING_SMALL, -SPACING_SMALL, SPACING_SMALL, 0);
}
