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

import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.OS;
import com.atlauncher.data.Pack;
import com.atlauncher.data.mojang.OperatingSystem;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.SettingsManager;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Various utility methods for interacting with the ATLauncher API.
 */
public class ATLauncherAPI {
    private static boolean sendAPICall(String path, Object data) throws IOException {
        Request.Builder request = new Request.Builder().url(Constants.API_BASE_URL + path);

        if (data != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gsons.DEFAULT
                    .toJson(data));

            request = request.post(body);
        }

        Response response = Network.CLIENT.newCall(request.build()).execute();

        return (response.code() / 200) == 0;
    }

    private static String sendAPICallWithResponse(String path) throws IOException {
        return sendAPICallWithResponse(path, null);
    }

    private static String sendAPICallWithResponse(String path, Object data) throws IOException {
        Request.Builder request = new Request.Builder().url(Constants.API_BASE_URL + path);

        if (data != null) {
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Gsons.DEFAULT
                    .toJson(data));

            request = request.post(body);
        }

        Response response = Network.CLIENT.newCall(request.build()).execute();

        return response.body().string();
    }

    public static boolean postSystemInfo() {
        Map<String, Object> request = new HashMap<>();

        request.put("launcher_version", Constants.VERSION.toString());
        request.put("os_name", OperatingSystem.getOS().getName());
        request.put("os_version", OperatingSystem.getVersion());
        request.put("java_version", System.getProperty("java.version"));
        request.put("ram", OS.getSystemRam());
        request.put("64_bit", OS.is64Bit());

        try {
            return sendAPICall("system-info", request);
        } catch (IOException e) {
            LogManager.logStackTrace("Error sending in details of system", e);
        }

        return false;
    }

    public static String getShareCode(Pack pack, String version, String code) {
        try {
            String response = sendAPICallWithResponse("pack/" + pack.getSafeName() + "/" + version + "/share-code/" +
                    code);

            APIResponse apiResponse = Gsons.DEFAULT.fromJson(response, APIResponse.class);

            if (apiResponse.wasError()) {
                return null;
            }

            return apiResponse.getDataAsString();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static String postShareCode(Instance instance) {
        try {
            String response = sendAPICallWithResponse("pack/" + instance.getRealPack().getSafeName() + "/" + instance
                    .getVersion() + "/share-code", instance.getShareCodeData());

            APIResponse apiResponse = Gsons.DEFAULT.fromJson(response, APIResponse.class);

            if (apiResponse.wasError()) {
                return null;
            }

            return apiResponse.getDataAsString();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public static boolean addPackAction(Pack pack, String version, String action) {
        Map<String, Object> request = new HashMap<>();

        request.put("username", AccountManager.getActiveAccount().getMinecraftUsername());
        request.put("version", version);

        try {
            return sendAPICall("pack/" + pack.getSafeName() + "/" + action + "/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return false;
    }

    public static boolean addPackTimePlayed(Pack pack, int time, String version) {
        Map<String, Object> request = new HashMap<>();

        if (SettingsManager.enableLeaderboards()) {
            request.put("username", AccountManager.getActiveAccount().getMinecraftUsername());
        } else {
            request.put("username", null);
        }
        request.put("version", version);
        request.put("time", time);

        try {
            return sendAPICall("pack/" + pack.getSafeName() + "/timeplayed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        return false;
    }
}
