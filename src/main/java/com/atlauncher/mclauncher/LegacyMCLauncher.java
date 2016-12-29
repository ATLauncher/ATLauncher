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
package com.atlauncher.mclauncher;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.Update;
import com.atlauncher.data.Account;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.utils.Utils;

import java.applet.Applet;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class LegacyMCLauncher {

    public static Process launch(Account account, Instance instance, LoginResponse sess) throws IOException {
        String lwjgl = "lwjgl.jar";
        String lwjgl_util = "lwjgl_util.jar";
        String jinput = "jinput.jar";
        File[] files = instance.getBinDirectory().listFiles();
        for (File file : files) {
            if (file.getName().startsWith("lwjgl-")) {
                lwjgl = file.getName();
            } else if (file.getName().startsWith("lwjgl_util-")) {
                lwjgl_util = file.getName();
            } else if (file.getName().startsWith("jinput-")) {
                jinput = file.getName();
            }
        }
        String[] jarFiles = new String[]{"minecraft.jar", lwjgl, lwjgl_util, jinput};
        StringBuilder cpb = new StringBuilder("");
        File jarMods = instance.getJarModsDirectory();
        if (jarMods.exists() && (instance.hasJarMods() || jarMods.listFiles().length != 0)) {
            if (instance.hasJarMods()) {
                ArrayList<String> jarmods = new ArrayList<String>(Arrays.asList(instance.getJarOrder().split(",")));
                for (File file : jarMods.listFiles()) {
                    if (jarmods.contains(file.getName())) {
                        continue;
                    }
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
                for (String mod : jarmods) {
                    File thisFile = new File(jarMods, mod);
                    if (thisFile.exists()) {
                        cpb.append(File.pathSeparator);
                        cpb.append(thisFile);
                    }
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

        String path = App.settings.getJavaPath() + File.separator + "bin" + File.separator + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        if (Utils.isWindows()) {
            arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

        arguments.add("-XX:-OmitStackTraceInFastThrow");

        if (App.settings.getJavaParameters().isEmpty()) {
            // Mojang launcher defaults if user has no custom java arguments
            arguments.add("-XX:+UseConcMarkSweepGC");
            arguments.add("-XX:+CMSIncrementalMode");
            arguments.add("-XX:-UseAdaptiveSizePolicy");
        }

        arguments.add("-Xms" + App.settings.getInitialMemory() + "M");

        if (App.settings.getMaximumMemory() < instance.getMemory()) {
            if ((Utils.getMaximumRam() / 2) < instance.getMemory()) {
                arguments.add("-Xmx" + App.settings.getMaximumMemory() + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + App.settings.getMaximumMemory() + "M");
        }
        if (App.settings.getPermGen() < instance.getPermGen() && (Utils.getMaximumRam() / 8) < instance.getPermGen()) {
            if (Utils.isJava8()) {
                arguments.add("-XX:MetaspaceSize=" + instance.getPermGen() + "M");
            } else {
                arguments.add("-XX:PermSize=" + instance.getPermGen() + "M");
            }
        } else {
            if (Utils.isJava8()) {
                arguments.add("-XX:MetaspaceSize=" + App.settings.getPermGen() + "M");
            } else {
                arguments.add("-XX:PermSize=" + App.settings.getPermGen() + "M");
            }
        }

        arguments.add("-Duser.language=en");
        arguments.add("-Duser.country=US");
        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (Utils.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:icon=" + new File(App.settings.getImagesDir(), "OldMinecraftIcon.png")
                    .getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");
        }

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                if (!arg.isEmpty()) {
                    if (instance.hasExtraArguments()) {
                        if (instance.getExtraArguments().contains(arg)) {
                            LogManager.error("Duplicate argument " + arg + " found and not added!");
                            continue;
                        }
                    }

                    if (arguments.toString().contains(arg)) {
                        LogManager.error("Duplicate argument " + arg + " found and not added!");
                        continue;
                    }

                    arguments.add(arg);
                }
            }
        }

        arguments.add("-cp");
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String pathh = null;
        try {
            pathh = thisFile.getCanonicalPath();
            pathh = URLDecoder.decode(pathh, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            pathh = System.getProperty("java.class.path");
            App.settings.logStackTrace(e);
        } catch (IOException e) {
            pathh = System.getProperty("java.class.path");
            App.settings.logStackTrace(e);
        }
        System.out.println(System.getProperty("java.class.path"));
        arguments.add(pathh + cpb.toString());

        arguments.add(LegacyMCLauncher.class.getCanonicalName());

        // Start or passed in arguments
        arguments.add(instance.getRootDirectory().getAbsolutePath()); // Path
        arguments.add(account.getMinecraftUsername()); // Username
        arguments.add(sess.getAuth().getAuthenticatedToken()); // Session
        arguments.add(instance.getName()); // Instance Name
        arguments.add(App.settings.getWindowWidth() + ""); // Window Width
        arguments.add(App.settings.getWindowHeight() + ""); // Window Height
        if (App.settings.startMinecraftMaximised()) {
            arguments.add("true"); // Maximised
        } else {
            arguments.add("false"); // Not Maximised
        }

        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            if (App.settings != null) {
                argsString = argsString.replace(App.settings.getBaseDir().getAbsolutePath(), "USERSDIR");
            }

            argsString = argsString.replace(account.getMinecraftUsername(), "REDACTED");
            argsString = argsString.replace(sess.getAuth().getAuthenticatedToken(), "REDACTED");
        }

        LogManager.info("Launching Minecraft with the following arguments " + "(user related stuff has been removed):" +
                " " + argsString);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }

    public static void main(String[] args) {
        String workingDirectory = args[0];
        String username = args[1];
        String session = args[2];
        String instanceName = args[3];
        int screenWidth = Integer.parseInt(args[4]);
        int screenHeight = Integer.parseInt(args[5]);
        boolean maximize = Boolean.parseBoolean(args[6]);

        Dimension winSize = new Dimension(screenWidth, screenHeight);
        boolean compatMode = false;

        File cwd = new File(workingDirectory);

        try {
            File binDir = new File(cwd, "bin");
            File lwjglDir = binDir;

            System.out.println("Loading jars...");
            String lwjgl = "lwjgl.jar";
            String lwjgl_util = "lwjgl_util.jar";
            String jinput = "jinput.jar";
            File[] files = new File(workingDirectory, "bin").listFiles();
            for (File file : files) {
                if (file.getName().startsWith("lwjgl-")) {
                    lwjgl = file.getName();
                } else if (file.getName().startsWith("lwjgl_util-")) {
                    lwjgl_util = file.getName();
                } else if (file.getName().startsWith("jinput-")) {
                    jinput = file.getName();
                }
            }
            String[] lwjglJars = new String[]{lwjgl, lwjgl_util, jinput};

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

            URLClassLoader cl = new URLClassLoader(urls, LegacyMCLauncher.class.getClassLoader());

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
                        String entryName = entry.getName().substring(0, entry.getName().lastIndexOf('.'));
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
                            System.out.println("Found class implementing runnable: " + cls.getName());
                        }

                        if (getMCPathField(cls) == null) {
                            continue;
                        } else {
                            System.out.println("Found class implementing runnable " + "with mcpath field: " + cls
                                    .getName());
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
                    MCFrame mcWindow = new MCFrame(Constants.LAUNCHER_NAME + " - " + instanceName);
                    mcWindow.start(mcappl, username, session, winSize, maximize);
                } catch (InstantiationException e) {
                    System.out.println("Applet wrapper failed! Falling back " + "to compatibility mode.");
                    mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
                } finally {
                    try {
                        cl.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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