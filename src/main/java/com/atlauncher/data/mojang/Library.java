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

import com.atlauncher.FileSystem;
import com.atlauncher.utils.Utils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Library {

    private String name;
    private Map<OperatingSystem, String> natives;
    private List<Rule> rules;
    private ExtractRule extract;
    private String url;

    public boolean shouldInstall() {
        if (this.rules == null) {
            return true; // No rules setup so we need it
        }
        Action lastAction = Action.DISALLOW;
        for (Rule rule : this.rules) { // Loop through all the rules
            if (rule.ruleApplies()) { // See if this rule applies to this system
                lastAction = rule.getAction();
            }
        }
        return (lastAction == Action.ALLOW); // Check if we are allowing it
    }

    public boolean shouldExtract() {
        return this.extract != null;
    }

    public ExtractRule getExtractRule() {
        return this.extract;
    }

    public String getName() {
        return this.name;
    }

    public String getURL() {
        String[] parts = this.name.split(":", 3);

        return MojangConstants.LIBRARIES_BASE.getURL(parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] +
                "/" + parts[1] + "-" + parts[2] + getClassifier() + ".jar");
    }

    public Path getFilePath() {
        String[] parts = this.name.split(":", 3);

        return FileSystem.LIBRARIES.resolve(parts[1] + "-" + parts[2] + getClassifier() + ".jar");
    }

    public String getClassifier() {
        if (this.natives == null) {
            return "";
        }

        return "-" + this.natives.get(OperatingSystem.getOS()).replace("${arch}", Utils.getArch());
    }
}
