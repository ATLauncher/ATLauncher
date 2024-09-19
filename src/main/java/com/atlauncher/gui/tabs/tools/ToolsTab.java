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
package com.atlauncher.gui.tabs.tools;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.Tab;

public class ToolsTab extends HierarchyPanel implements Tab {

    private IToolsViewModel viewModel;

    public ToolsTab() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    @Override
    protected void onShow() {
        JPanel mainPanel = new JPanel();

        mainPanel.setLayout(new GridLayout(3, 2, 10, 10));

        mainPanel.add(new NetworkCheckerToolPanel(viewModel));
        mainPanel.add(new LogClearerToolPanel(viewModel));
        mainPanel.add(new DebugModePanel(viewModel));
        mainPanel.add(new DownloadClearerToolPanel(viewModel));
        mainPanel.add(new SkinUpdaterToolPanel(viewModel));
        mainPanel.add(new LibrariesDeleterToolPanel(viewModel));

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return GetText.tr("Tools");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Tools";
    }

    @Override
    protected void createViewModel() {
        viewModel = new ToolsViewModel();
    }

    @Override
    protected void onDestroy() {
        removeAll();
    }
}
