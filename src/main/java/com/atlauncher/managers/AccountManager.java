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
package com.atlauncher.managers;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.data.Account;

public class AccountManager {
    public static List<Account> getAccounts() {
        return Data.ACCOUNTS;
    }

    public static Account getSelectedAccount() {
        return Data.SELECTED_ACCOUNT;
    }

    /**
     * Loads the saved Accounts
     */
    public static void loadAccounts() {
        PerformanceManager.start();
        LogManager.debug("Loading accounts");
        if (Files.exists(FileSystem.USER_DATA)) {
            FileInputStream in = null;
            ObjectInputStream objIn = null;
            try {
                in = new FileInputStream(FileSystem.USER_DATA.toFile());
                objIn = new ObjectInputStream(in);
                Object obj;
                while ((obj = objIn.readObject()) != null) {
                    if (obj instanceof Account) {
                        Data.ACCOUNTS.add((Account) obj);
                    }
                }
            } catch (EOFException e) {
                // Don't log this, it always happens when it gets to the end of the file
            } catch (IOException | ClassNotFoundException e) {
                LogManager.logStackTrace("Exception while trying to read accounts in from file.", e);
            } finally {
                try {
                    if (objIn != null) {
                        objIn.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace(
                            "Exception while trying to close FileInputStream/ObjectInputStream when reading in " + ""
                                    + "accounts.",
                            e);
                }
            }
        }
        LogManager.debug("Finished loading accounts");
        PerformanceManager.end();
    }

    public static void saveAccounts() {
        FileOutputStream out = null;
        ObjectOutputStream objOut = null;
        try {
            out = new FileOutputStream(FileSystem.USER_DATA.toFile());
            objOut = new ObjectOutputStream(out);
            for (Account account : Data.ACCOUNTS) {
                objOut.writeObject(account);
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (objOut != null) {
                    objOut.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Exception while trying to close FileOutputStream/ObjectOutputStream when saving "
                                + "accounts.",
                        e);
            }
        }
    }

    public static void addAccount(Account account) {
        Data.ACCOUNTS.add(account);
    }

    public static void removeAccount(Account account) {
        if (Data.SELECTED_ACCOUNT == account) {
            if (Data.ACCOUNTS.size() == 1) {
                // if this was the only account, don't set an account
                switchAccount(null);
            } else {
                // if they have more accounts, switch to the first one
                switchAccount(Data.ACCOUNTS.get(0));
            }
        }
        Data.ACCOUNTS.remove(account);
        saveAccounts();
        com.atlauncher.evnt.manager.AccountManager.post();
    }

    /**
     * Switch account currently used and save it
     *
     * @param account Account to switch to
     */
    public static void switchAccount(Account account) {
        if (Data.SELECTED_ACCOUNT == null) {
            LogManager.info("Logging out of account");
            Data.SELECTED_ACCOUNT = null;
        } else {
            if (account.isReal()) {
                LogManager.info("Changed account to " + account);
                Data.SELECTED_ACCOUNT = account;
            } else {
                LogManager.info("Logging out of account");
                Data.SELECTED_ACCOUNT = null;
            }
        }
        App.launcher.refreshVanillaPacksPanel();
        App.launcher.refreshFeaturedPacksPanel();
        App.launcher.refreshPacksPanel();
        App.launcher.reloadInstancesPanel();
        App.launcher.reloadServersPanel();
        com.atlauncher.evnt.manager.AccountManager.post();
        App.settings.save();
    }

    /**
     * Finds an Account from the given username
     *
     * @param username Username of the Account to find
     * @return Account if the Account is found from the username
     */
    public static Account getAccountByName(String username) {
        for (Account account : Data.ACCOUNTS) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Finds if an Account is available
     *
     * @param username The username of the Account
     * @return true if found, false if not
     */
    public static boolean isAccountByName(String username) {
        for (Account account : Data.ACCOUNTS) {
            if (account.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}
