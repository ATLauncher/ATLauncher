/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.atlauncher.App;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

public abstract class AbstractToolPanel extends JPanel {
    private static final long serialVersionUID = -7755529465856056647L;

    protected final Font BOLD_FONT = App.THEME.getBoldFont().deriveFont(Utils.getBaseFontSize());
    protected final JPanel TOP_PANEL = new JPanel();
    protected final JPanel MIDDLE_PANEL = new JPanel();
    protected final JPanel BOTTOM_PANEL = new JPanel();

    protected final JButton LAUNCH_BUTTON = new JButton(GetText.tr("Launch"));

    public AbstractToolPanel() {
        setLayout(new BorderLayout());
        add(TOP_PANEL, BorderLayout.NORTH);
        add(MIDDLE_PANEL, BorderLayout.CENTER);
        add(BOTTOM_PANEL, BorderLayout.SOUTH);
    }
}
