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
package com.atlauncher.utils;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.mojang.authlib.Agent;
import com.mojang.authlib.UserAuthentication;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.UserMigratedException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

public class AuthenticationNew {
    public static LoginResponse checkAccount(String username, String password) {
        return checkAccount(username, password, "42");
    }

    public static LoginResponse checkAccount(String username, String password, String clientToken) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(App
                .settings.getProxyForAuth(), clientToken).createUserAuthentication(Agent.MINECRAFT);
        LoginResponse response = new LoginResponse();

        auth.setUsername(username);
        auth.setPassword(password);

        if(auth.canLogIn()) {
            try {
                auth.logIn();
                response.setAuth(auth);
            } catch(AuthenticationException e) {
                response.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }
        }

        return response;
    }
}
