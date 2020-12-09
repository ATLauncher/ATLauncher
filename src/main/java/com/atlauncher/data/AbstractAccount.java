/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.ImageIcon;

import com.atlauncher.FileSystem;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.gui.tabs.ServersTab;
import com.atlauncher.utils.SkinUtils;
import com.mojang.util.UUIDTypeAdapter;

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

    public abstract void updateUsername();

    public abstract void updateSkin();

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
            file = FileSystem.SKINS.resolve("default.png").toFile();
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
            file = FileSystem.SKINS.resolve("default.png").toFile();
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

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        System.out.println(1);
    }
}
