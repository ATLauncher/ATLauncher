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
package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.PackImagePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ViewModsDialog;
import com.atlauncher.utils.Utils;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class for displaying packs in the Pack Tab
 *
 * @author Ryan
 */
public class PackCard extends CollapsiblePanel implements RelocalizationListener {
    private static final long serialVersionUID = -2617283435728223314L;
    private final JTextArea descArea = new JTextArea();
    private final JButton newInstanceButton = new JButton(Language.INSTANCE.localize("common.newinstance"));
    private final JButton createServerButton = new JButton(Language.INSTANCE.localize("common.createserver"));
    private final JButton supportButton = new JButton(Language.INSTANCE.localize("common.support"));
    private final JButton websiteButton = new JButton(Language.INSTANCE.localize("common.website"));
    private final JButton modsButton = new JButton(Language.INSTANCE.localize("pack.viewmods"));
    private final JPanel actionsPanel = new JPanel(new BorderLayout());
    private final JSplitPane splitter = new JSplitPane();
    private final Pack pack;

    public PackCard(final Pack pack) {
        super(pack);
        RelocalizationManager.addListener(this);
        this.pack = pack;

        this.splitter.setLeftComponent(new PackImagePanel(pack));
        this.splitter.setRightComponent(this.actionsPanel);
        this.splitter.setEnabled(false);

        JPanel abPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        abPanel.add(this.newInstanceButton);
        abPanel.add(this.createServerButton);
        abPanel.add(this.supportButton);
        abPanel.add(this.websiteButton);
        abPanel.add(this.modsButton);

        this.descArea.setText(pack.getDescription());
        this.descArea.setLineWrap(true);
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setWrapStyleWord(true);

        this.actionsPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.actionsPanel.add(abPanel, BorderLayout.SOUTH);
        this.actionsPanel.setPreferredSize(new Dimension(this.actionsPanel.getPreferredSize().width, 180));

        this.getContentPane().add(this.splitter);

        this.addActionListeners();

        if (this.pack.getVersionCount() == 0) {
            this.modsButton.setVisible(false);
        }

        if (!this.pack.canCreateServer()) {
            this.createServerButton.setVisible(false);
        }
    }

    public Pack getPack() {
        return this.pack;
    }

    private void addActionListeners() {
        this.newInstanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("pack" + "" +
                                    ".offlinenewinstance"), Language.INSTANCE.localize("common.offline"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance"
                                + ".cannotcreate"), Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack);
                    }
                }
            }
        });
        this.createServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("pack" + "" +
                                    ".offlinecreateserver"), Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance"
                                + ".cannotcreate"), Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack, true);
                    }
                }
            }
        });
        this.supportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getSupportURL());
            }
        });
        this.websiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getWebsiteURL());
            }
        });

        this.modsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewModsDialog(pack).setVisible(true);
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.newInstanceButton.setText(Language.INSTANCE.localize("common.newinstance"));
        this.createServerButton.setText(Language.INSTANCE.localize("common.createserver"));
        this.supportButton.setText(Language.INSTANCE.localize("common.support"));
        this.websiteButton.setText(Language.INSTANCE.localize("common.website"));
        this.modsButton.setText(Language.INSTANCE.localize("pack.viewmods"));
    }
}