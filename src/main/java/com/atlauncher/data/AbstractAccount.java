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
package com.atlauncher.data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;

import com.atlauncher.FileSystem;
import com.atlauncher.gui.dialogs.UpdateSkinDialog;
import com.atlauncher.gui.dialogs.UpdateUsernameDialog;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.utils.SkinUtils;

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
     * The pack names this account has collapsed, if any.
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

    public abstract String getAccessToken();

    public abstract String getSessionToken();

    public abstract String getUserType();

    /**
     * TODO move business code to separate class
     */
    public abstract String getCurrentUsername();

    /**
     * TODO move business code to separate class
     */
    public abstract void updateSkinPreCheck();

    /**
     * TODO move business code to separate class
     */
    public abstract void changeSkinPreCheck();

    /**
     * TODO move business code to separate class
     */
    public abstract String getSkinUrl();

    /**
     * @deprecated Business code in data class. Use UpdateUsernameDialog
     */
    @Deprecated
    public void updateUsername() {
        UpdateUsernameDialog.show(this);
    }

    /**
     * Updates this Account's skin by redownloading the Minecraft skin from Mojang's
     * skin server.
     *
     * @deprecated Business code in data class
     */
    @Deprecated
    public synchronized void updateSkin() {
        UpdateSkinDialog.show(this);
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin, getting just
     * the head of it.
     * <p>
     * TODO, Consider for moving to separate repository
     *
     * @return The Account's Minecraft usernames head
     */
    public ImageIcon getMinecraftHead() {
        File file = FileSystem.SKINS.resolve((this.uuid == null ? "default" : this.getUUIDNoDashes()) + ".png")
                .toFile();

        // If the file doesn't exist then use the default Minecraft skin.
        if (file == null || !file.exists()) {
            return SkinUtils.getDefaultHead();
        }

        return SkinUtils.getHead(file);
    }

    /**
     * Creates an {@link ImageIcon} of the Account's Minecraft skin.
     * <p>
     * TODO, Consider for moving to separate repository
     *
     * @return The Account's Minecraft usernames skin
     */
    public ImageIcon getMinecraftSkin() {
        File file = FileSystem.SKINS.resolve(this.getUUIDNoDashes() + ".png").toFile();

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
     * @see <a href="https://stackoverflow.com/a/19399768">https://stackoverflow.com/a/19399768</a>
     * @return uuid with dashes.
     */
    private String dashedUUID() {
        return this.uuid
            .replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                "$1-$2-$3-$4-$5"
            );
    }

    /**
     * Gets the real UUID of this account.
     *
     * @return The real UUID for this Account
     */
    public UUID getRealUUID() {
        return (this.uuid == null ? UUID.randomUUID() : UUID.fromString(dashedUUID()));
    }

    @Override
    public String toString() {
        return this.minecraftUsername;
    }
}
