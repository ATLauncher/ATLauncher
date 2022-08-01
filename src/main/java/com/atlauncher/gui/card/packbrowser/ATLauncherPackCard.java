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
package com.atlauncher.gui.card.packbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.PackImagePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ViewModsDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;

@SuppressWarnings("serial")
public class ATLauncherPackCard extends JPanel implements RelocalizationListener {
    private final JButton newInstanceButton = new JButton(GetText.tr("New Instance"));
    private final JButton createServerButton = new JButton(GetText.tr("Create Server"));
    private final JButton discordInviteButton = new JButton("Discord");
    private final JButton supportButton = new JButton(GetText.tr("Support"));
    private final JButton websiteButton = new JButton(GetText.tr("Website"));
    private final JButton serversButton = new JButton(GetText.tr("Servers"));
    private final JButton modsButton = new JButton(GetText.tr("View Mods"));
    private final Pack pack;
    private final boolean featured;

    public ATLauncherPackCard(final Pack pack, final boolean featured) {
        super();
        this.pack = pack;
        this.featured = featured;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(null, pack.name, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                App.THEME.getBoldFont().deriveFont(15f)));

        RelocalizationManager.addListener(this);

        JSplitPane splitter = new JSplitPane();
        splitter.setLeftComponent(new PackImagePanel(pack));
        JPanel actionsPanel = new JPanel(new BorderLayout());
        splitter.setRightComponent(actionsPanel);
        splitter.setEnabled(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        as.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        top.add(this.newInstanceButton);
        top.add(this.createServerButton);

        if (pack.getDiscordInviteURL() != null) {
            bottom.add(this.discordInviteButton);
        }

        if (pack.getSupportURL() != null) {
            bottom.add(this.supportButton);
        }

        if (pack.getWebsiteURL() != null) {
            bottom.add(this.websiteButton);
        }

        if (!this.pack.isSystem()) {
            bottom.add(this.serversButton);

            bottom.add(this.modsButton);
        }

        JTextArea descArea = new JTextArea();
        descArea.setText(pack.getDescription());
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setWrapStyleWord(true);
        descArea.setCaretPosition(0);

        actionsPanel.add(new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        actionsPanel.add(as, BorderLayout.SOUTH);
        actionsPanel.setPreferredSize(new Dimension(actionsPanel.getPreferredSize().width, 155));

        add(splitter, BorderLayout.CENTER);

        this.addActionListeners();

        if (this.pack.getVersionCount() == 0) {
            this.modsButton.setVisible(false);
        }

        if (!this.pack.canCreateServer()) {
            this.createServerButton.setVisible(false);
        }

        if (this.pack.system) {
            actionsPanel.add(top, BorderLayout.SOUTH);
            actionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        }
    }

    public Pack getPack() {
        return this.pack;
    }

    private void addActionListeners() {
        this.newInstanceButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot create instance as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(pack.getName(), "Install", featured ? "ATLauncherFeaturedPack" : "ATLauncherPack");
                new InstanceInstallerDialog(pack);
            }
        });

        this.createServerButton.addActionListener(e -> {
            // user has no instances, they may not be aware this is not how to play
            if (InstanceManager.getInstances().size() == 0) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Are you sure you want to create a server?"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Creating a server won't allow you play Minecraft, it's for letting others play together.<br/><br/>If you just want to play Minecraft, you don't want to create a server, and instead will want to create an instance.<br/><br/>Are you sure you want to create a server?"))
                                .build())
                        .setType(DialogManager.QUESTION).show();

                if (ret != 0) {
                    return;
                }
            }

            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot create server as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(pack.getName(), "ServerInstall",
                        featured ? "ATLauncherFeaturedPack" : "ATLauncherPack");
                new InstanceInstallerDialog(pack, true);
            }
        });

        this.discordInviteButton.addActionListener(e -> OS.openWebBrowser(pack.getDiscordInviteURL()));

        this.supportButton.addActionListener(e -> OS.openWebBrowser(pack.getSupportURL()));

        this.websiteButton.addActionListener(e -> OS.openWebBrowser(pack.getWebsiteURL()));

        this.serversButton.addActionListener(e -> OS
                .openWebBrowser(String.format("%s/%s?utm_source=launcher&utm_medium=button&utm_campaign=pack_button",
                        Constants.SERVERS_LIST_PACK, pack.getSafeName())));

        this.modsButton.addActionListener(e -> {
            Analytics.sendEvent(pack.getName(), "ViewMods", featured ? "ATLauncherFeaturedPack" : "ATLauncherPack");
            new ViewModsDialog(pack).setVisible(true);
        });
    }

    @Override
    public void onRelocalization() {
        this.newInstanceButton.setText(GetText.tr("New Instance"));
        this.createServerButton.setText(GetText.tr("Create Server"));
        this.supportButton.setText(GetText.tr("Support"));
        this.websiteButton.setText(GetText.tr("Website"));
        this.serversButton.setText(GetText.tr("Servers"));
        this.modsButton.setText(GetText.tr("View Mods"));
    }
}
