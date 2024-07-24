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
package com.atlauncher.gui.panels;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.json.Mod;
import com.atlauncher.utils.OS;

public class BrowserDownloadMod extends JPanel {
    private JButton openButton = new JButton(GetText.tr("Open"));
    private JButton copyLinkButton = new JButton(GetText.tr("Copy Link"));

    public BrowserDownloadMod(Mod mod) {
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(new GridBagLayout());

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        add(new JLabel(mod.name), gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        add(new JLabel(mod.file), gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        add(openButton, gbc);
        openButton.addActionListener(l -> OS.openWebBrowser(mod.url));

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        add(copyLinkButton, gbc);
        copyLinkButton.addActionListener(l -> OS.copyToClipboard(mod.url));
    }
}
