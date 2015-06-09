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
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

public final class CompressionUtils {
    public static void unzip(Path zip, Path dest) throws IOException {
        if (Files.exists(dest)) {
            FileUtils.delete(dest);
        }

        if (Files.notExists(dest)) {
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
            throw new IllegalStateException("File " + dir + " isn't a directory!");
        }

        try (FileSystem zipfs = createZipFileSystem(zip, true)) {
            Files.walkFileTree(dir, new ZipVisitor(zipfs, dir));
        }
    }

    public static void unXZPackFile(Path xzFile, Path packFile, Path outputFile) {
        unXZFile(xzFile, packFile);
        unpackFile(packFile, outputFile);
    }

    public static void unXZFile(Path input, Path output) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        XZInputStream xzis = null;
        try {
            fis = new FileInputStream(input.toFile());
            xzis = new XZInputStream(fis);
            fos = new FileOutputStream(output.toFile());

            final byte[] buffer = new byte[8192];
            int n = 0;
            while (-1 != (n = xzis.read(buffer))) {
                fos.write(buffer, 0, n);
            }

        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (xzis != null) {
                    xzis.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    /*
         * From: http://atl.pw/1
         */
    public static void unpackFile(Path input, Path output) {
        if (Files.exists(output)) {
            FileUtils.delete(output);
        }

        byte[] decompressed = null;

        try {
            decompressed = Files.readAllBytes(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (decompressed == null) {
            LogManager.error("unpackFile: While reading in " + input + " the file returned null");
            return;
        }

        String end = new String(decompressed, decompressed.length - 4, 4);
        if (!end.equals("SIGN")) {
            LogManager.error("unpackFile: Unpacking failed, signature missing " + end);
            return;
        }

        int x = decompressed.length;
        int len = ((decompressed[x - 8] & 0xFF)) | ((decompressed[x - 7] & 0xFF) << 8) | ((decompressed[x - 6] &
                0xFF) << 16) | ((decompressed[x - 5] & 0xFF) << 24);
        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);
        try {
            FileOutputStream jarBytes = new FileOutputStream(output.toFile());
            JarOutputStream jos = new JarOutputStream(jarBytes);

            Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);

            jos.putNextEntry(new JarEntry("checksums.sha1"));
            jos.write(checksums);
            jos.closeEntry();

            jos.close();
            jarBytes.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
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