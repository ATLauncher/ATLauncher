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
package com.atlauncher.managers;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Account;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("deprecation")
public class AccountManager {
    private static final Type abstractAccountListType = new TypeToken<List<AbstractAccount>>() {
    }.getType();

    public static List<AbstractAccount> getAccounts() {
        return Data.ACCOUNTS;
    }

    public static AbstractAccount getSelectedAccount() {
        return Data.SELECTED_ACCOUNT;
    }

    /**
     * Loads the saved Accounts
     */
    public static void loadAccounts() {
        PerformanceManager.start();
        LogManager.debug("Loading accounts");

        if (Files.exists(FileSystem.USER_DATA)) {
            LogManager.info("Converting old account format to new format.");
            convertAccounts();
        }

        if (Files.exists(FileSystem.ACCOUNTS)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                    new FileInputStream(FileSystem.ACCOUNTS.toFile()), StandardCharsets.UTF_8)) {
                List<AbstractAccount> accounts = Gsons.DEFAULT.fromJson(fileReader, abstractAccountListType);

                Data.ACCOUNTS.addAll(accounts.stream().filter(account -> {
                    if (account instanceof MicrosoftAccount) {
                        MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;
                        return microsoftAccount.accessToken != null
                                && microsoftAccount.accessToken.split("\\.").length == 3;
                    }

                    if (account instanceof MojangAccount) {
                        MojangAccount mojangAccount = (MojangAccount) account;
                        return !mojangAccount.uuid.equals("00000000000000000000000000000000")
                                && !mojangAccount.clientToken.isEmpty();
                    }

                    return !account.uuid.equals("00000000000000000000000000000000");
                }).collect(Collectors.toList()));
            } catch (Exception e) {
                LogManager.logStackTrace("Exception loading accounts", e);
            }
        }

        for (AbstractAccount account : Data.ACCOUNTS) {
            if (account.username.equalsIgnoreCase(App.settings.lastAccount)) {
                Data.SELECTED_ACCOUNT = account;
            }

            if (account instanceof MojangAccount) {
                MojangAccount mojangAccount = (MojangAccount) account;

                if (mojangAccount.encryptedPassword == null) {
                    mojangAccount.password = "";
                    mojangAccount.remember = false;
                } else {
                    mojangAccount.password = Utils.decrypt(mojangAccount.encryptedPassword);
                    if (mojangAccount.password == null) {
                        LogManager.error("Error reading in saved password from file!");
                        mojangAccount.password = "";
                        mojangAccount.remember = false;
                    }
                }
            }
        }

        if (Data.SELECTED_ACCOUNT == null && Data.ACCOUNTS.size() >= 1) {
            Data.SELECTED_ACCOUNT = Data.ACCOUNTS.get(0);
        }

        LogManager.debug("Finished loading accounts");
        PerformanceManager.end();
    }

    /**
     * Converts the saved Accounts from old format to new one
     */
    private static void convertAccounts() {
        FileInputStream in = null;
        ObjectInputStream objIn = null;
        List<AbstractAccount> convertedAccounts = new LinkedList<>();
        try {
            in = new FileInputStream(FileSystem.USER_DATA.toFile());
            objIn = new ObjectInputStream(in);
            Object obj;
            while ((obj = objIn.readObject()) != null) {
                Account account = (Account) obj;

                convertedAccounts.add(new MojangAccount(account.username, account.password, account.minecraftUsername,
                        account.uuid, account.remember, account.clientToken, account.store));
            }
        } catch (EOFException e) {
            // Don't log this, it always happens when it gets to the end of the file
        } catch (IOException | ClassNotFoundException e) {
            LogManager.logStackTrace("Exception while trying to convert accounts from file.", e);
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

        saveAccounts(convertedAccounts);

        try {
            Files.delete(FileSystem.USER_DATA);
        } catch (IOException e) {
            LogManager.logStackTrace("Exception trying to remove old userdata file after conversion.", e);
        }
    }

    public static void saveAccounts() {
        saveAccounts(Data.ACCOUNTS);
    }

    private static void saveAccounts(List<AbstractAccount> accounts) {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(FileSystem.ACCOUNTS.toFile()), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(accounts, abstractAccountListType, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public static void addAccount(AbstractAccount account) {
        String accountType = account instanceof MicrosoftAccount ? "Microsoft" : "Mojang";

        Analytics.trackEvent(AnalyticsEvent.forAccountAdd(accountType));
        LogManager.info("Added " + accountType + " Account " + account);

        Data.ACCOUNTS.add(account);

        account.updateSkin();

        if (Data.ACCOUNTS.size() > 1) {
            // not first account? ask if they want to switch to it
            int ret = DialogManager.optionDialog().setTitle(GetText.tr("Account Added"))
                    .setContent(GetText.tr("Account added successfully. Switch to it now?")).setType(DialogManager.INFO)
                    .addOption(GetText.tr("Yes"), true).addOption(GetText.tr("No")).show();

            if (ret == 0) {
                switchAccount(account);
            }
        } else {
            // first account? switch to it immediately
            switchAccount(account);
        }

        saveAccounts();
        com.atlauncher.evnt.manager.AccountManager.post();
    }

    public static void removeAccount(AbstractAccount account) {
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
    public static void switchAccount(AbstractAccount account) {
        if (account == null) {
            LogManager.info("Logging out of account");
            Data.SELECTED_ACCOUNT = null;
            App.settings.lastAccount = null;
        } else {
            LogManager.info("Changed account to " + account);
            Data.SELECTED_ACCOUNT = account;
            App.settings.lastAccount = account.username;
        }
        App.launcher.refreshPacksBrowserPanel();
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
    public static AbstractAccount getAccountByName(String username) {
        for (AbstractAccount account : Data.ACCOUNTS) {
            if (account.username.equalsIgnoreCase(username)) {
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
        for (AbstractAccount account : Data.ACCOUNTS) {
            if (account.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}
