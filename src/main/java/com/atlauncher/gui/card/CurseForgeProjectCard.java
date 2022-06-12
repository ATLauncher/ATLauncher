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
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.curseforge.CurseForgeAttachment;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.BackgroundImageWorker;

@SuppressWarnings("serial")
public final class CurseForgeProjectCard extends JPanel {
    public CurseForgeProjectCard(final CurseForgeProject mod, ActionListener al) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 180));

        JPanel summaryPanel = new JPanel(new BorderLayout());
        JTextArea summary = new JTextArea();
        summary.setText(mod.summary);
        summary.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        summary.setEditable(false);
        summary.setHighlighter(null);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setEditable(false);

        JLabel icon = new JLabel(Utils.getIconImage("/assets/image/no-icon.png"));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        icon.setVisible(false);

        summaryPanel.add(icon, BorderLayout.WEST);
        summaryPanel.add(summary, BorderLayout.CENTER);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton(GetText.tr("Add"));
        JButton viewButton = new JButton(GetText.tr("View"));
        buttonsPanel.add(addButton);
        buttonsPanel.add(viewButton);

        addButton.addActionListener(al);

        viewButton.addActionListener(e -> OS.openWebBrowser(mod.getWebsiteUrl()));

        add(summaryPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        TitledBorder border = new TitledBorder(null, mod.name, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, App.THEME.getBoldFont().deriveFont(12f));
        setBorder(border);

        Optional<CurseForgeAttachment> attachment = mod.getLogo();
        if (attachment.isPresent()) {
            new BackgroundImageWorker(icon, attachment.get().thumbnailUrl, 60, 60).execute();
        }
    }
}
