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
