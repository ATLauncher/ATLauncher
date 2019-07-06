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
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.PackImagePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ViewModsDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;

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
    private final JButton discordInviteButton = new JButton("Discord");
    private final JButton supportButton = new JButton(Language.INSTANCE.localize("common.support"));
    private final JButton websiteButton = new JButton(Language.INSTANCE.localize("common.website"));
    private final JButton modsButton = new JButton(Language.INSTANCE.localize("pack.viewmods"));
    private final JButton removePackButton = new JButton(Language.INSTANCE.localize("pack.removepack"));
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

        JPanel top = new JPanel(new FlowLayout());
        JPanel bottom = new JPanel(new FlowLayout());
        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        top.add(this.newInstanceButton);
        top.add(this.createServerButton);

        if (pack.getDiscordInviteURL() != null) {
            bottom.add(this.discordInviteButton);
        }

        bottom.add(this.supportButton);
        bottom.add(this.websiteButton);

        if (!this.pack.getName().startsWith("Vanilla Minecraft")) {
            bottom.add(this.modsButton);
        }

        bottom.add(this.removePackButton);

        this.descArea.setText(pack.getDescription());
        this.descArea.setLineWrap(true);
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setWrapStyleWord(true);

        this.actionsPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.actionsPanel.add(as, BorderLayout.SOUTH);
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
        this.newInstanceButton.addActionListener(e -> {
            if (App.settings.isInOfflineMode()) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.offline"))
                        .setContent(Language.INSTANCE.localize("pack.offlinenewinstance")).setType(DialogManager.ERROR)
                        .show();
            } else {
                if (App.settings.getAccount() == null) {
                    DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                            .setContent(Language.INSTANCE.localize("instance.cannotcreate"))
                            .setType(DialogManager.ERROR).show();
                } else {
                    Analytics.sendEvent(pack.getName(), "Install", "Pack");
                    new InstanceInstallerDialog(pack);
                }
            }
        });

        this.createServerButton.addActionListener(e -> {
            if (App.settings.isInOfflineMode()) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.offline"))
                        .setContent(Language.INSTANCE.localize("pack.offlinecreateserver")).setType(DialogManager.ERROR)
                        .show();
            } else {
                if (App.settings.getAccount() == null) {
                    DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                            .setContent(Language.INSTANCE.localize("instance.cannotcreate"))
                            .setType(DialogManager.ERROR).show();
                } else {
                    Analytics.sendEvent(pack.getName(), "ServerInstall", "Pack");
                    new InstanceInstallerDialog(pack, true);
                }
            }
        });

        this.discordInviteButton.addActionListener(e -> OS.openWebBrowser(pack.getDiscordInviteURL()));

        this.supportButton.addActionListener(e -> OS.openWebBrowser(pack.getSupportURL()));

        this.websiteButton.addActionListener(e -> OS.openWebBrowser(pack.getWebsiteURL()));

        this.modsButton.addActionListener(e -> {
            Analytics.sendEvent(pack.getName(), "ViewMods", "Pack");
            new ViewModsDialog(pack).setVisible(true);
        });

        this.removePackButton.addActionListener(e -> {
            Analytics.sendEvent(pack.getName(), "Remove", "Pack");
            App.settings.removePack(pack.getCode());
        });
    }

    @Override
    public void onRelocalization() {
        this.newInstanceButton.setText(Language.INSTANCE.localize("common.newinstance"));
        this.createServerButton.setText(Language.INSTANCE.localize("common.createserver"));
        this.supportButton.setText(Language.INSTANCE.localize("common.support"));
        this.websiteButton.setText(Language.INSTANCE.localize("common.website"));
        this.modsButton.setText(Language.INSTANCE.localize("pack.viewmods"));
        this.removePackButton.setText(Language.INSTANCE.localize("pack.removepack"));
    }
}
