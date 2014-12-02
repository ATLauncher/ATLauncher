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
import com.atlauncher.data.Account;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.mojang.api.ProfileResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

public class Authentication {
    public static LoginResponse checkAccount(String username, String password) {
        return checkAccount(username, password, "1");
    }

    public static LoginResponse checkAccount(String username, String password, String clientToken) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(App
                .settings.getProxyForAuth(), clientToken).createUserAuthentication(Agent.MINECRAFT);

        LoginResponse response = new LoginResponse(username);

        auth.setUsername(username);
        auth.setPassword(password);

        if (auth.canLogIn()) {
            try {
                auth.logIn();
                response.setAuth(auth);
            } catch (AuthenticationException e) {
                response.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }
        }

        return response;
    }

    public static LoginResponse login(Account account) {
        return login(account, false);
    }

    public static LoginResponse login(Account account, boolean logout) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(App
                .settings.getProxyForAuth(), "1").createUserAuthentication(Agent.MINECRAFT);
        LoginResponse response = new LoginResponse(account.getUsername());

        if (account.hasStore()) {
            auth.loadFromStorage(account.getStore());
        }

        if (logout) {
            auth.logOut();

            auth.setUsername(account.getUsername());
            auth.setPassword(account.getPassword());
        }

        if (auth.canLogIn()) {
            try {
                auth.logIn();
                response.setAuth(auth);
                response.save();
            } catch (AuthenticationUnavailableException e) {
                response.setErrorMessage(e.getMessage());
                response.setOffline();
                e.printStackTrace();
            } catch (AuthenticationException e) {
                response.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }
        }

        return response;
    }

    public static String getUUID(String username) {
        Downloadable downloadable = new Downloadable("https://api.mojang.com/users/profiles/minecraft/" + username,
                false);

        ProfileResponse profile = Gsons.DEFAULT.fromJson(downloadable.getContents(), ProfileResponse.class);

        return profile.getId();
    }
}
