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
package com.atlauncher.data.loaders.forge;

import com.atlauncher.Gsons;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class DownloadsTypeAdapter implements JsonDeserializer<Downloads> {
    @Override
    public Downloads deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        DownloadsItem artifact = null;

        final JsonObject rootJsonObject = json.getAsJsonObject();

        if (rootJsonObject.has("artifact")) {
            final JsonObject artifactObject = rootJsonObject.getAsJsonObject("artifact");
            artifact = Gsons.DEFAULT_ALT.fromJson(artifactObject, DownloadsItem.class);
        }

        return new Downloads(artifact);
    }
}
