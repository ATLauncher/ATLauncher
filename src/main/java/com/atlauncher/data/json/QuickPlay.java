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
import com.atlauncher.data.QuickPlayOption;

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
     */
    public static QuickPlay getDefault() {
        return new QuickPlay(null, null, null);
    }

    public final String serverAddress;
    public final String worldName;
    // TODO: Should we rename the realmId (--quickPlayRealms "1234")? if yes then make sure to rename it from all places
    public final String realmId;

    public QuickPlay(String serverAddress, String worldName, String realmId) {
        this.serverAddress = serverAddress;
        this.worldName = worldName;
        this.realmId = realmId;
    }

    /**
     * @return The current/selected quick play option based on the data in this data class
     */
    public QuickPlayOption getSelectedQuickPlayOption() {
        if (serverAddress != null) {
            return QuickPlayOption.multiPlayer;
        }
        if (worldName != null) {
            return QuickPlayOption.singlePlayer;
        }
        if (realmId != null) {
            return QuickPlayOption.realm;
        }
        return QuickPlayOption.disabled;
    }
}
