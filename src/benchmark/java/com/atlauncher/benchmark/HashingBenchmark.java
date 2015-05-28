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
package com.atlauncher.benchmark;

import com.atlauncher.FileSystemData;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.codec.digest.DigestUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.InputStream;
import java.security.MessageDigest;


public class HashingBenchmark {
    public static void main(String... args) throws Exception {
        Options opts = new OptionsBuilder().include(HashingBenchmark.class.getSimpleName()).forks(1).build();
        new Runner(opts).run();
    }

    @Benchmark
    public void guava() throws Exception {
        Files.hash(FileSystemData.PROPERTIES.toFile(), Hashing.md5());
    }

    @Benchmark
    public void commons() throws Exception {
        DigestUtils.md5Hex(java.nio.file.Files.readAllBytes(FileSystemData.PROPERTIES));
    }

    @Benchmark
    public void java() throws Exception {
        try (InputStream stream = java.nio.file.Files.newInputStream(FileSystemData.PROPERTIES)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int len;
            while ((len = stream.read(buffer, 0, 1024)) != -1) {
                digest.digest(buffer, 0, len);
            }
            StringBuilder builder = new StringBuilder();
            for (byte b : buffer) {
                builder.append(Integer.toString((b & 0xFF) + 0x100, 16).substring(1));
            }
        }
    }
}