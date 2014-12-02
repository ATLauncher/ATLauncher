/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.data;

import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class LoginResponse {
    private boolean hasError;
    private String errorMessage;
    private YggdrasilUserAuthentication auth;

    public LoginResponse() {
        this.hasError = false;
        this.auth = null;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.hasError = true;
    }

    public boolean hasError() {
        return this.hasError;
    }

    public String getErrorMessage() {
        return (this.errorMessage == null ? "Unknown Error Occured" : this.errorMessage);
    }

    public void setAuth(YggdrasilUserAuthentication auth) {
        this.auth = auth;
    }

    public boolean hasAuth() {
        return (this.auth != null);
    }

    public YggdrasilUserAuthentication getAuth() {
        return this.auth;
    }

    public boolean isValidAuth() {
        if(!this.hasAuth()) {
            return false;
        }

        if(!auth.isLoggedIn() || auth.getAuthenticatedToken() == null || auth.getSelectedProfile() == null) {
            this.setErrorMessage("Error with response received from Mojang!");
        }

        return this.auth.isLoggedIn() && this.auth.getSelectedProfile() != null;
    }
}
