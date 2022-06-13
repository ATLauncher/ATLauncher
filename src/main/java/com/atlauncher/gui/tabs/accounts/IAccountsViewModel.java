package com.atlauncher.gui.tabs.accounts;

import com.atlauncher.data.AbstractAccount;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 12 / 06 / 2022
 */
public interface IAccountsViewModel {
    @NotNull
    List<String> getAccounts();

    void onAccountSelected(Consumer<AbstractAccount> onAccountSelected);

    void setSelectedAccount(int index);

    boolean isLoginUsernameSet();

    @Nullable
    String getLoginUsername();

    void setLoginUsername(String username);

    boolean isLoginPasswordSet();

    void setLoginPassword(String password);

    void setRememberLogin(boolean rememberLogin);

    @Nullable
    LoginPreCheckResult loginPreCheck();

    void login();

    @NotNull
    LoginPostResult loginPost();

    /**
     * Get the selected index
     *
     * @return UI index of selected item
     */
    int getSelectedIndex();


    abstract class LoginPreCheckResult {
        static class Exists extends LoginPreCheckResult {
        }
    }

    abstract class LoginPostResult {
        static class Added extends LoginPostResult {
        }

        static class Edited extends LoginPostResult {
        }

        static class Error extends LoginPostResult {
            @Nullable
            String errorContent;

            public Error(@Nullable String errorContent) {
                this.errorContent = errorContent;
            }
        }
    }
}
