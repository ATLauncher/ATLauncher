/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

public enum LogMessageType {
    error, warning, info;

    public String getColourCode() {
        switch (this) {
            case info:
            default:
                return Utils.colorHex(App.THEME.getLogInfoColor());
            case warning:
                return Utils.colorHex(App.THEME.getLogWarnColor());
            case error:
                return Utils.colorHex(App.THEME.getLogErrorColor());
        }
    }

    public String getType() {
        switch (this) {
            case info:
            default:
                return "INFO";
            case warning:
                return "WARN";
            case error:
                return "ERROR";
        }
    }
}
