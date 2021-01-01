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
package com.atlauncher.data.mojang.api;

import java.util.Map;

import com.atlauncher.annot.Json;
import com.atlauncher.managers.LogManager;

@Json
public class UserProperty {
    private long timestamp;
    private String profileId;
    private String profileName;
    private boolean isPublic;
    private Map<String, ProfileTexture> textures;

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getProfileId() {
        return this.profileId;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public ProfileTexture getTexture(String name) {
        if (!textures.containsKey(name)) {
            LogManager.error("No texture " + name + " for account " + this.profileName);
            return null;
        }

        return textures.get(name);
    }
}
