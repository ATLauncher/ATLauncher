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
package com.atlauncher.gui.tabs.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.atlauncher.App;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public abstract class AbstractToolPanel extends JPanel {
    protected final JPanel MIDDLE_PANEL = new JPanel();
    protected final JPanel BOTTOM_PANEL = new JPanel(new FlowLayout());

    protected final JButton LAUNCH_BUTTON = new JButton(GetText.tr("Launch"));

    public AbstractToolPanel(String TITLE) {
        setLayout(new BorderLayout());
        add(MIDDLE_PANEL, BorderLayout.CENTER);
        add(BOTTOM_PANEL, BorderLayout.SOUTH);

        LAUNCH_BUTTON.setFont(App.THEME.getNormalFont().deriveFont(16f));

        if (TITLE != null) {
            setBorder(BorderFactory.createTitledBorder(null, TITLE, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                    App.THEME.getBoldFont()));
        }
    }
}
