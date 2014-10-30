/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
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
