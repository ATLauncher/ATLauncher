package com.atlauncher.gui.tabs.accounts;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.managers.AccountManager;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 12 / 06 / 2022
 */
public class AccountsViewModel implements IAccountsViewModel {
    private List<String> _accountUIs = null;
    private List<AbstractAccount> accounts = AccountManager.getAccounts();

    @Override
    public List<String> getAccounts() {
        if (_accountUIs == null)
            _accountUIs = accounts
                .stream()
                .map(account -> account.minecraftUsername)
                .collect(Collectors.toList());

        return _accountUIs;
    }

    private Consumer<AbstractAccount> selected;

    @Override
    public void onAccountSelected(Consumer<AbstractAccount> onAccountSelected) {
        selected = onAccountSelected;
    }

    @Override
    public void setSelectedAccount(int index) {
        if (index == 0)
            selected.accept(null);
        else
            selected.accept(accounts.get(index - 1));
    }
}
