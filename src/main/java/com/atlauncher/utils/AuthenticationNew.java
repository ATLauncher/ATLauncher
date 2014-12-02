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
import com.atlauncher.data.Account;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.data.mojang.auth.RefreshRequest;
import com.atlauncher.data.mojang.auth.ValidateRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.authlib.yggdrasil.response.RefreshResponse;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

public class AuthenticationNew {
    public static LoginResponse checkAccount(String username, String password) {
        return checkAccount(username, password, "42");
    }

    public static LoginResponse checkAccount(String username, String password, String clientToken) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(App
                .settings.getProxyForAuth(), clientToken).createUserAuthentication(Agent.MINECRAFT);

        LoginResponse response = new LoginResponse(username);

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

    public static boolean checkAccessToken(String accessToken) {
        boolean success = false;
        Gson gson = new Gson();
        StringBuilder response = null;
        try {
            URL url = new URL("https://authserver.mojang.com/validate");
            String request = gson.toJson(new ValidateRequest(accessToken));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes(Charset.forName("UTF-8")));
            writer.flush();
            writer.close();

            // Read the result

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                success = true; // All Good, token refreshed
            } catch (IOException e) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                // Nope token is bad, not good
            }
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
            return false; // Something bad happened, assume token is bad
        }
        return success;
    }

    public static AuthenticationResponse refreshAccessToken(Account account) {
        Gson gson = new Gson();
        StringBuilder response = null;
        try {
            URL url = new URL("https://authserver.mojang.com/refresh");
            String request = gson.toJson(new RefreshRequest(account.getAccessToken(), account.getClientToken()));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes(Charset.forName("UTF-8")));
            writer.flush();
            writer.close();

            // Read the result

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
            return null;
        }
        AuthenticationResponse result = null;
        if (response != null) {
            try {
                result = gson.fromJson(response.toString(), AuthenticationResponse.class);
            } catch (JsonSyntaxException e) {
                App.settings.logStackTrace(e);
            }
            if (result != null) {
                result.setUUID(UUID.randomUUID().toString());
            }
        }
        return result;
    }

    public static LoginResponse login(Account account) {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(App
                .settings.getProxyForAuth(), account.getClientToken()).createUserAuthentication(Agent.MINECRAFT);
        LoginResponse response = new LoginResponse(account.getUsername());

        auth.loadFromStorage(account.getStore());

        if(auth.canLogIn()) {
            try {
                auth.logIn();
                response.setAuth(auth);
                response.save();
            } catch(AuthenticationException e) {
                response.setErrorMessage(e.getMessage());
                e.printStackTrace();
            }
        }

        return response;
    }
}
