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
package com.atlauncher.data.json;

import com.atlauncher.annot.Json;

/**
 * A data class that contains information about the quick play feature
 * Either one of the fields should be not null or all of them should be null to disable the quick play feature.
 * <p>
 * This data is specific to ATLauncher and doesn't confirm or have the same underline data as Minecraft Launcher
 */
@Json
public class QuickPlay {

    /**
     * The default value is null to all the properties
     * */
    public static QuickPlay getDefault() {
        return new QuickPlay(null, null);
    }

    private final String serverAddress;
    private final String worldName;
    // TODO: Add support for realms quick play later

    public QuickPlay(String serverAddress, String worldName) {
        this.serverAddress = serverAddress;
        this.worldName = worldName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public String getWorldName() {
        return worldName;
    }
}
