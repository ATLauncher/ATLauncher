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
package com.atlauncher.gui.panels.packbrowser;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.ThemeListener;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.utils.Utils;

public class PacksBrowserTabTitlePanel extends JPanel implements ThemeListener {
    private final JLabel label = new JLabel(null, null, SwingConstants.CENTER);
    private final String icon;

    public PacksBrowserTabTitlePanel(String platform, String icon) {
        this.icon = icon;

        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 1));

        label.setIcon(Utils.getIconImage(App.THEME.getResourcePath("image/modpack-platform", icon)));

        add(label, BorderLayout.CENTER);

        ThemeManager.addListener(this);

        JLabel title = new JLabel(platform);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.SOUTH);
    }

    public PacksBrowserTabTitlePanel(String platform) {
        this(platform, platform.toLowerCase());
    }

    @Override
    public void onThemeChange() {
        label.setIcon(Utils.getIconImage(App.THEME.getResourcePath("image/modpack-platform", icon)));
    }
}
