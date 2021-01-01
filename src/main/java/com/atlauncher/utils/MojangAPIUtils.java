/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.util.List;

import com.atlauncher.data.mojang.api.NameHistory;
import com.atlauncher.data.mojang.api.ProfileResponse;
import com.atlauncher.network.Download;
import com.google.gson.reflect.TypeToken;

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
}
