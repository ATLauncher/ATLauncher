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
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class ZipVisitor extends SimpleFileVisitor<Path> {
    private final FileSystem fs;
    private final Path root;
    private final Path dir;

    public ZipVisitor(FileSystem fs, Path dir) throws IOException {
        this.fs = fs;
        this.dir = dir;
        this.root = fs.getPath("/");
        if (Files.notExists(this.root)) {
            FileUtils.createDirectory(this.root);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String name = this.dir.relativize(file).toString();
        if (name.endsWith("aux_class")) {
            name = "aux.class";
        }
        Path dest = this.fs.getPath(name);
        Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        String path = this.dir.relativize(dir).toString();
        if (path.isEmpty()) {
            return FileVisitResult.CONTINUE;
        }

        Path d = this.fs.getPath(this.dir.relativize(dir).toString());
        if (Files.notExists(d)) {
            FileUtils.createDirectory(d);
        }

        return FileVisitResult.CONTINUE;
    }
}