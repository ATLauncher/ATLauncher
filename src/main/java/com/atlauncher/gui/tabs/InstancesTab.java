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

import com.atlauncher.gui.tabs.instances.InstanceImportButton;
import com.atlauncher.gui.tabs.instances.InstanceListPanel;
import com.atlauncher.gui.tabs.instances.InstanceSearchPanel;
import com.atlauncher.utils.Utils;
import com.google.common.collect.Sets;
import org.mini2Dx.gettext.GetText;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;

@Singleton
public class InstancesTab extends JPanel implements Tab {
    private static final long serialVersionUID = -969812552965390610L;
    private final InstanceListPanel instanceListPanel = new InstanceListPanel(Sets.newHashSet());

    @Inject
    public InstancesTab() {
        this.setLayout(new BorderLayout());
        this.add(createSearchPanel(), BorderLayout.NORTH);
        this.add(this.createScrollPane(), BorderLayout.CENTER);
    }

    private JPanel createSearchPanel(){
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new InstanceImportButton(), BorderLayout.WEST);
        panel.add(new InstanceSearchPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createScrollPane(){
        final JScrollPane pane = Utils.wrapInVerticalScroller(this.instanceListPanel, 16);
        pane.getVerticalScrollBar().setUnitIncrement(16);
        return pane;
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
