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

import com.atlauncher.utils.FileUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class CopyDirVisitor extends SimpleFileVisitor<Path> {
    private final Path from;
    private final Path to;
    private final StandardCopyOption option;

    public CopyDirVisitor(Path from, Path to, StandardCopyOption option) {
        this.from = from;
        this.to = to;
        this.option = option;
    }

    public CopyDirVisitor(Path from, Path to) {
        this(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path target = this.to.resolve(this.from.relativize(dir));
        if (!Files.exists(target)) {
            FileUtils.createDirectory(target);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, this.to.resolve(this.from.relativize(file)), this.option);
        return FileVisitResult.CONTINUE;
    }
}