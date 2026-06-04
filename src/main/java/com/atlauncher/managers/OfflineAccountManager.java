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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.OfflineAccount;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class OfflineAccountManager {
    private static final Type offlineAccountListType = new TypeToken<List<OfflineAccount>>() {
    }.getType();

    public static final BehaviorSubject<List<OfflineAccount>> ACCOUNTS = BehaviorSubject
        .createDefault(new ArrayList<>());

    public static final BehaviorSubject<Optional<OfflineAccount>> SELECTED_ACCOUNT = BehaviorSubject
        .createDefault(Optional.empty());

    public static Observable<List<OfflineAccount>> getAccountsObservable() {
        return ACCOUNTS;
    }

    public static Observable<Optional<OfflineAccount>> getSelectedAccountObservable() {
        return SELECTED_ACCOUNT;
    }

    @Nonnull
    public static List<OfflineAccount> getAccounts() {
        return Optional.ofNullable(ACCOUNTS.getValue()).orElse(new ArrayList<>());
    }

    @Nullable
    public static OfflineAccount getSelectedAccount() {
        return SELECTED_ACCOUNT.getValue().orElse(null);
    }

    public static void loadAccounts() {
        LogManager.debug("Loading offline accounts");

        ArrayList<OfflineAccount> newAccounts = new ArrayList<>();

        if (Files.exists(FileSystem.OFFLINE_ACCOUNTS)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(FileSystem.OFFLINE_ACCOUNTS), StandardCharsets.UTF_8)) {
                List<OfflineAccount> accounts = Gsons.DEFAULT.fromJson(fileReader, offlineAccountListType);
                if (accounts != null) {
                    newAccounts.addAll(accounts);
                }
            } catch (Exception e) {
                LogManager.logStackTrace("Exception loading offline accounts", e);
            }
        }

        ACCOUNTS.onNext(newAccounts);

        String lastOfflineAccount = App.settings == null ? null : App.settings.lastOfflineAccount;
        for (OfflineAccount account : newAccounts) {
            if (account.username.equalsIgnoreCase(lastOfflineAccount)) {
                SELECTED_ACCOUNT.onNext(Optional.of(account));
            }
        }

        if (!SELECTED_ACCOUNT.getValue().isPresent() && !newAccounts.isEmpty()) {
            SELECTED_ACCOUNT.onNext(Optional.of(newAccounts.get(0)));
        }

        LogManager.debug("Finished loading offline accounts");
    }

    public static void saveAccounts() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            Files.newOutputStream(FileSystem.OFFLINE_ACCOUNTS), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(ACCOUNTS.getValue(), offlineAccountListType, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public static void addAccount(OfflineAccount account) {
        LogManager.info("Added Offline Account " + account);

        List<OfflineAccount> accounts = ACCOUNTS.getValue();
        accounts.add(account);
        ACCOUNTS.onNext(accounts);

        switchAccount(account);
        saveAccounts();
    }

    public static void removeAccount(OfflineAccount account) {
        List<OfflineAccount> accounts = ACCOUNTS.getValue();
        if (SELECTED_ACCOUNT.getValue().orElse(null) == account) {
            if (accounts.size() == 1) {
                switchAccount(null);
            } else {
                switchAccount(accounts.get(0) == account ? accounts.get(1) : accounts.get(0));
            }
        }
        accounts.remove(account);
        ACCOUNTS.onNext(accounts);
        saveAccounts();
    }

    public static void switchAccount(@Nullable OfflineAccount account) {
        if (account == null) {
            SELECTED_ACCOUNT.onNext(Optional.empty());
            if (App.settings != null) {
                App.settings.lastOfflineAccount = null;
                App.settings.save();
            }
        } else {
            LogManager.info("Changed offline account to " + account);
            SELECTED_ACCOUNT.onNext(Optional.of(account));
            if (App.settings != null) {
                App.settings.lastOfflineAccount = account.username;
                App.settings.save();
            }
        }
    }

    public static OfflineAccount getAccountByName(String username) {
        for (OfflineAccount account : ACCOUNTS.getValue()) {
            if (account.username.equalsIgnoreCase(username)) {
                return account;
            }
        }
        return null;
    }

    public static boolean isAccountByName(String username) {
        return getAccountByName(username) != null;
    }
}
