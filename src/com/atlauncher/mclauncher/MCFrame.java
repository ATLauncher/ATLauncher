/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.mclauncher;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.Launcher;

import com.atlauncher.utils.Utils;

public class MCFrame extends Frame implements WindowListener {
    private Launcher appletWrap = null;

    public MCFrame(String title) {
        super(title);
        setIconImage(Utils.getImage("/resources/OldMinecraftIcon.png"));
        this.addWindowListener(this);
    }

    public void start(Applet mcApplet, String user, String session, Dimension winSize,
            boolean maximize) {
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
        if (maximize)
            this.setExtendedState(MAXIMIZED_BOTH);

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
            public void run() {
                try {
                    Thread.sleep(30000L);
                } catch (InterruptedException localInterruptedException) {
                    localInterruptedException.printStackTrace();
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