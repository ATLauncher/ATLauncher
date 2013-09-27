/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.Utils;

public class MCLauncher {

    public static Process launch(Account account, Instance instance, String session)
            throws IOException {
        String[] jarFiles = new String[] { "minecraft.jar", "lwjgl.jar", "lwjgl_util.jar",
                "jinput.jar" };
        StringBuilder cpb = new StringBuilder("");
        File jarMods = instance.getJarModsDirectory();
        if (jarMods.exists() && (instance.hasJarMods() || jarMods.listFiles().length != 0)) {
            if (instance.hasJarMods()) {
                ArrayList<String> jarmods = new ArrayList<String>(Arrays.asList(instance
                        .getJarOrder().split(",")));
                for (String mod : jarmods) {
                    File thisFile = new File(jarMods, mod);
                    if (thisFile.exists()) {
                        cpb.append(File.pathSeparator);
                        cpb.append(thisFile);
                    }
                }
                for (File file : jarMods.listFiles()) {
                    if (jarmods.contains(file.getName())) {
                        continue;
                    }
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
            } else {
                for (File file : jarMods.listFiles()) {
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
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
        if (App.settings.getMemory() < instance.getMemory()) {
            if (Utils.getMaximumRam() < instance.getMemory()) {
                arguments.add("-Xmx" + App.settings.getMemory() + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + App.settings.getMemory() + "M");
        }
        if (App.settings.getPermGen() < instance.getPermGen()) {
            arguments.add("-XX:MaxPermSize=" + instance.getPermGen() + "M");
        } else {
            arguments.add("-XX:MaxPermSize=" + App.settings.getPermGen() + "M");
        }

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                arguments.add(arg);
            }
        }

        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (Utils.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:icon="
                    + new File(App.settings.getImagesDir(), "OldMinecraftIcon.png")
                            .getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");
        }

        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path") + cpb.toString());

        arguments.add(MCLauncher.class.getCanonicalName());

        // Start or passed in arguments
        arguments.add(instance.getRootDirectory().getAbsolutePath()); // Path
        arguments.add(account.getMinecraftUsername()); // Username
        arguments.add(session); // Session
        arguments.add(instance.getName()); // Instance Name
        arguments.add(App.settings.getWindowWidth() + ""); // Window Width
        arguments.add(App.settings.getWindowHeight() + ""); // Window Height

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public static void main(String[] args) {
        String workingDirectory = args[0];
        String username = args[1];
        String session = args[2];
        String instanceName = args[3];
        int screenWidth = Integer.parseInt(args[4]);
        int screenHeight = Integer.parseInt(args[5]);

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
                File f = new File(binDir, "minecraft.jar");
                urls[0] = f.toURI().toURL();
                System.out.println("Loading URL: " + urls[0].toString());

                for (int i = 1; i <= lwjglJars.length; i++) {
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