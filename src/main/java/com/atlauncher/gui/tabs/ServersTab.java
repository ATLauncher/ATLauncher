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
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.viewmodel.base.IServersTabViewModel;
import com.atlauncher.viewmodel.impl.ServersTabViewModel;
import com.formdev.flatlaf.icons.FlatSearchIcon;

@SuppressWarnings("serial")
public class ServersTab extends JPanel implements Tab, RelocalizationListener {
    private JTextField searchBox;

    private JPanel panel;
    private JScrollPane scrollPane;

    private NilCard nilCard = new NilCard(
            getNilMessage(),
            new NilCard.Action[] {
                    NilCard.Action.createCreatePackAction(),
                    NilCard.Action.createDownloadPackAction()
            });

    private final IServersTabViewModel viewModel = new ServersTabViewModel();

    public ServersTab() {
        setLayout(new BorderLayout());
        createView();
        RelocalizationManager.addListener(this);
    }

    public void createView() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        searchBox = new JTextField(16);
        viewModel.addOnSearchChangeListener(searchBox::setText);
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    String text = searchBox.getText();
                    Analytics.trackEvent(AnalyticsEvent.forSearchEvent("servers", text));
                    viewModel.setSearch(text);
                }
            }
        });
        searchBox.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchBox.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchBox.putClientProperty("JTextField.showClearButton", true);
        searchBox.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            viewModel.setSearch("");
        });
        topPanel.add(searchBox);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.insets = UIConstants.FIELD_INSETS_SMALL;
        gbc.fill = GridBagConstraints.BOTH;

        viewModel.addOnChangeViewListener(servers -> {
            viewModel.setViewPosition(scrollPane.getVerticalScrollBar().getValue());
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
        });

        viewModel.addOnViewPositionChangedListener(scrollPane.getVerticalScrollBar()::setValue);
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
    public void onRelocalization() {
        searchBox.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        nilCard.setMessage(getNilMessage());
        nilCard.setActions(new NilCard.Action[] {
                NilCard.Action.createCreatePackAction(),
                NilCard.Action.createDownloadPackAction()
        });
    }
}
