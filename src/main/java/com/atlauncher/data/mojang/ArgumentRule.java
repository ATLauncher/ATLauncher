/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.data.mojang;

import java.util.List;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;

@Json
public class ArgumentRule {
    private List<Rule> rules;
    private Object value;

    public List<Rule> getRules() {
        return this.rules;
    }

    public Object getValue() {
        return this.value;
    }

    public Boolean applies() {
        if (this.rules == null) {
            return true; // No rules setup so we need it
        }

        Action lastAction = Action.DISALLOW;

        for (Rule rule : this.rules) { // Loop through all the rules
            if (rule.ruleApplies()) { // See if this rule applies to this system
                lastAction = rule.getAction();
            }
        }

        return (lastAction == Action.ALLOW); // Check if we should use this argument it
    }
}
