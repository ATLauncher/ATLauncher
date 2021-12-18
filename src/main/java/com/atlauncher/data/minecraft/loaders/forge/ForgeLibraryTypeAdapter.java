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
package com.atlauncher.data.minecraft.loaders.forge;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import okhttp3.Request;
import okhttp3.Response;

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
                // forge installer provides this out the zip, but when the file is removed from
                // shared libraries, we need to change the url to grab the launcher jar (same)
                library.downloads.artifact.url = Constants.FORGE_MAVEN_BASE
                        + library.downloads.artifact.path.replace(".jar", "-launcher.jar");
            }

            // use our own mirror for Forge downloads
            if (library.downloads.artifact.url.contains(Constants.FORGE_MAVEN_BASE)) {
                library.downloads.artifact.url = library.downloads.artifact.url.replace(Constants.FORGE_MAVEN_BASE,
                        Constants.DOWNLOAD_SERVER + "/maven/");
            }
        } else {
            if (object.has("checksums")) {
                library.checksums = new Gson().fromJson(object.get("checksums").getAsJsonArray(),
                        new TypeToken<List<String>>() {
                        }.getType());
            }

            Downloads downloads = new Downloads();
            Download artifact = new Download();

            // older forge versions dont distinguish between non native and native
            // libraries, this is defintely hacky but should be fine to just ignore them
            if (object.has("natives")) {
                return null;
            } else {
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
                        String url = object.get("url").getAsString();

                        if (url.equalsIgnoreCase(Constants.FORGE_MAVEN_BASE)
                                || url.equalsIgnoreCase(Constants.FORGE_MAVEN_BASE.replace("https://", "http://"))
                                || url.equalsIgnoreCase(Constants.FORGE_OLD_MAVEN_BASE) || url.equalsIgnoreCase(
                                        Constants.FORGE_OLD_MAVEN_BASE.replace("https://", "http://"))) {
                            // make sure it exists first before blindly using it for old Forge
                            try {
                                Request.Builder headRequestBuilder = new Request.Builder()
                                        .url(Constants.DOWNLOAD_SERVER + "/maven/" + artifact.path).head();
                                Response headResponse = Network.CLIENT.newCall(headRequestBuilder.build()).execute();

                                if (headResponse.code() == 200) {
                                    url = Constants.DOWNLOAD_SERVER + "/maven/";
                                }
                            } catch (IOException ignored) {
                            }
                        }

                        artifact.url = url + artifact.path;
                    }
                } else {
                    artifact.url = Constants.MINECRAFT_LIBRARIES + artifact.path;
                }

                // library is missing this information, so grab it from the url
                if (artifact.size == -1L || artifact.sha1 == null) {
                    Path downloadedLibrary = FileSystem.LIBRARIES.resolve(artifact.path);

                    try {
                        // if the file exists, assume it's good. This is only needed for older Forge
                        // versions anyway, so should be okay :finger_crossed:
                        if (Files.exists(downloadedLibrary)) {
                            artifact.size = Files.size(downloadedLibrary);
                            artifact.sha1 = Hashing.sha1(downloadedLibrary).toString();
                        }
                    } catch (Throwable t) {
                        LogManager.logStackTrace(t);
                    }
                }

                downloads.artifact = artifact;

                library.downloads = downloads;
            }

            if (object.has("clientreq")) {
                library.clientreq = object.get("clientreq").getAsBoolean();
            }

            if (object.has("serverreq")) {
                library.serverreq = object.get("serverreq").getAsBoolean();
            }
        }

        // fix Log4J exploits
        library.fixLog4jVersion();

        return library;
    }
}
