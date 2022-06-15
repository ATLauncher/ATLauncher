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

import com.atlauncher.AppEventBus;
import com.atlauncher.data.Server;
import com.atlauncher.events.servers.ServerAddedEvent;
import com.atlauncher.gui.tabs.servers.ServerListPanel;
import com.atlauncher.gui.tabs.servers.ServerSearchField;
import com.atlauncher.gui.tabs.servers.ServerSearchPanel;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.utils.Utils;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
@Singleton
public class ServersTab extends JPanel implements Tab{
    @Inject
    public ServersTab(final ServerSearchPanel searchPanel,
                      final ServerListPanel serverListPanel) {
        AppEventBus.registerToUIOnly(this);
        this.setLayout(new BorderLayout());
        this.add(searchPanel, BorderLayout.NORTH);
        this.add(Utils.wrapInVerticalScroller(serverListPanel, 16), BorderLayout.CENTER);
    }

    @Override
    public String getTitle() {
        return GetText.tr("Servers");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Servers";
    }

    @Subscribe
    public void onServerAdded(final ServerAddedEvent event){
        this.repaint();
    }
}
