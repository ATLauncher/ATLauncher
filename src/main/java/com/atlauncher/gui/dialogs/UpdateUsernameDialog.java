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
package com.atlauncher.gui.dialogs;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;

/**
 * @since 2024 / 05 / 23
 */
public class UpdateUsernameDialog {
    public static synchronized void show(AbstractAccount account) {
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(
            GetText.tr("Checking For Username Change"),
            0,
            GetText.tr("Checking Username Change For {0}", account.minecraftUsername),
            "Aborting checking for username change for " + account.minecraftUsername
        );

        dialog.addThread(new Thread(() -> {
            String currentUsername = account.getCurrentUsername();

            if (currentUsername == null) {
                dialog.setReturnValue(false);
                dialog.close();
                return;
            }

            if (!currentUsername.equals(account.minecraftUsername)) {
                LogManager.info("The username for account with UUID of " + account.getUUIDNoDashes() + " changed from "
                    + account.minecraftUsername + " to " + currentUsername);
                account.minecraftUsername = currentUsername;
                dialog.setReturnValue(true);
            }

            dialog.close();
        }));

        dialog.start();

        if (dialog.getReturnValue() == null) {
            DialogManager.okDialog()
                .setTitle(GetText.tr("No Changes"))
                .setContent(GetText.tr("Your username hasn't changed."))
                .setType(DialogManager.INFO)
                .show();
        } else if (dialog.getReturnValue()) {
            AccountManager.saveAccounts();
            DialogManager.okDialog()
                .setTitle(GetText.tr("Username Updated"))
                .setContent(GetText.tr("Your username has been updated."))
                .setType(DialogManager.INFO)
                .show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                .setContent(GetText.tr("Error checking for username change. Check the error logs and try again later."))
                .setType(DialogManager.ERROR)
                .show();
        }
    }
}
