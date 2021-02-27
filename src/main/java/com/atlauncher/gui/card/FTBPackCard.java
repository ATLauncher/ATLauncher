/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
import java.awt.Window;
import java.util.Comparator;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.atlauncher.App;
import com.atlauncher.data.modpacksch.ModpacksChPackArt;
import com.atlauncher.data.modpacksch.ModpacksChPackArtType;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.BackgroundImageWorker;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public final class FTBPackCard extends JPanel {
    private final Window parent;
    public final ModpacksChPackManifest pack;

    public FTBPackCard(Window parent, final ModpacksChPackManifest pack) {
        this.parent = parent;
        this.pack = pack;

        setupComponents();
    }

    private void setupComponents() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 180));

        JPanel summaryPanel = new JPanel(new BorderLayout());
        JTextArea summary = new JTextArea();
        summary.setText(pack.synopsis);
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

        addButton.addActionListener(e -> {
            Analytics.sendEvent(pack.name, "Add", "FTBPack");

            new InstanceInstallerDialog(parent, pack);
        });

        // The Feed The Beast website only displays modpacks with the 'FTB'
        // tag present, so we should disable the view button for packs without.
        if (!pack.hasTag("FTB")) {
            viewButton.setEnabled(false);
        }

        viewButton.addActionListener(e -> OS.openWebBrowser(this.pack.getWebsiteUrl()));

        add(summaryPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        TitledBorder border = new TitledBorder(null, pack.name, TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION, App.THEME.getBoldFont().deriveFont(12f));
        setBorder(border);

        Optional<ModpacksChPackArt> art = pack.art.stream()
                .filter(a -> a.type == ModpacksChPackArtType.LOGO || a.type == ModpacksChPackArtType.SQUARE)
                .sorted(Comparator.comparingInt((ModpacksChPackArt a) -> a.updated).reversed()).findFirst();
        if (art.isPresent()) {
            new BackgroundImageWorker(icon, art.get().url).execute();
        }
    }
}
