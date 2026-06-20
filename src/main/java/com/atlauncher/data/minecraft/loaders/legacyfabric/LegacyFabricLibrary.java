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
package com.atlauncher.data.minecraft.loaders.legacyfabric;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlauncher.FileSystem;
import com.atlauncher.annot.Json;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.Rule;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.google.common.hash.HashCode;

@Json
public class LegacyFabricLibrary extends Library {

    public LegacyFabricLibrary(String name, String url, Map<String, String> natives, List<Rule> rules) {
        this.name = name;
        this.rules = rules;

        Downloads downloads = new Downloads();
        Download artifact = new Download();
        artifact.path = Utils.convertMavenIdentifierToPath(name);
        artifact.url = String.format("%s%s", url, artifact.path);

        Path localLibraryPath = FileSystem.LIBRARIES.resolve(artifact.path);

        if (Files.exists(localLibraryPath)) {
            artifact.size = localLibraryPath.toFile().length();

            HashCode sha1 = Hashing.sha1(localLibraryPath);
            if (sha1 != null) {
                artifact.sha1 = sha1.toString();
            }
        }

        downloads.artifact = artifact;

        if (natives != null && !natives.isEmpty()) {
            this.natives = natives;

            Map<String, Download> classifiers = new HashMap<>();

            for (Map.Entry<String, String> entry : natives.entrySet()) {
                String classifier = entry.getValue();

                if (classifier.contains("${arch}")) {
                    for (String arch : new String[] { "32", "64" }) {
                        addClassifier(classifiers, name, url, classifier.replace("${arch}", arch));
                    }
                } else {
                    addClassifier(classifiers, name, url, classifier);
                }
            }

            downloads.classifiers = classifiers;
        }

        this.downloads = downloads;
    }

    public LegacyFabricLibrary(String name, String url, Map<String, String> natives) {
        this(name, url, natives, null);
    }

    public LegacyFabricLibrary(String name, String url) {
        this(name, url, null, null);
    }

    public LegacyFabricLibrary(String name) {
        this(name, Constants.LEGACY_FABRIC_MAVEN);
    }

    private void addClassifier(Map<String, Download> classifiers, String name, String url, String classifier) {
        String nativeName = String.format("%s:%s", name, classifier);
        String nativePath = Utils.convertMavenIdentifierToPath(nativeName);
        String nativeUrl = String.format("%s%s", url, nativePath);

        Download nativeDownload = new Download();
        nativeDownload.path = nativePath;
        nativeDownload.url = nativeUrl;

        Path localNativePath = FileSystem.LIBRARIES.resolve(nativePath);

        if (Files.exists(localNativePath)) {
            nativeDownload.size = localNativePath.toFile().length();

            HashCode sha1 = Hashing.sha1(localNativePath);
            if (sha1 != null) {
                nativeDownload.sha1 = sha1.toString();
            }
        }

        classifiers.put(classifier, nativeDownload);
    }
}
