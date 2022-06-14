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
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Server;
import com.atlauncher.events.localization.LocalizationChangedEvent;
import com.atlauncher.events.servers.ServerAddedEvent;
import com.atlauncher.events.servers.ServerRemovedEvent;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.atlauncher.gui.tabs.servers.ServerListComponent;
import com.atlauncher.gui.tabs.servers.ServerSearchField;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class ServersTab extends JPanel implements Tab{
    private final ServerListComponent serversList = new ServerListComponent(ServerManager.getServers());
    private final ServerSearchField searchField = new ServerSearchField();
    private final JScrollPane scrollPane = createScrollPanel(this.serversList);
    private int currentPosition = 0;

    public ServersTab() {
        AppEventBus.register(this);
        this.setLayout(new BorderLayout());
        this.add(this.createTopPanel(), BorderLayout.NORTH);
        this.add(this.scrollPane, BorderLayout.CENTER);
    }

    private static JScrollPane createScrollPanel(final ServerListComponent component){
        final JScrollPane scrollPane = new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createTopPanel(){
        final JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(this.searchField);
        return topPanel;
    }

    public void loadContent(boolean keepFilters) {
//        ServerManager.getServers().forEach(server -> {
//            if (keepFilters) {
//                boolean showServer = true;
//
//                if (searchText != null) {
//                    if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE).matcher(server.name)
//                        .find()) {
//                        showServer = false;
//                    }
//                }
//
//                if (showServer) {
//                    panel.add(new ServerCard(server), gbc);
//                    gbc.gridy++;
//                }
//            } else {
//                panel.add(new ServerCard(server), gbc);
//                gbc.gridy++;
//            }
//        });
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(currentPosition));
    }

    public void reload() {
        this.currentPosition = scrollPane.getVerticalScrollBar().getValue();
        removeAll();

        loadContent(true);

        validate();
        repaint();
        this.searchField.requestFocus();
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
    public final void onLocalizationChanged(final LocalizationChangedEvent event) {
    }

    @Subscribe
    public final void onServerAdded(final ServerAddedEvent event){
        this.serversList.addServer(event.getServer());
    }

    @Subscribe
    public final void onServerRemoved(final ServerRemovedEvent event){
        this.serversList.removeServer(event.getServer());
    }
}
