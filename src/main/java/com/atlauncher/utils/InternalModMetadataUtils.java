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
package com.atlauncher.utils;

import java.io.File;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import com.atlauncher.Gsons;
import com.atlauncher.data.minecraft.metadata.FabricMod;
import com.atlauncher.data.minecraft.metadata.MCMod;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.moandjiezana.toml.Toml;

public class InternalModMetadataUtils {

    public static String getRawInternalModMetadata(File file, String filename) {
        String data = null;
        if (ArchiveUtils.archiveContainsFile(file.toPath(), filename)) {
            data = ArchiveUtils.getFile(file.toPath(), filename);
        }

        return data;
    }

    public static MCMod getMCModForFile(File file) {
        try {
            java.lang.reflect.Type type = new TypeToken<List<MCMod>>() {
            }.getType();

            List<MCMod> mods = Gsons.MINECRAFT.fromJson(getRawInternalModMetadata(file, "mcmod.info"), type);

            if (mods.size() != 0 && mods.get(0) != null) {
                return mods.get(0);
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    public static FabricMod parseFabricModForFile(File file) {
        try {
            FabricMod mod = Gsons.MINECRAFT.fromJson(getRawInternalModMetadata(file, "fabric.mod.json"),
                    FabricMod.class);

            if (mod != null) {
                return mod;
            }
        } catch (Exception ignored2) {

        }

        return null;
    }

    public static JsonObject parseMcModInfoFile(String data) {
        try {
            return Gsons.MINECRAFT.fromJson(data, JsonObject.class);
        } catch (Exception ignored) {

        }

        return null;
    }

    public static JsonObject parseFabricModFile(String data) {
        try {
            return Gsons.MINECRAFT.fromJson(data, JsonObject.class);
        } catch (Exception ignored) {

        }

        return null;
    }

    public static JsonObject parseQuiltModFile(String data) {
        try {
            return Gsons.MINECRAFT.fromJson(data, JsonObject.class);
        } catch (Exception ignored) {

        }

        return null;
    }

    public static Toml parseModsTomlFile(String data) {
        try {
            return new Toml().read(data);
        } catch (Exception ignored) {

        }

        return null;
    }

    public static Properties parseManifestMfFile(String data) {
        try {
            Properties props = new Properties();
            props.load(new StringReader(data));
            return props;
        } catch (Exception ignored) {

        }

        return null;
    }

}
