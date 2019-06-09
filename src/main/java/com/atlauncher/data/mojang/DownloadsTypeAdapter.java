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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.atlauncher.Gsons;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class DownloadsTypeAdapter implements JsonDeserializer<Downloads> {
    @Override
    public Downloads deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        DownloadsItem artifact = null;
        Map<String, DownloadsItem> classifiers = new HashMap<String, DownloadsItem>();

        final JsonObject rootJsonObject = json.getAsJsonObject();

        if (rootJsonObject.has("artifact")) {
            final JsonObject artifactObject = rootJsonObject.getAsJsonObject("artifact");
            artifact = Gsons.DEFAULT_ALT.fromJson(artifactObject, DownloadsItem.class);
        }

        if (rootJsonObject.has("classifiers")) {
            final JsonObject classifiersObject = rootJsonObject.getAsJsonObject("classifiers");

            Set<Entry<String, JsonElement>> entrySet = classifiersObject.entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                classifiers.put(entry.getKey(),
                        Gsons.DEFAULT_ALT.fromJson(entry.getValue().getAsJsonObject(), DownloadsItem.class));
            }
        }

        return new Downloads(artifact, classifiers);
    }
}
