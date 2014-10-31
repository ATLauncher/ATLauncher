/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.utils;

import com.atlauncher.App;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Timestamper {
    private static final SimpleDateFormat format = new SimpleDateFormat(App.settings.getDateFormat() + " HH:mm:ss a");

    public static String now() {
        return format.format(new Date());
    }

    public static String was(Date date) {
        return format.format(date);
    }

    public static void updateDateFormat() {
        format.applyLocalizedPattern(App.settings.getDateFormat() + " HH:mm:ss a");
    }
}