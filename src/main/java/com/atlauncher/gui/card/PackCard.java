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
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.OS;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.PackImagePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ViewModsDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.PackManager;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class for displaying packs in the Pack Tab
 */
public class PackCard extends CollapsiblePanel {
    private static final long serialVersionUID = -2617283435728223314L;
    private final JTextArea descArea = new JTextArea();
    private final JButton newInstanceButton = new JButton(LanguageManager.localize("common.newinstance"));
    private final JButton createServerButton = new JButton(LanguageManager.localize("common.createserver"));
    private final JButton supportButton = new JButton(LanguageManager.localize("common.support"));
    private final JButton websiteButton = new JButton(LanguageManager.localize("common.website"));
    private final JButton modsButton = new JButton(LanguageManager.localize("pack.viewmods"));
    private final JButton removePackButton = new JButton(LanguageManager.localize("pack.removepack"));
    private final JPanel actionsPanel = new JPanel(new BorderLayout());
    private final JSplitPane splitter = new JSplitPane();
    private final Pack pack;

    public PackCard(final Pack pack) {
        super(pack);
        EventHandler.EVENT_BUS.subscribe(this);
        this.pack = pack;

        this.splitter.setLeftComponent(new PackImagePanel(pack));
        this.splitter.setRightComponent(this.actionsPanel);
        this.splitter.setEnabled(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        top.add(this.newInstanceButton);
        top.add(this.createServerButton);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottom.add(this.supportButton);
        bottom.add(this.websiteButton);
        bottom.add(this.modsButton);
        bottom.add(this.removePackButton);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setEnabled(false);
        splitPane.setTopComponent(top);
        splitPane.setBottomComponent(bottom);
        splitPane.setDividerSize(1);

        this.descArea.setText(pack.getDescription());
        this.descArea.setLineWrap(true);
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setWrapStyleWord(true);

        this.actionsPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.actionsPanel.add(splitPane, BorderLayout.SOUTH);
        this.actionsPanel.setPreferredSize(new Dimension(this.actionsPanel.getPreferredSize().width, 180));

        this.getContentPane().add(this.splitter);

        this.addActionListeners();

        if (this.pack.getVersionCount() == 0) {
            this.modsButton.setVisible(false);
        }

        if (!this.pack.canCreateServer()) {
            this.createServerButton.setVisible(false);
        }

        if (!this.pack.isSemiPublic() || this.pack.isTester()) {
            this.removePackButton.setVisible(false);
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
                    String[] options = {LanguageManager.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("pack" + "" +
                            ".offlinenewinstance"), LanguageManager.localize("common.offline"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (AccountManager.getActiveAccount() == null) {
                        String[] options = {LanguageManager.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("instance" + "" +
                                ".cannotcreate"), LanguageManager.localize("instance.noaccountselected"), JOptionPane
                                .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
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
                    String[] options = {LanguageManager.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("pack" + "" +
                            ".offlinecreateserver"), LanguageManager.localize("common.offline"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (AccountManager.getActiveAccount() == null) {
                        String[] options = {LanguageManager.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("instance" + "" +
                                ".cannotcreate"), LanguageManager.localize("instance.noaccountselected"), JOptionPane
                                .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack, true);
                    }
                }
            }
        });
        this.supportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openWebBrowser(pack.getSupportURL());
            }
        });
        this.websiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OS.openWebBrowser(pack.getWebsiteURL());
            }
        });

        this.modsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewModsDialog(pack).setVisible(true);
            }
        });

        this.removePackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PackManager.removeSemiPublicPack(pack.getCode());
            }
        });
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.newInstanceButton.setText(LanguageManager.localize("common.newinstance"));
        this.createServerButton.setText(LanguageManager.localize("common.createserver"));
        this.supportButton.setText(LanguageManager.localize("common.support"));
        this.websiteButton.setText(LanguageManager.localize("common.website"));
        this.modsButton.setText(LanguageManager.localize("pack.viewmods"));
        this.removePackButton.setText(LanguageManager.localize("pack.removepack"));
    }
}