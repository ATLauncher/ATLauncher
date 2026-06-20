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
package com.atlauncher.data.minecraft.loaders.legacyfabric;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.minecraft.Rule;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class LegacyFabricLibraryTypeAdapter implements JsonDeserializer<LegacyFabricLibrary> {
    @Override
    public LegacyFabricLibrary deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        String url = object.has("url") ? object.get("url").getAsString() : null;

        Map<String, String> natives = object.has("natives")
                ? new Gson().fromJson(object.get("natives"), new TypeToken<Map<String, String>>() {
                }.getType())
                : null;

        List<Rule> rules = object.has("rules")
                ? new Gson().fromJson(object.get("rules"), new TypeToken<List<Rule>>() {
                }.getType())
                : null;

        return new LegacyFabricLibrary(object.get("name").getAsString(), url, natives, rules);
    }
}
