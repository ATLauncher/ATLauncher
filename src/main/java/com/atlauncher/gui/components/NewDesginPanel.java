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
package com.atlauncher.gui.components;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Server;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.listener.ThemeListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.evnt.manager.ThemeManager;

/**
 * The user-triggered collapsible panel containing the component (trigger) in
 * the titled border
 */
public class NewDesginPanel extends JPanel implements ThemeListener, RelocalizationListener {
    public static final long serialVersionUID = -343234;

    public JLabel mainTitile = createTitleLabel();// the arrow
    Server server = null;
    Instance instance = null;

    /**
     * @param instance            Given instance
     * @param instanceTitleFormat Title format for said instance
     */
    public NewDesginPanel(Instance instance, String instanceTitleFormat) {
        this.instance = instance;
        String title;

        try {
            title = String.format(instanceTitleFormat, instance.launcher.name, instance.launcher.pack,
                    instance.launcher.version, instance.id);
        } catch (Throwable t) {
            title = instance.launcher.name;
        }

        if (instance.launcher.isPlayable) {
            mainTitile.setText(title.substring(0, 16) + "...");
            mainTitile.setToolTipText(title);
            mainTitile.setForeground(UIManager.getColor("CollapsiblePanel.normal"));
        } else {
            mainTitile.setText(title.substring(0, 12) + "..." + " - " + "Corrupted)");
            mainTitile.setToolTipText(title + " - " + "Corrupted)");
            mainTitile.setForeground(UIManager.getColor("CollapsiblePanel.error"));
        }
        commonConstructor();

    }

    public NewDesginPanel(Server server) {
        this.server = server;
        mainTitile.setText(server.name.substring(0, 8) + "..." + " (" + server.pack + " " + server.version + ")");
        mainTitile.setToolTipText(server.name + " (" + server.pack + " " + server.version + ")");
        mainTitile.setForeground(UIManager.getColor("CollapsiblePanel.normal"));
        commonConstructor();

    }

    /**
     * Sets layout, creates the content panel and adds it and the title component to
     * the container, all constructors have this procedure in common.
     */
    private void commonConstructor() {
        mainTitile.setAlignmentX(Component.LEFT_ALIGNMENT);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createLineBorder(getBackground().brighter(), 2));
        ThemeManager.addListener(this);
        RelocalizationManager.addListener(this);
    }

    /**
     * Returns a button with an arrow icon and a collapse/expand action listener.
     */
    private JLabel createTitleLabel() {
        JLabel label = new JLabel();
        label.setFont(App.THEME.getBoldFont().deriveFont(15f));
        label.setFocusable(false);
        return label;
    }

    @Override
    public void onThemeChange() {
        updateUI();
    }

    @Override
    public void onRelocalization() {
        mainTitile.setFont(App.THEME.getBoldFont().deriveFont(15f));
    }

}
