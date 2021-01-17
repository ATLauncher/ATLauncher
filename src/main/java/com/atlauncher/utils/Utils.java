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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.ExtractRule;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.managers.LogManager;
import com.google.gson.reflect.TypeToken;

import org.tukaani.xz.LZMAInputStream;
import org.tukaani.xz.XZInputStream;
import org.zeroturnaround.zip.ZipUtil;

import io.pack200.Pack200;
import net.iharder.Base64;

public class Utils {
    public static EnumSet<StandardOpenOption> WRITE = EnumSet.of(StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE);
    public static EnumSet<StandardOpenOption> READ = EnumSet.of(StandardOpenOption.READ);

    public static String error(Throwable t) {
        StringBuilder builder = new StringBuilder();

        builder.append(t.toString()).append("\n");
        StackTraceElement[] elements = t.getStackTrace();
        for (int i = 0; i < elements.length; i++) {
            builder.append("\t").append(elements[i].toString());
            if (i < (elements.length - 1)) {
                builder.append("\n");
            }
        }

        return builder.toString();
    }

    /**
     * Gets the icon image.
     *
     * @param path the path
     * @return the icon image
     */
    public static ImageIcon getIconImage(String path) {
        URL url = App.class.getResource(path);

        if (url == null) {
            LogManager.error("Unable to load resource " + path);
            return null;
        }

        return new ImageIcon(url);
    }

    public static File getOSStorageDir() {
        switch (OS.getOS()) {
            case WINDOWS:
                return new File(System.getenv("APPDATA"), "/." + Constants.LAUNCHER_NAME.toLowerCase());
            case OSX:
                return new File(System.getProperty("user.home"),
                        "/Library/Application Support/." + Constants.LAUNCHER_NAME.toLowerCase());
            default:
                return new File(System.getProperty("user.home"), "/." + Constants.LAUNCHER_NAME.toLowerCase());
        }
    }

    /**
     * Gets the icon image.
     *
     * @param file the file
     * @return the icon image
     */
    public static ImageIcon getIconImage(File file) {
        if (!file.exists()) {
            LogManager.error("Unable to load file " + file.getAbsolutePath());
            return null;
        }

        return new ImageIcon(file.getAbsolutePath());
    }

    public static BufferedImage getImage(String img) {
        String name;
        if (!img.startsWith("/assets/image/")) {
            name = "/assets/image/" + img;
        } else {
            name = img;
        }

        if (!name.endsWith(".png")) {
            name += ".png";
        }

        InputStream stream = App.class.getResourceAsStream(name);

        if (stream == null) {
            throw new NullPointerException("Stream == null");
        }

        try {
            return ImageIO.read(stream);
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to read image", e);
            return null;
        }
    }

    public static InputStream getResourceInputStream(String path) {
        return App.class.getResourceAsStream(path);
    }

