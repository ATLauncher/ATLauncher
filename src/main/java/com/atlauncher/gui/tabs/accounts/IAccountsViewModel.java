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
