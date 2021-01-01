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
package com.atlauncher.data.minecraft.loaders.fabric;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class FabricMetaLauncherMetaTypeAdapter implements JsonDeserializer<FabricMetaLauncherMeta> {
    @Override
    public FabricMetaLauncherMeta deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        String version;
        Map<String, String> mainClass = new HashMap<>();
        Map<String, List<FabricLibrary>> libraries = new HashMap<>();

        final JsonObject rootJsonObject = json.getAsJsonObject();
        version = rootJsonObject.get("version").getAsString();

        if (rootJsonObject.get("mainClass").isJsonObject()) {
            final JsonObject mainClassObject = rootJsonObject.getAsJsonObject("mainClass");

            mainClass.put("client", mainClassObject.get("client").getAsString());
            mainClass.put("server", mainClassObject.get("server").getAsString());
        } else {
            String mainClassString = rootJsonObject.get("mainClass").getAsString();

            mainClass.put("client", mainClassString);
            mainClass.put("server", mainClassString);
        }

        final JsonObject librariesObject = rootJsonObject.getAsJsonObject("libraries");

        List<FabricLibrary> clientLibraries = new ArrayList<>();
        final JsonArray clientLibrariesArray = librariesObject.getAsJsonArray("client");
        for (JsonElement libraryElement : clientLibrariesArray) {
            if (libraryElement.getAsJsonObject().has("name") && libraryElement.getAsJsonObject().has("url")) {
                clientLibraries.add(new FabricLibrary(libraryElement.getAsJsonObject().get("name").getAsString(),
                        libraryElement.getAsJsonObject().get("url").getAsString()));
            }
        }
        libraries.put("client", clientLibraries);

        List<FabricLibrary> commonLibraries = new ArrayList<>();
        final JsonArray commonLibrariesArray = librariesObject.getAsJsonArray("common");
        for (JsonElement libraryElement : commonLibrariesArray) {
            if (libraryElement.getAsJsonObject().has("name") && libraryElement.getAsJsonObject().has("url")) {
                commonLibraries.add(new FabricLibrary(libraryElement.getAsJsonObject().get("name").getAsString(),
                        libraryElement.getAsJsonObject().get("url").getAsString()));
            }
        }
        libraries.put("common", commonLibraries);

        List<FabricLibrary> serverLibraries = new ArrayList<>();
        final JsonArray serverLibrariesArray = librariesObject.getAsJsonArray("server");
        for (JsonElement libraryElement : serverLibrariesArray) {
            if (libraryElement.getAsJsonObject().has("name") && libraryElement.getAsJsonObject().has("url")) {
                serverLibraries.add(new FabricLibrary(libraryElement.getAsJsonObject().get("name").getAsString(),
                        libraryElement.getAsJsonObject().get("url").getAsString()));
            }
        }
        libraries.put("server", serverLibraries);

        return new FabricMetaLauncherMeta(version, mainClass, libraries);
    }
}