    /**
     * Upload paste.
     *
     * @param title the title
     * @param log   the log
     * @return the string
     */
    public static String uploadPaste(String title, String log) {
        String line;
        String result = "";
        try {
            String urlParameters = "";
            urlParameters += "title=" + URLEncoder.encode(title, "ISO-8859-1") + "&";
            urlParameters += "language=" + URLEncoder.encode("text", "ISO-8859-1") + "&";
            urlParameters += "private=" + URLEncoder.encode("1", "ISO-8859-1") + "&";
            urlParameters += "text=" + URLEncoder.encode(log, "ISO-8859-1");
            URL url = new URL(Constants.PASTE_API_URL);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(urlParameters);
            writer.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                result = line;
            }
            writer.close();
            reader.close();
        } catch (IOException e1) {
            LogManager.logStackTrace(e1);
        }
        return result;
    }

    /**
     * Move file.
     *
     * @param from         the from
     * @param to           the to
     * @param withFilename the with filename
     * @return true, if successful
     */
    public static boolean moveFile(File from, File to, boolean withFilename) {
        if (copyFile(from, to, withFilename)) {
            delete(from);
            return true;
        } else {
            LogManager.error("Couldn't move file " + from.getAbsolutePath() + " to " + to.getAbsolutePath());
            return false;
        }
    }

    /**
     * Copy file.
     *
     * @param from the from
     * @param to   the to
     * @return true, if successful
     */
    public static boolean copyFile(File from, File to) {
        return copyFile(from, to, false);
    }

    /**
     * Copy file.
     *
     * @param from         the from
     * @param to           the to
     * @param withFilename the with filename
     * @return true, if successful
     */
    @SuppressWarnings("resource")
    public static boolean copyFile(File from, File to, boolean withFilename) {
        if (!from.isFile()) {
            LogManager.error("File " + from.getAbsolutePath() + " cannot be copied to " + to.getAbsolutePath() + " as"
                    + " it isn't a file");
        }
        if (!from.exists()) {
            LogManager.error("File " + from.getAbsolutePath() + " cannot be copied to " + to.getAbsolutePath() + " as"
                    + " it doesn't exist");
            return false;
        }
        if (!withFilename) {
            to = new File(to, from.getName());
        }
        if (to.exists()) {
            to.delete();
        }

        try {
            to.createNewFile();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return false;
        }

        FileChannel source = null;
        FileChannel destination = null;

        LogManager.debug("Copying file from " + from.getAbsolutePath() + " to " + to.getAbsolutePath());

        try {
            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return false;
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                return false;
            }
        }
        return true;
    }

    public static boolean safeCopy(File from, File to) throws IOException {
        if (to.exists()) {
            to.delete();
        }

        try (InputStream is = new FileInputStream(from); OutputStream os = new FileOutputStream(to)) {

            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
        }

        return true;
    }

    /**
     * Move directory.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @return true, if successful
     */
    public static boolean moveDirectory(File sourceLocation, File targetLocation) {
        if (copyDirectory(sourceLocation, targetLocation)) {
            delete(sourceLocation);
            return true;
        } else {
            LogManager.error("Couldn't move directory " + sourceLocation.getAbsolutePath() + " to "
                    + targetLocation.getAbsolutePath());
            return false;
        }
    }

    /**
     * Copy directory.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @return true, if successful
     */
    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        return copyDirectory(sourceLocation, targetLocation, false);
    }

    /**
     * Copy directory.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @param copyFolder     the copy folder
     * @return true, if successful
     */
    public static boolean copyDirectory(File sourceLocation, File targetLocation, boolean copyFolder) {
        if (copyFolder) {
            targetLocation = new File(targetLocation, sourceLocation.getName());
        }
        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdirs();
                }

                String[] children = sourceLocation.list();
                for (String child : children) {
                    copyDirectory(new File(sourceLocation, child), new File(targetLocation, child));
                }
            } else {

                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return false;
        }
        return true;
    }

    /**
     * Unzip.
     *
     * @param in  the in
     * @param out the out
     */
    public static void unzip(File in, File out) {
        unzip(in, out, null);
    }

    /**
     * Unzip.
     *
     * @param in          the in
     * @param out         the out
     * @param extractRule the extract rule
     */
    public static void unzip(File in, File out, ExtractRule extractRule) {
        try {
            ZipFile zipFile;
            if (!out.exists()) {
                out.mkdirs();
            }
            zipFile = new ZipFile(in);
            Enumeration<?> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String entryName = entry.getName();
                if (entry.getName().endsWith("aux.class")) {
                    entryName = "aux_class";
                }
                if (extractRule != null && extractRule.shouldExclude(entryName)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    File folder = new File(out, entryName);
                    folder.mkdirs();
                }
                File destinationFilePath = new File(out, entryName);
                destinationFilePath.getParentFile().mkdirs();
                if (!entry.isDirectory() && !entry.getName().equals(".minecraft")) {
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    byte[] buffer = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    /**
     * Delete.
     *
     * @param file the file
     */
    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory() && !isSymlink(file)) {
            if (file.listFiles() != null) {
                for (File c : file.listFiles()) {
                    delete(c);
                }
            }
        }

        if (!file.delete()) {
            LogManager.error(
                    (file.isFile() ? "File" : "Folder") + " " + file.getAbsolutePath() + " couldn't be " + "deleted");
        }
    }

    public static boolean isSymlink(File file) {
        if (file == null) {
            throw new NullPointerException("File must not be null");
        }

        File canon;

        if (file.getParent() == null) {
            canon = file;
        } else {
            File canonDir;

            try {
                canonDir = file.getParentFile().getCanonicalFile();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to get canonical file", e);
                return false;
            }

            canon = new File(canonDir, file.getName());
        }

        try {
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to get canonical file", e);
            return false;
        }
    }

    public static void deleteWithFilter(File file, final List<String> files) {
        deleteWithFilter(file, files, false);
    }

    public static void deleteWithFilter(File file, final List<String> files, boolean delete) {
        FilenameFilter ffFilter;

        if (delete) {
            ffFilter = (dir, name) -> files.contains(name);
        } else {
            ffFilter = (dir, name) -> !files.contains(name);
        }

        for (File aFile : file.listFiles(ffFilter)) {
            Utils.delete(aFile);
        }
    }

    /**
     * Delete contents.
     *
     * @param file the file
     */
    public static void deleteContents(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                // No contents in this folder so there are no files to delete
                return;
            }
            for (File c : files) {
                delete(c);
            }
        }
    }

    /**
     * Zip.
     *
     * @param in  the in
     * @param out the out
     */
    public static void zip(File in, File out) {
        try {
            URI base = in.toURI();
            Deque<File> queue = new LinkedList<>();
            queue.push(in);
            OutputStream stream = new FileOutputStream(out);
            Closeable res = stream;
            ZipOutputStream zout = null;
            try {
                zout = new ZipOutputStream(stream);
                res = zout;
                while (!queue.isEmpty()) {
                    in = queue.pop();
                    for (File kid : in.listFiles()) {
                        String name = base.relativize(kid.toURI()).getPath();
                        if (name.endsWith("aux_class")) {
                            name = "aux.class";
                        }
                        if (kid.isDirectory()) {
                            queue.push(kid);
                            name = name.endsWith("/") ? name : name + "/";
                            zout.putNextEntry(new ZipEntry(name));
                        } else {
                            zout.putNextEntry(new ZipEntry(name));
                            copy(kid, zout);
                            zout.closeEntry();
                        }
                    }
                }
            } finally {
                res.close();
                if (zout != null) {
                    zout.close();
                }
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    /**
     * Copy.
     *
     * @param in  the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    /**
     * Copy.
     *
     * @param file the file
     * @param out  the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void copy(File file, OutputStream out) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            copy(in, out);
        }
    }

    /**
     * Encrypt.
     *
     * @param Data the data
     * @return the string
     */
    public static String encrypt(String Data) {
        Key key;
        String encryptedValue = null;
        try {
            key = generateKey();
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(Data.getBytes());
            encryptedValue = Base64.encodeBytes(encVal);
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
        return encryptedValue;
    }

    /**
     * Decrypt.
     *
     * @param encryptedData the encrypted data
     * @return the string
     */
    public static String decrypt(String encryptedData) {
        Key key;
        String decryptedValue = null;
        try {
            key = generateKey();
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            return Utils.decryptOld(encryptedData);
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
        return decryptedValue;
    }

    /**
     * Decrypt using old method.
     *
     * @param encryptedData the encrypted data
     * @return the string
     */
    public static String decryptOld(String encryptedData) {
        Key key;
        String decryptedValue = null;
        try {
            key = new SecretKeySpec("NotARandomKeyYes".getBytes(), "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decryptedValue = new String(decValue);
        } catch (Exception ignored) {
        }
        return decryptedValue;
    }

    /**
     * Generate key.
     *
     * @return the key
     * @throws Exception the exception
     */
    private static Key generateKey() throws Exception {
        return new SecretKeySpec(getMACAdressHash().getBytes(), 0, 16, "AES");
    }

    /**
     * Replace text.
     *
     * @param originalFile    the original file
     * @param destinationFile the destination file
     * @param replaceThis     the replace this
     * @param withThis        the with this
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void replaceText(InputStream fs, File destinationFile, String replaceThis, String withThis)
            throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(fs));

        FileWriter writer1 = new FileWriter(destinationFile);

        String line = br.readLine();
        while (line != null) {
            if (line.contains(replaceThis)) {
                line = line.replace(replaceThis, withThis);
            }
            writer1.write(line);
            writer1.write(System.getProperty("line.separator"));
            line = br.readLine();
        }
        writer1.flush();
        writer1.close();
        br.close();
    }

    /**
     * Send post data.
     *
     * @param urll the urll
     * @param text the text
     * @param key  the key
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String sendPostData(String urll, String text, String key) throws IOException {
        String write = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(text, "UTF-8");
        StringBuilder response;
        URL url = new URL(urll);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", Network.USER_AGENT);
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");

        connection.setRequestProperty("Content-Length", "" + write.getBytes().length);

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(write.getBytes());
        writer.flush();
        writer.close();

        // Read the result

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        reader.close();
        return response.toString();
    }

    public static String sendAPICall(String path, Object data) throws IOException {
        StringBuilder response;

        byte[] contents = Gsons.DEFAULT.toJson(data).getBytes();

        URL url = new URL(Constants.API_BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", Network.USER_AGENT);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");

        connection.setRequestProperty("Content-Length", "" + contents.length);

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
        writer.write(contents);
        writer.flush();
        writer.close();

        // Read the result

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        reader.close();
        return response.toString();
    }

    public static String sendGetAPICall(String path) throws IOException {
        StringBuilder response;

        URL url = new URL(Constants.API_BASE_URL + path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", Network.USER_AGENT);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        // Read the result

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        reader.close();
        return response.toString();
    }

    /**
     * Checks for meta inf.
     *
     * @param minecraftJar the minecraft jar
     * @return true, if successful
     */
    public static boolean hasMetaInf(File minecraftJar) {
        JarInputStream input = null;
        try {
            input = new JarInputStream(new FileInputStream(minecraftJar));
            JarEntry entry;
            boolean found = false;
            while ((entry = input.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    found = true;
                }
            }
            return found;
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LogManager.logStackTrace("Unable to close input stream", e);
                }
            }
        }
        return false;
    }

    /**
     * Gets the instance file filter.
     *
     * @return the instance file filter
     */
    public static FilenameFilter getInstanceFileFilter() {
        return (dir, name) -> {
            File instanceDir = new File(dir, name);
            if (instanceDir.isDirectory()) {
                return new File(instanceDir, "instance.json").exists();
            }
            return false;
        };
    }

    public static FilenameFilter getServerFileFilter() {
        return (dir, name) -> {
            File serverDir = new File(dir, name);
            if (serverDir.isDirectory()) {
                return new File(serverDir, "server.json").exists();
            }
            return false;
        };
    }

    /**
     * Gets the open eye pending reports file filter.
     *
     * @return the open eye pending reports file filter
     */
    public static FilenameFilter getOpenEyePendingReportsFileFilter() {
        return (dir, name) -> {
            File file = new File(dir, name);
            Pattern pattern = Pattern.compile("^pending-crash-[0-9\\-_.]+\\.json$");
            return file.isFile() && pattern.matcher(name).matches();
        };
    }

    /**
     * Sends a pending crash report generated by OpenEye and retrieves and returns
     * it's response to display to the user.
     *
     * @param report a {@link File} object of the pending crash report to send the
     *               contents of
     * @return the response received from OpenEye about the crash that was sent
     *         which is of {@link OpenEyeReportResponse} type
     */
    public static OpenEyeReportResponse sendOpenEyePendingReport(File report) {
        StringBuilder response;
        String request = Utils.getFileContents(report);
        if (request == null) {
            LogManager.error("OpenEye: Couldn't read contents of file '" + report.getAbsolutePath() + "'. Pending "
                    + "report sending failed!");
            return null;
        }

        HttpURLConnection connection;

        try {
            URL url = new URL("http://openeye.openmods.info/api/v1/crash");
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes(StandardCharsets.UTF_8));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return null; // Report not sent
        }

        // Read the result

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return null; // Report sent, but no response
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        // Return an OpenEyeReportResponse object from the singular array returned in
        // JSON
        return Gsons.DEFAULT.fromJson(response.toString(), OpenEyeReportResponse[].class)[0];
    }

    /**
     * Gets the file contents.
     *
     * @param file the file
     * @return the file contents
     */
    public static String getFileContents(File file) {
        if (!file.exists()) {
            LogManager.error("File '" + file.getAbsolutePath() + "' doesn't exist so cannot read contents of file!");
            return null;
        }
        String contents = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
                line = br.readLine();
            }
            contents = sb.toString();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
        return contents;
    }

    /**
     * This splits up a string into a multi lined string by adding a separator at
     * every space after a given count.
     *
     * @param string        the string to split up
     * @param maxLineLength the number of characters minimum to have per line
     * @param lineSeparator the string to place when a new line should be placed
     * @return the new multi lined string
     */
    public static String splitMultilinedString(String string, int maxLineLength, String lineSeparator) {
        char[] chars = string.toCharArray();
        StringBuilder sb = new StringBuilder();
        char spaceChar = " ".charAt(0);
        int count = 0;
        for (char character : chars) {
            if (count >= maxLineLength && character == spaceChar) {
                sb.append(lineSeparator);
                count = 0;
            } else {
                count++;
                sb.append(character);
            }
        }
        return sb.toString();
    }

    public static boolean testProxy(Proxy proxy) {
        try {
            HttpURLConnection connection;
            URL url = new URL(String.format("%s/ping", Constants.DOWNLOAD_SERVER));
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", Network.USER_AGENT);
            connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.connect();
            LogManager.info("Proxy returned code " + connection.getResponseCode() + " when testing!");
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            LogManager.logStackTrace("Proxy couldn't establish a connection when testing!", e);
            return false;
        }
    }

    public static FilenameFilter getThemesFileFilter() {
        return (dir, name) -> {
            File file = new File(dir, name);
            return file.exists() && file.isFile() && name.endsWith(".zip");
        };
    }

    /**
     * Flips a given {@link BufferedImage}
     *
     * @param image The image to flip
     * @return The flipped image
     */
    public static BufferedImage flipImage(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);
        return image;
    }

    /**
     * Counts the numbers of non transparent pixels in a given
     * {@link BufferedImage}.
     *
     * @param image The image to count the number of non transparent pixels in
     * @return The number of non transparent pixels
     */
    public static int nonTransparentPixels(BufferedImage image) {
        int count = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (image.getRGB(x, y) == -1) {
                    count++;
                }
            }
        }
        return count;
    }

    public static String pingAddress(String host) {
        String pingStats = "";
        StringBuilder response = new StringBuilder();
        try {
            InetAddress address = InetAddress.getByName(host);
            Process traceRoute;
            if (OS.isWindows()) {
                traceRoute = Runtime.getRuntime().exec("ping -n 10 " + address.getHostAddress());
            } else {
                traceRoute = Runtime.getRuntime().exec("ping -c 10 " + address.getHostAddress());
            }

            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(traceRoute.getInputStream()));

            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();

            pingStats = response.toString();

        } catch (IOException e) {
            LogManager.logStackTrace("IOException while running ping on host " + host, e);
        }

        return pingStats;
    }

    public static String traceRoute(String host) {
        String route = "";
        StringBuilder response;
        try {
            InetAddress address = InetAddress.getByName(host);
            Process traceRoute;
            if (OS.isWindows()) {
                traceRoute = Runtime.getRuntime().exec("tracert " + address.getHostAddress());
            } else {
                traceRoute = Runtime.getRuntime().exec("traceroute " + address.getHostAddress());
            }

            BufferedReader reader;
            reader = new BufferedReader(new InputStreamReader(traceRoute.getInputStream()));

            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();

            route = response.toString();

        } catch (IOException e) {
            LogManager.logStackTrace("IOException while running traceRoute on host " + host, e);
        }

        return route;
    }

    // public static Object[] prepareMessageForMinecraftLog(String text) {
    // LogType type = null; // The log message type
    // String message = null; // The log message

    // if (text.contains("[INFO] [STDERR]")) {
    // message = text.substring(text.indexOf("[INFO] [STDERR]"));
    // type = LogType.WARN;
    // } else if (text.contains("[INFO]")) {
    // message = text.substring(text.indexOf("[INFO]"));
    // if (message.contains("CONFLICT")) {
    // type = LogType.ERROR;
    // } else if (message.contains("overwriting existing item")) {
    // type = LogType.WARN;
    // } else {
    // type = LogType.INFO;
    // }
    // } else if (text.contains("[WARNING]")) {
    // message = text.substring(text.indexOf("[WARNING]"));
    // type = LogType.WARN;
    // } else if (text.contains("WARNING:")) {
    // message = text.substring(text.indexOf("WARNING:"));
    // type = LogType.WARN;
    // } else if (text.contains("INFO:")) {
    // message = text.substring(text.indexOf("INFO:"));
    // type = LogType.INFO;
    // } else if (text.contains("Exception")) {
    // message = text;
    // type = LogType.ERROR;
    // } else if (text.contains("[SEVERE]")) {
    // message = text.substring(text.indexOf("[SEVERE]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[Sound Library Loader/ERROR]")) {
    // message = text.substring(text.indexOf("[Sound Library Loader/ERROR]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[Sound Library Loader/WARN]")) {
    // message = text.substring(text.indexOf("[Sound Library Loader/WARN]"));
    // type = LogType.WARN;
    // } else if (text.contains("[Sound Library Loader/INFO]")) {
    // message = text.substring(text.indexOf("[Sound Library Loader/INFO]"));
    // type = LogType.INFO;
    // } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
    // message = text.substring(text.indexOf("[MCO Availability Checker
    // #1/ERROR]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
    // message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]"));
    // type = LogType.WARN;
    // } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
    // message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]"));
    // type = LogType.INFO;
    // } else if (text.contains("[Client thread/ERROR]")) {
    // message = text.substring(text.indexOf("[Client thread/ERROR]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[Client thread/WARN]")) {
    // message = text.substring(text.indexOf("[Client thread/WARN]"));
    // type = LogType.WARN;
    // } else if (text.contains("[Client thread/INFO]")) {
    // message = text.substring(text.indexOf("[Client thread/INFO]"));
    // type = LogType.INFO;
    // } else if (text.contains("[Server thread/ERROR]")) {
    // message = text.substring(text.indexOf("[Server thread/ERROR]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[Server thread/WARN]")) {
    // message = text.substring(text.indexOf("[Server thread/WARN]"));
    // type = LogType.WARN;
    // } else if (text.contains("[Server thread/INFO]")) {
    // message = text.substring(text.indexOf("[Server thread/INFO]"));
    // type = LogType.INFO;
    // } else if (text.contains("[main/ERROR]")) {
    // message = text.substring(text.indexOf("[main/ERROR]"));
    // type = LogType.ERROR;
    // } else if (text.contains("[main/WARN]")) {
    // message = text.substring(text.indexOf("[main/WARN]"));
    // type = LogType.WARN;
    // } else if (text.contains("[main/INFO]")) {
    // message = text.substring(text.indexOf("[main/INFO]"));
    // type = LogType.INFO;
    // } else {
    // message = text;
    // type = LogType.INFO;
    // }

    // return new Object[] { type, message };
    // }

    public static byte[] readFile(File file) {
        byte[] bytes = null;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r");
            bytes = new byte[(int) f.length()];
            f.read(bytes);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }
        return bytes;
    }

    public static void unXZPackFile(File inputFile, File outputFile) throws IOException {
        File packFile = new File(inputFile.getAbsolutePath().substring(0, inputFile.getAbsolutePath().length() - 3));
        LogManager.debug("unXZPackFile " + inputFile.getAbsolutePath() + " : " + packFile.getAbsolutePath() + " : "
                + outputFile.getAbsolutePath());

        unXZFile(inputFile, packFile);
        unpackFile(packFile, outputFile);

        Utils.delete(inputFile);
        Utils.delete(packFile);
    }

    public static void unLzmaFile(File input, File output) {
        if (output.exists()) {
            Utils.delete(output);
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        LZMAInputStream lis = null;

        try {
            fis = new FileInputStream(input);
            lis = new LZMAInputStream(fis);
            fos = new FileOutputStream(output);

            final byte[] buffer = new byte[8192];
            int n;
            while (-1 != (n = lis.read(buffer))) {
                fos.write(buffer, 0, n);
            }

        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (fos != null) {
                    fos.close();
                }
                if (lis != null) {
                    lis.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    public static void unXZFile(File input, File output) throws IOException {
        FileInputStream fis;
        FileOutputStream fos;
        XZInputStream xzis;
        fis = new FileInputStream(input);
        xzis = new XZInputStream(fis);
        fos = new FileOutputStream(output);

        final byte[] buffer = new byte[8192];
        int n;
        while (-1 != (n = xzis.read(buffer))) {
            fos.write(buffer, 0, n);
        }

        if (fis != null) {
            fis.close();
        }
        if (fos != null) {
            fos.close();
        }
        if (xzis != null) {
            xzis.close();
        }
    }

    /*
     * From: http://atl.pw/1
     */
    public static void unpackFile(File input, File output) throws IOException {
        if (output.exists()) {
            Utils.delete(output);
        }

        byte[] decompressed = readFile(input);

        if (decompressed == null) {
            LogManager.error("unpackFile: While reading in " + input.getName() + " the file returned null");
            return;
        }

        String end = new String(decompressed, decompressed.length - 4, 4);
        if (!end.equals("SIGN")) {
            LogManager.error("unpackFile: Unpacking failed, signature missing " + end);
            return;
        }

        int x = decompressed.length;
        int len = ((decompressed[x - 8] & 0xFF)) | ((decompressed[x - 7] & 0xFF) << 8)
                | ((decompressed[x - 6] & 0xFF) << 16) | ((decompressed[x - 5] & 0xFF) << 24);
        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);
        FileOutputStream jarBytes = new FileOutputStream(output);
        JarOutputStream jos = new JarOutputStream(jarBytes);

        Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);

        jos.putNextEntry(new JarEntry("checksums.sha1"));
        jos.write(checksums);
        jos.closeEntry();

        jos.close();
        jarBytes.close();
    }

    private static String getMACAdressHash() {
        String returnStr = null;
        try {
            InetAddress ip;
            ip = InetAddress.getLocalHost();

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            // If network is null, user may be using Linux or something it doesn't support
            // so try alternative way
            if (network == null) {
                Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

                while (e.hasMoreElements()) {
                    NetworkInterface n = e.nextElement();
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress i = ee.nextElement();
                        if (!i.isLoopbackAddress() && !i.isLinkLocalAddress() && i.isSiteLocalAddress()) {
                            ip = i;
                        }
                    }
                }

                network = NetworkInterface.getByInetAddress(ip);
            }

            // If network is still null, well you're SOL
            if (network != null) {
                byte[] mac = network.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    returnStr = sb.toString();
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        } finally {
            returnStr = (returnStr == null ? "NotARandomKeyYes" : returnStr);
        }

        return Hashing.md5(returnStr).toString();
    }

    public static String convertMavenIdentifierToPath(String identifier) {
        String[] parts = identifier.split(":", 3);
        String name = parts[1];
        String version = parts[2];
        String extension = "jar";
        String classifier = "";

        if (version.indexOf('@') != -1) {
            extension = version.substring(version.indexOf('@') + 1);
            version = version.substring(0, version.indexOf('@'));
        }

        if (version.indexOf(':') != -1) {
            classifier = "-" + version.substring(version.indexOf(':') + 1);
            version = version.substring(0, version.indexOf(':'));
        }

        String path = parts[0].replace(".", "/") + "/" + name + "/" + version + "/" + name + "-" + version + classifier
                + "." + extension;

        return path;
    }

    public static File convertMavenIdentifierToFile(String identifier, File base) {
        return new File(base, convertMavenIdentifierToPath(identifier).replace("/", File.separatorChar + ""));
    }

    public static boolean matchVersion(String version, String matches, boolean lessThan, boolean equal) {
        String[] versionParts = version.split("\\.", 3);
        String[] matchedParts = matches.split("\\.", 2);

        if (equal && versionParts[0].equals(matchedParts[0]) && versionParts[1].equals(matchedParts[1])) {
            return true;
        }

        if (lessThan && versionParts[0].equals(matchedParts[0])
                && Integer.parseInt(versionParts[1]) < Integer.parseInt(matchedParts[1])) {
            return true;
        }

        if (!lessThan && versionParts[0].equals(matchedParts[0])
                && Integer.parseInt(versionParts[1]) > Integer.parseInt(matchedParts[1])) {
            return true;
        }

        return false;
    }

    public static MCMod getMCModForFile(File file) {
        try {
            java.lang.reflect.Type type = new TypeToken<List<MCMod>>() {
            }.getType();

            List<MCMod> mods = Gsons.MINECRAFT.fromJson(new String(ZipUtil.unpackEntry(file, "mcmod.info")), type);

            if (mods.size() != 0 && mods.get(0) != null) {
                return mods.get(0);
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    public static FabricMod getFabricModForFile(File file) {
        try {
            FabricMod mod = Gsons.MINECRAFT.fromJson(new String(ZipUtil.unpackEntry(file, "fabric.mod.json")),
                    FabricMod.class);

            if (mod != null) {
                return mod;
            }
        } catch (Exception ignored2) {

        }

        return null;
    }

    public static boolean executableInPath(String executableName) {
        try {
            return java.util.stream.Stream
                    .of(System.getenv("PATH").split(java.util.regex.Pattern.quote(File.pathSeparator)))
                    .map(path -> path.replace("\"", "")).map(Paths::get)
                    .anyMatch(path -> Files.exists(path.resolve(executableName))
                            && Files.isExecutable(path.resolve(executableName)));
        } catch (Exception e) {
            return false;
        }
    }

    public static String runProcess(String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();

            try {
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                br.close();
            }

            return sb.toString().trim();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return "";
    }

    public static boolean isAcceptedModFile(File file) {
        return isAcceptedModFile(file.getName());
    }

    public static boolean isAcceptedModFile(Path path) {
        return isAcceptedModFile(path.getFileName().toString());
    }

    public static boolean isAcceptedModFile(String filename) {
        return filename.endsWith(".jar") || filename.endsWith(".zip") || filename.endsWith(".litemod");
    }
}
