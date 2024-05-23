package com.atlauncher.gui.dialogs;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.FileSystem;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.utils.Utils;

/**
 * @since 2024 / 05 / 23
 */
public class UpdateSkinDialog {
    public static synchronized void show(AbstractAccount account) {
        final File file = FileSystem.SKINS.resolve(account.getUUIDNoDashes() + ".png").toFile();
        LogManager.info("Downloading skin for " + account.minecraftUsername);
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(
            GetText.tr("Downloading Skin"),
            0,
            GetText.tr("Downloading Skin For {0}", account.minecraftUsername),
            "Aborting downloading Minecraft skin for " + account.minecraftUsername
        );

        final UUID uid = account.getRealUUID();
        dialog.addThread(new Thread(() -> {
            account.updateSkinPreCheck();

            dialog.setReturnValue(false);
            String skinURL = account.getSkinUrl();
            if (skinURL == null) {
                LogManager.warn("Couldn't download skin because the url found was NULL. Using default skin");
                if (!file.exists()) {
                    String skinFilename = "default.png";

                    // even UUID's use the alex skin
                    if ((uid.hashCode() & 1) != 0) {
                        skinFilename = "default-alex.png";
                    }

                    // Only copy over the default skin if there is no skin for the user
                    try {
                        java.nio.file.Files.copy(
                            Utils.getResourceInputStream("/assets/image/skins/" + skinFilename), file.toPath());
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }

                    dialog.setReturnValue(true);
                }
            } else {
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(skinURL).openConnection();
                    if (conn.getResponseCode() == 200) {
                        if (file.exists()) {
                            Utils.delete(file);
                        }
                        Download.build().setUrl(skinURL).downloadTo(file.toPath()).downloadFile();
                        dialog.setReturnValue(true);
                    } else {
                        if (!file.exists()) {
                            String skinFilename = "default.png";

                            // even UUID's use the alex skin
                            if ((uid.hashCode() & 1) != 0) {
                                skinFilename = "default-alex.png";
                            }

                            // Only copy over the default skin if there is no skin for the user
                            try {
                                java.nio.file.Files.copy(
                                    Utils.getResourceInputStream("/assets/image/skins/" + skinFilename),
                                    file.toPath());
                            } catch (IOException e) {
                                LogManager.logStackTrace(e);
                            }

                            dialog.setReturnValue(true);
                        }
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
            dialog.close();
        }));
        dialog.start();
        if (!dialog.getReturnValue()) {
            DialogManager.okDialog()
                .setTitle(GetText.tr("Error"))
                .setContent(GetText.tr("Error downloading skin. Please try again later!"))
                .setType(DialogManager.ERROR)
                .show();
        }
    }
}
