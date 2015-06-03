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
package com.atlauncher.managers;

import com.atlauncher.Data;
import com.atlauncher.FileSystemData;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.utils.MojangAPIUtils;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.List;

public class AccountManager {
    private static Account activeAccount = null;

    public static List<Account> getAccounts() {
        return Data.ACCOUNTS;
    }

    public static Account getActiveAccount() {
        return AccountManager.activeAccount;
    }

    /**
     * Should only be used to set the account when first starting up. If you need to switch an account, use
     * switchAccount(Account)
     *
     * @param account the account to be active, or null if none
     */
    public static void setActiveAccount(Account account) {
        if (AccountManager.activeAccount != null && account.isActive()) {
            AccountManager.activeAccount.setActive(false);
        }

        AccountManager.activeAccount = account;

        if (account != null && !account.isActive()) {
            account.setActive(true);
        }

        AccountManager.saveAccounts();
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

    public static void loadAccounts() {
        LogManager.debug("Loading Accounts");

        Data.ACCOUNTS.clear();

        if (Files.exists(FileSystemData.USER_DATA)) {
            try (ObjectInputStream oin = new ObjectInputStream(Files.newInputStream(FileSystemData.USER_DATA))) {
                Object obj;
                while ((obj = oin.readObject()) != null) {
                    if (obj instanceof Account) {
                        Data.ACCOUNTS.add((Account) obj);
                    }
                }
            } catch (EOFException e) {
                // Fallthrough
            } catch (Exception e) {
                LogManager.logStackTrace("Exception while trying to read accounts from file", e);
            }
        }

        for (Account account : Data.ACCOUNTS) {
            if (account.isActive()) {
                AccountManager.setActiveAccount(account);
                break;
            }
        }

        LogManager.debug("Finished loading accounts");
    }

    public static void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(FileSystemData.USER_DATA))) {
            for (Account acc : Data.ACCOUNTS) {
                oos.writeObject(acc);
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    public static void removeAccount(Account account) {
        if (AccountManager.activeAccount == account) {
            AccountManager.switchAccount(null);
        }

        Data.ACCOUNTS.remove(account);
        saveAccounts();
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.AccountsChangeEvent.class));
    }

    /**
     * Switch account currently used and save it
     *
     * @param account Account to switch to
     */
    public static void switchAccount(Account account) {
        if (account == null) {
            LogManager.info("Logging out of account");
            AccountManager.setActiveAccount(null);
        } else {
            if (account.isReal()) {
                LogManager.info("Changed account to " + account);
                AccountManager.setActiveAccount(account);
            } else {
                LogManager.info("Logging out of account");
                AccountManager.setActiveAccount(null);
            }
        }

        EventHandler.EVENT_BUS.publish(new EventHandler.PacksChangeEvent(true));
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.InstancesChangeEvent.class));
        EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.AccountsChangeEvent.class));
    }

    public static void setPackVisbility(Pack pack, boolean collapsed) {
        Account account = AccountManager.activeAccount;

        if (pack != null && account != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().add(pack.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedPacks().contains(pack.getName())) {
                    account.getCollapsedPacks().remove(pack.getName());
                }
            }

            AccountManager.saveAccounts();
            EventHandler.EVENT_BUS.publish(new EventHandler.PacksChangeEvent(true));
        }
    }

    public static void setInstanceVisbility(Instance instance, boolean collapsed) {
        Account account = AccountManager.activeAccount;

        if (instance != null && account.isReal()) {
            if (collapsed) {
                // Closed It
                if (!account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().add(instance.getName());
                }
            } else {
                // Opened It
                if (account.getCollapsedInstances().contains(instance.getName())) {
                    account.getCollapsedInstances().remove(instance.getName());
                }
            }

            AccountManager.saveAccounts();
            EventHandler.EVENT_BUS.publish(EventHandler.get(EventHandler.InstancesChangeEvent.class));
        }
    }

    public static void checkUUIDs() {
        LogManager.debug("Checking account UUIDs");
        for (Account account : Data.ACCOUNTS) {
            if (account.isUUIDNull()) {
                account.setUUID(MojangAPIUtils.getUUID(account.getMinecraftUsername()));
            }
        }
        AccountManager.saveAccounts();
        LogManager.debug("Done checking account UUIDs");
    }

    public static void checkForNameChanges() {
        LogManager.info("Checking for username changes");
        boolean changed = false;
        for (Account acc : Data.ACCOUNTS) {
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