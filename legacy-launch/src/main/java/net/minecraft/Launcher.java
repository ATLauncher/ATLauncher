/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package net.minecraft;

import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

public class Launcher extends Applet implements AppletStub {
    private Applet wrappedApplet;
    private boolean active = false;
    private final Map<String, String> params;

    public Launcher(Applet applet) {
        params = new TreeMap<String, String>();

        this.setLayout(new BorderLayout());
        this.add(applet, "Center");
        this.wrappedApplet = applet;
    }

    public void setParameter(String name, String value) {
        params.put(name, value);
    }

    public void replace(Applet applet) {
        this.wrappedApplet = applet;

        applet.setStub(this);
        applet.setSize(getWidth(), getHeight());

        this.setLayout(new BorderLayout());
        this.add(applet, "Center");

        applet.init();
        active = true;
        applet.start();
        validate();
    }

    @Override
    public String getParameter(String name) {
        String param = params.get(name);
        if (param != null)
            return param;
        try {
            return super.getParameter(name);
        } catch (Exception ignore) {
        }
        return null;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void appletResize(int width, int height) {
        wrappedApplet.resize(width, height);
    }

    @Override
    public void resize(int width, int height) {
        wrappedApplet.resize(width, height);
    }

    @Override
    public void resize(Dimension d) {
        wrappedApplet.resize(d);
    }

    @Override
    public void init() {
        if (wrappedApplet != null) {
            wrappedApplet.init();
        }
    }

    @Override
    public void start() {
        wrappedApplet.start();
        active = true;
    }

    @Override
    public void stop() {
        wrappedApplet.stop();
        active = false;
    }

    public void destroy() {
        wrappedApplet.destroy();
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException ignored) {
        }

        return null;
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new URL("http://www.minecraft.net/game/");
        } catch (MalformedURLException ignored) {
        }

        return null;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        wrappedApplet.setVisible(b);
    }

    public void update(Graphics paramGraphics) {
    }

    public void paint(Graphics paramGraphics) {
    }
}
