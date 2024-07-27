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
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.graphql.GetServersForModPackQuery;
import com.atlauncher.gui.borders.IconTitledBorder;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.BackgroundImageWorker;

public final class ServerForModPackCard extends JPanel {
    private final JButton playAndJoinButton = new JButton(GetText.tr("Play & Join"));

    public ServerForModPackCard(final GetServersForModPackQuery.ServersForPack server, ActionListener playAl) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(380, 250));
        setBorder(new IconTitledBorder(server.name(), App.THEME.getBoldFont().deriveFont(12f), null));

        JPanel summaryPanel = new JPanel(new BorderLayout());
        JTextArea summary = new JTextArea();
        summary.setText(server.description());
        summary.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        summary.setEditable(false);
        summary.setHighlighter(null);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setEditable(false);

        JLabel icon = new JLabel(Utils.getIconImage("/assets/image/no-icon.png"));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        icon.setVisible(false);

        summaryPanel.add(icon, BorderLayout.NORTH);
        summaryPanel.add(summary, BorderLayout.CENTER);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton viewButton = new JButton(GetText.tr("View"));

        buttonsPanel.add(playAndJoinButton);
        buttonsPanel.add(viewButton);

        playAndJoinButton.addActionListener(e -> {
            playAl.actionPerformed(e);
        });
        viewButton.addActionListener(e -> {
            OS.openWebBrowser(String.format("https://atlauncher.com/server/%s", server.safeName()));
        });

        add(summaryPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        if (server.bannerUrl() != null && !server.bannerUrl().isEmpty()) {
            new BackgroundImageWorker(icon, server.bannerUrl(), 350, 60).execute();
        }
    }
}
