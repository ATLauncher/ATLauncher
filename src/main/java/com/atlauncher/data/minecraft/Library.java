/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.util.List;
import java.util.Map;

import com.atlauncher.constants.Constants;
import com.atlauncher.utils.OS;

public class Library {
    public String name;
    public Downloads downloads;
    public Map<String, String> natives;
    public List<Rule> rules;
    public ExtractRule extract;

    public boolean shouldInstall() {
        if (this.rules == null || this.rules.isEmpty()) {
            return true; // No rules setup so we need it
        }

        if (this.rules.stream().noneMatch(Rule::applies)) {
            return false; // No rules apply to us, so we don't need this
        }

        return this.rules.stream().filter(Rule::applies).allMatch(rule -> rule.action.equalsIgnoreCase("allow"));
    }

    public boolean hasNativeForOS() {
        if (this.natives == null) {
            return false;
        }

        if (OS.isWindows() && this.natives.containsKey("windows") && this.downloads.classifiers
                .containsKey(this.natives.get("windows").replace("${arch}", OS.getNativesArch()))) {
            return true;
        }

        if (OS.isLinux() && this.natives.containsKey("linux") && this.downloads.classifiers
                .containsKey(this.natives.get("linux").replace("${arch}", OS.getNativesArch()))) {
            return true;
        }

        if (OS.isMac() && this.natives.containsKey("osx") && this.downloads.classifiers
                .containsKey(this.natives.get("osx").replace("${arch}", OS.getNativesArch()))) {
            return true;
        }

        return false;
    }

    public Download getNativeDownloadForOS() {
        if (OS.isWindows() && this.natives != null && this.natives.containsKey("windows")) {
            return this.downloads.classifiers
                    .get(this.natives.get("windows").replace("${arch}", OS.getNativesArch()));
        }

        if (OS.isLinux() && this.natives != null && this.natives.containsKey("linux")) {
            return this.downloads.classifiers
                    .get(this.natives.get("linux").replace("${arch}", OS.getNativesArch()));
        }

        if (OS.isMac() && this.natives != null && this.natives.containsKey("osx")) {
            // if on ARM based Mac and there is a classifier for it, use it
            if (this.downloads.classifiers.containsKey(this.natives.get("osx") + "-arm64") && OS.isMacArm()
                    && OS.is64Bit()) {
                return this.downloads.classifiers
                        .get(this.natives.get("osx").replace("${arch}", OS.getNativesArch()) + "-arm64");
            }

            // else fall back to the standard natives, ARM based Macs can run in Rosetta
            return this.downloads.classifiers
                    .get(this.natives.get("osx").replace("${arch}", OS.getNativesArch()));
        }

        return null;
    }

    public void fixLog4jVersion() {
        if (!name.startsWith("org.apache.logging.log4j")) {
            return;
        }

        final String[] libraryParts = name.split(":");
        final String[] versionParts = libraryParts[2].split("\\.");

        // if using newer than 2.16 version, use the Mojang provided library
        try {
            if (Integer.parseInt(versionParts[0]) > 2
                    || (Integer.parseInt(versionParts[0]) == 2
                            && Integer.parseInt(versionParts[1]) >= 16)) {
                return;
            }
        } catch (NumberFormatException ignored) {
        }

        if (libraryParts[1].equals("log4j-api")) {
            if (libraryParts[2].startsWith("2.0-beta9")) {
                name = "org.apache.logging.log4j:log4j-api:2.0-beta9-fixed";
                downloads.artifact.path = "org/apache/logging/log4j/log4j-api/2.0-beta9-fixed/log4j-api-2.0-beta9-fixed.jar";
                downloads.artifact.sha1 = "b61eaf2e64d8b0277e188262a8b771bbfa1502b3";
                downloads.artifact.size = 107347;
            } else {
                name = "org.apache.logging.log4j:log4j-api:2.16.0";
                downloads.artifact.path = "org/apache/logging/log4j/log4j-api/2.16.0/log4j-api-2.16.0.jar";
                downloads.artifact.sha1 = "f821a18687126c2e2f227038f540e7953ad2cc8c";
                downloads.artifact.size = 301892;
            }
        } else if (libraryParts[1].equals("log4j-core")) {
            if (libraryParts[2].startsWith("2.0-beta9")) {
                name = "org.apache.logging.log4j:log4j-core:2.0-beta9-fixed";
                downloads.artifact.path = "org/apache/logging/log4j/log4j-core/2.0-beta9-fixed/log4j-core-2.0-beta9-fixed.jar";
                downloads.artifact.sha1 = "677991ea2d7426f76309a73739cecf609679492c";
                downloads.artifact.size = 677588;
            } else {
                name = "org.apache.logging.log4j:log4j-core:2.16.0";
                downloads.artifact.path = "org/apache/logging/log4j/log4j-core/2.16.0/log4j-core-2.16.0.jar";
                downloads.artifact.sha1 = "539a445388aee52108700f26d9644989e7916e7c";
                downloads.artifact.size = 1789565;
            }
        } else if (libraryParts[1].equals("log4j-slf4j18-impl")) {
            name = "org.apache.logging.log4j:log4j-slf4j18-impl:2.16.0";
            downloads.artifact.path = "org/apache/logging/log4j/log4j-slf4j18-impl/2.16.0/log4j-slf4j18-impl-2.16.0.jar";
            downloads.artifact.sha1 = "0c880a059056df5725f5d8d1035276d9749eba6d";
            downloads.artifact.size = 21249;
        }

        // adjust the download url
        downloads.artifact.url = String.format("%s/maven/%s", Constants.DOWNLOAD_SERVER, downloads.artifact.path);
    }
}
