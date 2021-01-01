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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.gui.tabs.PacksTab;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Utils;

/**
 * This class deals with the Accounts in the launcher.
 *
 * @deprecated This is no longer used. Replaced with AbstractAccount as the base
 *             and MojangAccount and MicrosoftAccount
 */
public class Account implements Serializable {
    /**
     * Auto generated serial.
     */
    public static final long serialVersionUID = 525763616120118176L;

    /**
     * The username/email used to login to Mojang servers.
     */
    public String username;

    /**
     * The account's password to login to Mojang servers.
     */
    public transient String password;

    /**
     * The encrypted password.
     */
    public String encryptedPassword;

    /**
     * The client token.
     */
    public String clientToken;

    /**
     * The account's Minecraft username.
     */
    public String minecraftUsername;

    /**
     * The UUID of the account.
     */
    public String uuid;

    /**
     * If this account should remember the password or not.
     */
    public boolean remember;

    /**
     * If this account is a real user or not.
     */
    public transient boolean isReal;

    /**
     * The pack names this account has collapsed in the {@link PacksTab}, if any.
     */
    public List<String> collapsedPacks;

    /**
     * The instance names this account has collapsed in the {@link InstancesTab}, if
     * any.
     */
    public List<String> collapsedInstances;

    public List<String> collapsedServers;

    /**
     * If the skin is currently being updated.
     */
    public boolean skinUpdating = false;

    /**
     * This is the store for this username as returned by Mojang.
     */
    public Map<String, Object> store;

    /**
     * Reads in the object from file into an Object.
     *
     * @param ois The InputStream for the object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Read the object in
        if (this.encryptedPassword == null) {
            this.password = "";
            this.remember = false;
        } else {
            this.password = Utils.decrypt(this.encryptedPassword);
            if (this.password == null) {
                LogManager.error("Error reading in saved password from file!");
                this.password = "";
                this.remember = false;
            }
        }
        this.isReal = true;
    }
}
