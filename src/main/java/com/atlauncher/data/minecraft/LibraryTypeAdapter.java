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
package com.atlauncher.data.minecraft;

import java.lang.reflect.Type;

import com.atlauncher.data.minecraft.loaders.forge.ForgeLibrary;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class LibraryTypeAdapter implements JsonDeserializer<Library> {
    @Override
    public Library deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        Library library;

        if (object.has("checksums")) {
            library = new Gson().fromJson(object, ForgeLibrary.class);
        } else {
            library = new Gson().fromJson(object, Library.class);

            if (library.name.contains("forge") && library.downloads.artifact.url.endsWith("-launcher.jar")) {
                library.downloads.artifact.url = library.downloads.artifact.url.replace("-launcher.jar", ".jar");
            }
        }

        return library;
    }
}
