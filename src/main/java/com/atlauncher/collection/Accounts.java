/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.collection;

import com.atlauncher.managers.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.utils.MojangAPIUtils;

import java.util.LinkedList;

public final class Accounts extends LinkedList<Account> {
    public Account getByName(String name) {
        for (Account acc : this) {
            if (acc.getUsername().equalsIgnoreCase(name)) {
                return acc;
            }
        }

        return null;
    }

    public void checkUUIDs() {
        LogManager.debug("Checking account UUIDs");
        for (Account acc : this) {
            if (acc.isUUIDNull()) {
                acc.setUUID(MojangAPIUtils.getUUID(acc.getMinecraftUsername()));
            }
        }
        AccountManager.saveAccounts();
        LogManager.debug("Done checking account UUIDs");
    }

    public void checkForNameChanges() {
        LogManager.info("Checking for username changes");
        boolean changed = false;
        for (Account acc : this) {
            if (acc.checkForUsernameChange()) {
                changed = true;
                break;
            }
        }

        if (changed) {
            AccountManager.saveAccounts();
        }

        LogManager.info("Checking for username changes complete");
    }
}