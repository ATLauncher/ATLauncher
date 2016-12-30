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

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import com.atlauncher.utils.Utils;

import net.minecraft.Launcher;

public class MCFrame extends Frame implements WindowListener {

    private static final long serialVersionUID = -2036853903287698498L;
    private Launcher appletWrap = null;

    public MCFrame(String title) {
        super(title);
        setIconImage(Utils.getImage("/assets/image/OldMinecraftIcon.png"));
        this.addWindowListener(this);
    }

    public void start(Applet mcApplet, String user, String session, Dimension winSize, boolean maximize) {
        try {
            appletWrap = new Launcher(mcApplet, new URL("http://www.minecraft.net/game"));
        } catch (MalformedURLException ignored) {
        }

        appletWrap.setParameter("username", user);
        appletWrap.setParameter("sessionid", session);
        appletWrap.setParameter("stand-alone", "true"); // Show the quit button.
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

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
                System.out.println("FORCING EXIT!");
                System.exit(0);
            }
        }.start();

        if (appletWrap != null) {
            appletWrap.stop();
            appletWrap.destroy();
        }
        // old minecraft versions can hang without this >_<
        System.exit(0);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }
}