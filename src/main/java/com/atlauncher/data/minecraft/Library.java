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
package com.atlauncher.data.minecraft;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.utils.OS;

public class Library {
    public String name;
    public Downloads downloads;
    public Map<String, String> natives;
    public List<Rule> rules;
    public ExtractRule extract;

    public boolean shouldInstall() {
        if (this.rules == null) {
            return true; // No rules setup so we need it
        }

        return this.rules.stream().filter(rule -> rule.applies()).allMatch(rule -> rule.action == Action.ALLOW);
    }

    public boolean shouldExtract() {
        return this.extract != null;
    }

    public boolean hasArtifact() {
        return this.downloads != null && this.downloads.artifact != null;
    }

    public boolean hasClassifier(String classifier) {
        return this.downloads != null && this.downloads.classifiers.containsKey(classifier);
    }

    public String getURL() {
        if (this.hasArtifact()) {
            return this.downloads.artifact.url;
        }

        String[] parts = this.name.split(":", 3);

        return MojangConstants.LIBRARIES_BASE.getURL(String.format("%1$s/%2$s/%3$s/%2$s-%3$s%4$s.jar",
                parts[0].replace(".", "/"), parts[1], parts[2], this.getClassifier()));
    }

    public File getFile() {
        if (this.hasArtifact()) {
            return new File(App.settings.getGameLibrariesDir(), this.downloads.artifact.path);
        }

        String[] parts = this.name.split(":", 3);

        return new File(App.settings.getLibrariesDir(),
                String.format("%s-%s%s.jar", parts[1], parts[2], this.getClassifier()));
    }

    public String getPathFromRoot() {
        if (this.hasArtifact()) {
            return this.downloads.artifact.path;
        }

        String[] parts = this.name.split(":", 3);

        return String.format("%s-%s%s.jar", parts[1], parts[2], this.getClassifier());
    }

    public String getNativeURL() {
        return this.getNativeClassifier().url;
    }

    public File getNativeFile() {
        return new File(App.settings.getGameLibrariesDir(), this.getNativeClassifier().path);
    }

    public String getNativePathFromRoot() {
        return this.getNativeClassifier().path;
    }

    public Download getNativeClassifier() {
        return this.downloads.classifiers.get(this.natives.get(OperatingSystem.getOS().name));
    }

    public boolean hasNatives() {
        return this.natives != null && this.natives.containsKey(OperatingSystem.getOS().name)
                && this.hasClassifier(this.natives.get(OperatingSystem.getOS().name));
    }

    public String getClassifier() {
        if (this.natives == null || !this.natives.containsKey(OperatingSystem.getOS().name)) {
            return "";
        }
        return "-" + this.natives.get(OperatingSystem.getOS().name).replace("${arch}", OS.getArch());
    }
}
