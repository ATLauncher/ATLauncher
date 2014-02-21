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
import java.awt.FontFormatException;
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
import java.io.FileWriter;
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
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.mojang.ExtractRule;
import com.atlauncher.data.mojang.OperatingSystem;
import com.atlauncher.gui.ProgressDialog;

public class Utils {

    public static ImageIcon getIconImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            App.settings.log("Unable to load resource " + path, LogMessageType.error, false);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);

        return icon;
    }

    public static ImageIcon getIconImage(File file) {
        if (!file.exists()) {
            App.settings.log("Unable to load file " + file.getAbsolutePath(), LogMessageType.error,
                    false);
            return null;
        }

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

        return icon;
    }

    public static Font getFont() {
        if (isMac()) {
            return new Font("SansSerif", Font.PLAIN, 11);
        } else {
            return new Font("SansSerif", Font.PLAIN, 12);
        }
    }

    public static Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            App.settings.log("Unable to load resource " + path, LogMessageType.error, false);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);

        return icon.getImage();
    }

    public static void openExplorer(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    public static void openBrowser(String URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(URL));
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    public static void openBrowser(URL URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URL.toURI());
            } catch (Exception e) {
                App.settings.logStackTrace(e);
            }
        }
    }

    public static Font makeFont(String name) {
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    System.class.getResource("/resources/" + name + ".ttf").openStream());
        } catch (FontFormatException e) {
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return font;
    }

    public static String osSlash() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    public static String osDelimiter() {
        if (isWindows()) {
            return ";";
        } else {
            return ":";
        }
    }

    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    public static String getJavaVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static boolean isWindows() {
        return OperatingSystem.getOS() == OperatingSystem.WINDOWS;
    }

    public static boolean isMac() {
        return OperatingSystem.getOS() == OperatingSystem.OSX;
    }

    public static boolean isLinux() {
        return OperatingSystem.getOS() == OperatingSystem.LINUX;
    }

    public static boolean is64Bit() {
        String osType = System.getProperty("sun.arch.data.model");
        return Boolean.valueOf(osType.contains("64"));
    }

    public static String getArch() {
        if (is64Bit()) {
            return "64";
        } else {
            return "32";
        }
    }

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
     * Returns the amount of RAM in the users system
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

    public static int getMaximumWindowWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.width;
    }

    public static int getMaximumWindowHeight() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.height;
    }

    public static String uploadPaste(String title, String log) {
        String line = "";
        String result = "";
        try {
            String urlParameters = "";
            urlParameters += "title=" + URLEncoder.encode(title, "ISO-8859-1") + "&";
            urlParameters += "language=" + URLEncoder.encode("text", "ISO-8859-1") + "&";
            urlParameters += "private=" + URLEncoder.encode("1", "ISO-8859-1") + "&";
            urlParameters += "text=" + URLEncoder.encode(log, "ISO-8859-1");
            URL url = new URL("%PASTEAPIURL%");
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
            ;
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
            ;
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

    public static boolean copyFile(File from, File to) {
        return copyFile(from, to, false);
    }

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

    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        return copyDirectory(sourceLocation, targetLocation, false);
    }

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

    public static void unzip(File in, File out) {
        unzip(in, out, null);
    }

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

    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles()) {
                delete(c);
            }
        }
        boolean deleted = file.delete();
        if (!deleted) {
            if (file.isFile()) {
                App.settings.log("File " + file.getAbsolutePath() + " couldn't be deleted",
                        LogMessageType.error, false);
            }
            if (file.isDirectory()) {
                App.settings.log("Folder " + file.getAbsolutePath() + " couldn't be deleted",
                        LogMessageType.error, false);
            }
        }
    }

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

    public static void deleteContents(File file) {
        if (file.isDirectory()) {
            for (File c : file.listFiles())
                delete(c);
        } else {
            return;
        }
    }

    public static void zip(File in, File out) {
        try {
            URI base = in.toURI();
            Deque<File> queue = new LinkedList<File>();
            queue.push(in);
            OutputStream stream = new FileOutputStream(out);
            Closeable res = stream;
            try {
                ZipOutputStream zout = new ZipOutputStream(stream);
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
            }
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

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

    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }

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

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec("NotARandomKeyYes".getBytes(), "AES");
        return key;
    }

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

    public static String uploadLog() {
        final ProgressDialog dialog = new ProgressDialog(
                App.settings.getLocalizedString("console.uploadinglog"), 0,
                App.settings.getLocalizedString("console.uploadinglog"), "Aborting log upload!");
        dialog.addThread(new Thread() {
            public void run() {
                String result = Utils.uploadPaste("ATLauncher Log", App.settings.getLog());
                dialog.setReturnValue(result);
                dialog.close();
            };
        });
        dialog.start();
        return (String) dialog.getReturnValue();
    }

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

    public static boolean hasMetaInf(File minecraftJar) {
        try {
            JarInputStream input = new JarInputStream(new FileInputStream(minecraftJar));
            JarEntry entry;
            boolean found = false;
            while ((entry = input.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    found = true;
                }
            }
            input.close();
            return found;
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return false;
    }
}
