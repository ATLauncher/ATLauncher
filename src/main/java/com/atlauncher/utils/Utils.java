/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;
import javax.swing.text.html.StyleSheet;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Settings;
import com.atlauncher.data.mojang.ExtractRule;
import com.atlauncher.data.mojang.OperatingSystem;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.gui.ProgressDialog;

// TODO: Auto-generated Javadoc
/**
 * The Class Utils.
 */
public class Utils {

    /**
     * Gets the icon image.
     * 
     * @param path
     *            the path
     * @return the icon image
     */
    public static ImageIcon getIconImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            App.settings.log("Unable to load resource " + path, LogMessageType.error, false);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);

        return icon;
    }

    public static File getCoreGracefully() {
        if (Utils.isLinux()) {
            return new File(App.class.getProtectionDomain().getCodeSource().getLocation().getFile())
                    .getParentFile();
        } else {
            return new File(System.getProperty("user.dir"));
        }
    }

    /**
     * Gets the icon image.
     * 
     * @param file
     *            the file
     * @return the icon image
     */
    public static ImageIcon getIconImage(File file) {
        if (!file.exists()) {
            App.settings.log("Unable to load file " + file.getAbsolutePath(), LogMessageType.error,
                    false);
            return null;
        }

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

        return icon;
    }

    /**
     * Gets the font.
     * 
     * @return the font
     */
    public static Font getFont() {
        if (isMac()) {
            return new Font("SansSerif", Font.PLAIN, 11);
        } else {
            return new Font("SansSerif", Font.PLAIN, 12);
        }
    }

    /**
     * Gets the image.
     * 
     * @param path
     *            the path
     * @return the image
     */
    public static Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            App.settings.log("Unable to load resource " + path, LogMessageType.error, false);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);

        return icon.getImage();
    }

    /**
     * Open explorer.
     * 
     * @param file
     *            the file
     */
    public static void openExplorer(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    /**
     * Open browser.
     * 
     * @param URL
     *            the url
     */
    public static void openBrowser(String URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(URL));
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    /**
     * Open browser.
     * 
     * @param URL
     *            the url
     */
    public static void openBrowser(URL URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URL.toURI());
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    /**
     * Make font.
     * 
     * @param name
     *            the name
     * @return the font
     */
    public static Font makeFont(String name) {
        Font font = null;
        boolean found = false; // If the user has the font
        GraphicsEnvironment g = null;
        g = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = g.getAvailableFontFamilyNames();
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(name)) {
                found = true;
            }
        }
        if (found) {
            return new Font(name, Font.PLAIN, 0);
        }
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    System.class.getResource("/assets/font/" + name + ".ttf").openStream());
        } catch (FontFormatException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return font;
    }

    /**
     * Os slash.
     * 
     * @return the string
     */
    public static String osSlash() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    /**
     * Os delimiter.
     * 
     * @return the string
     */
    public static String osDelimiter() {
        if (isWindows()) {
            return ";";
        } else {
            return ":";
        }
    }

    /**
     * Gets the java home.
     * 
     * @return the java home
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    /**
     * Gets the java version.
     * 
     * @return the java version
     */
    public static String getJavaVersion() {
        return System.getProperty("java.runtime.version");
    }

    /**
     * Checks if is windows.
     * 
     * @return true, if is windows
     */
    public static boolean isWindows() {
        return OperatingSystem.getOS() == OperatingSystem.WINDOWS;
    }

    /**
     * Checks if is mac.
     * 
     * @return true, if is mac
     */
    public static boolean isMac() {
        return OperatingSystem.getOS() == OperatingSystem.OSX;
    }

    /**
     * Checks if is linux.
     * 
     * @return true, if is linux
     */
    public static boolean isLinux() {
        return OperatingSystem.getOS() == OperatingSystem.LINUX;
    }

    /**
     * Checks if is 64 bit.
     * 
     * @return true, if is 64 bit
     */
    public static boolean is64Bit() {
        String osType = System.getProperty("sun.arch.data.model");
        return Boolean.valueOf(osType.contains("64"));
    }

    /**
     * Gets the arch.
     * 
     * @return the arch
     */
    public static String getArch() {
        if (is64Bit()) {
            return "64";
        } else {
            return "32";
        }
    }

    /**
     * Gets the memory options.
     * 
     * @return the memory options
     */
    public static String[] getMemoryOptions() {
        int options = Utils.getMaximumRam() / 512;
        int ramLeft = 0;
        int count = 0;
        String[] ramOptions = new String[options];
        while ((ramLeft + 512) <= Utils.getMaximumRam()) {
            ramLeft = ramLeft + 512;
            ramOptions[count] = ramLeft + " MB";
            count++;
        }
        return ramOptions;
    }

    /**
     * Returns the amount of RAM in the users system.
     * 
     * @return The amount of RAM in the system
     */
    public static int getSystemRam() {
        long ramm = 0;
        int ram = 0;
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            Method m = operatingSystemMXBean.getClass().getDeclaredMethod(
                    "getTotalPhysicalMemorySize");
            m.setAccessible(true);
            Object value = m.invoke(operatingSystemMXBean);
            if (value != null) {
                ramm = Long.parseLong(value.toString());
                ram = (int) (ramm / 1048576);
            } else {
                ram = 1024;
            }
        } catch (SecurityException e) {
            App.settings.logStackTrace(e);
        } catch (NoSuchMethodException e) {
            App.settings.logStackTrace(e);
        } catch (IllegalArgumentException e) {
            App.settings.logStackTrace(e);
        } catch (IllegalAccessException e) {
            App.settings.logStackTrace(e);
        } catch (InvocationTargetException e) {
            App.settings.logStackTrace(e);
        }
        return ram;
    }

    /**
     * Returns the maximum RAM available to Java. If on 64 Bit system then its all of the System RAM
     * otherwise its limited to 1GB or less due to allocations of PermGen
     * 
     * @return The maximum RAM available to Java
     */
    public static int getMaximumRam() {
        int maxRam = getSystemRam();
        if (!is64Bit()) {
            if (maxRam < 1024) {
                return maxRam;
            } else {
                return 1024;
            }
        } else {
            return maxRam;
        }
    }

    /**
     * Returns the safe amount of maximum ram available to Java. This is set to half of the total
     * maximum ram available to Java in order to not allocate too much and leave enough RAM for the
     * OS and other application
     * 
     * @return Half the maximum RAM available to Java
     */
    public static int getSafeMaximumRam() {
        int maxRam = getSystemRam();
        if (!is64Bit()) {
            if (maxRam < 1024) {
                return maxRam / 2;
            } else {
                return 512;
            }
        } else {
            return maxRam / 2;
        }
    }

    /**
     * Gets the maximum window width.
     * 
     * @return the maximum window width
     */
    public static int getMaximumWindowWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.width;
    }

    /**
     * Gets the maximum window height.
     * 
     * @return the maximum window height
     */
    public static int getMaximumWindowHeight() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.height;
    }

    /**
     * Upload paste.
     * 
     * @param title
     *            the title
     * @param log
     *            the log
     * @return the string
     */
    public static String uploadPaste(String title, String log) {
        String line = "";
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
            App.settings.logStackTrace(e1);
        }
        return result;
    }

    /**
     * Gets the m d5.
     * 
     * @param file
     *            the file
     * @return the m d5
     */
    public static String getMD5(File file) {
        if (!file.exists()) {
            App.settings.log(
                    "Cannot get MD5 of " + file.getAbsolutePath() + " as it doesn't exist",
                    LogMessageType.error, false);
            return "0"; // File doesn't exists so MD5 is nothing
        }
        StringBuffer sb = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            if (fis != null) {
                fis.close();
            }
        } catch (NoSuchAlgorithmException e) {
            App.settings.logStackTrace(e);
        } catch (FileNotFoundException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return sb.toString();
    }

    /**
     * Gets the SH a1.
     * 
     * @param file
     *            the file
     * @return the SH a1
     */
    public static String getSHA1(File file) {
        if (!file.exists()) {
            App.settings.log("Cannot get SHA-1 hash of " + file.getAbsolutePath()
                    + " as it doesn't exist", LogMessageType.error, false);
            return "0"; // File doesn't exists so MD5 is nothing
        }
        StringBuffer sb = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            FileInputStream fis = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            if (fis != null) {
                fis.close();
            }
        } catch (NoSuchAlgorithmException e) {
            App.settings.logStackTrace(e);
        } catch (FileNotFoundException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return sb.toString();
    }

    /**
     * Gets the m d5.
     * 
     * @param string
     *            the string
     * @return the m d5
     */
    public static String getMD5(String string) {
        if (string == null) {
            App.settings.log("Cannot get MD5 of null", LogMessageType.error, false);
            return "0"; // String null so return 0
        }
        StringBuffer sb = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytesOfMessage = string.getBytes("UTF-8");
            byte[] mdbytes = md.digest(bytesOfMessage);

            // convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return sb.toString();
    }

    /**
     * Move file.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     * @param withFilename
     *            the with filename
     * @return true, if successful
     */
    public static boolean moveFile(File from, File to, boolean withFilename) {
        if (copyFile(from, to, withFilename)) {
            delete(from);
            return true;
        } else {
            App.settings.log(
                    "Couldn't move file " + from.getAbsolutePath() + " to " + to.getAbsolutePath(),
                    LogMessageType.error, false);
            return false;
        }
    }

    /**
     * Copy file.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     * @return true, if successful
     */
    public static boolean copyFile(File from, File to) {
        return copyFile(from, to, false);
    }

    /**
     * Copy file.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     * @param withFilename
     *            the with filename
     * @return true, if successful
     */
    public static boolean copyFile(File from, File to, boolean withFilename) {
        if (!from.isFile()) {
            App.settings.log(
                    "File " + from.getAbsolutePath() + " cannot be copied to "
                            + to.getAbsolutePath() + " as it isn't a file", LogMessageType.error,
                    false);
            return false;
        }
        if (!from.exists()) {
            App.settings.log(
                    "File " + from.getAbsolutePath() + " cannot be copied to "
                            + to.getAbsolutePath() + " as it doesn't exist", LogMessageType.error,
                    false);
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
            App.settings.logStackTrace(e);
            return false;
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
                App.settings.logStackTrace(e);
                return false;
            }
        }
        return true;
    }

    /**
     * Move directory.
     * 
     * @param sourceLocation
     *            the source location
     * @param targetLocation
     *            the target location
     * @return true, if successful
     */
    public static boolean moveDirectory(File sourceLocation, File targetLocation) {
        if (copyDirectory(sourceLocation, targetLocation)) {
            delete(sourceLocation);
            return true;
        } else {
            App.settings.log("Couldn't move directory " + sourceLocation.getAbsolutePath() + " to "
                    + targetLocation.getAbsolutePath(), LogMessageType.error, false);
            return false;
        }
    }

    /**
     * Copy directory.
     * 
     * @param sourceLocation
     *            the source location
     * @param targetLocation
     *            the target location
     * @return true, if successful
     */
    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        return copyDirectory(sourceLocation, targetLocation, false);
    }

    /**
     * Copy directory.
     * 
     * @param sourceLocation
     *            the source location
     * @param targetLocation
     *            the target location
     * @param copyFolder
     *            the copy folder
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
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation,
                            children[i]));
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
            App.settings.logStackTrace(e);
            return false;
        }
        return true;
    }

    /**
     * Unzip.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     */
    public static void unzip(File in, File out) {
        unzip(in, out, null);
    }

    /**
     * Unzip.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     * @param extractRule
     *            the extract rule
     */
    public static void unzip(File in, File out, ExtractRule extractRule) {
        try {
            ZipFile zipFile = null;
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
                    byte buffer[] = new byte[1024];
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
            App.settings.logStackTrace(e);
        }
    }

    /**
     * Clean temp directory.
     */
    public static void cleanTempDirectory() {
        File file = App.settings.getTempDir();
        String[] myFiles;
        if (file.isDirectory()) {
            myFiles = file.list();
            for (int i = 0; i < myFiles.length; i++) {
                new File(file, myFiles[i]).delete();
            }
        }
    }

    /**
     * Delete.
     * 
     * @param file
     *            the file
     */
    public static void delete(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        if (!file.delete()) {
            App.settings.log((file.isFile() ? "File" : "Folder") + " " + file.getAbsolutePath()
                    + " couldn't be deleted", LogMessageType.error, false);
        }
    }

    /**
     * Spread out resource files.
     * 
     * @param dir
     *            the dir
     */
    public static void spreadOutResourceFiles(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                spreadOutResourceFiles(file);
            } else {
                String hash = getSHA1(file);
                File saveTo = new File(App.settings.getObjectsAssetsDir(), hash.substring(0, 2)
                        + File.separator + hash);
                saveTo.mkdirs();
                copyFile(file, saveTo, true);
            }
        }
    }

    /**
     * Delete contents.
     * 
     * @param file
     *            the file
     */
    public static void deleteContents(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                delete(c);
        } else {
            return;
        }
    }

    /**
     * Zip.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     */
    public static void zip(File in, File out) {
        try {
            URI base = in.toURI();
            Deque<File> queue = new LinkedList<File>();
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
                if (zout != null)
                    zout.close();
            }
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

    /**
     * Copy.
     * 
     * @param in
     *            the in
     * @param out
     *            the out
     * @throws IOException
     *             Signals that an I/O exception has occurred.
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
     * @param file
     *            the file
     * @param out
     *            the out
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

    /**
     * Encrypt.
     * 
     * @param Data
     *            the data
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
            App.settings.logStackTrace(e);
        }
        return encryptedValue;
    }

    /**
     * Decrypt.
     * 
     * @param encryptedData
     *            the encrypted data
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
        } catch (Exception e) {
            App.settings.logStackTrace(e);
        }
        return decryptedValue;
    }

    /**
     * Generate key.
     * 
     * @return the key
     * @throws Exception
     *             the exception
     */
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec("NotARandomKeyYes".getBytes(), "AES");
        return key;
    }

    /**
     * Replace text.
     * 
     * @param originalFile
     *            the original file
     * @param destinationFile
     *            the destination file
     * @param replaceThis
     *            the replace this
     * @param withThis
     *            the with this
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void replaceText(File originalFile, File destinationFile, String replaceThis,
            String withThis) throws IOException {

        FileInputStream fs = new FileInputStream(originalFile);
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
        fs.close();
    }

    /**
     * Upload log.
     * 
     * @return the string
     */
    public static String uploadLog() {
        final ProgressDialog dialog = new ProgressDialog(
                App.settings.getLocalizedString("console.uploadinglog"), 0,
                App.settings.getLocalizedString("console.uploadinglog"), "Aborting log upload!");
        dialog.addThread(new Thread() {
            @Override
            public void run() {
                String result = Utils.uploadPaste("ATLauncher Log", App.settings.getLog());
                dialog.setReturnValue(result);
                dialog.close();
            }
        });
        dialog.start();
        return (String) dialog.getReturnValue();
    }

    /**
     * Send post data.
     * 
     * @param urll
     *            the urll
     * @param text
     *            the text
     * @param key
     *            the key
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String sendPostData(String urll, String text, String key) throws IOException {
        String write = URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(text, "UTF-8");
        StringBuilder response = null;
        URL url = new URL(urll);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
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

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
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
     * @param minecraftJar
     *            the minecraft jar
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
            App.settings.logStackTrace(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    App.settings.log("Unable to close input stream");
                    App.settings.logStackTrace(e);
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
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File instanceDir = new File(dir, name);
                if (instanceDir.isDirectory()) {
                    return new File(instanceDir, "instance.json").exists();
                }
                return false;
            }
        };
    }

    /**
     * Gets the actual java version.
     * 
     * @return the actual java version
     */
    public static String getActualJavaVersion() {
        if (App.settings.isUsingCustomJavaPath()) {
            File folder = new File(App.settings.getJavaPath(), "bin/");
            List<String> arguments = new ArrayList<String>();
            arguments.add(folder + File.separator + "java" + (Utils.isWindows() ? ".exe" : ""));
            arguments.add("-version");
            ProcessBuilder processBuilder = new ProcessBuilder(arguments);
            processBuilder.directory(folder);
            processBuilder.redirectErrorStream(true);
            String version = "Unknown";
            BufferedReader br = null;
            try {
                Process process = processBuilder.start();
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                version = br.readLine(); // Read first line
                version = br.readLine(); // Get second line

                // Extract version information
                Pattern p = Pattern.compile("build ([0-9.-_a-zA-Z]+)");
                Matcher m = p.matcher(version);

                if (m.find()) {
                    version = m.group(1);
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        App.settings.log("Cannot close process input stream reader");
                        App.settings.logStackTrace(e);
                    }
                }
            }
            return "Launcher: " + System.getProperty("java.version") + ", Minecraft: " + version;
        } else {
            return "Launcher: " + System.getProperty("java.version") + ", Minecraft: "
                    + System.getProperty("java.version");
        }
    }

    /**
     * Checks if is java8.
     * 
     * @return true, if is java8
     */
    public static boolean isJava8() {
        if (App.settings.isUsingCustomJavaPath()) {
            File folder = new File(App.settings.getJavaPath(), "bin/");
            List<String> arguments = new ArrayList<String>();
            arguments.add(folder + File.separator + "java" + (Utils.isWindows() ? ".exe" : ""));
            arguments.add("-version");
            ProcessBuilder processBuilder = new ProcessBuilder(arguments);
            processBuilder.directory(folder);
            processBuilder.redirectErrorStream(true);
            BufferedReader br = null;
            try {
                Process process = processBuilder.start();
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line = br.readLine(); // Read first line only
                return line.contains("\"1.8");
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        App.settings.log("Cannot close input stream reader ");
                        App.settings.logStackTrace(e);
                    }
                }
            }
            return false; // Can't determine version, so fall back to not being Java 8
        } else {
            return System.getProperty("java.version").substring(0, 3).equalsIgnoreCase("1.8");
        }
    }

    /**
     * Creates the style sheet.
     * 
     * @param name
     *            the name
     * @return the style sheet
     */
    public static StyleSheet createStyleSheet(String name) {
        try {
            StyleSheet sheet = new StyleSheet();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    App.class.getResourceAsStream("/assets/css/" + name + ".css")));
            sheet.loadRules(reader, null);
            reader.close();

            return sheet;
        } catch (Exception e) {
            App.settings.logStackTrace(e);
            return new StyleSheet(); // If fails just return blank StyleSheetF
        }
    }

    /**
     * Gets the open eye pending reports file filter.
     * 
     * @return the open eye pending reports file filter
     */
    public static FilenameFilter getOpenEyePendingReportsFileFilter() {
        return new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                Pattern pattern = Pattern.compile("^pending-crash-[0-9\\-_\\.]+\\.json$");
                if (file.isFile() && pattern.matcher(name).matches()) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Sends a pending crash report generated by OpenEye and retrieves and returns it's response to
     * display to the user.
     * 
     * @param report
     *            a {@link File} object of the pending crash report to send the contents of
     * @return the response received from OpenEye about the crash that was sent which is of
     *         {@link OpenEyeReportResponse} type
     */
    public static OpenEyeReportResponse sendOpenEyePendingReport(File report) {
        StringBuilder response = null;
        String request = Utils.getFileContents(report);
        if (request == null) {
            App.settings.log("OpenEye: Couldn't read contents of file '" + report.getAbsolutePath()
                    + "'. Pending report sending failed!", LogMessageType.error, false);
            return null;
        }

        HttpURLConnection connection;

        try {
            URL url = new URL("http://openeye.openmods.info/api/v1/crash");
            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes(Charset.forName("UTF-8")));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
            App.settings.logStackTrace(e);
            return null; // Report sent, but no response
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            }
        }

        // Return an OpenEyeReportResponse object from the singular array returned in JSON
        return Settings.gson.fromJson(response.toString(), OpenEyeReportResponse[].class)[0];
    }

    /**
     * Gets the file contents.
     * 
     * @param file
     *            the file
     * @return the file contents
     */
    public static String getFileContents(File file) {
        if (!file.exists()) {
            App.settings.log("File '" + file.getAbsolutePath()
                    + "' doesn't exist so cannot read contents of file!", LogMessageType.error,
                    false);
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
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            contents = sb.toString();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            }
        }
        return contents;
    }

    /**
     * This splits up a string into a multi lined string by adding a separator at every space after
     * a given count.
     * 
     * @param string
     *            the string to split up
     * @param maxLineLength
     *            the number of characters minimum to have per line
     * @param lineSeparator
     *            the string to place when a new line should be placed
     * @return the new multi lined string
     */
    public static String splitMultilinedString(String string, int maxLineLength,
            String lineSeparator) {
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

    public static Float getBaseFontSize() {
        if (isMac()) {
            return (float) 11;
        } else {
            return (float) 12;
        }
    }

    public static Color getColourFromTheme(int[] colour) {
        if (colour[0] < 0 || colour[0] > 255) {
            colour[0] = 0; // Invalid colour
        }
        if (colour[1] < 0 || colour[1] > 255) {
            colour[1] = 0; // Invalid colour
        }
        if (colour[2] < 0 || colour[2] > 255) {
            colour[2] = 0; // Invalid colour
        }
        return new Color(colour[0], colour[1], colour[2]);
    }

    public static boolean testProxy(Proxy proxy, int timeout) {
        try {
            HttpURLConnection connection;
            URL url = new URL(App.settings.getFileURL("ping"));
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setConnectTimeout(timeout * 1000);
            connection.setReadTimeout(timeout * 1000);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
            connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.connect();
            App.settings.log("Proxy returned code " + connection.getResponseCode()
                    + " when testing!", (connection.getResponseCode() == 200 ? LogMessageType.info
                    : LogMessageType.error), false);
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            App.settings.log("Proxy couldn't establish a connection when testing!",
                    LogMessageType.error, false);
            return false;
        }
    }
}
