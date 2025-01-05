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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public class ModrinthExportOverridesDialog extends JDialog {
    public ModrinthExportOverridesDialog(Dialog parent, String text) {
        super(parent, GetText.tr("Overrides Included"), true);

        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(500, 400));
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                close();
            }
        });

        // Create the text area
        JLabel infoLabel = new JLabel(new HTMLBuilder().text(GetText.tr(
                "Your exported instance contains mods not on Modrinth and were included in the overrides folder. If you're uploading this to Modrinth, you will need to make sure you have permissions to distribute the below mods, else your modpack will get denied on Modrinth."))
                .center().build());
        add(infoLabel, BorderLayout.NORTH);

        // Create the text area
        JTextArea textArea = new JTextArea();
        textArea.setText(text);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Create the button panel
        JPanel buttonPanel = new JPanel();

        // Copy button
        JButton copyButton = new JButton(GetText.tr("Copy"));
        copyButton.addActionListener(e -> {
            OS.copyToClipboard(text);
        });
        buttonPanel.add(copyButton);

        // Close button
        JButton closeButton = new JButton(GetText.tr("Close"));
        closeButton.addActionListener(e -> {
            close();
        });
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}
