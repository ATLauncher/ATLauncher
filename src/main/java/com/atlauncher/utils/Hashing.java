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
package com.atlauncher.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.atlauncher.managers.LogManager;
import com.google.common.hash.HashCode;
import com.sangupta.murmur.Murmur2;

import org.apache.commons.lang3.ArrayUtils;

public final class Hashing {
    public static final HashCode EMPTY_HASH_CODE = HashCode.fromBytes(new byte[1]);

    public static HashCode md5(String str) {
        if (str == null || str.isEmpty()) {
            return EMPTY_HASH_CODE;
        }

        return com.google.common.hash.Hashing.md5().hashString(str, StandardCharsets.UTF_8);
    }

    public static HashCode md5(Path file) {
        if (!Files.exists(file)) {
            return EMPTY_HASH_CODE;
        }

        try {
            return com.google.common.io.Files.asByteSource(file.toFile()).hash(com.google.common.hash.Hashing.md5());
        } catch (IOException e) {
            LogManager.logStackTrace("Error hashing (MD5) file " + file.toAbsolutePath().toString(), e);
            return EMPTY_HASH_CODE;
        }
    }

    public static HashCode sha1(Path file) {
        if (!Files.exists(file)) {
            return EMPTY_HASH_CODE;
        }

        try {
            return com.google.common.io.Files.asByteSource(file.toFile()).hash(com.google.common.hash.Hashing.sha1());
        } catch (IOException e) {
            LogManager.logStackTrace("Error hashing (SHA1) file " + file.toAbsolutePath().toString(), e);
            return EMPTY_HASH_CODE;
        }
    }

    public static HashCode sha512(Path file) {
        if (!Files.exists(file)) {
            return EMPTY_HASH_CODE;
        }

        try {
            return com.google.common.io.Files.asByteSource(file.toFile()).hash(com.google.common.hash.Hashing.sha512());
        } catch (IOException e) {
            LogManager.logStackTrace("Error hashing (SHA512) file " + file.toAbsolutePath().toString(), e);
            return EMPTY_HASH_CODE;
        }
    }

    // TODO: this is really not efficient or good on memory
    public static long murmur(Path to) throws IOException {
        byte[] bytes = ArrayUtils
                .removeAllOccurrences(ArrayUtils.removeAllOccurrences(
                        ArrayUtils.removeAllOccurrences(
                                ArrayUtils.removeAllOccurrences(Files.readAllBytes(to), (byte) 9), (byte) 10),
                        (byte) 13), (byte) 32);

        return Murmur2.hash(bytes, bytes.length, 1L);
    }

    public static HashCode toHashCode(String hash) {
        if (hash == null || hash.length() < 2 || hash.length() % 2 != 0) {
            return EMPTY_HASH_CODE;
        }

        return HashCode.fromString(hash);
    }
}
