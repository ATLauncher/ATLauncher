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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.atlauncher.Gsons;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public final class MinecraftVersionUpdates {
    private MinecraftVersionUpdates() {
    }

    public static JsonObject changes(JsonObject installed, JsonObject previous, JsonObject latest,
        boolean hasLoader, Predicate<String> acceptsNewLibrary, Consumer<String> recordChange) {
        JsonObject changes = new JsonObject();
        MinecraftVersion latestVersion = Gsons.DEFAULT.fromJson(latest, MinecraftVersion.class);
        JsonObject updated = Gsons.DEFAULT.toJsonTree(latestVersion).getAsJsonObject();
        MinecraftVersion previousVersion = Gsons.DEFAULT.fromJson(previous, MinecraftVersion.class);
        JsonObject original = previousVersion == null ? null : Gsons.DEFAULT.toJsonTree(previousVersion).getAsJsonObject();

        for (String field : Arrays.asList("complianceLevel", "javaVersion", "type", "time", "releaseTime",
            "minimumLauncherVersion", "assetIndex", "assets", "rules", "logging")) {
            JsonElement value = installed.get(field);
            if (original != null ? Objects.equals(value, original.get(field)) : value != JsonNull.INSTANCE) {
                addChange(changes, field, value, updated.get(field));
            }
        }

        JsonElement downloads = installed.get("downloads");
        if (original == null) {
            if (downloads != JsonNull.INSTANCE) {
                addChange(changes, "downloads", downloads, updated.get("downloads"));
            }
        } else if (downloads != null && downloads.isJsonObject() && original.has("downloads")
            && updated.has("downloads")) {
            JsonObject merged = downloads.getAsJsonObject().deepCopy();
            JsonObject downloadChanges = new JsonObject();
            for (String kind : Arrays.asList("client", "server", "windows_server")) {
                if (Objects.equals(merged.get(kind), original.getAsJsonObject("downloads").get(kind))) {
                    addChange(downloadChanges, kind, merged.get(kind), updated.getAsJsonObject("downloads").get(kind));
                }
            }
            apply(merged, downloadChanges);
            addChange(changes, "downloads", downloads, merged);
        }

        if (original != null) {
            for (String field : Arrays.asList("mainClass", "minecraftArguments")) {
                JsonElement value = installed.get(field);
                if (Objects.equals(value, original.get(field))) {
                    addChange(changes, field, value, updated.get(field));
                }
            }
            JsonElement argumentsField = installed.get("arguments");
            if (argumentsField != null && argumentsField.isJsonObject()) {
                JsonObject installedArguments = argumentsField.getAsJsonObject();
                JsonObject arguments = mergeArguments(installedArguments, previousVersion.getBaseArguments(),
                    latestVersion.getBaseArguments(), hasLoader);
                addChange(changes, "arguments", installedArguments, arguments);
            }
        }

        changes.entrySet().forEach(entry -> describeFieldChanges(entry.getKey(),
            installed.get(entry.getKey()), entry.getValue(), recordChange));

        JsonArray libraries = installed.getAsJsonArray("libraries");
        if (libraries != null) {
            JsonArray newLibraries = updated.has("libraries") ? updated.getAsJsonArray("libraries") : new JsonArray();
            JsonArray oldLibraries = original == null ? null
                : original.has("libraries") ? original.getAsJsonArray("libraries") : new JsonArray();
            addChange(changes, "libraries", libraries,
                MinecraftLibraryUpdates.merge(libraries, oldLibraries, newLibraries, acceptsNewLibrary, recordChange));
        }
        return changes;
    }

    private static JsonObject mergeArguments(JsonObject installed, Arguments previous, Arguments latest,
        boolean hasLoader) {
        JsonObject result = installed.deepCopy();
        JsonObject oldBase = Gsons.DEFAULT.toJsonTree(previous).getAsJsonObject();
        JsonObject newBase = Gsons.DEFAULT.toJsonTree(latest).getAsJsonObject();

        boolean hasBaseArguments = Arrays.asList("game", "jvm").stream().anyMatch(kind -> {
            JsonArray base = oldBase.getAsJsonArray(kind);
            return !base.isEmpty() && startsWith(installed.getAsJsonArray(kind), base);
        });
        for (String kind : Arrays.asList("game", "jvm")) {
            JsonArray current = installed.getAsJsonArray(kind);
            JsonArray base = oldBase.getAsJsonArray(kind);
            if (startsWith(current, base) && (!base.isEmpty() || !hasLoader || hasBaseArguments)) {
                JsonArray replacement = newBase.getAsJsonArray(kind).deepCopy();
                for (int i = base.size(); i < current.size(); i++) {
                    replacement.add(current.get(i));
                }
                result.add(kind, replacement);
            }
        }
        return result;
    }

    private static boolean startsWith(JsonArray values, JsonArray prefix) {
        if (values == null || values.size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            if (!values.get(i).equals(prefix.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static void addChange(JsonObject changes, String field, JsonElement previous, JsonElement updated) {
        if (!Objects.equals(previous, updated)) {
            changes.add(field, updated == null ? JsonNull.INSTANCE : updated);
        }
    }

    static void describeFieldChanges(String field, JsonElement previous, JsonElement updated,
        Consumer<String> recordChange) {
        if (Objects.equals(previous, updated)) {
            return;
        }
        if (previous != null && previous.isJsonObject() && updated != null && updated.isJsonObject()) {
            JsonObject oldObject = previous.getAsJsonObject();
            JsonObject newObject = updated.getAsJsonObject();
            Set<String> keys = new LinkedHashSet<>(oldObject.keySet());
            keys.addAll(newObject.keySet());
            String prefix = field.isEmpty() ? "" : field + ".";
            for (String key : keys) {
                describeFieldChanges(prefix + key, oldObject.get(key), newObject.get(key), recordChange);
            }
        } else if (updated == null || updated.isJsonNull()) {
            recordChange.accept("Removed " + field);
        } else if (previous == null) {
            recordChange.accept("Added " + field);
        } else {
            recordChange.accept("Updated " + field);
        }
    }

    public static void apply(JsonObject settings, JsonObject changes) {
        changes.entrySet().forEach(entry -> {
            if (entry.getValue().isJsonNull()) {
                settings.remove(entry.getKey());
            } else {
                settings.add(entry.getKey(), entry.getValue());
            }
        });
    }
}
