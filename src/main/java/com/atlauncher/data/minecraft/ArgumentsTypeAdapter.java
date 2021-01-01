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
package com.atlauncher.data.minecraft;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.Gsons;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ArgumentsTypeAdapter implements JsonDeserializer<Arguments>, JsonSerializer<Arguments> {
    @Override
    public Arguments deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        List<ArgumentRule> game = new ArrayList<>();
        List<ArgumentRule> jvm = new ArrayList<>();

        final JsonObject rootJsonObject = json.getAsJsonObject();

        if (rootJsonObject.has("game")) {
            final JsonArray gameArray = rootJsonObject.getAsJsonArray("game");
            for (JsonElement gameArg : gameArray) {
                if (gameArg.isJsonObject()) {
                    JsonObject argument = gameArg.getAsJsonObject();
                    game.add(Gsons.DEFAULT_ALT.fromJson(argument, ArgumentRule.class));
                } else {
                    String argument = gameArg.getAsString();
                    game.add(new ArgumentRule(null, argument));
                }
            }
        }

        if (rootJsonObject.has("jvm")) {
            final JsonArray jvmArray = rootJsonObject.getAsJsonArray("jvm");
            for (JsonElement jvmArg : jvmArray) {
                if (jvmArg.isJsonObject()) {
                    JsonObject argument = jvmArg.getAsJsonObject();
                    jvm.add(Gsons.DEFAULT_ALT.fromJson(argument, ArgumentRule.class));
                } else {
                    String argument = jvmArg.getAsString();
                    jvm.add(new ArgumentRule(null, argument));
                }
            }
        }

        return new Arguments(game, jvm);
    }

    @Override
    public JsonElement serialize(Arguments arguments, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();

        if (arguments.game != null) {
            JsonArray gameArguments = new JsonArray();

            arguments.game.forEach(arg -> {
                if (arg.rules != null) {
                    JsonObject object = new JsonObject();

                    object.add("rules", Gsons.MINECRAFT.toJsonTree(arg.rules));

                    if (arg.value instanceof String || arg.value instanceof List) {
                        object.add("value", Gsons.MINECRAFT.toJsonTree(arg.value));
                    }

                    gameArguments.add(object);
                } else {
                    gameArguments.add((String) arg.value);
                }
            });

            root.add("game", gameArguments);
        }

        if (arguments.jvm != null) {
            JsonArray jvmArguments = new JsonArray();

            arguments.jvm.forEach(arg -> {
                if (arg.rules != null) {
                    JsonObject object = new JsonObject();

                    object.add("rules", Gsons.MINECRAFT.toJsonTree(arg.rules));

                    if (arg.value instanceof String || arg.value instanceof List) {
                        object.add("value", Gsons.MINECRAFT.toJsonTree(arg.value));
                    }

                    jvmArguments.add(object);
                } else {
                    jvmArguments.add((String) arg.value);
                }
            });

            root.add("jvm", jvmArguments);
        }

        return root;
    }
}
