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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlauncher.App;

public class Authentication {

    public static String checkAccount(String username, String password) throws IOException {
        StringBuilder response = null;
        URL url = new URL("https://authserver.mojang.com/authenticate");
        String request = "{\"agent\":{\"name\":\"Minecraft\",\"version\":10},\"username\":\""
                + username + "\",\"password\":\"" + password + "\",\"clientToken\":\""
                + UUID.randomUUID() + "\"}";
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
        return response.toString();
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

    public static String getSessionToken(boolean isNewLoginMethod, String username, String password) {
        String authToken = null;
        String auth = null;
        String authError = null;
        if (App.settings.isInOfflineMode()) {
            authToken = "token:0:0";
        } else if (!isNewLoginMethod) {
            auth = checkAccountOld(username, password);
            if (auth == null) {
                authToken = "token:0:0";
            } else {
                if (auth.contains(":")) {
                    String[] parts = auth.split(":");
                    if (parts.length == 5) {
                        authToken = "token:" + parts[3] + ":0";
                    } else {
                        authError = auth;
                    }
                } else {
                    authError = auth;
                }
            }
        } else {
            try {
                auth = checkAccount(username, password);
            } catch (IOException e) {
                App.settings.logStackTrace(e);
            }
            if (auth == null) {
                authToken = "token:0:0";
            } else {
                JSONParser parser = new JSONParser();
                JSONObject object = null;
                try {
                    object = (JSONObject) parser.parse(auth);
                } catch (ParseException e) {
                    App.settings.logStackTrace(e);
                    return "An unknown error occured!";
                }
                if (object.containsKey("errorMessage")) {
                    authError = (String) object.get("errorMessage");
                } else if (object.containsKey("accessToken")) {
                    String accessToken = (String) object.get("accessToken");
                    JSONObject selectedProfileObject = (JSONObject) object.get("selectedProfile");
                    String profileID = (String) selectedProfileObject.get("id");
                    authToken = "token:" + accessToken + ":" + profileID;
                } else {
                    authError = "An unknown error occured!";
                }
            }
        }
        if (authError == null) {
            return authToken;
        } else {
            return authError.replace("Invalid credentials. ", "");
        }
    }
}
