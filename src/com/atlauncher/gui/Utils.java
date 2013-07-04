/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.data.Base64;
import com.atlauncher.data.Downloader;

public class Utils {

    public static ImageIcon getIconImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        ImageIcon icon = new ImageIcon(url);

        return icon;
    }

    public static ImageIcon getIconImage(File file) {
        if (!file.exists()) {
            System.err.println("Unable to load image: " + file.getAbsolutePath());
        }

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

        return icon;
    }

    public static ImageIcon getMinecraftHead(String user) {
        File file = new File(App.settings.getSkinsDir(), user + ".png");
        if (!file.exists()) {
            new Downloader("http://s3.amazonaws.com/MinecraftSkins/" + user + ".png",
                    file.getAbsolutePath()).run();
        }

        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage main = image.getSubimage(8, 8, 8, 8);
        BufferedImage helmet = image.getSubimage(40, 8, 8, 8);
        BufferedImage head = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);

        Graphics g = head.getGraphics();
        g.drawImage(main, 0, 0, null);
        g.drawImage(helmet, 0, 0, null);

        ImageIcon icon = new ImageIcon(head.getScaledInstance(32, 32, Image.SCALE_SMOOTH));

        return icon;
    }

    public static Image getImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        ImageIcon icon = new ImageIcon(url);

        return icon.getImage();
    }

    public static void openBrowser(String URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(URL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void openBrowser(URL URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URL.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Font makeFont(String name, Float point) {
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    new File(System.class.getResource("/resources/" + name + ".ttf").toURI()));
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        font.deriveFont(point);
        return font;
    }

    public static enum OS {
        windows, mac, linux
    }

    private static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.windows;
        } else if (osName.contains("mac")) {
            return OS.mac;
        } else {
            return OS.linux;
        }
    }

    public static String osSlash() {
        if (getOS() == OS.windows) {
            return "\\";
        } else {
            return "/";
        }
    }

    public static String osDelimiter() {
        if (getOS() == OS.windows) {
            return ";";
        } else {
            return ":";
        }
    }

    public static String getJavaVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static boolean isWindows() {
        return getOS() == OS.windows;
    }

    public static boolean isMac() {
        return getOS() == OS.mac;
    }

    public static boolean isLinux() {
        return getOS() == OS.linux;
    }

    public static boolean is64Bit() {
        String osType = System.getProperty("sun.arch.data.model");
        return Boolean.valueOf(osType.contains("64"));
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
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return ram;
    }

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
            URL url = new URL("http://paste.atlauncher.com/api/create");
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
            e1.printStackTrace();
        }
        return result;
    }

    public static String getMD5(File file) {
        if (!file.exists()) {
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

            // convert the byte to hex format method 1
            sb = new StringBuffer();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void copyFile(File from, File to) {
        copyFile(from, to, false);
    }

    public static void copyFile(File from, File to, boolean withFilename) {
        if (!from.isFile()) {
            App.settings.getConsole().log(
                    "File " + from.getAbsolutePath() + " cannot be copied to "
                            + to.getAbsolutePath() + " as it isn't a file");
            return;
        }
        if (!from.exists()) {
            App.settings.getConsole().log(
                    "File " + from.getAbsolutePath() + " cannot be copied to "
                            + to.getAbsolutePath() + " as it doesn't exist");
            return;
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
            e.printStackTrace();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(from).getChannel();
            destination = new FileOutputStream(to).getChannel();
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) {
        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdir();
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
            e.printStackTrace();
        }
    }

    public static void unzip(File in, File out) {
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
            e.printStackTrace();
        }
    }

    // public static void deleteMetaInf(String pack) {
    // File inputFile = new File(PackUtils.getMinecraftJar(pack));
    // File outputTmpFile = new File(PackUtils.getMinecraftJar(pack) + ".tmp");
    // try {
    // JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
    // JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
    // JarEntry entry;
    //
    // while ((entry = input.getNextJarEntry()) != null) {
    // if (entry.getName().contains("META-INF")) {
    // continue;
    // }
    // output.putNextEntry(entry);
    // byte buffer[] = new byte[1024];
    // int amo;
    // while ((amo = input.read(buffer, 0, 1024)) != -1) {
    // output.write(buffer, 0, amo);
    // }
    // output.closeEntry();
    // }
    //
    // input.close();
    // output.close();
    //
    // inputFile.delete();
    // outputTmpFile.renameTo(inputFile);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

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
            for (File c : file.listFiles())
                delete(c);
        }
        boolean deleted = file.delete();
        if (!deleted) {
            if (file.isFile())
                App.settings.getConsole().log(
                        "File " + file.getAbsolutePath() + " couldn't be deleted");
            if (file.isDirectory())
                App.settings.getConsole().log(
                        "Folder " + file.getAbsolutePath() + " couldn't be deleted");
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec("NotARandomKeyYes".getBytes(), "AES");
        return key;
    }

    public static String urlToString(String url) {
        StringBuilder response = null;
        try {
            URL urll = new URL(url);
            URLConnection connection = urll.openConnection();
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.toString();
    }
}
