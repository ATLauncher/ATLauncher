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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.instances.InstancesListPanel;
import com.atlauncher.gui.tabs.instances.InstancesNavigationPanel;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import com.atlauncher.viewmodel.impl.InstancesTabViewModel;

public class InstancesTab extends HierarchyPanel implements Tab {
    private static final long serialVersionUID = -969812552965390610L;

    private IInstancesTabViewModel viewModel;
    private InstancesNavigationPanel navigationPanel;
    private InstancesListPanel instancesListPanel;
    private JScrollPane scrollPane;

    public InstancesTab() {
        super(new BorderLayout());
        this.setName("instancesPanel");
    }

    @Override
    public String getTitle() {
        return GetText.tr("Instances");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Instances";
    }

    @Override
    protected void createViewModel() {
        viewModel = new InstancesTabViewModel();
    }

    @Override
    protected void onShow() {
        navigationPanel = new InstancesNavigationPanel(this, viewModel);
        instancesListPanel = new InstancesListPanel(this, viewModel);
        scrollPane = Utils.wrapInVerticalScroller(this.instancesListPanel, 16);
        this.add(this.navigationPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void onDestroy() {
        removeAll();
        navigationPanel = null;
        instancesListPanel = null;
        scrollPane = null;
    }
}
