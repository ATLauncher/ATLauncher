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
package com.atlauncher.utils;

import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.walker.UnzipVisitor;
import com.atlauncher.utils.walker.ZipVisitor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class CompressionUtils {
    public static void unzip(Path zip, Path dest) throws IOException {
        if (Files.exists(dest)) {
            FileUtils.delete(dest);
        }

        if (Files.notExists(dest)) {
            LogManager.warn(dest.toString() + " doesn't exist, Creating");
            FileUtils.createDirectory(dest);
        }

        try (FileSystem zipfs = createZipFileSystem(zip, false)) {
            Path root = zipfs.getPath("/");
            Files.walkFileTree(root, new UnzipVisitor(dest));
        }
    }

    public static void zip(Path zip, Path dir) throws IOException {
        if (Files.exists(zip)) {
            FileUtils.delete(zip);
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalStateException("File " + dir.toString() + " isnt a directory");
        }

        try (FileSystem zipfs = createZipFileSystem(zip, true)) {
            Files.walkFileTree(dir, new ZipVisitor(zipfs, dir));
        }
    }

    private static FileSystem createZipFileSystem(Path zip, boolean create) throws IOException {
        URI uri = URI.create("jar:file:" + zip.toUri().getPath());

        Map<String, String> env = new HashMap<>();

        if (create) {
            env.put("create", "true");
        }

        return FileSystems.newFileSystem(uri, env);
    }
}