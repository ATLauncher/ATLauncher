/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlauncher.App;
import com.atlauncher.data.mojang.auth.AuthenticationRequest;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.google.gson.Gson;

public class Authentication {

    public static AuthenticationResponse checkAccount(String username, String password)
            throws IOException {
        Gson gson = new Gson();
        StringBuilder response = null;
        URL url = new URL("https://authserver.mojang.com/authenticate");
        String request = gson.toJson(new AuthenticationRequest(username, password, UUID
                .randomUUID() + ""));
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
        writer.write(request.getBytes());
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
        AuthenticationResponse result = gson.fromJson(response.toString(),
                AuthenticationResponse.class);
        return result;
    }

    public static String checkAccountOld(String username, String password) {
        StringBuilder response = null;
        try {
            URL urll = new URL("https://login.minecraft.net/?user="
                    + URLEncoder.encode(username, "UTF-8") + "&password="
                    + URLEncoder.encode(password, "UTF-8") + "&version=999");
            URLConnection connection = urll.openConnection();
            connection.setConnectTimeout(5000);
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
            return null;
        }
        return response.toString();
    }

    public static String loginOld(String username, String password) {
        if (App.settings.isInOfflineMode()) {
            return "token:0:0";
        }
        String authToken = null;
        String auth = checkAccountOld(username, password);
        if (auth == null) {
            authToken = "token:0:0";
        } else {
            if (auth.contains(":")) {
                String[] parts = auth.split(":");
                if (parts.length == 5) {
                    authToken = "token:" + parts[3] + ":0";
                } else {
                    authToken = auth;
                }
            } else {
                authToken = auth;
            }
        }
        return authToken;
    }

    public static AuthenticationResponse login(String username, String password) {
        if (App.settings.isInOfflineMode()) {
            return null;
        }
        AuthenticationResponse response = null;
        try {
            response = checkAccount(username, password);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
        return response;
    }
}
