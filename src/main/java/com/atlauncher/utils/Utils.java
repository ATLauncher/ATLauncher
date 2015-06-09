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
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.data.Constants;
import com.atlauncher.data.OS;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.managers.SettingsManager;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.swing.ImageIcon;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            Path theme = SettingsManager.getThemeFile();
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
        if (OS.isLinux()) {
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
        if (OS.isMac()) {
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

            Path theme = SettingsManager.getThemeFile();
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
        return Executors.newFixedThreadPool(SettingsManager.getConcurrentConnections());
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
        return new SecretKeySpec(OS.getMACAdressHash().getBytes(), 0, 16, "AES");
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
        if (OS.isMac()) {
            return (float) 11;
        } else {
            return (float) 12;
        }
    }

    public static boolean testProxy(Proxy proxy) {
        try {
            HttpURLConnection connection;
            URL url = new URL(ServerManager.getFileURL("ping"));
            connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setRequestProperty("User-Agent", Network.USER_AGENT);
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
            if (OS.isWindows()) {
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
            if (OS.isWindows()) {
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

}
