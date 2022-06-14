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
package com.atlauncher.gui.tabs.tools;

import com.atlauncher.AppEventBus;
import com.atlauncher.Data;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.events.ToolRunEvent;
import com.atlauncher.events.account.AccountChangedEvent;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.google.common.eventbus.Subscribe;
import org.mini2Dx.gettext.GetText;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class SkinUpdaterToolPanel extends AbstractToolPanel implements ActionListener {

    public SkinUpdaterToolPanel() {
        super(GetText.tr("Skin Updater"));

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70)
            .text(GetText.tr("This tool will update all your accounts skins on the launcher.")).build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        AppEventBus.register(this);
        this.checkLaunchButtonEnabled();
    }

    private void checkLaunchButtonEnabled() {
        LAUNCH_BUTTON.setEnabled(Data.ACCOUNTS.size() != 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AppEventBus.postToDefault(ToolRunEvent.skinUpdater());
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Skin Updater"), Data.ACCOUNTS.size(),
            GetText.tr("Updating Skins. Please Wait!"), "Skin Updater Tool Cancelled!");
        dialog.addThread(new Thread(() -> {
            Data.ACCOUNTS.forEach(account -> {
                account.updateSkin();
                dialog.doneTask();
            });

            dialog.setReturnValue(true);
            dialog.close();
        }));

        dialog.start();

        DialogManager.okDialog().setType(DialogManager.INFO).setTitle(GetText.tr("Success"))
            .setContent(GetText.tr("Successfully updated skins.")).show();
    }

    @Subscribe
    public void onAccountChanged(final AccountChangedEvent e) {
        this.checkLaunchButtonEnabled();
    }
}
