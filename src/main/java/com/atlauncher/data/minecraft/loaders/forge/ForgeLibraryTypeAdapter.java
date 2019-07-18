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
package com.atlauncher.data.minecraft.loaders.forge;

import java.lang.reflect.Type;
import java.util.List;

import com.atlauncher.data.Constants;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class ForgeLibraryTypeAdapter implements JsonDeserializer<ForgeLibrary> {
    @Override
    public ForgeLibrary deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        ForgeLibrary library = new ForgeLibrary();

        JsonObject object = json.getAsJsonObject();

        library.name = object.get("name").getAsString();

        // forge 1.13 and newer already has this in the correct format
        if (object.has("downloads")) {
            library.downloads = new Gson().fromJson(object.get("downloads").getAsJsonObject(), Downloads.class);

            if (library.downloads.artifact.url.isEmpty()) {
                library.downloads.artifact.url = Constants.FORGE_MAVEN + library.downloads.artifact.path;
            }
        } else {
            Downloads downloads = new Downloads();
            Download artifact = new Download();

            if (object.has("checksums")) {
                library.checksums = new Gson().fromJson(object.get("checksums").getAsJsonArray(),
                        new TypeToken<List<String>>() {
                        }.getType());
            }

            if (library.checksums != null && library.checksums.size() == 1) {
                artifact.sha1 = library.checksums.get(0);
            }

            artifact.path = Utils.convertMavenIdentifierToPath(object.get("name").getAsString());

            if (library.name.startsWith("net.minecraftforge:forge:") && !object.has("clientreq")
                    && !object.has("serverreq") && !object.has("checksums")) {
                artifact.path = artifact.path.substring(0, artifact.path.lastIndexOf(".jar")) + "-universal.jar";
            }

            if (object.has("url")) {
                if (object.get("url").getAsString().isEmpty()) {
                    artifact.url = Constants.FORGE_MAVEN + artifact.path;
                } else {
                    artifact.url = object.get("url").getAsString() + artifact.path;
                }
            } else {
                artifact.url = Constants.MINECRAFT_LIBRARIES + artifact.path;
            }

            downloads.artifact = artifact;

            library.downloads = downloads;

            if (object.has("clientreq")) {
                library.clientreq = object.get("clientreq").getAsBoolean();
            }

            if (object.has("serverreq")) {
                library.serverreq = object.get("serverreq").getAsBoolean();
            }
        }

        return library;
    }
}
