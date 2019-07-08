/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.utils.OS;

public class Library {

    private String name;
    private Map<OperatingSystem, String> natives;
    private List<Rule> rules;
    private ExtractRule extract;
    private Downloads downloads;
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

    public Downloads getDownloads() {
        return this.downloads;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean hasArtifact() {
        return this.downloads != null && this.downloads.getArtifact() != null;
    }

    public boolean hasClassifier(String name) {
        return this.downloads != null && this.downloads.hasClassifier(name);
    }

    public String getURL() {
        if (this.hasArtifact()) {
            return this.downloads.getArtifact().getUrl();
        }

        String path;
        String[] parts = this.name.split(":", 3);
        path = parts[0].replace(".", "/") + "/" + parts[1] + "/" + parts[2] + "/" + parts[1] + "-" + parts[2]
                + getClassifier() + ".jar";
        return MojangConstants.LIBRARIES_BASE.getURL(path);
    }

    public File getFile() {
        if (this.hasArtifact()) {
            return FileSystem.LIBRARIES.resolve(this.downloads.getArtifact().getPath()).toFile();
        }

        String[] parts = this.name.split(":", 3);
        return new File(App.settings.getLibrariesDir(), parts[1] + "-" + parts[2] + getClassifier() + ".jar");
    }

    public String getPathFromRoot() {
        if (this.hasArtifact()) {
            return this.downloads.getArtifact().getPath();
        }

        String[] parts = this.name.split(":", 3);
        return parts[1] + "-" + parts[2] + getClassifier() + ".jar";
    }

    public String getNativeURL() {
        return this.getNativeClassifier().getUrl();
    }

    public File getNativeFile() {
        return FileSystem.LIBRARIES.resolve(this.getNativeClassifier().getPath()).toFile();
    }

    public String getNativePathFromRoot() {
        return this.getNativeClassifier().getPath();
    }

    public DownloadsItem getNativeClassifier() {
        return this.downloads.getClassifier(this.natives.get(OperatingSystem.getOS()));
    }

    public boolean hasNatives() {
        return this.natives != null && this.natives.containsKey(OperatingSystem.getOS())
                && this.hasClassifier(this.natives.get(OperatingSystem.getOS()));
    }

    public String getClassifier() {
        if (this.natives == null || !this.natives.containsKey(OperatingSystem.getOS())) {
            return "";
        }
        return "-" + this.natives.get(OperatingSystem.getOS()).replace("${arch}", OS.getArch());
    }
}
