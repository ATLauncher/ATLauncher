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
package com.atlauncher.utils.walker;

import com.atlauncher.data.json.CaseType;
import com.atlauncher.utils.FileUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class CaseFileVisitor extends SimpleFileVisitor<Path> {
    private CaseType caseType;
    private List<String> filesToIgnore;

    public CaseFileVisitor(CaseType caseType) {
        this.caseType = caseType;
        this.filesToIgnore = new ArrayList<>();
    }

    public CaseFileVisitor(CaseType caseType, List<String> filesToIgnore) {
        this.caseType = caseType;
        this.filesToIgnore = filesToIgnore;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (this.filesToIgnore.contains(path.getFileName().toString())) {
            return FileVisitResult.CONTINUE;
        }

        if (!Files.isRegularFile(path) || (!path.getFileName().endsWith("jar") && !path.getFileName().endsWith("zip") &&
                !path.getFileName().endsWith("litemod"))) {
            if (caseType == CaseType.upper) {
                String filename = path.getFileName().toString();
                filename = filename.substring(0, filename.lastIndexOf(".")).toUpperCase() + filename.substring
                        (filename.lastIndexOf("."));
                FileUtils.moveFile(path, path.getParent().resolve(filename), true);
            } else if (caseType == CaseType.lower) {
                FileUtils.moveFile(path, path.getParent().resolve(path.getFileName().toString().toLowerCase()));
            }
        }

        return FileVisitResult.CONTINUE;
    }
}