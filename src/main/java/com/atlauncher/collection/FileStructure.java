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
package com.atlauncher.collection;

import com.atlauncher.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

public final class FileStructure extends LinkedList<Path> {
    public static FileStructure of(Path... paths) {
        return new FileStructure(paths);
    }

    private FileStructure(Path... paths) {
        super(Arrays.asList(paths));
    }

    public FileStructure() {
    }

    public void setup() {
        for (Path p : this) {
            if (!Files.exists(p)) {
                FileUtils.createDirectory(p);
            }
        }
    }
}