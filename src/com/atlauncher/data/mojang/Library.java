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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

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
        for (Rule rule : this.rules) { // Loop through all the rules
            if (rule.ruleApplies()) { // See if this rule applies to this system
                return (rule.getAction() == Action.ALLOW); // Check if we are allowing it
            }
        }
        return false;
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
        path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-"
                + parts[2] + getClassifier() + ".jar";
        return MojangConstants.LIBRARIES_BASE.getURL(path);
    }

    public File getFile() {
        String[] parts = this.name.split(":", 3);
        return new File(App.settings.getLibrariesDir(), parts[1] + "-" + parts[2] + getClassifier()
                + ".jar");
    }

    public String getClassifier() {
        if (this.natives == null) {
            return "";
        }
        return "-" + this.natives.get(OperatingSystem.getOS()).replace("${arch}", Utils.getArch());
    }
}
