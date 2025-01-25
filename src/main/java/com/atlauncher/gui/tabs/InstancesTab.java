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

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.instances.InstancesListPanel;
import com.atlauncher.gui.tabs.instances.InstancesNavigationPanel;
import com.atlauncher.gui.tabs.instances.NewDesignInstancesListPanel;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import com.atlauncher.viewmodel.impl.InstancesTabViewModel;

public class InstancesTab extends HierarchyPanel implements Tab {
    private static final long serialVersionUID = -969812552965390610L;

    private IInstancesTabViewModel viewModel;
    private InstancesNavigationPanel navigationPanel;
    private HierarchyPanel instancesListPanel;
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
        this.add(this.navigationPanel, BorderLayout.NORTH);
        if (App.settings.selectDesignLayout == UIConstants.LAYOUT_DEFAULT) {
            instancesListPanel = new InstancesListPanel(this, viewModel);
        } else if (App.settings.selectDesignLayout == UIConstants.LAYOUT_GRID) {
            instancesListPanel = new NewDesignInstancesListPanel(this, viewModel);
        }

        scrollPane = Utils.wrapInVerticalScroller(this.instancesListPanel, 16);

        this.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void onDestroy() {
        viewModel.setScroll(scrollPane.getVerticalScrollBar().getValue());
        removeAll();
        navigationPanel = null;
        instancesListPanel = null;
        scrollPane = null;
    }

    /**
     * Set the current scroll.
     * <p>
     * Avoid using this as much as possible.
     *
     * @param scroll scroll value
     */
    public void setScroll(int scroll) {
        scrollPane.getVerticalScrollBar().setValue(scroll);
    }
}
