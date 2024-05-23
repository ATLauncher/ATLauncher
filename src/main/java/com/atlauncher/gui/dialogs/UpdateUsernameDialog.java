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
