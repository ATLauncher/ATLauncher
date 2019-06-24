/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.utils.CurseApi;
import com.atlauncher.utils.Utils;

public class CurseModFileSelectorDialog extends JDialog {
    private static final long serialVersionUID = -6984886874482721558L;
    private int filesLength = 0;
    private CurseMod mod;
    private Instance instance;

    private JPanel middle;
    private JButton addButton;
    private JLabel versionsLabel;
    private JComboBox<CurseFile> filesDropdown;
    private List<CurseFile> files = new ArrayList<>();

    public CurseModFileSelectorDialog(CurseMod mod, Instance instance) {
        super(App.settings.getParent(), ModalityType.APPLICATION_MODAL);

        this.mod = mod;
        this.instance = instance;

        setTitle(mod.name);

        setSize(500, 225);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addButton = new JButton(Language.INSTANCE.localize("common.add"));
        addButton.setEnabled(false);

        // Top Panel Stuff
        JPanel top = new JPanel();
        top.add(new JLabel(Language.INSTANCE.localize("common.installing") + " " + mod.name));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc = this.setupFilesDropdown(gbc);

        this.getFiles();

        // Bottom Panel Stuff
        JPanel bottom = new JPanel();
        bottom.setLayout(new FlowLayout());

        addButton.addActionListener(e -> {
            CurseFile file = (CurseFile) filesDropdown.getSelectedItem();

            final JDialog dialog = new JDialog(this,
                    Language.INSTANCE.localize("common.installing") + " " + file.displayName,
                    ModalityType.DOCUMENT_MODAL);

            dialog.setLocationRelativeTo(this);
            dialog.setSize(300, 100);
            dialog.setResizable(false);

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            final JLabel doing = new JLabel(Language.INSTANCE.localize("common.installing") + " " + file.displayName);
            doing.setHorizontalAlignment(JLabel.CENTER);
            doing.setVerticalAlignment(JLabel.TOP);
            topPanel.add(doing);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setLayout(new BorderLayout());

            JProgressBar progressBar = new JProgressBar(0, 100);
            bottomPanel.add(progressBar, BorderLayout.NORTH);
            progressBar.setIndeterminate(true);

            dialog.add(topPanel, BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            Runnable r = new Runnable() {
                public void run() {
                    instance.addModFromCurse(mod, file);
                    dialog.dispose();
                    dispose();
                }
            };

            new Thread(r).start();

            dialog.setVisible(true);
        });

        JButton cancel = new JButton(Language.INSTANCE.localize("common.cancel"));
        cancel.addActionListener(e -> dispose());
        bottom.add(addButton);
        bottom.add(cancel);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    protected void getFiles() {
        versionsLabel.setVisible(true);
        filesDropdown.setVisible(true);

        Runnable r = new Runnable() {
            public void run() {
                files.addAll(CurseApi.getFilesForMod(mod.id).stream()
                        .sorted(Comparator.comparingInt((CurseFile file) -> file.id).reversed())
                        .filter(file -> file.gameVersion.contains(instance.getMinecraftVersion()))
                        .collect(Collectors.toList()));

                // ensures that font width is taken into account
                for (CurseFile file : files) {
                    filesLength = Math.max(filesLength,
                            getFontMetrics(Utils.getFont()).stringWidth(file.displayName) + 100);
                }

                files.stream().forEach(version -> {
                    filesDropdown.addItem(version);
                });

                // ensures that the dropdown is at least 200 px wide
                filesLength = Math.max(200, filesLength);

                // ensures that there is a maximum width of 350 px to prevent overflow
                filesLength = Math.min(350, filesLength);

                filesDropdown.setPreferredSize(new Dimension(filesLength, 25));

                filesDropdown.setEnabled(true);
                versionsLabel.setVisible(true);
                filesDropdown.setVisible(true);
                addButton.setEnabled(true);
                filesDropdown.setEnabled(true);
            }
        };

        new Thread(r).start();
    }

    private GridBagConstraints setupFilesDropdown(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        versionsLabel = new JLabel(Language.INSTANCE.localize("instance.versiontoinstall") + ": ");
        middle.add(versionsLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        filesDropdown = new JComboBox<>();
        filesDropdown.setEnabled(false);
        middle.add(filesDropdown, gbc);

        return gbc;
    }
}
