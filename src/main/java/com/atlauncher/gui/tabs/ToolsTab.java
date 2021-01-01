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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.atlauncher.gui.tabs.tools.BlankToolPanel;
import com.atlauncher.gui.tabs.tools.DebugModePanel;
import com.atlauncher.gui.tabs.tools.LogClearerToolPanel;
import com.atlauncher.gui.tabs.tools.NetworkCheckerToolPanel;
import com.atlauncher.gui.tabs.tools.RuntimeDownloaderToolPanel;
import com.atlauncher.gui.tabs.tools.ServerCheckerToolPanel;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ToolsTab extends JPanel implements Tab {

    public ToolsTab() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridLayout(3, 2, 10, 10));

        mainPanel.add(new NetworkCheckerToolPanel());
        mainPanel.add(new ServerCheckerToolPanel());
        mainPanel.add(new LogClearerToolPanel());
        mainPanel.add(new DebugModePanel());
        mainPanel.add(new RuntimeDownloaderToolPanel());
        mainPanel.add(new BlankToolPanel());

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return GetText.tr("Tools");
    }
}
