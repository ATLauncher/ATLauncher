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

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Constants;
import com.atlauncher.data.mojang.OperatingSystem;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.walker.ClearDirVisitor;
import org.tukaani.xz.XZInputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {
    public static EnumSet<StandardOpenOption> WRITE = EnumSet.of(StandardOpenOption.CREATE_NEW, StandardOpenOption
            .WRITE);
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
        try {
            Path theme = App.settings.getThemeFile();
            if (theme != null) {
                InputStream stream = null;
                try (ZipFile zip = new ZipFile(theme.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().equals("image/" + path.substring(path.lastIndexOf("/") + 1))) {
                            stream = zip.getInputStream(entry);
                            break;
                        }
                    }

                    if (stream != null) {
                        BufferedImage image = ImageIO.read(stream);
                        stream.close();
                        return new ImageIcon(image);
                    }
                }
            }

            URL url = System.class.getResource(path);

            if (url == null) {
                LogManager.error("Unable to load resource " + path);
                return null;
            }

            return new ImageIcon(url);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            return null;
        }
    }

    public static Path getCoreGracefully() {
        if (Utils.isLinux()) {
            try {
                return Paths.get(App.class.getProtectionDomain().getCodeSource().getLocation().toURI()
                        .getSchemeSpecificPart()).getParent();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return Paths.get(System.getProperty("user.dir"), Constants.LAUNCHER_NAME);
            }
        } else {
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    public static File getOSStorageDir() {
        switch (OperatingSystem.getOS()) {
            case WINDOWS:
                return new File(System.getenv("APPDATA"), "/." + Constants.LAUNCHER_NAME.toLowerCase());
            case OSX:
                return new File(System.getProperty("user.home"), "/Library/Application Support/." + Constants
                        .LAUNCHER_NAME.toLowerCase());
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

    public static ImageIcon getIconImage(Path p) {
        if (!Files.exists(p)) {
            LogManager.error("Unable to load file " + p);
            return null;
        }

        return new ImageIcon(p.toAbsolutePath().toString());
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

            Path theme = App.settings.getThemeFile();
            if (theme != null) {
                InputStream stream = null;
                try (ZipFile zip = new ZipFile(theme.toFile())) {
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        if (entry.getName().equals("image/" + name.substring(name.lastIndexOf("/") + 1))) {
                            stream = zip.getInputStream(entry);
                            break;
                        }
                    }

                    if (stream != null) {
                        BufferedImage image = ImageIO.read(stream);
                        stream.close();
                        return image;
                    }
                }
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
     * @deprecated user openExplorer(Path)
     */
    public static void openExplorer(File file) {
        Utils.openExplorer(file.toPath());
    }

    public static void openExplorer(Path path) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(path.toFile());
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    /**
     * Open browser.
     *
     * @param URL the url
     */
    public static void openBrowser(String URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(URL));
            } catch (Exception e) {
                LogManager.error("Failed to open link " + URL + " in browser!");
                LogManager.logStackTrace(e);
            }
        }
    }

    /**
     * Open browser.
     *
     * @param URL the url
     */
    public static void openBrowser(URL URL) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(URL.toURI());
            } catch (Exception e) {
                LogManager.error("Failed to open link " + URL + " in browser!");
                LogManager.logStackTrace(e);
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
            Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
            m.setAccessible(true);
            Object value = m.invoke(operatingSystemMXBean);
            if (value != null) {
                ramm = Long.parseLong(value.toString());
                ram = (int) (ramm / 1048576);
            } else {
                ram = 1024;
            }
        } catch (SecurityException e) {
            LogManager.logStackTrace(e);
        } catch (NoSuchMethodException e) {
            LogManager.logStackTrace(e);
        } catch (IllegalArgumentException e) {
            LogManager.logStackTrace(e);
        } catch (IllegalAccessException e) {
            LogManager.logStackTrace(e);
        } catch (InvocationTargetException e) {
            LogManager.logStackTrace(e);
        }
        return ram;
    }

    /**
     * Returns the maximum RAM available to Java. If on 64 Bit system then its all of the System RAM otherwise its
     * limited to 1GB or less due to allocations of PermGen
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
     * Returns the safe amount of maximum ram available to Java. This is set to half of the total maximum ram available
     * to Java in order to not allocate too much and leave enough RAM for the OS and other application
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
     * @param title the title
     * @param log the log
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
            LogManager.logStackTrace(e1);
        }
        return result;
    }

    public static ExecutorService generateDownloadExecutor() {
        return Executors.newFixedThreadPool(App.settings.getConcurrentConnections());
    }

    /**
     * Clean temp directory.
     */
    public static void cleanTempDirectory() {
        try {
            Files.walkFileTree(FileSystem.TMP, new ClearDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace("Error trying to clean the temp directory", e);
        }
    }

    public static void spreadOutResourceFiles(Path p) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(p)) {
            for (Path file : stream) {
                if (Files.isDirectory(file)) {
                    spreadOutResourceFiles(file);
                } else {
                    String hash = Hashing.sha1(p).toString();
                    Path save = FileSystem.RESOURCES.resolve("assets").resolve(hash.substring(0, 2) + File.separator
                            + hash);
                    FileUtils.createDirectory(save);
                    Files.copy(file, save, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
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
        } catch (InvalidKeyException e) {
            return Utils.decryptOld(encryptedData);
        } catch (BadPaddingException e) {
            return Utils.decryptOld(encryptedData);
        } catch (IllegalBlockSizeException e) {
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
        } catch (Exception e) {
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
     * @param originalFile the original file
     * @param destinationFile the destination file
     * @param replaceThis the replace this
     * @param withThis the with this
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void replaceText(File originalFile, File destinationFile, String replaceThis, String withThis)
            throws IOException {

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
     * @param urll the urll
     * @param text the text
     * @param key the key
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
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
        StringBuilder response = null;

        byte[] contents = Gsons.DEFAULT.toJson(data).getBytes();

        URL url = new URL(Constants.API_BASE_URL + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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
     * @deprecated use hasMetainf(Path)
     */
    public static boolean hasMetaInf(File minecraftJar) {
        return Utils.hasMetaInf(minecraftJar.toPath());
    }

    public static boolean hasMetaInf(Path minecraftJar) {
        JarInputStream input = null;
        try {
            input = new JarInputStream(new FileInputStream(minecraftJar.toFile()));
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
                LogManager.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        LogManager.logStackTrace("Cannot close process input stream reader", e);
                    }
                }
            }
            return "Launcher: " + System.getProperty("java.version") + ", Minecraft: " + version;
        } else {
            return "Launcher: " + System.getProperty("java.version") + ", Minecraft: " + System.getProperty("java" +
                    ".version");
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
                    LogManager.warn("Cannot get java version number from the ouput of java -version");
                } else {
                    return version >= 7;
                }
            } catch (NumberFormatException e) {
                LogManager.logStackTrace("Cannot get number from the ouput of java -version", e);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        LogManager.logStackTrace("Cannot close input stream reader", e);
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
                LogManager.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        LogManager.logStackTrace("Cannot close input stream reader", e);
                    }
                }
            }
            return false; // Can't determine version, so fall back to not being Java 8
        } else {
            return System.getProperty("java.version").substring(0, 3).equalsIgnoreCase("1.8");
        }
    }

    /**
     * Checks if is java9.
     *
     * @return true, if is java9
     */
    public static boolean isJava9() {
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
                return line.contains("\"1.9");
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        LogManager.logStackTrace("Cannot close input stream reader", e);
                    }
                }
            }
            return false; // Can't determine version, so fall back to not being Java 8
        } else {
            return System.getProperty("java.version").substring(0, 3).equalsIgnoreCase("1.9");
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
                return file.isFile() && pattern.matcher(name).matches();
            }
        };
    }

    /**
     * @deprecated use sendOpenEyeReport
     */
    public static OpenEyeReportResponse sendOpenEyePendingReport(File report) {
        return Utils.sendOpenEyePendingReport(report.toPath());
    }

    /**
     * Sends a pending crash report generated by OpenEye and retrieves and returns it's response to display to the
     * user.
     *
     * @param report a {@link Path} object of the pending crash report to send the contents of
     * @return the response received from OpenEye about the crash that was sent which is of {@link
     * OpenEyeReportResponse} type
     */
    public static OpenEyeReportResponse sendOpenEyePendingReport(Path report) {
        StringBuilder response = null;
        String request;

        try {
            request = new String(Files.readAllBytes(report));
        } catch (IOException e) {
            LogManager.logStackTrace("OpenEye: Couldn't read contents of file '" + report + "'. Pending  report " +
                    "sending failed!", e);
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

        // Return an OpenEyeReportResponse object from the singular array returned in JSON
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
     * This splits up a string into a multi lined string by adding a separator at every space after a given count.
     *
     * @param string the string to split up
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
            if (App.useGzipForDownloads) {
                connection.setRequestProperty("Accept-Encoding", "gzip");
            }
            connection.setRequestProperty("User-Agent", App.settings.getUserAgent());
            connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
            connection.setRequestProperty("Expires", "0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.connect();
            LogManager.info("Proxy returned code " + connection.getResponseCode() + " when testing!");
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
                return file.exists() && file.isFile() && name.endsWith(".zip");
            }
        };
    }

    /**
     * Flips a given {@link BufferedImage}
     *
     * @param image The image to flip
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
     * @param image The image to count the number of non transparent pixels in
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
            LogManager.logStackTrace("IOException while running traceRoute on host " + host, e);
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

        return new Object[]{type, message};
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

    private static String getMACAdressHash() {
        String returnStr = null;
        try {
            InetAddress ip;
            ip = InetAddress.getLocalHost();

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            // If network is null, user may be using Linux or something it doesn't support so try alternative way
            if (network == null) {
                Enumeration e = NetworkInterface.getNetworkInterfaces();

                while (e.hasMoreElements()) {
                    NetworkInterface n = (NetworkInterface) e.nextElement();
                    Enumeration ee = n.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress i = (InetAddress) ee.nextElement();
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

    /**
     * @deprecated use addToClasspath(Path)
     */
    public static boolean addToClasspath(File file) {
        return Utils.addToClasspath(file.toPath());
    }

    /**
     * Credit to https://github.com/Slowpoke101/FTBLaunch/blob/master/src/main/java/net/ftb/workers/AuthlibDLWorker.java
     */
    public static boolean addToClasspath(Path path) {
        LogManager.info("Loading external library " + path.getFileName() + " to classpath!");

        try {
            if (Files.exists(path)) {
                addURL(path.toUri().toURL());
            } else {
                LogManager.error("Error loading " + path.getFileName() + " to classpath as it doesn't exist!");
                return false;
            }
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                LogManager.error(t.getMessage());
            }

            return false;
        }

        return true;
    }

    public static boolean checkAuthLibLoaded() {
        try {
            App.settings.getClass().forName("com.mojang.authlib.exceptions.AuthenticationException");
            App.settings.getClass().forName("com.mojang.authlib.Agent");
            App.settings.getClass().forName("com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService");
            App.settings.getClass().forName("com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication");
        } catch (ClassNotFoundException e) {
            LogManager.logStackTrace(e);
            return false;
        }

        return true;
    }

    /**
     * Credit to https://github.com/Slowpoke101/FTBLaunch/blob/master/src/main/java/net/ftb/workers/AuthlibDLWorker.java
     */
    public static void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader) App.settings.getClass().getClassLoader();
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(sysloader, u);
        } catch (Throwable t) {
            if (t.getMessage() != null) {
                LogManager.error(t.getMessage());
            }
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

}
