/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;

import com.atlauncher.FileSystem;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.utils.SkinUtils;
import com.atlauncher.utils.Utils;
import com.mojang.util.UUIDTypeAdapter;

import org.mini2Dx.gettext.GetText;

/**
 * This class deals with the Accounts in the launcher.
 */
public abstract class AbstractAccount implements Serializable {
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = 4257130983068362891L;

    /**
     * The username/email/id of the account.
     */
    public String username;

    /**
     * The account's Minecraft username.
     */
    public String minecraftUsername;

    /**
     * The UUID of the account.
     */
    public String uuid;

    /**
     * The type of the account.
     */
    public String type;

    /**
     * The pack names this account has collapsed in the {@link PacksTab}, if any.
     */
    public List<String> collapsedPacks = new ArrayList<>();

    /**
     * The instance names this account has collapsed in the {@link InstancesTab}, if
     * any.
     */
    public List<String> collapsedInstances = new ArrayList<>();

    /**
     * The server names this account has collapsed in the {@link ServersTab}, if
     * any.
     */
    public List<String> collapsedServers = new ArrayList<>();

    /**
     * If the skin is currently being updated.
     */
    public boolean skinUpdating = false;

    public abstract String getAccessToken();

    public abstract String getSessionToken();

    public abstract String getCurrentUsername();

    public abstract String getSkinUrl();

    public void updateUsername() {
        final ProgressDialog<Boolean> dialog = new ProgressDialog<>(GetText.tr("Checking For Username Change"), 0,
                GetText.tr("Checking Username Change For {0}", this.minecraftUsername),
                "Aborting checking for username change for " + this.minecraftUsername);

        dialog.addThread(new Thread(() -> {
            String currentUsername = getCurrentUsername();

            if (currentUsername == null) {
                dialog.setReturnValue(false);
                dialog.close();
                return;
            }

            if (!currentUsername.equals(this.minecraftUsername)) {
                LogManager.info("The username for account with UUID of " + this.getUUIDNoDashes() + " changed from "
                        + this.minecraftUsername + " to " + currentUsername);
                this.minecraftUsername = currentUsername;
                dialog.setReturnValue(true);
            }

            dialog.close();
        }));

        dialog.start();

        if (dialog.getReturnValue() == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Changes"))
                    .setContent(GetText.tr("Your username hasn't changed.")).setType(DialogManager.INFO).show();
        } else if (dialog.getReturnValue()) {
            AccountManager.saveAccounts();
            DialogManager.okDialog().setTitle(GetText.tr("Username Updated"))
                    .setContent(GetText.tr("Your username has been updated.")).setType(DialogManager.INFO).show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(
                            GetText.tr("Error checking for username change. Check the error logs and try again later."))
                    .setType(DialogManager.ERROR).show();
        }
    }

    /**
     * Updates this Account's skin by redownloading the Minecraft skin from Mojang's
     * skin server.
     */
    public void updateSkin() {
        if (!this.skinUpdating) {
            this.skinUpdating = true;
            final File file = FileSystem.SKINS.resolve(this.getUUIDNoDashes() + ".png").toFile();
            LogManager.info("Downloading skin for " + this.minecraftUsername);
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Downloading Skin"), 0,
                    GetText.tr("Downloading Skin For {0}", this.minecraftUsername),
                    "Aborting downloading Minecraft skin for " + this.minecraftUsername);
            final UUID uid = this.getRealUUID();
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(false);
                String skinURL = getSkinUrl();
                if (skinURL == null) {
                    LogManager.warn("Couldn't download skin because the url found was NULL. Using default skin");
                    if (!file.exists()) {
                        String skinFilename = "default.png";

                        // even UUID's use the alex skin
                        if ((uid.hashCode() & 1) != 0) {
                            skinFilename = "default_alex.png";
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
                                    skinFilename = "default_alex.png";
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
                    com.atlauncher.evnt.manager.AccountManager.post();
                }
                dialog.close();
            }));
            dialog.start();
            if (!(Boolean) dialog.getReturnValue()) {
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(GetText.tr("Error downloading skin. Please try again later!"))
                        .setType(DialogManager.ERROR).show();
            }
            this.skinUpdating = false;
        }
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin, getting just
     * the head of it.
     *
     * @return The Account's Minecraft usernames head
     */
    public ImageIcon getMinecraftHead() {
        File file = FileSystem.SKINS.resolve((this.uuid == null ? "default" : this.getUUIDNoDashes()) + ".png")
                .toFile();

        if (!file.exists()) {
            this.updateSkin(); // Download/update the users skin
        }

        // If the file doesn't exist then use the default Minecraft skin.
        if (file == null || !file.exists()) {
            return SkinUtils.getDefaultHead();
        }

        return SkinUtils.getHead(file);
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin.
     *
     * @return The Account's Minecraft usernames skin
     */
    public ImageIcon getMinecraftSkin() {
        File file = FileSystem.SKINS.resolve(this.getUUIDNoDashes() + ".png").toFile();

        if (!file.exists()) {
            this.updateSkin(); // Download/update the users skin
        }

        // If the file doesn't exist then use the default Minecraft skin.
        if (file == null || !file.exists()) {
            return SkinUtils.getDefaultSkin();
        }

        return SkinUtils.getSkin(file);
    }

    /**
     * Gets the UUID of this account with no dashes.
     *
     * @return The UUID for this Account with no dashes
     */
    public String getUUIDNoDashes() {
        return (this.uuid == null ? "0" : this.uuid.replace("-", ""));
    }

    /**
     * Gets the real UUID of this account.
     *
     * @return The real UUID for this Account
     */
    public UUID getRealUUID() {
        return (this.uuid == null ? UUID.randomUUID() : UUIDTypeAdapter.fromString(this.uuid));
    }

    @Override
    public String toString() {
        return this.minecraftUsername;
    }
}
