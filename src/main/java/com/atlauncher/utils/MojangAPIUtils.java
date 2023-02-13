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
package com.atlauncher.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.mojang.api.NameHistory;
import com.atlauncher.data.mojang.api.ProfileResponse;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.network.DownloadException;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Various utility methods for interacting with the Mojang API.
 */
public class MojangAPIUtils {
    /**
     * Gets a UUID of a Minecraft account from a given username.
     *
     * @param username the username to get the UUID for
     * @return the UUID for the username given
     */
    public static String getUUID(String username) {
        ProfileResponse profile = Download.build().setUrl("https://api.mojang.com/users/profiles/minecraft/" + username)
                .asClass(ProfileResponse.class);

        return profile.getId();
    }

    public static boolean uploadSkin(AbstractAccount account, File skinPath, String skinType) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), skinPath);

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("variant", skinType)
                .addFormDataPart("file", "skin.png", requestBody)
                .build();

        try {
            Download.build().setUrl("https://api.minecraftservices.com/minecraft/profile/skins")
                    .header("Authorization", "Bearer " + account.getAccessToken())
                    .header("Content-Type", "multipart/form-data").post(body)
                    .asClassWithThrow(JsonObject.class);

            return true;
        } catch (DownloadException e) {
            LogManager.error("Error updating skin. Response was " + e.response);
        } catch (IOException e) {
            LogManager.logStackTrace("Error updating skin", e);
        }

        return false;
    }

    public static String getCurrentUsername(String uuid) {
        java.lang.reflect.Type type = new TypeToken<List<NameHistory>>() {
        }.getType();

        List<NameHistory> history = Download.build().setUrl("https://api.mojang.com/user/profiles/" + uuid + "/names")
                .asType(type);

        // Mojang API is down??
        if (history == null) {
            return null;
        }

        // If there is only 1 entry that means they haven't done a name change
        if (history.size() == 1) {
            return history.get(0).getName();
        }

        // The username of the latest name
        String username = null;

        // The time that the latest name change occurred
        long time = 0;

        for (NameHistory name : history) {
            if (!name.isAUsernameChange()) {
                username = name.getName();
            } else if (time < name.getChangedToAt()) {
                time = name.getChangedToAt();
                username = name.getName();
            }
        }

        // Just in case, this should never happen, but better to be safe I guess
        if (username == null) {
            return history.get(0).getName();
        }

        return username;
    }

    public static boolean checkUsernameAvailable(String accessToken, String username) {
        Request request = new Request.Builder()
                .url("https://api.minecraftservices.com/minecraft/profile/name/" + username + "/available")
                .get()
                .header("Authorization", "Bearer " + accessToken)
                .build();

        try (Response response = Network.CLIENT.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject jsonResponse = Gsons.DEFAULT.fromJson(responseBody, JsonObject.class);

                if (jsonResponse.get("status").getAsString().equals("AVAILABLE")) {
                    return true;
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to check username availability", e);
        }

        return false;
    }

    public static boolean createMcProfile(String accessToken, String username) {
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("profileName", username);

        RequestBody body = RequestBody.create(Gsons.DEFAULT.toJson(jsonBody),
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://api.minecraftservices.com/minecraft/profile")
                .post(body)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        try (Response response = Network.CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LogManager.error(response.body().string());
            }

            return response.isSuccessful();
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to create profile", e);
        }

        return false;
    }
}
