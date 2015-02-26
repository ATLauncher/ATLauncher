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
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.mojang.api.ProfileResponse;

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
        Downloadable downloadable = new Downloadable("https://api.mojang.com/users/profiles/minecraft/" + username,
                false);

        ProfileResponse profile = Gsons.DEFAULT.fromJson(downloadable.getContents(), ProfileResponse.class);

        return profile.getId();
    }
}
