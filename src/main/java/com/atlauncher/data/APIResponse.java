/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import com.atlauncher.App;

public class APIResponse {
    private boolean error;
    private int code;
    private String message;

    public boolean wasError() {
        return this.error;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public int getMessageAsInt() {
        try {
            return Integer.parseInt(this.message);
        } catch (NumberFormatException e) {
            App.settings.logStackTrace(e);
            return 0;
        }
    }
}
