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
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.ImageIcon;

public class Utils {

    public static ImageIcon getIconImage(String path) {
        URL url = System.class.getResource(path);

        if (url == null) {
            System.err.println("Unable to load image: " + path);
        }

        ImageIcon icon = new ImageIcon(url);

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
            font = Font.createFont(Font.TRUETYPE_FONT, new File(System.class
                    .getResource("/resources/" + name + ".ttf").toURI()));
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
    
    public static boolean is64Bit() {
        String osType = System.getProperty("sun.arch.data.model");
        return Boolean.valueOf(osType.contains("64"));
    }

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
}
