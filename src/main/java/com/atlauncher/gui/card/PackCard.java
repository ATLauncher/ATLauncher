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

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class PackCard extends CollapsiblePanel implements RelocalizationListener {
    private final JTextArea descArea = new JTextArea();
    private final JButton newInstanceButton = new JButton(GetText.tr("New Instance"));
    private final JButton createServerButton = new JButton(GetText.tr("Create Server"));
    private final JButton discordInviteButton = new JButton("Discord");
    private final JButton supportButton = new JButton(GetText.tr("Support"));
    private final JButton websiteButton = new JButton(GetText.tr("Website"));
    private final JButton modsButton = new JButton(GetText.tr("View Mods"));
    private final JButton removePackButton = new JButton(GetText.tr("Remove"));
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
                DialogManager.okDialog().setTitle(GetText.tr("You're In Offline Mode")).setContent(GetText.tr(
                        "Cannot create new instance as you're in offline mode. Please connect to the internet and try again."))
                        .setType(DialogManager.ERROR).show();
            } else {
                if (App.settings.getAccount() == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                            .setContent(GetText.tr("Cannot create instance as you have no account selected."))
                            .setType(DialogManager.ERROR).show();
                } else {
                    Analytics.sendEvent(pack.getName(), "Install", "Pack");
                    new InstanceInstallerDialog(pack);
                }
            }
        });

        this.createServerButton.addActionListener(e -> {
            if (App.settings.isInOfflineMode()) {
                DialogManager.okDialog().setTitle(GetText.tr("You're In Offline Mode")).setContent(GetText.tr(
                        "Cannot create server as you're in offline mode. Please connect to the internet and try again."))
                        .setType(DialogManager.ERROR).show();
            } else {
                if (App.settings.getAccount() == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                            .setContent(GetText.tr("Cannot create instance as you have no account selected."))
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
        this.newInstanceButton.setText(GetText.tr("New Instance"));
        this.createServerButton.setText(GetText.tr("Create Server"));
        this.supportButton.setText(GetText.tr("Support"));
        this.websiteButton.setText(GetText.tr("Website"));
        this.modsButton.setText(GetText.tr("View Mods"));
        this.removePackButton.setText(GetText.tr("Remove"));
    }
}
