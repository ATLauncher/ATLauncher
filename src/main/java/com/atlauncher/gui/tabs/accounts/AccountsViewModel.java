package com.atlauncher.gui.tabs.accounts;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Authentication;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 12 / 06 / 2022
 */
public class AccountsViewModel implements IAccountsViewModel {
    private static final Logger LOG = LogManager.getLogger(AccountsViewModel.class);

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

    private AbstractAccount getSelectedAccount() {
        return accounts.get(selectedAccountIndex);
    }

    private String loginUsername = null;
    private String loginPassword = null;
    private boolean loginRemember = false;

    @Override
    public boolean isLoginUsernameSet() {
        return loginUsername != null && !loginUsername.isEmpty();
    }

    @Override
    public String getLoginUsername() {
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
    @Override
    public String getClientToken() {
        if (clientToken == null)
            clientToken = UUID.randomUUID().toString().replace("-", "");

        return clientToken;
    }

    @Override
    public void invalidateClientToken() {
        clientToken = null;
    }

    @Override
    public LoginPreCheckResult loginPreCheck() {
        if (AccountManager.isAccountByName(loginUsername)) {
            return new LoginPreCheckResult.Exists();
        }
        return null;
    }

    @Override
    public void addNewAccount(LoginResponse response) {
        MojangAccount account = new MojangAccount(loginUsername,
            loginPassword,
            response,
            loginRemember,
            getClientToken()
        );

        AccountManager.addAccount(account);
        _accountUIs = null; // Invalidate old list
    }

    @Override
    public void editAccount(LoginResponse response) {
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
        _accountUIs = null; // Invalidate old list
    }

    @Override
    public int getSelectedIndex() {
        return selectedAccountIndex + 1;
    }

    @NotNull
    @Override
    public LoginResponse checkAccount() {
        return Authentication.checkAccount(loginUsername, loginPassword, getClientToken());
    }
}
