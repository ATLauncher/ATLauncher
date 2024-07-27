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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.NotNull;
import org.mini2Dx.gettext.GetText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy.FetchStrategy;
import com.apollographql.apollo.exception.ApolloException;
import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.graphql.GetServersForModPackQuery;
import com.atlauncher.gui.card.ServerForModPackCard;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.panels.NoServersPanel;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.network.analytics.AnalyticsEvent;

@SuppressWarnings("serial")
public final class ServersDialog extends JDialog {
    private final Instance instance;

    private final ActionListener playAl;

    private final JPanel contentPanel = new JPanel(new WrapLayout());

    private JScrollPane jscrollPane;
    private final JPanel mainPanel = new JPanel(new BorderLayout());

    public ServersDialog(Instance instance, ActionListener playAl) {
        this(App.launcher.getParent(), instance, playAl);
    }

    public ServersDialog(Window parent, Instance instance, ActionListener playAl) {
        // #. {0} is the name of the modpack
        super(parent, GetText.tr("Servers For {0}", instance.launcher.pack), ModalityType.DOCUMENT_MODAL);
        this.instance = instance;
        this.playAl = playAl;

        this.setPreferredSize(new Dimension(1200, 810));
        this.setMinimumSize(new Dimension(1200, 810));
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        setupComponents();

        loadServers();

        this.pack();
        this.setLocationRelativeTo(parent);
        this.setVisible(true);
    }

    private void setupComponents() {
        Analytics.sendScreenView("Servers Dialog");

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.jscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(this.jscrollPane, BorderLayout.CENTER);

        this.add(mainPanel, BorderLayout.CENTER);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            contentPanel.removeAll();
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new LoadingPanel(), BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    private void loadServers() {
        setLoading(true);

        GraphqlClient.apolloClient
                .query(new GetServersForModPackQuery(instance.getPackId(), instance.getServersPackPlatform()))
                .toBuilder()
                .httpCachePolicy(new HttpCachePolicy.Policy(FetchStrategy.CACHE_FIRST, 30, TimeUnit.MINUTES, false))
                .build()
                .enqueue(new ApolloCall.Callback<GetServersForModPackQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetServersForModPackQuery.Data> response) {
                        setLoading(false);
                        setServers(response.getData().serversForPack());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LogManager.logStackTrace("Error fetching servers", e);
                        setLoading(false);
                    }
                });
    }

    private void setServers(List<GetServersForModPackQuery.ServersForPack> servers) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        if (servers.isEmpty()) {
            contentPanel.setLayout(new BorderLayout());
            contentPanel.add(new NoServersPanel(), BorderLayout.CENTER);
        } else {
            contentPanel.setLayout(new WrapLayout());

            servers.stream().sorted(Comparator.comparing(GetServersForModPackQuery.ServersForPack::isFeatured,
                    Comparator.reverseOrder()).thenComparingInt(GetServersForModPackQuery.ServersForPack::position))
                    .forEach(server -> {
                        contentPanel.add(new ServerForModPackCard(server, e -> {
                            Analytics.trackEvent(
                                    AnalyticsEvent.forInstanceServerEvent("instance_server_launch", instance, server));
                            playAl.actionPerformed(e);
                            setVisible(false);
                            dispose();
                        }), gbc);
                        gbc.gridy++;
                    });
        }

        SwingUtilities.invokeLater(() -> jscrollPane.getVerticalScrollBar().setValue(0));

        revalidate();
        repaint();
    }
}
