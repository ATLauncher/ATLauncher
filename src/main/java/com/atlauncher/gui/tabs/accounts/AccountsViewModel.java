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
package com.atlauncher.gui.tabs.accounts;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.gui.dialogs.ChangeSkinDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Authentication;

/**
 * 12 / 06 / 2022
 */
public class AccountsViewModel implements IAccountsViewModel {
    private static final Logger LOG = LogManager.getLogger(AccountsViewModel.class);

    @Override
    public int accountCount() {
        return AccountManager.getAccounts().size();
    }

    private List<AbstractAccount> accounts() {
        return AccountManager.getAccounts();
    }

    private Consumer<List<String>> _onAccountsChanged;

    @Override
    public void onAccountsNamesChanged(Consumer<List<String>> onAccountsChanged) {
        _onAccountsChanged = onAccountsChanged;
        pushNewAccounts();
    }

    /**
     * Update the UI with new accounts
     */
    @Override
    public void pushNewAccounts() {
        _onAccountsChanged.accept(
                accounts().stream()
                        .map(account -> account.minecraftUsername)
                        .collect(Collectors.toList()));
    }

    private Consumer<AbstractAccount> selected;
    private int selectedAccountIndex = -1;

    @Override
    public void onAccountSelected(Consumer<AbstractAccount> onAccountSelected) {
        selected = onAccountSelected;
    }

    @Override
    public void setSelectedAccount(int index) {
        selectedAccountIndex = index - 1;
        if (index == 0)
            selected.accept(null);
        else
            selected.accept(getSelectedAccount());
    }

    @NotNull
    @Override
    public AbstractAccount getSelectedAccount() {
        return accounts().get(selectedAccountIndex);
    }

    @Nullable
    @Override
    public MicrosoftAccount getSelectedAccountAs() {
        AbstractAccount account = getSelectedAccount();

        if (account instanceof MicrosoftAccount)
            return (MicrosoftAccount) account;
        return null;
    }

    private String loginUsername = null;
    private String loginPassword = null;
    private boolean loginRemember = false;

    @Override
    public boolean isLoginUsernameSet() {
        return loginUsername != null && !loginUsername.isEmpty();
    }

    @Override
    public @Nullable String getLoginUsername() {
        return loginUsername;
    }

    @Override
    public void setLoginUsername(String username) {
        loginUsername = username;
    }

    @Override
    public boolean isLoginPasswordSet() {
        return loginPassword != null && !loginPassword.isEmpty();
    }

    @Override
    public void setLoginPassword(String password) {
        loginPassword = password;
    }

    @Override
    public void setRememberLogin(boolean rememberLogin) {
        loginRemember = rememberLogin;
    }

    private String clientToken = null;

    @NotNull
    private String getClientToken() {
        if (clientToken == null)
            clientToken = UUID.randomUUID().toString().replace("-", "");

        return clientToken;
    }

    private void invalidateClientToken() {
        clientToken = null;
    }

    @Override
    public LoginPreCheckResult loginPreCheck() {
        if (AccountManager.isAccountByName(loginUsername)) {
            return new LoginPreCheckResult.Exists();
        }
        return null;
    }

    private void addNewAccount(LoginResponse response) {
        MojangAccount account = new MojangAccount(loginUsername,
                loginPassword,
                response,
                loginRemember,
                getClientToken());

        AccountManager.addAccount(account);
        pushNewAccounts();
    }

    private void editAccount(LoginResponse response) {
        AbstractAccount account = getSelectedAccount();

        if (account instanceof MojangAccount) {
            MojangAccount mojangAccount = (MojangAccount) account;

            mojangAccount.username = loginUsername;
            mojangAccount.minecraftUsername = response.getAuth().getSelectedProfile().getName();
            mojangAccount.uuid = response.getAuth().getSelectedProfile().getId().toString();
            if (loginRemember) {
                mojangAccount.setPassword(loginPassword);
            } else {
                mojangAccount.encryptedPassword = null;
                mojangAccount.password = null;
            }
            mojangAccount.remember = loginRemember;
            mojangAccount.clientToken = getClientToken();
            mojangAccount.store = response.getAuth().saveForStorage();

            AccountManager.saveAccounts();
            com.atlauncher.evnt.manager.AccountManager.post();
        }

        Analytics.sendEvent("Edit", "Account");
        LOG.info("Edited Account {}", account);
        pushNewAccounts();
    }

    private LoginResponse loginResponse = null;

    @NotNull
    @Override
    public LoginPostResult loginPost() {
        if (loginResponse != null && loginResponse.hasAuth() && loginResponse.isValidAuth()) {
            if (selectedAccountIndex == -1) {
                addNewAccount(loginResponse);
                invalidateClientToken();
                return new LoginPostResult.Added();
            } else {
                editAccount(loginResponse);
                invalidateClientToken();
                return new LoginPostResult.Edited();
            }
        } else {
            return new LoginPostResult.Error(loginResponse != null ? loginResponse.getErrorMessage() : null);
        }
    }

    @Override
    public int getSelectedIndex() {
        return selectedAccountIndex + 1;
    }

    @Override
    public void login() {
        loginResponse = Authentication.checkAccount(loginUsername, loginPassword, getClientToken());
    }

    @Override
    public boolean refreshAccessToken() {
        Analytics.sendEvent("RefreshAccessToken", "Account");

        AbstractAccount abstractAccount = getSelectedAccount();
        if (abstractAccount instanceof MicrosoftAccount) {
            MicrosoftAccount account = (MicrosoftAccount) abstractAccount;
            boolean success = account
                    .refreshAccessToken(true);

            if (!success) {
                account.mustLogin = true;
            }

            AccountManager.saveAccounts();

            return success;
        }
        return false;
    }

    @Override
    public void updateUsername() {
        AbstractAccount account = getSelectedAccount();
        Analytics.sendEvent("UpdateUsername", "Account");
        account.updateUsername();
        AccountManager.saveAccounts();
        pushNewAccounts();
    }

    @Override
    public void changeSkin() {
        AbstractAccount account = getSelectedAccount();

        new ChangeSkinDialog(account);
    }

    @Override
    public void updateSkin() {
        AbstractAccount account = getSelectedAccount();
        Analytics.sendEvent("UpdateSkin", "Account");
        account.updateSkin();
    }

    @Override
    public void deleteAccount() {
        Analytics.sendEvent("Delete", "Account");
        AccountManager.removeAccount(getSelectedAccount());
        pushNewAccounts();
    }
}
