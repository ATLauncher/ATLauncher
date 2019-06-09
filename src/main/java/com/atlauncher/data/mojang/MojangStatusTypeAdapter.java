/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.data.mojang;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class MojangStatusTypeAdapter implements JsonDeserializer<MojangStatus> {
    @Override
    public MojangStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String authServer = null;
        String sessionServer = null;

        for (JsonElement status : json.getAsJsonArray()) {
            Set<Map.Entry<String, JsonElement>> entrySet = status.getAsJsonObject().entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getKey().equalsIgnoreCase("authserver.mojang.com")) {
                    authServer = entry.getValue().getAsString();
                    continue;
                }

                if (entry.getKey().equalsIgnoreCase("sessionserver.mojang.com")) {
                    sessionServer = entry.getValue().getAsString();
                    continue;
                }
            }
        }

        return new MojangStatus(authServer, sessionServer);
    }
}
