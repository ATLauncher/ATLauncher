/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.mclauncher.legacy;

import java.applet.Applet;
import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class LegacyMCLauncher {
    public static void main(String[] args) {
        String workingDirectory = args[0];
        String username = args[1];
        String session = args[2];
        String frameTitle = args[3];
        int screenWidth = Integer.parseInt(args[4]);
        int screenHeight = Integer.parseInt(args[5]);
        boolean maximize = Boolean.parseBoolean(args[6]);

        Dimension winSize = new Dimension(screenWidth, screenHeight);

        File cwd = new File(workingDirectory);

        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();

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
                System.err.println("Failed to find Minecraft main class.");
                System.exit(-1);
            }

            System.setProperty("minecraft.applet.TargetDirectory", cwd.getAbsolutePath());

            String[] mcArgs = new String[2];
            mcArgs[0] = username;
            mcArgs[1] = session;

            System.out.println("Launching with applet wrapper...");
            try {
                Class<?> MCAppletClass = cl.loadClass("net.minecraft.client.MinecraftApplet");
                Applet mcappl = (Applet) MCAppletClass.newInstance();
                MCFrame mcWindow = new MCFrame(frameTitle);
                mcWindow.start(mcappl, username, session, winSize, maximize);
            } catch (InstantiationException e) {
                System.out.println("Applet wrapper failed! Falling back " + "to compatibility mode.");
                mc.getMethod("main", String[].class).invoke(null, (Object) mcArgs);
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
