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
package com.atlauncher.viewmodel.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.gui.dialogs.ChangeSkinDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.viewmodel.base.IAccountsViewModel;

public class AccountsViewModel implements IAccountsViewModel {
    @Override
    public int accountCount() {
        return AccountManager.getAccounts().size();
    }

    private List<MicrosoftAccount> accounts() {
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

    private Consumer<MicrosoftAccount> selected;
    private int selectedAccountIndex = -1;

    @Override
    public void onAccountSelected(Consumer<MicrosoftAccount> onAccountSelected) {
        selected = onAccountSelected;
    }

    @Override
    public void setSelectedAccount(int index) {
        selectedAccountIndex = index - 1;
        if (index == 0) {
            selected.accept(null);
        } else {
            selected.accept(getSelectedAccount());
        }
    }

    @Nullable
    @Override
    public MicrosoftAccount getSelectedAccount() {
        return accounts().get(selectedAccountIndex);
    }

    @Override
    public int getSelectedIndex() {
        return selectedAccountIndex + 1;
    }

    @Override
    public boolean refreshAccessToken() {
        Analytics.trackEvent(AnalyticsEvent.simpleEvent("account_refresh_access_token"));

        MicrosoftAccount account = getSelectedAccount();
        if (account == null) {
            return false;
        }

        boolean success = account
                .refreshAccessToken(true);

        if (!success) {
            account.mustLogin = true;
        }

        AccountManager.saveAccounts();

        return success;
    }

    @Override
    public void updateUsername() {
        MicrosoftAccount account = getSelectedAccount();
        if (account != null) {
            Analytics.trackEvent(AnalyticsEvent.simpleEvent("account_update_username"));
            account.updateUsername();
            AccountManager.saveAccounts();
            pushNewAccounts();
        }
    }

    @Override
    public void changeSkin() {
        MicrosoftAccount account = getSelectedAccount();

        if (account != null) {
            ChangeSkinDialog changeSkinDialog = new ChangeSkinDialog(account);
            changeSkinDialog.setVisible(true);
        }
    }

    @Override
    public void updateSkin() {
        MicrosoftAccount account = getSelectedAccount();
        if (account != null) {
            Analytics.trackEvent(AnalyticsEvent.simpleEvent("account_update_skin"));
            account.updateSkin();
        }
    }

    @Override
    public void deleteAccount() {
        MicrosoftAccount account = getSelectedAccount();
        if (account != null) {
            Analytics.trackEvent(AnalyticsEvent.simpleEvent("account_delete"));
            AccountManager.removeAccount(account);
            pushNewAccounts();
        }
    }
}
