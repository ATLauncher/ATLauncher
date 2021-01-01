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

import java.io.IOException;
import java.util.List;

import com.atlauncher.annot.Json;
import com.atlauncher.managers.LogManager;

@Json
public class MinecraftProfileResponse {
    private String id;
    private String name;
    private List<UserPropertyRaw> properties;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasProperties() {
        return this.properties != null;
    }

    public UserProperty getUserProperty(String name) {
        for (UserPropertyRaw property : this.properties) {
            if (property.getName().equals(name)) {
                try {
                    return property.parse();
                } catch (IOException e) {
                    LogManager.logStackTrace("Error parsing user property " + name + " for username " + name, e);
                }
            }
        }
        return null;
    }
}
