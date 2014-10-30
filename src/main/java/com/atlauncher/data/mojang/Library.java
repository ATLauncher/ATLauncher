/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

import java.io.File;
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
        if (this.extract == null) {
            return false;
        }
        return true;
    }

    public ExtractRule getExtractRule() {
        return this.extract;
    }

    public String getName() {
        return this.name;
    }

    public String getURL() {
        String path;
        String[] parts = this.name.split(":", 3);
        path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2] +
                getClassifier() + ".jar";
        return MojangConstants.LIBRARIES_BASE.getURL(path);
    }

    public File getFile() {
        String[] parts = this.name.split(":", 3);
        return new File(App.settings.getLibrariesDir(), parts[1] + "-" + parts[2] + getClassifier() + ".jar");
    }

    public String getClassifier() {
        if (this.natives == null) {
            return "";
        }
        return "-" + this.natives.get(OperatingSystem.getOS()).replace("${arch}", Utils.getArch());
    }
}
