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
package com.atlauncher.gui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.ThemeListener;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;

public abstract class BottomBar extends JPanel implements ThemeListener {
    private static final long serialVersionUID = -7488195680365431776L;

    public final JButton nodeCraftIcon = new SMButton("/assets/image/social/nodecraft.png",
            "Nodecraft - Setup a Minecraft server with an ATLauncher modpack in less than 60 seconds");
    public final JButton discordIcon = new SMButton("/assets/image/social/discord.png", "Discord");
    public final JButton facebookIcon = new SMButton("/assets/image/social/facebook.png", "Facebook");
    public final JButton githubIcon = new SMButton("/assets/image/social/github.png", "GitHub");
    public final JButton twitterIcon = new SMButton("/assets/image/social/twitter.png", "Twitter");
    public final JButton redditIcon = new SMButton("/assets/image/social/reddit.png", "Reddit");

    protected final JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 8));

    public BottomBar() {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("BottomBar.dividerColor")));
        this.setPreferredSize(new Dimension(0, 50));

        this.add(this.rightSide, BorderLayout.EAST);
        this.setupSocialButtonListeners();
        this.rightSide.add(this.nodeCraftIcon);
        this.rightSide.add(this.discordIcon);
        this.rightSide.add(this.facebookIcon);
        this.rightSide.add(this.githubIcon);
        this.rightSide.add(this.redditIcon);
        this.rightSide.add(this.twitterIcon);

        ThemeManager.addListener(this);
    }

    private void setupSocialButtonListeners() {
        nodeCraftIcon.addActionListener(e -> {
            LogManager.info("Opening Up Nodecraft");
            OS.openWebBrowser("https://atl.pw/nodecraft-from-launcher");
        });
        discordIcon.addActionListener(e -> {
            if (App.launcher.lastInstanceCrashTime != null && App.launcher.lastInstanceCrash != null
                    && new Date().getTime() - App.launcher.lastInstanceCrashTime.getTime() < 300000
                    && App.launcher.lastInstanceCrash.getDiscordInviteUrl() != null) {
                int ret = DialogManager.yesNoDialog(false).setTitle(GetText.tr("Visit Modpack Discord?"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Would you like to open the Discord server for the last instance that crashed?"))
                                .build())
                        .setType(DialogManager.QUESTION).show();

                if (ret == DialogManager.YES_OPTION) {
                    Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_crashed_discord_button",
                            App.launcher.lastInstanceCrash));
                    LogManager.info("Opening Up Discord for Modpack");
                    OS.openWebBrowser(App.launcher.lastInstanceCrash.getDiscordInviteUrl());
                    return;
                }
            }

            LogManager.info("Opening Up ATLauncher Discord");
            OS.openWebBrowser("https://atl.pw/discord");
        });
        facebookIcon.addActionListener(e -> {
            LogManager.info("Opening Up ATLauncher Facebook Page");
            OS.openWebBrowser("https://atl.pw/facebook");
        });
        githubIcon.addActionListener(e -> {
            LogManager.info("Opening Up ATLauncher GitHub Page");
            OS.openWebBrowser("https://atl.pw/github-launcher-3");
        });
        redditIcon.addActionListener(e -> {
            LogManager.info("Opening Up ATLauncher Reddit Page");
            OS.openWebBrowser("https://atl.pw/reddit");
        });
        twitterIcon.addActionListener(e -> {
            LogManager.info("Opening Up ATLauncher Twitter Page");
            OS.openWebBrowser("https://atl.pw/twitter");
        });
    }

    @Override
    public void onThemeChange() {
        this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("BottomBar.dividerColor")));
    }
}
