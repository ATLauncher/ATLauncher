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
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.EventListenerList;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.gui.tabs.instances.InstancesListPanel;
import com.atlauncher.gui.tabs.instances.InstancesNavigationPanel;
import com.atlauncher.gui.tabs.instances.InstancesSearchEvent;
import com.atlauncher.gui.tabs.instances.InstancesSearchEventListener;
import com.atlauncher.gui.tabs.instances.InstancesSortEvent;
import com.atlauncher.gui.tabs.instances.InstancesSortEventListener;
import com.atlauncher.utils.Utils;

public class InstancesTab extends JPanel implements Tab {
    private static final long serialVersionUID = -969812552965390610L;

    private final EventListenerList eventListeners = new EventListenerList();
    private final InstancesNavigationPanel navigationPanel = new InstancesNavigationPanel(this);
    private final InstancesListPanel instancesListPanel = new InstancesListPanel(this);
    private final JScrollPane scrollPane = Utils.wrapInVerticalScroller(this.instancesListPanel, 16);

    public InstancesTab() {
        this.setLayout(new BorderLayout());
        this.add(this.navigationPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public void addSearchEventListener(final InstancesSearchEventListener listener) {
        this.eventListeners.add(InstancesSearchEventListener.class, listener);
    }

    public void addSortEventListener(final InstancesSortEventListener listener) {
        this.eventListeners.add(InstancesSortEventListener.class, listener);
    }

    public void fireSearchEvent(final InstancesSearchEvent e) {
        Arrays.stream(this.eventListeners.getListeners(InstancesSearchEventListener.class))
                .forEach((l) -> l.onSearch(e));
    }

    public void fireSortEvent(final InstancesSortEvent e) {
        Arrays.stream(this.eventListeners.getListeners(InstancesSortEventListener.class))
                .forEach((l) -> l.onSort(e));
    }

    public void reload() {
        instancesListPanel.loadInstances();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Instances");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Instances";
    }
}
