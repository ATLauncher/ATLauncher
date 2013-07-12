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
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.Launcher;

import com.atlauncher.gui.Utils;

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