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
package com.atlauncher.mclauncher;

import java.applet.Applet;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class MCLauncher {

    public static Process launch(Account account, Instance instance, String session)
            throws IOException {
        String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar",
                "jinput.jar" };
        StringBuilder cpb = new StringBuilder("");
        File jarMods = instance.getJarModsDirectory();
        if (jarMods.exists() && instance.hasJarMods()) {
            for (String mod : instance.getJarOrder().split(",")) {
                cpb.append(File.pathSeparator);
                cpb.append(new File(jarMods, mod));
            }
        }

        for (String jarFile : jarFiles) {
            cpb.append(File.pathSeparator);
            cpb.append(new File(instance.getBinDirectory(), jarFile));
        }

        List<String> arguments = new ArrayList<String>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        arguments.add("-Xms256M");
        arguments.add("-Xmx" + LauncherFrame.settings.getMemory() + "M");
        arguments.add("-XX:MaxPermSize=" + LauncherFrame.settings.getPermGen() + "M");

        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path") + cpb.toString());

        arguments.add(MCLauncher.class.getCanonicalName());

        // Start or passed in arguments
        arguments.add(instance.getMinecraftDirectory().getAbsolutePath()); // Path
        arguments.add(account.getMinecraftUsername()); // Username
        arguments.add(session); // Session
        arguments.add(instance.getName()); // Instance Name
        if (instance.hasJarMods()) {
            arguments.add(instance.getJarOrder()); // Jar Order
        }
        arguments.add(LauncherFrame.settings.getWindowWidth() + ""); // Window Width
        arguments.add(LauncherFrame.settings.getWindowHeight() + ""); // Window Height

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public static void main(String[] args) {
        String workingDirectory = args[0];
        String username = args[1];
        String session = args[2];
        String instanceName = args[3];
        int screenWidth, screenHeight;
        String jarOrder = null;
        if (args.length == 7) {
            // Has JarOrder
            jarOrder = args[4];
            screenWidth = Integer.parseInt(args[5]);
            screenHeight = Integer.parseInt(args[6]);
        } else {
            // Has No JarOrder
            screenWidth = Integer.parseInt(args[4]);
            screenHeight = Integer.parseInt(args[5]);
        }

        Dimension winSize = new Dimension(screenWidth, screenHeight);
        boolean maximize = false;
        boolean compatMode = false;

        File cwd = new File(workingDirectory);

        try {
            File binDir = new File(cwd, "bin");
            File lwjglDir = binDir;

            System.out.println("Loading jars...");
            String[] lwjglJars = new String[] { "lwjgl.jar", "lwjgl_util.jar", "jinput.jar" };

            URL[] urls = new URL[4];
            try {
                if (jarOrder != null) {
                    File jarModsDir = new File(cwd, "jarmods");
                    String[] mods = jarOrder.split(",");
                    for (String mod : mods) {
                        File f = new File(jarModsDir, mod);
                        urls[0] = f.toURI().toURL();
                        System.out.println("Loading URL: " + urls[0].toString());
                    }
                }

                File f = new File(binDir, "minecraft.jar");
                urls[0] = f.toURI().toURL();
                System.out.println("Loading URL: " + urls[0].toString());

                for (int i = 1; i < urls.length; i++) {
                    File jar = new File(lwjglDir, lwjglJars[i - 1]);
                    urls[i] = jar.toURI().toURL();
                    System.out.println("Loading URL: " + urls[i].toString());
                }
            } catch (MalformedURLException e) {
                System.err.println("MalformedURLException, " + e.toString());
                System.exit(5);
            }

            System.out.println("Loading natives...");
            String nativesDir = new File(lwjglDir, "natives").toString();

            System.setProperty("org.lwjgl.librarypath", nativesDir);
            System.setProperty("net.java.games.input.librarypath", nativesDir);

            System.setProperty("user.home", cwd.getAbsolutePath());

            URLClassLoader cl = new URLClassLoader(urls, MCLauncher.class.getClassLoader());

            // Get the Minecraft Class.
            Class<?> mc = null;
            try {
                mc = cl.loadClass("net.minecraft.client.Minecraft");

                Field f = getMCPathField(mc);

                if (f == null) {
                    System.err.println("Could not find Minecraft path field. Launch failed.");
                    System.exit(-1);
                }

                f.setAccessible(true);
                f.set(null, cwd);
                // And set it.
                System.out.println("Fixed Minecraft Path: Field was " + f.toString());
            } catch (ClassNotFoundException e) {
                System.err.println("Can't find main class. Searching...");

                // Look for any class that looks like the main class.
                File mcJar = new File(new File(cwd, "bin"), "minecraft.jar");
                ZipFile zip = null;
                try {
                    zip = new ZipFile(mcJar);
                } catch (ZipException e1) {
                    e1.printStackTrace();
                    System.err.println("Search failed.");
                    System.exit(-1);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    System.err.println("Search failed.");
                    System.exit(-1);
                }

                Enumeration<? extends ZipEntry> entries = zip.entries();
                ArrayList<String> classes = new ArrayList<String>();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String entryName = entry.getName().substring(0,
                                entry.getName().lastIndexOf('.'));
                        entryName = entryName.replace('/', '.');
                        System.out.println("Found class: " + entryName);
                        classes.add(entryName);
                    }
                }

                for (String clsName : classes) {
                    try {
                        Class<?> cls = cl.loadClass(clsName);
                        if (!Runnable.class.isAssignableFrom(cls)) {
                            continue;
                        } else {
                            System.out.println("Found class implementing runnable: "
                                    + cls.getName());
                        }

                        if (getMCPathField(cls) == null) {
                            continue;
                        } else {
                            System.out.println("Found class implementing runnable "
                                    + "with mcpath field: " + cls.getName());
                        }

                        mc = cls;
                        break;
                    } catch (ClassNotFoundException e1) {
                        // Ignore
                        continue;
                    }
                }

                if (mc == null) {
                    System.err.println("Failed to find Minecraft main class.");
                    System.exit(-1);
                } else {
                    System.out.println("Found main class: " + mc.getName());
                }
            }

            System.setProperty("minecraft.applet.TargetDirectory", cwd.getAbsolutePath());

            String[] mcArgs = new String[2];
            mcArgs[0] = username;
            mcArgs[1] = session;

            if (compatMode) {
                System.out.println("Launching in compatibility mode...");
                mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
            } else {
                System.out.println("Launching with applet wrapper...");
                try {
                    Class<?> MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
                    Applet mcappl = (Applet) MCAppletClass.newInstance();
                    MCFrame mcWindow = new MCFrame("ATLauncher - " + instanceName);
                    mcWindow.start(mcappl, username, session, winSize, maximize);
                } catch (InstantiationException e) {
                    System.out.println("Applet wrapper failed! Falling back "
                            + "to compatibility mode.");
                    mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(4);
        }
    }

    public static Field getMCPathField(Class<?> mc) {
        Field[] fields = mc.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (f.getType() != File.class) {
                // Has to be File
                continue;
            }
            if (f.getModifiers() != (Modifier.PRIVATE + Modifier.STATIC)) {
                // And Private Static.
                continue;
            }
            return f;
        }
        return null;
    }
}