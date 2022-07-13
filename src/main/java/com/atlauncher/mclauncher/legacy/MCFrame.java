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
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.Launcher;

public class MCFrame extends Frame {
    private Launcher appletWrap = null;

    public MCFrame(String title) {
        super(title);

        try {
            setIconImage(MCFrame.getImage("/assets/image/old-minecraft-icon.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(30000L);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        System.exit(0);
                    }
                }.start();

                if (appletWrap != null) {
                    appletWrap.stop();
                    appletWrap.destroy();
                }

                dispose();
                System.exit(0);
            }
        });
    }

    public static BufferedImage getImage(String img) {
        InputStream stream = MCFrame.class.getResourceAsStream(img);

        if (stream == null) {
            return null;
        }

        try {
            return ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void start(Applet mcApplet, String user, String session, Dimension winSize, boolean maximize) {
        appletWrap = new Launcher(mcApplet);

        appletWrap.setParameter("username", user);
        appletWrap.setParameter("sessionid", session);
        appletWrap.setParameter("stand-alone", "true");

        mcApplet.setStub(appletWrap);

        this.add(appletWrap);
        appletWrap.setPreferredSize(winSize);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
        if (maximize) {
            this.setExtendedState(MAXIMIZED_BOTH);
        }

        validate();
        appletWrap.init();
        appletWrap.start();
        setVisible(true);
    }
}
