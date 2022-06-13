package com.atlauncher.gui.tabs.accounts;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.LoginResponse;
import jnr.ffi.annotations.In;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 12 / 06 / 2022
 */
public interface IAccountsViewModel {
    List<String> getAccounts();

    void onAccountSelected(Consumer<AbstractAccount> onAccountSelected);

    void setSelectedAccount(int index);

    boolean isLoginUsernameSet();

    String getLoginUsername();
    void setLoginUsername(String username);

    boolean isLoginPasswordSet();

    void setLoginPassword(String password);

    void setRememberLogin(boolean rememberLogin);

    @NotNull String getClientToken();

    void invalidateClientToken();

    @Nullable
    LoginPreCheckResult loginPreCheck();

    @NotNull
    LoginResponse checkAccount();

    /**
     * Handle the response as adding a new account
     * @param response TODO describe
     */
    void addNewAccount(LoginResponse response);

    /**
     * Edit the currently selected account
     * @param response
     */
    void editAccount(LoginResponse response);

    /**
     * Get the selected index
     * @return UI index of selected item
     */
    int getSelectedIndex();


    abstract class LoginPreCheckResult {
        static class Exists extends LoginPreCheckResult {
        }
    }
}
