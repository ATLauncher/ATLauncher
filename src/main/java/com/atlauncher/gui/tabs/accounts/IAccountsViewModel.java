package com.atlauncher.gui.tabs.accounts;

import com.atlauncher.data.AbstractAccount;

import java.util.List;
import java.util.function.Consumer;

/**
 * 12 / 06 / 2022
 */
public interface IAccountsViewModel {
    List<String> getAccounts();

    void onAccountSelected(Consumer<AbstractAccount> onAccountSelected);

    void setSelectedAccount(int index);
}
