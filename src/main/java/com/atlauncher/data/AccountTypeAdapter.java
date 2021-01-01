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

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AccountTypeAdapter implements JsonSerializer<AbstractAccount>, JsonDeserializer<AbstractAccount> {
    @Override
    public final JsonElement serialize(final AbstractAccount object, final Type interfaceType,
            final JsonSerializationContext context) {
        final JsonObject rootJsonObject = context.serialize(object).getAsJsonObject();

        rootJsonObject.addProperty("internalType", object.getClass().getName());

        return rootJsonObject;
    }

    @Override
    public AbstractAccount deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        Type actualType;
        try {
            actualType = Class.forName(json.getAsJsonObject().get("internalType").getAsString());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }

        return context.deserialize(json, actualType);
    }
}
