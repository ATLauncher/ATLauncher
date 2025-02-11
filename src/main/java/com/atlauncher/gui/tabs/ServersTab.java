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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.viewmodel.base.IServersTabViewModel;
import com.atlauncher.viewmodel.impl.ServersTabViewModel;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class ServersTab extends HierarchyPanel implements Tab, RelocalizationListener {
    private JTextField searchBox;

    private JPanel panel;
    private JScrollPane scrollPane;

    private NilCard nilCard = new NilCard(
            getNilMessage(),
            new NilCard.Action[] {
                    NilCard.Action.createCreateServerAction(),
                    NilCard.Action.createDownloadServerAction()
            });

    private IServersTabViewModel viewModel;

    public ServersTab() {
        super(new BorderLayout());
    }

    @Override
    protected void onShow() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        searchBox = new JTextField(16);
        addDisposable(
                viewModel.getSearchObservable().subscribe(it -> searchBox.setText(it.orElse(null))));
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String text = searchBox.getText();
                    Analytics.trackEvent(AnalyticsEvent.forSearchEvent("servers", text));
                    viewModel.setSearchSubject(text);
                }
            }
        });
        searchBox.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchBox.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchBox.putClientProperty("JTextField.showClearButton", true);
        searchBox.putClientProperty("JTextField.clearCallback", (Runnable) () -> viewModel.setSearchSubject(""));
        topPanel.add(searchBox);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());

        addDisposable(viewModel.getServersObservable().subscribe(servers -> {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.insets = UIConstants.FIELD_INSETS_SMALL;
            gbc.fill = GridBagConstraints.BOTH;

            panel.removeAll();
            gbc.gridy = 0;

            servers.forEach(server -> {
                panel.add(new ServerCard(server), gbc);
                gbc.gridy++;
            });

            if (panel.getComponentCount() == 0) {
                panel.add(nilCard, gbc);
            }

            validate();
            repaint();
            searchBox.requestFocus();
        }));

        addDisposable(
                viewModel.getViewPosition().subscribe(scrollPane.getVerticalScrollBar()::setValue));
    }

    @Override
    public String getTitle() {
        return GetText.tr("Servers");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Servers";
    }

    private static String getNilMessage() {
        return new HTMLBuilder()
                .text(GetText.tr("There are no servers to display.<br/><br/>Install one from the Packs tab."))
                .build();
    }

    @Override
    protected void createViewModel() {
        viewModel = new ServersTabViewModel();
    }

    @Override
    protected void onDestroy() {
        if (scrollPane != null) {
            viewModel.setViewPosition(scrollPane.getVerticalScrollBar().getValue());
        }
        removeAll();
        searchBox = null;
        panel = null;
        scrollPane = null;
    }
}
