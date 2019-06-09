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

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.card.ModCard;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class ViewModsDialog extends JDialog {
    private final Pack pack;
    private final JPanel contentPanel = new JPanel(new GridBagLayout());
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JTextField searchField = new JTextField(16);
    private final List<ModCard> cards = new LinkedList<ModCard>();

    public ViewModsDialog(Pack pack) {
        super(App.settings.getParent(), Language.INSTANCE.localizeWithReplace("pack.mods", pack.getName()),
                ModalityType.APPLICATION_MODAL);
        this.pack = pack;

        this.setPreferredSize(new Dimension(550, 450));
        this.setResizable(false);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.topPanel.add(new JLabel(Language.INSTANCE.localize("common.search") + ": "));
        this.topPanel.add(this.searchField);

        this.add(this.topPanel, BorderLayout.NORTH);
        this.add(new JScrollPane(this.contentPanel) {
            {
                this.getVerticalScrollBar().setUnitIncrement(16);
            }
        }, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        this.searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                reload();
            }
        });

        List<Mod> mods = this.pack.getJsonVersion(this.pack.getLatestVersion().getVersion()).getMods();
        Collections.sort(mods, new Comparator<Mod>() {
            @Override
            public int compare(Mod o1, Mod o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        for (Mod mod : mods) {
            ModCard card = new ModCard(mod);
            cards.add(card);
            contentPanel.add(card, gbc);
            gbc.gridy++;
        }

        this.pack();
        this.setLocationRelativeTo(App.settings.getParent());
    }

    private void reload() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.set(2, 2, 2, 2);

        this.contentPanel.removeAll();
        for (ModCard card : this.cards) {
            boolean show = true;

            if (!this.searchField.getText().isEmpty()) {
                if (!Pattern.compile(Pattern.quote(this.searchField.getText()), Pattern.CASE_INSENSITIVE)
                        .matcher(card.mod.getName()).find()) {

                    show = false;
                }
            }

            if (show) {
                this.contentPanel.add(card, gbc);
                gbc.gridy++;
            }
        }

        revalidate();
        repaint();
    }
}
