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
package com.atlauncher.data;

import java.lang.reflect.Type;

import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class PackVersionTypeAdapter implements JsonDeserializer<PackVersion> {
    @Override
    public PackVersion deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        PackVersion packVersion = new PackVersion();

        final JsonObject rootJsonObject = json.getAsJsonObject();

        if (rootJsonObject.has("version")) {
            packVersion.version = rootJsonObject.get("version").getAsString();
        }

        if (rootJsonObject.has("hash")) {
            packVersion.hash = rootJsonObject.get("hash").getAsString();
            packVersion.isDev = true;
        }

        if (rootJsonObject.has("minecraft")) {
            try {
                packVersion.minecraftVersion = MinecraftManager
                        .getMinecraftVersion(rootJsonObject.get("minecraft").getAsString());
            } catch (InvalidMinecraftVersion e) {
                LogManager.error(e.getMessage());
            }
        }

        if (rootJsonObject.has("canUpdate")) {
            packVersion.canUpdate = rootJsonObject.get("canUpdate").getAsBoolean();
        }

        if (rootJsonObject.has("isRecommended")) {
            packVersion.isRecommended = rootJsonObject.get("isRecommended").getAsBoolean();
        }

        if (rootJsonObject.has("hasLoader")) {
            packVersion.hasLoader = rootJsonObject.get("hasLoader").getAsBoolean();
        }

        if (rootJsonObject.has("hasChoosableLoader")) {
            packVersion.hasChoosableLoader = rootJsonObject.get("hasChoosableLoader").getAsBoolean();
        }

        return packVersion;
    }
}
