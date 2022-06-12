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

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.modrinth.ModrinthSearchHit;
import com.atlauncher.events.LocalizationEvent;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.utils.OS;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class ModrinthPackCard extends JPanel{
    private final JButton newInstanceButton = new JButton(GetText.tr("New Instance"));
    private final JButton createServerButton = new JButton(GetText.tr("Create Server"));
    private final JButton websiteButton = new JButton(GetText.tr("Website"));

    public ModrinthPackCard(final ModrinthSearchHit searchHit) {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(null, searchHit.title, TitledBorder.LEADING,
                TitledBorder.DEFAULT_POSITION, App.THEME.getBoldFont().deriveFont(15f)));

        String imageUrl = searchHit.iconUrl;

        JSplitPane splitter = new JSplitPane();

        BackgroundImageLabel imageLabel = new BackgroundImageLabel(imageUrl, 150, 150);
        imageLabel.setPreferredSize(new Dimension(300, 150));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        splitter.setLeftComponent(imageLabel);

        JPanel actionsPanel = new JPanel(new BorderLayout());
        splitter.setRightComponent(actionsPanel);
        splitter.setEnabled(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        newInstanceButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot create instance as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(searchHit.title, "Install", "ModrinthPack");
                new InstanceInstallerDialog(searchHit, false);
            }
        });
        buttonsPanel.add(newInstanceButton);

        createServerButton.addActionListener(e -> {
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
                Analytics.sendEvent(searchHit.title, "ServerInstall", "ModrinthPack");
                new InstanceInstallerDialog(searchHit, true);
            }
        });
        buttonsPanel.add(createServerButton);

        websiteButton.addActionListener(
                e -> OS.openWebBrowser(String.format("https://modrinth.com/modpack/%s", searchHit.slug)));
        buttonsPanel.add(websiteButton);

        JTextArea descArea = new JTextArea();
        descArea.setText(searchHit.description);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setWrapStyleWord(true);
        descArea.setCaretPosition(0);

        actionsPanel.add(descArea, BorderLayout.CENTER);
        actionsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        actionsPanel.setPreferredSize(new Dimension(actionsPanel.getPreferredSize().width, 155));

        add(splitter, BorderLayout.CENTER);

        AppEventBus.register(this);
    }

    @Subscribe
    public final void onLocalizationChanged(final LocalizationEvent.LocalizationChangedEvent event){
        newInstanceButton.setText(GetText.tr("New Instance"));
        websiteButton.setText(GetText.tr("Website"));
    }
}
