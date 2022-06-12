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

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.events.LocalizationEvent;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.ServerCard;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class ServersTab extends JPanel implements Tab{
    private JTextField searchBox;

    private String searchText = null;

    private JPanel panel;
    private JScrollPane scrollPane;
    private int currentPosition = 0;

    private NilCard nilCard;

    public ServersTab() {
        setLayout(new BorderLayout());
        loadContent(false);
        AppEventBus.register(this);
    }

    public void loadContent(boolean keepFilters) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        searchBox = new JTextField(16);
        if (keepFilters) {
            searchBox.setText(this.searchText);
        }
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    Analytics.sendEvent(searchBox.getText(), "Search", "Server");
                    reload();
                }
            }
        });
        searchBox.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchBox.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchBox.putClientProperty("JTextField.showClearButton", true);
        searchBox.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchBox.setText("");
            reload();
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

        ServerManager.getServersSorted().forEach(server -> {
            if (keepFilters) {
                boolean showServer = true;

                if (searchText != null) {
                    if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE).matcher(server.name)
                            .find()) {
                        showServer = false;
                    }
                }

                if (showServer) {
                    panel.add(new ServerCard(server), gbc);
                    gbc.gridy++;
                }
            } else {
                panel.add(new ServerCard(server), gbc);
                gbc.gridy++;
            }
        });

        if (panel.getComponentCount() == 0) {
            nilCard = new NilCard(GetText.tr("There are no servers to display.\n\nInstall one from the Packs tab."));
            panel.add(nilCard, gbc);
        }

        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(currentPosition));
    }

    public void reload() {
        this.currentPosition = scrollPane.getVerticalScrollBar().getValue();
        this.searchText = searchBox.getText();
        if (this.searchText.isEmpty()) {
            this.searchText = null;
        }
        removeAll();
        loadContent(true);
        validate();
        repaint();
        searchBox.requestFocus();
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
    public final void onLocalizationChanged(final LocalizationEvent.LocalizationChangedEvent event){
        searchBox.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        if (nilCard != null) {
            nilCard.setMessage(GetText.tr("There are no servers to display.\n\nInstall one from the Packs tab."));
        }
    }
}
