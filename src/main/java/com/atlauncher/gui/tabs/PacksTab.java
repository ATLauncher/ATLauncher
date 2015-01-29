/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.TabChangeListener;
import com.atlauncher.evnt.manager.TabChangeManager;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.PackCard;
import com.atlauncher.gui.dialogs.AddPackDialog;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public final class PacksTab extends JPanel implements Tab {
    private final JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel contentPanel = new JPanel(new GridBagLayout());
    private final JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JButton addButton = new JButton(Language.INSTANCE.localize("pack.addpack"));
    private final JButton clearButton = new JButton(Language.INSTANCE.localize("common.clear"));
    private final JButton expandAllButton = new JButton("Expand All");
    private final JButton collapseAllButton = new JButton("Collapse All");
    private final JTextField searchField = new JTextField(16);
    private final JCheckBox serversBox = new JCheckBox(Language.INSTANCE.localize("pack.cancreateserver"));
    private final JCheckBox privateBox = new JCheckBox(Language.INSTANCE.localize("pack.privatepacksonly"));
    private final JCheckBox searchDescBox = new JCheckBox("Search Description");

    private List<PackCard> cards = new LinkedList<PackCard>();

    public PacksTab() {
        super(new BorderLayout());
        this.topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.contentPanel.setLayout(new GridBagLayout());

        final JScrollPane scrollPane = new JScrollPane(this.contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(this.topPanel, BorderLayout.NORTH);
        this.add(this.bottomPanel, BorderLayout.SOUTH);

        this.setupTopPanel();
        this.preload();

        TabChangeManager.addListener(new TabChangeListener() {
            @Override
            public void on() {
                searchField.setText("");
                serversBox.setSelected(false);
                privateBox.setSelected(false);
                searchDescBox.setSelected(false);
            }
        });

        this.collapseAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component comp : contentPanel.getComponents()) {
                    if (comp instanceof PackCard) {
                        ((PackCard) comp).setCollapsed(true);
                    }
                }
            }
        });
        this.expandAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Component comp : contentPanel.getComponents()) {
                    if (comp instanceof PackCard) {
                        ((PackCard) comp).setCollapsed(false);
                    }
                }
            }
        });
        this.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AddPackDialog();
                reload();
            }
        });
        this.clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                searchDescBox.setSelected(false);
                serversBox.setSelected(false);
                privateBox.setSelected(false);
                reload();
            }
        });

        this.searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                reload();
            }
        });
        this.privateBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                reload();
            }
        });
        this.serversBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                reload();
            }
        });
        this.searchDescBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                reload();
            }
        });
    }

    private void setupTopPanel() {
        this.topPanel.add(this.addButton);
        this.topPanel.add(this.clearButton);
        this.topPanel.add(this.searchField);
        this.topPanel.add(this.serversBox);
        this.topPanel.add(this.privateBox);
        this.topPanel.add(this.searchDescBox);

        this.bottomPanel.add(this.expandAllButton);
        this.bottomPanel.add(this.collapseAllButton);
    }

    private void preload() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        List<Pack> packs = App.settings.sortPacksAlphabetically() ? App.settings.getPacksSortedAlphabetically() : App
                .settings.getPacksSortedPositionally();

        int count = 0;
        for (Pack pack : packs) {
            if (pack.canInstall()) {
                PackCard card = new PackCard(pack);
                this.cards.add(card);
                this.contentPanel.add(card, gbc);
                gbc.gridy++;
                count++;
            }
        }

        if (count == 0) {
            this.contentPanel.add(new NilCard(Language.INSTANCE.localizeWithReplace("pack.nodisplay", "\n\n")), gbc);
        }
    }

    private void load(boolean keep) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        Pack pack;
        boolean show;
        int count = 0;
        for (PackCard card : this.cards) {
            show = true;
            pack = card.getPack();
            if (keep) {
                if (!this.searchField.getText().isEmpty()) {
                    if (!Pattern.compile(Pattern.quote(this.searchField.getText()), Pattern.CASE_INSENSITIVE).matcher
                            (pack.getName()).find()) {
                        show = false;
                    }
                }

                if (this.searchDescBox.isSelected()) {
                    if (Pattern.compile(Pattern.quote(this.searchField.getText()), Pattern.CASE_INSENSITIVE).matcher
                            (pack.getDescription()).find()) {
                        show = true;
                    }
                }

                if (this.serversBox.isSelected()) {
                    if (!pack.canCreateServer()) {
                        show = false;
                    }
                }

                if (privateBox.isSelected()) {
                    if (!pack.isPrivate()) {
                        show = false;
                    }
                }

                if (show) {
                    this.contentPanel.add(card, gbc);
                    gbc.gridy++;
                    count++;
                }
            }
        }

        ((LauncherFrame) App.settings.getParent()).updateTitle("Packs - " + count);

        if (count == 0) {
            this.contentPanel.add(new NilCard(Language.INSTANCE.localizeWithReplace("pack.nodisplay", "\n\n")), gbc);
        }
    }

    public void reload() {
        this.contentPanel.removeAll();
        load(true);
        revalidate();
        repaint();
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.packs");
    }
}