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
package com.atlauncher.data;

import java.awt.Color;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ColorTypeAdapter implements JsonDeserializer<Color>, JsonSerializer<Color> {

    @Override
    public Color deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(json instanceof JsonObject)) {
            throw new JsonParseException("The color " + json + " is not an object!");
        }

        if (!json.getAsJsonObject().has("value")) {
            throw new JsonParseException("The color " + json + " has no value!");
        }

        Color color = new Color(json.getAsJsonObject().get("value").getAsInt());

        return color;
    }

    @Override
    public JsonElement serialize(Color value, Type type, JsonSerializationContext context) {
        JsonObject colorObject = new JsonObject();
        colorObject.add("value", new JsonPrimitive(value.getRGB()));
        colorObject.add("falpha", new JsonPrimitive(0.0f)); // seems to be no way to get this easily
        return colorObject;
    }
}
