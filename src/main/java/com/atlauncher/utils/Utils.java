/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
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
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;
import javax.swing.text.html.StyleSheet;

import org.tukaani.xz.XZInputStream;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Settings;
import com.atlauncher.data.mojang.ExtractRule;
import com.atlauncher.data.mojang.OperatingSystem;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.evnt.LogEvent.LogType;

public class Utils {
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
     * @param path
     *            the path
     * @return the icon image
     */
    public static ImageIcon getIconImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            LogManager.error("Unable to load resource " + path);
            return null;
        }

        return new ImageIcon(url);
    }

    public static File getCoreGracefully() {
        if (Utils.isLinux()) {
            try {
                return new File(App.class.getProtectionDomain().getCodeSource().getLocation()
                        .toURI().getSchemeSpecificPart()).getParentFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return new File(System.getProperty("user.dir"), "ATLauncher");
            }
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
            LogManager.error("Unable to load file " + file.getAbsolutePath());
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

    public static BufferedImage getImage(String img) {
        try {
            String name;
            if (!img.startsWith("/assets/image/")) {
                name = "/assets/image/" + img;
            } else {
                name = img;
            }

            if (!name.endsWith(".png")) {
                name = name + ".png";
            }

            InputStream stream = App.class.getResourceAsStream(name);

            if (stream == null) {
                throw new NullPointerException("Stream == null");
            }

            return ImageIO.read(stream);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
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
                LogManager.error("Failed to open link " + URL + " in browser!");
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
                LogManager.error("Failed to open link " + URL + " in browser!");
                App.settings.logStackTrace(e);
            }
        }
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
        return System.getProperty("sun.arch.data.model").contains("64");
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
            LogManager
                    .error("Cannot get MD5 of " + file.getAbsolutePath() + " as it doesn't exist");
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
            LogManager.error("Cannot get SHA-1 hash of " + file.getAbsolutePath()
                    + " as it doesn't exist");
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
            LogManager.error("Cannot get MD5 of null");
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
            LogManager.error("Couldn't move file " + from.getAbsolutePath() + " to "
                    + to.getAbsolutePath());
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
            LogManager.error("File " + from.getAbsolutePath() + " cannot be copied to "
                    + to.getAbsolutePath() + " as it isn't a file");
        }
        if (!from.exists()) {
            LogManager.error("File " + from.getAbsolutePath() + " cannot be copied to "
                    + to.getAbsolutePath() + " as it doesn't exist");
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

    public static boolean safeCopy(File from, File to) throws IOException {
        if (to.exists()) {
            to.delete();
        }

        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(from);
            os = new FileOutputStream(to);

            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
        } finally {
            if (is != null) {
                is.close();
            }

            if (os != null) {
                os.close();
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
            LogManager.error("Couldn't move directory " + sourceLocation.getAbsolutePath() + " to "
                    + targetLocation.getAbsolutePath());
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
            LogManager.error((file.isFile() ? "File" : "Folder") + " " + file.getAbsolutePath()
                    + " couldn't be deleted");
        }
    }

    /**
     * Delete.
     * 
     * @param file
     *            the file
     */
    public static void deleteWithFilter(File file, final List<String> filesToIgnore) {
        FilenameFilter ffFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !filesToIgnore.contains(name);
            }
        };
        for (File aFile : file.listFiles(ffFilter)) {
            Utils.delete(aFile);
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
        return new SecretKeySpec("NotARandomKeyYes".getBytes(), "AES");
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

    public static String sendAPICall(String path, Object data) throws IOException {
        StringBuilder response = null;

        byte[] contents = Settings.gson.toJson(data).getBytes();

        URL url = new URL(Constants.API_BASE_URL + path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
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

    public static String sendGetAPICall(String path) throws IOException {
        StringBuilder response = null;

        URL url = new URL(Constants.API_BASE_URL + path);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.setRequestProperty("Pragma", "no-cache");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

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
                    App.settings.logStackTrace("Unable to close input stream", e);
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
                String line = null;
                Pattern p = Pattern.compile("build ([0-9.-_a-zA-Z]+)");
                while ((line = br.readLine()) != null) {
                    // Extract version information
                    Matcher m = p.matcher(line);

                    if (m.find()) {
                        version = m.group(1);
                        break;
                    }
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        App.settings.logStackTrace("Cannot close process input stream reader", e);
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
     * Checks if the user is using Java 7 or above
     * 
     * @return true if the user is using Java 7 or above else false
     */
    public static boolean isJava7OrAbove(boolean checkCustomPath) {
        if (App.settings.isUsingCustomJavaPath() && checkCustomPath) {
            File folder = new File(App.settings.getJavaPath(), "bin/");
            List<String> arguments = new ArrayList<String>();
            arguments.add(folder + File.separator + "java" + (Utils.isWindows() ? ".exe" : ""));
            arguments.add("-version");
            ProcessBuilder processBuilder = new ProcessBuilder(arguments);
            processBuilder.directory(folder);
            processBuilder.redirectErrorStream(true);
            BufferedReader br = null;
            int version = -1;
            try {
                Process process = processBuilder.start();
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.contains("build 1.")) {
                        int buildIndex = line.indexOf("build 1.") + 8;
                        version = Integer.parseInt(line.substring(buildIndex, buildIndex + 1));
                        break;
                    }
                }
                if (version == -1) {
                    LogManager
                            .warn("Cannot get java version number from the ouput of java -version");
                } else {
                    return version >= 7;
                }
            } catch (NumberFormatException e) {
                App.settings.logStackTrace("Cannot get number from the ouput of java -version", e);
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        App.settings.logStackTrace("Cannot close input stream reader", e);
                    }
                }
            }
            return true; // Can't determine version, so assume true.
        } else {
            return Integer.parseInt(System.getProperty("java.version").substring(2, 3)) >= 7;
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
                        App.settings.logStackTrace("Cannot close input stream reader", e);
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
            LogManager.error("OpenEye: Couldn't read contents of file '" + report.getAbsolutePath()
                    + "'. Pending report sending failed!");
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
            LogManager.error("File '" + file.getAbsolutePath()
                    + "' doesn't exist so cannot read contents of file!");
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

    public static boolean testProxy(Proxy proxy) {
        try {
            HttpURLConnection connection;
            URL url = new URL(App.settings.getFileURL("ping"));
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("Accept-Encoding", "gzip");
            connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
            connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.connect();
            LogManager.info("Proxy returned code " + connection.getResponseCode()
                    + " when testing!");
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            LogManager.error("Proxy couldn't establish a connection when testing!");
            return false;
        }
    }

    public static FilenameFilter getThemesFileFilter() {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File file = new File(dir, name);
                if (file.exists() && file.isFile() && name.endsWith(".json")) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Flips a given {@link BufferedImage}
     * 
     * @param image
     *            The image to flip
     * @return The flipped image
     */
    public static Image flipImage(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = op.filter(image, null);
        return image;
    }

    /**
     * Counts the numbers of non transparent pixels in a given {@link BufferedImage}.
     * 
     * @param image
     *            The image to count the number of non transparent pixels in
     * @return The number of non transparent pixels
     */
    public static int nonTransparentPixels(BufferedImage image) {
        int count = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
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
            if (Utils.isWindows()) {
                traceRoute = Runtime.getRuntime().exec("ping -n 10 " + address.getHostAddress());
            } else {
                traceRoute = Runtime.getRuntime().exec("ping -c 10 " + address.getHostAddress());
            }

            BufferedReader reader = null;
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
            App.settings.logStackTrace("IOException while running ping on host " + host, e);
        }

        return pingStats;
    }

    public static String traceRoute(String host) {
        String route = "";
        StringBuilder response;
        try {
            InetAddress address = InetAddress.getByName(host);
            Process traceRoute;
            if (Utils.isWindows()) {
                traceRoute = Runtime.getRuntime().exec("tracert " + address.getHostAddress());
            } else {
                traceRoute = Runtime.getRuntime().exec("traceroute " + address.getHostAddress());
            }

            BufferedReader reader = null;
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
            App.settings.logStackTrace("IOException while running traceRoute on host " + host, e);
        }

        return route;
    }

    public static Object[] prepareMessageForMinecraftLog(String text) {
        LogType type = null; // The log message type
        String message = null; // The log message

        if (text.contains("[INFO] [STDERR]")) {
            message = text.substring(text.indexOf("[INFO] [STDERR]"));
            type = LogType.WARN;
        } else if (text.contains("[INFO]")) {
            message = text.substring(text.indexOf("[INFO]"));
            if (message.contains("CONFLICT")) {
                type = LogType.ERROR;
            } else if (message.contains("overwriting existing item")) {
                type = LogType.WARN;
            } else {
                type = LogType.INFO;
            }
        } else if (text.contains("[WARNING]")) {
            message = text.substring(text.indexOf("[WARNING]"));
            type = LogType.WARN;
        } else if (text.contains("WARNING:")) {
            message = text.substring(text.indexOf("WARNING:"));
            type = LogType.WARN;
        } else if (text.contains("INFO:")) {
            message = text.substring(text.indexOf("INFO:"));
            type = LogType.INFO;
        } else if (text.contains("Exception")) {
            message = text;
            type = LogType.ERROR;
        } else if (text.contains("[SEVERE]")) {
            message = text.substring(text.indexOf("[SEVERE]"));
            type = LogType.ERROR;
        } else if (text.contains("[Sound Library Loader/ERROR]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Sound Library Loader/WARN]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Sound Library Loader/INFO]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[Client thread/ERROR]")) {
            message = text.substring(text.indexOf("[Client thread/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Client thread/WARN]")) {
            message = text.substring(text.indexOf("[Client thread/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Client thread/INFO]")) {
            message = text.substring(text.indexOf("[Client thread/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[Server thread/ERROR]")) {
            message = text.substring(text.indexOf("[Server thread/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Server thread/WARN]")) {
            message = text.substring(text.indexOf("[Server thread/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Server thread/INFO]")) {
            message = text.substring(text.indexOf("[Server thread/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[main/ERROR]")) {
            message = text.substring(text.indexOf("[main/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[main/WARN]")) {
            message = text.substring(text.indexOf("[main/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[main/INFO]")) {
            message = text.substring(text.indexOf("[main/INFO]"));
            type = LogType.INFO;
        } else {
            message = text;
            type = LogType.INFO;
        }

        return new Object[] { type, message };
    }

    public static byte[] readFile(File file) {
        byte[] bytes = null;
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "r");
            bytes = new byte[(int) f.length()];
            f.read(bytes);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    App.settings.logStackTrace(e);
                }
            }
        }
        return bytes;
    }

    public static void unXZPackFile(File xzFile, File packFile, File outputFile) {
        unXZFile(xzFile, packFile);
        unpackFile(packFile, outputFile);
    }

    public static void unXZFile(File input, File output) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        BufferedInputStream bis = null;
        XZInputStream xzis = null;
        try {
            fis = new FileInputStream(input);
            xzis = new XZInputStream(fis);
            fos = new FileOutputStream(output);

            final byte[] buffer = new byte[8192];
            int n = 0;
            while (-1 != (n = xzis.read(buffer))) {
                fos.write(buffer, 0, n);
            }

        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
                if (xzis != null) {
                    xzis.close();
                }
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    /*
     * From: http://atl.pw/1
     */
    public static void unpackFile(File input, File output) {
        if (output.exists()) {
            Utils.delete(output);
        }

        byte[] decompressed = readFile(input);

        if (decompressed == null) {
            LogManager.error("unpackFile: While reading in " + input.getName()
                    + " the file returned null");
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
        byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8,
                decompressed.length - 8);
        try {
            FileOutputStream jarBytes = new FileOutputStream(output);
            JarOutputStream jos = new JarOutputStream(jarBytes);

            Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);

            jos.putNextEntry(new JarEntry("checksums.sha1"));
            jos.write(checksums);
            jos.closeEntry();

            jos.close();
            jarBytes.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }
}