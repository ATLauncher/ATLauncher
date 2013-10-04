/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.atlauncher.App;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PrivatePack;

public class PacksPanel extends JPanel {

    private JPanel topPanel;
    private JButton addPackButton;
    private JButton clearButton;
    private JTextField searchBox;
    private JButton searchButton;
    private JCheckBox privatePacks;
    private JLabel privatePacksLabel;
    private JCheckBox servers;
    private JLabel serversLabel;

    private String searchText = null;
    private boolean isServers = false;
    private boolean isPrivatePacks = false;

    private JPanel panel;
    private JScrollPane scrollPane;
    private int currentPosition = 0;

    public PacksPanel() {
        setLayout(new BorderLayout());
        loadContent(false);
    }

    public void loadContent(boolean keepFilters) {
        topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        addPackButton = new JButton(App.settings.getLocalizedString("pack.addpack"));
        addPackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AddPackDialog();
                reload();
            }
        });
        topPanel.add(addPackButton);

        clearButton = new JButton(App.settings.getLocalizedString("common.clear"));
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                searchBox.setText("");
                servers.setSelected(false);
                privatePacks.setSelected(false);
                reload();
            }
        });
        topPanel.add(clearButton);

        searchBox = new JTextField(16);
        if (keepFilters) {
            searchBox.setText(this.searchText);
        }
        searchBox.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    reload();
                }
            }
        });
        topPanel.add(searchBox);

        searchButton = new JButton(App.settings.getLocalizedString("common.search"));
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        topPanel.add(searchButton);

        servers = new JCheckBox();
        servers.setSelected(isServers);
        servers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        topPanel.add(servers);

        serversLabel = new JLabel(App.settings.getLocalizedString("pack.cancreateserver"));
        topPanel.add(serversLabel);

        privatePacks = new JCheckBox();
        privatePacks.setSelected(isPrivatePacks);
        privatePacks.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        });
        topPanel.add(privatePacks);

        privatePacksLabel = new JLabel(App.settings.getLocalizedString("pack.privatepacksonly"));
        topPanel.add(privatePacksLabel);

        add(topPanel, BorderLayout.NORTH);

        panel = new JPanel();
        scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setValue(currentPosition);
        add(scrollPane, BorderLayout.CENTER);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        int count = 0;
        for (Pack pack : (App.settings.sortPacksAlphabetically() ? App.settings.getPacksSorted()
                : App.settings.getPacks())) {
            if (pack.canInstall()) {
                if (keepFilters) {
                    boolean showPack = true;

                    if (searchText != null) {
                        if (!Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE)
                                .matcher(pack.getName()).find()) {
                            showPack = false;
                        }
                    }

                    if (isServers) {
                        if (!pack.canCreateServer()) {
                            showPack = false;
                        }
                    }

                    if (isPrivatePacks) {
                        if (!(pack instanceof PrivatePack)) {
                            showPack = false;
                        }
                    }

                    if (showPack) {
                        panel.add(new PackDisplay(pack), gbc);
                        gbc.gridy++;
                        count++;
                    }
                } else {
                    panel.add(new PackDisplay(pack), gbc);
                    gbc.gridy++;
                    count++;
                }
            }
        }
        if (count == 0) {
            panel.add(
                    new NothingToDisplay(App.settings.getLocalizedString("pack.nodisplay", "\n\n")),
                    gbc);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getVerticalScrollBar().setValue(currentPosition);
            }
        });
    }

    public void reload() {
        this.currentPosition = scrollPane.getVerticalScrollBar().getValue();
        this.searchText = searchBox.getText();
        this.isServers = servers.isSelected();
        this.isPrivatePacks = privatePacks.isSelected();
        if (this.searchText.isEmpty()) {
            this.searchText = null;
        }
        removeAll();
        loadContent(true);
        validate();
        repaint();
        searchBox.requestFocus();
    }

}