package com.atlauncher.utils;

import com.atlauncher.LogManager;
import com.atlauncher.io.ByteArrayInputStream;
import com.atlauncher.io.ByteArrayOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Hashing{
    private static final char[] hex = "0123456789abcdef".toCharArray();

    public static HashCode md5(Path file){
        if(!Files.exists(file)){
            return new HashCode(new byte[0]);
        }

        try(Hasher hasher = new MD5Hasher(Files.newInputStream(file))){
            return hasher.hash();
        } catch(Exception e){
            LogManager.logStackTrace("Error hashing (MD5) file " + file.getFileName(), e);
            return new HashCode(new byte[0]);
        }
    }

    public static HashCode md5(String str){
        if(str == null || str.isEmpty()){
            return new HashCode(new byte[0]);
        }

        try(Hasher hasher = new MD5Hasher(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))){
            return hasher.hash();
        } catch(Exception e){
            LogManager.logStackTrace("Error hashing (MD5) string " + str, e);
            return new HashCode(new byte[0]);
        }
    }

    public static HashCode sha1(Path file){
        if(!Files.exists(file)){
            return new HashCode(new byte[0]);
        }

        try(Hasher hasher = new SHA1Hasher(Files.newInputStream(file))){
            return hasher.hash();
        } catch(Exception e){
            LogManager.logStackTrace("Error hashing (SHA-1) file " + file.getFileName(), e);
            return new HashCode(new byte[0]);
        }
    }

    public static HashCode sha1(String str){
        if(str == null || str.isEmpty()){
            return new HashCode(new byte[0]);
        }

        try(Hasher hasher = new SHA1Hasher(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))){
            return hasher.hash();
        } catch(Exception e){
            LogManager.logStackTrace("Error hashing (SHA-1) string " + str, e);
            return new HashCode(new byte[0]);
        }
    }

    private interface Hasher
    extends Closeable{
        public HashCode hash();
    }

    private static final class SHA1Hasher
    implements Hasher{
        private final InputStream is;
        private final MessageDigest digest;

        private SHA1Hasher(InputStream is)
        throws NoSuchAlgorithmException{
            this.is = is;
            this.digest = MessageDigest.getInstance("SHA-1");
        }

        @Override
        public HashCode hash() {
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
                byte[] buffer = new byte[8192];
                int len;
                while((len = this.is.read(buffer, 0, 8192)) != -1){
                    bos.write(buffer, 0, len);
                }

                return new HashCode(this.digest.digest(bos.toByteArray()));
            } catch(Exception e){
                LogManager.logStackTrace("Error hashing (MD5)", e);
                return null;
            }
        }

        @Override
        public void close()
        throws IOException {
            this.is.close();
        }
    }

    private static final class MD5Hasher
    implements Hasher{
        private final InputStream is;
        private final MessageDigest digest;

        private MD5Hasher(InputStream is)
        throws NoSuchAlgorithmException{
            this.is = is;
            this.digest = MessageDigest.getInstance("MD5");
        }

        @Override
        public HashCode hash() {
            try(ByteArrayOutputStream bos = new ByteArrayOutputStream()){
                byte[] buffer = new byte[8192];
                int len;
                while((len = this.is.read(buffer, 0, 8192)) != -1){
                    bos.write(buffer, 0, len);
                }

                return new HashCode(this.digest.digest(bos.toByteArray()));
            } catch(Exception e){
                LogManager.logStackTrace("Error hashing (MD5)", e);
                return null;
            }
        }

        @Override
        public void close()
        throws IOException {
            this.is.close();
        }
    }

    public static final class HashCode
    implements Serializable{
        private final byte[] bits;

        private HashCode(byte[] bits){
            this.bits = bits;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(2 * this.bits.length);
            for(byte b : this.bits){
                sb.append(hex[(b >> 4) & 0xF])
                  .append(hex[b & 0xF]);
            }
            return sb.toString();
        }
    }
}