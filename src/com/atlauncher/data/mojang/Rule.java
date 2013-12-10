/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data.mojang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum Action {
    ALLOW, DISALLOW;
}

public class Rule {

    private Action action; // If it should be allowed
    private OperatingSystemRule os; // The OS this rule applies to

    public boolean ruleApplies() {
        if (this.os == null) {
            return true;
        }
        if (this.os.getName() != null && this.os.getName() != OperatingSystem.getOS()) {
            return false;
        }
        if (this.os.getVersion() == null) {
            return true;
        }
        Pattern pattern = Pattern.compile(this.os.getVersion());
        Matcher matcher = pattern.matcher(OperatingSystem.getVersion());
        return matcher.matches();
    }

    public Action getAction() {
        return this.action;
    }
}
