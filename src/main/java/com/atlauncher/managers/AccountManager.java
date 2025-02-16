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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class AccountManager {
    private static final Type abstractAccountListType = new TypeToken<List<MicrosoftAccount>>() {}.getType();

    public static final BehaviorSubject<List<MicrosoftAccount>> ACCOUNTS = BehaviorSubject
            .createDefault(new ArrayList<>());

    /**
     * Account using the Launcher
     */
    public static final BehaviorSubject<Optional<MicrosoftAccount>> SELECTED_ACCOUNT = BehaviorSubject
            .createDefault(Optional.empty());

    public static Observable<List<MicrosoftAccount>> getAccountsObservable() {
        return ACCOUNTS;
    }

    public static Observable<Optional<MicrosoftAccount>> getSelectedAccountObservable() {
        return SELECTED_ACCOUNT;
    }

    @Nonnull
    public static List<MicrosoftAccount> getAccounts() {
        return ACCOUNTS.getValue();
    }

    @Nullable
    public static MicrosoftAccount getSelectedAccount() {
        return SELECTED_ACCOUNT.getValue().orElse(null);
    }

    /**
     * Loads the saved Accounts
     */
    public static void loadAccounts() {
        PerformanceManager.start();
        LogManager.debug("Loading accounts");

        ArrayList<MicrosoftAccount> newAccounts = new ArrayList<>();

        if (Files.exists(FileSystem.ACCOUNTS)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                    Files.newInputStream(FileSystem.ACCOUNTS), StandardCharsets.UTF_8)) {
                List<MicrosoftAccount> accounts = Gsons.DEFAULT.fromJson(fileReader, abstractAccountListType);

                newAccounts.addAll(accounts.stream().filter(account -> account.accessToken != null
                        && account.accessToken.split("\\.").length == 3).collect(Collectors.toList()));
            } catch (Exception e) {
                LogManager.logStackTrace("Exception loading accounts", e);
            }
        }

        ACCOUNTS.onNext(newAccounts);

        for (MicrosoftAccount account : newAccounts) {
            if (account.username.equalsIgnoreCase(App.settings.lastAccount)) {
                SELECTED_ACCOUNT.onNext(Optional.of(account));
            }
        }

        if (!SELECTED_ACCOUNT.getValue().isPresent() && !newAccounts.isEmpty()) {
            SELECTED_ACCOUNT.onNext(Optional.of(newAccounts.get(0)));
        }

        LogManager.debug("Finished loading accounts");
        PerformanceManager.end();
    }

    public static void saveAccounts() {
        saveAccounts(ACCOUNTS.getValue());
    }

    private static void saveAccounts(List<MicrosoftAccount> accounts) {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                Files.newOutputStream(FileSystem.ACCOUNTS), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(accounts, abstractAccountListType, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public static void addAccount(MicrosoftAccount account) {
        String accountType = "Microsoft";

        Analytics.trackEvent(AnalyticsEvent.forAccountAdd(accountType));
        LogManager.info("Added " + accountType + " Account " + account);

        List<MicrosoftAccount> accounts = ACCOUNTS.getValue();
        accounts.add(account);
        ACCOUNTS.onNext(accounts);

        if (accounts.size() > 1) {
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
    }

    public static void removeAccount(MicrosoftAccount account) {
        List<MicrosoftAccount> accounts = ACCOUNTS.getValue();
        if (SELECTED_ACCOUNT.getValue().orElse(null) == account) {
            if (accounts.size() == 1) {
                // if this was the only account, don't set an account
                switchAccount(null);
            } else {
                // if they have more accounts, switch to the first one
                switchAccount(accounts.get(0));
            }
        }
        accounts.remove(account);
        ACCOUNTS.onNext(accounts);
        saveAccounts();
    }

    /**
     * Switch account currently used and save it
     *
     * @param account Account to switch to
     */
    public static void switchAccount(@Nullable MicrosoftAccount account) {
        if (account == null) {
            LogManager.info("Logging out of account");
            SELECTED_ACCOUNT.onNext(Optional.empty());
            App.settings.lastAccount = null;
        } else {
            LogManager.info("Changed account to " + account);
            SELECTED_ACCOUNT.onNext(Optional.of(account));
            App.settings.lastAccount = account.username;
        }
        App.launcher.refreshPacksBrowserPanel();
        App.settings.save();
    }

    /**
     * Finds an Account from the given username
     *
     * @param username Username of the Account to find
     * @return Account if the Account is found from the username
     */
    public static MicrosoftAccount getAccountByName(String username) {
        for (MicrosoftAccount account : ACCOUNTS.getValue()) {
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
        for (MicrosoftAccount account : ACCOUNTS.getValue()) {
            if (account.username.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
}
