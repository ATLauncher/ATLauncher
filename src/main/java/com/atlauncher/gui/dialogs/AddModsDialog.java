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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.gui.card.CurseModCard;
import com.atlauncher.utils.CurseApi;

@SuppressWarnings("serial")
public final class AddModsDialog extends JDialog {
    private final Instance instance;
    private final JPanel contentPanel = new JPanel(new GridBagLayout());
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JTextField searchField = new JTextField(16);
    private final JButton searchButton = new JButton(Language.INSTANCE.localize("common.search"));
    private final JScrollPane jscrollPane;

    public AddModsDialog(Instance instance) {
        super(App.settings.getParent(),
                Language.INSTANCE.localizeWithReplace("instance.addingmods", instance.getName()),
                ModalityType.APPLICATION_MODAL);
        this.instance = instance;

        this.setPreferredSize(new Dimension(550, 450));
        this.setResizable(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.topPanel.add(new JLabel(Language.INSTANCE.localize("common.search") + ": "));
        this.topPanel.add(this.searchField);
        this.topPanel.add(this.searchButton);

        this.jscrollPane = new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        };

        this.add(this.topPanel, BorderLayout.NORTH);
        this.add(this.jscrollPane, BorderLayout.CENTER);

        this.searchField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchForMods();
            }
        });

        this.searchButton.addActionListener(e -> searchForMods());

        this.loadDefaultMods();

        this.pack();
        this.setLocationRelativeTo(App.settings.getParent());
        this.setVisible(true);
    }

    private void loadDefaultMods() {
        Runnable r = new Runnable() {
            public void run() {
                setMods(CurseApi.searchMods(instance.getMinecraftVersion(), ""));
            }
        };

        new Thread(r).start();
    }

    private void searchForMods() {
        Runnable r = new Runnable() {
            public void run() {
                String query = searchField.getText();

                setMods(CurseApi.searchMods(instance.getMinecraftVersion(), query));
            }
        };

        new Thread(r).start();
    }

    private void setMods(List<CurseMod> mods) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        contentPanel.removeAll();

        mods.stream().forEach(curseMod -> {
            contentPanel.add(new CurseModCard(curseMod, this.instance), gbc);
            gbc.gridy++;
        });

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jscrollPane.getVerticalScrollBar().setValue(0);
            }
        });

        revalidate();
        repaint();
    }
}
