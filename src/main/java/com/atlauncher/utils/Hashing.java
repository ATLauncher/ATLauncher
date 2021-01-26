/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.atlauncher.collection.Caching;
import com.atlauncher.managers.LogManager;
import com.sangupta.murmur.Murmur2;

import org.apache.commons.lang3.ArrayUtils;

public final class Hashing {
    private static final char[] hex = "0123456789abcdef".toCharArray();
    private static final SoftReference<Caching.Cache<Object, HashCode>> hashcodes = new SoftReference<>(
            Caching.newLRU());

    public static HashCode md5(Path file) {
        if (!Files.exists(file)) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new MD5Hasher(Files.newInputStream(file))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (MD5) file " + file.getFileName(), e);
            return HashCode.EMPTY;
        }
    }

    public static long murmur(Path to) throws IOException {
        byte[] bytes = ArrayUtils
                .removeAllOccurrences(ArrayUtils.removeAllOccurrences(
                        ArrayUtils.removeAllOccurrences(
                                ArrayUtils.removeAllOccurrences(Files.readAllBytes(to), (byte) 9), (byte) 10),
                        (byte) 13), (byte) 32);

        return Murmur2.hash(bytes, bytes.length, 1L);
    }

    private static HashCode md5Internal(String str) {
        if (str == null || str.isEmpty()) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new MD5Hasher(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (MD5) string " + str, e);
            return HashCode.EMPTY;
        }
    }

    private static HashCode md5Internal(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new MD5Hasher(new ByteArrayInputStream(bytes))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (MD5) byte array", e);
            return HashCode.EMPTY;
        }
    }

    private static HashCode md5Internal(Object obj) {
        if (obj == null) {
            return HashCode.EMPTY;
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(obj);
            oos.flush();

            try (Hasher hasher = new MD5Hasher(new ByteArrayInputStream(bos.toByteArray()))) {
                return hasher.hash();
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (MD5) object " + obj.getClass(), e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode md5(String str) {
        try {
            HashCode code = hashcodes.get().get(str);
            if (code != null) {
                return code;
            }
            code = md5Internal(str);
            hashcodes.get().put(str, code);
            return code;
        } catch (Exception e) {
            return md5Internal(str);
        }
    }

    public static HashCode md5(Object obj) {
        try {
            HashCode code = hashcodes.get().get(obj);
            if (code != null) {
                return code;
            }
            code = md5Internal(obj);
            hashcodes.get().put(obj, code);
            return code;
        } catch (Exception e) {
            return md5Internal(obj);
        }
    }

    public static HashCode md5(byte[] bytes) {
        try {
            HashCode code = hashcodes.get().get(bytes);
            if (code != null) {
                return code;
            }
            code = md5Internal(bytes);
            hashcodes.get().put(bytes, code);
            return code;
        } catch (Exception e) {
            return md5Internal(bytes);
        }
    }

    public static HashCode sha512(Path file) {
        if (!Files.exists(file)) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA512Hasher(Files.newInputStream(file))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-512) file " + file.getFileName(), e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode sha512(String str) {
        if (str == null || str.isEmpty()) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA512Hasher(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-512) string " + str, e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode sha512(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA512Hasher(new ByteArrayInputStream(bytes))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-512) byte array", e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode sha1(Path file) {
        if (!Files.exists(file)) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA1Hasher(Files.newInputStream(file))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-1) file " + file.getFileName(), e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode sha1(String str) {
        if (str == null || str.isEmpty()) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA1Hasher(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-1) string " + str, e);
            return HashCode.EMPTY;
        }
    }

    public static HashCode sha1(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return HashCode.EMPTY;
        }

        try (Hasher hasher = new SHA1Hasher(new ByteArrayInputStream(bytes))) {
            return hasher.hash();
        } catch (Exception e) {
            LogManager.logStackTrace("Error hashing (SHA-1) byte array", e);
            return HashCode.EMPTY;
        }
    }

    private interface Hasher extends Closeable {
        HashCode hash();
    }

    private static final class SHA512Hasher implements Hasher {
        private final InputStream is;
        private final MessageDigest digest;

        private SHA512Hasher(InputStream is) throws NoSuchAlgorithmException {
            this.is = is;
            this.digest = MessageDigest.getInstance("SHA-512");
        }

        @Override
        public HashCode hash() {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = this.is.read(buffer, 0, 8192)) != -1) {
                    bos.write(buffer, 0, len);
                }

                return new HashCode(this.digest.digest(bos.toByteArray()));
            } catch (Exception e) {
                LogManager.logStackTrace("Error hashing (SHA-512)", e);
                return null;
            }
        }

        @Override
        public void close() throws IOException {
            this.is.close();
        }
    }

    private static final class SHA1Hasher implements Hasher {
        private final InputStream is;
        private final MessageDigest digest;

        private SHA1Hasher(InputStream is) throws NoSuchAlgorithmException {
            this.is = is;
            this.digest = MessageDigest.getInstance("SHA-1");
        }

        @Override
        public HashCode hash() {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = this.is.read(buffer, 0, 8192)) != -1) {
                    bos.write(buffer, 0, len);
                }

                return new HashCode(this.digest.digest(bos.toByteArray()));
            } catch (Exception e) {
                LogManager.logStackTrace("Error hashing (SHA-1)", e);
                return null;
            }
        }

        @Override
        public void close() throws IOException {
            this.is.close();
        }
    }

    private static final class MD5Hasher implements Hasher {
        private final InputStream is;
        private final MessageDigest digest;

        private MD5Hasher(InputStream is) throws NoSuchAlgorithmException {
            this.is = is;
            this.digest = MessageDigest.getInstance("MD5");
        }

        @Override
        public HashCode hash() {
            try {
                byte[] buffer = new byte[1024];
                for (int read = this.is.read(buffer, 0, 1024); read > -1; read = this.is.read(buffer, 0, 1024)) {
                    this.digest.update(buffer, 0, read);
                }

                return new HashCode(this.digest.digest());
            } catch (Exception e) {
                LogManager.logStackTrace("Error hashing (MD5)", e);
                return HashCode.EMPTY;
            }
        }

        @Override
        public void close() throws IOException {
            this.is.close();
        }
    }

    @SuppressWarnings("serial")
    public static final class HashCode implements Serializable, Cloneable {
        private static final SoftReference<Caching.Cache<String, HashCode>> hashescache = new SoftReference<>(
                Caching.newLRU());

        public static final HashCode EMPTY = new HashCode(new byte[0]);

        public static HashCode fromString(String str) {
            try {
                HashCode code = hashescache.get().get(str);
                if (code != null) {
                    return code;
                }
                code = fromStringInternal(str);
                hashescache.get().put(str, code);
                return code;
            } catch (Exception e) {
                return fromStringInternal(str);
            }
        }

        private static HashCode fromStringInternal(String str) {
            if (str == null || str.isEmpty()) {
                return EMPTY;
            }

            if (!(str.length() >= 2)) {
                return EMPTY;
            }

            if (!(str.length() % 2 == 0)) {
                return EMPTY;
            }

            byte[] bits = new byte[str.length() / 2];
            for (int i = 0; i < str.length(); i += 2) {
                int ch1 = decode(str.charAt(i)) << 4;
                int ch2 = decode(str.charAt(i + 1));
                bits[i / 2] = (byte) (ch1 + ch2);
            }

            return new HashCode(bits);
        }

        private static byte[] decode(String str) {
            byte[] bits = new byte[str.length() / 2];
            for (int i = 0; i < str.length(); i += 2) {
                int ch1 = decode(str.charAt(i)) << 4;
                int ch2 = decode(str.charAt(i + 1));
                bits[i / 2] = (byte) (ch1 + ch2);
            }

            return bits;
        }

        private static int decode(char c) {
            if (c >= '0' && c <= '9') {
                return c - '0';
            }

            if (c >= 'a' && c <= 'f') {
                return c - 'a' + 10;
            }

            throw new IllegalStateException("Illegal hex character: " + c);
        }

        private final byte[] bits;

        private HashCode(byte[] bits) {
            this.bits = bits;
        }

        public HashCode(String hash) {
            this(decode(hash));
        }

        public HashCode intern() {
            for (Map.Entry<String, HashCode> code : hashescache.get()) {
                if (code.getValue().equals(this)) {
                    return code.getValue();
                }
            }

            return this;
        }

        public int asInt() {
            if (!(this.bits.length >= 4)) {
                throw new IllegalStateException(
                        "HashCode#asInt() requires >= 4 bytes, it only has " + this.bits.length);
            }

            return (this.bits[0] & 0xFF) | ((this.bits[1] & 0xFF) << 8) | ((this.bits[2] & 0xFF) << 16)
                    | ((this.bits[3] & 0xFF) << 24);
        }

        public int bits() {
            return this.bits.length * 8;
        }

        public byte[] bytes() {
            return this.bits;
        }

        public boolean hasSameBits(HashCode code) {
            if (this.bits.length != code.bits.length) {
                return false;
            }

            boolean equal = true;
            for (int i = 0; i < this.bits.length; i++) {
                equal &= (this.bits[i] == code.bits[i]);
            }

            return equal;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HashCode) {
                HashCode code = (HashCode) obj;
                return this.bits() == code.bits() && this.hasSameBits(code);
            }

            return false;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
            byte[] bits = new byte[this.bits.length];
            System.arraycopy(bits, 0, this.bits, 0, this.bits.length);
            return new HashCode(bits);
        }

        @Override
        public int hashCode() {
            if (this.bits() >= 32) {
                return this.asInt();
            }

            int val = (this.bits[0] & 0xFF);
            for (int i = 1; i < this.bits.length; i++) {
                val |= ((this.bits[i] & 0xFF) << (i * 8));
            }

            return val;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(2 * this.bits.length);
            for (byte b : this.bits) {
                sb.append(hex[(b >> 4) & 0xF]).append(hex[b & 0xF]);
            }
            return sb.toString();
        }
    }
}
