/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnumTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!type.getRawType().isEnum()) {
            return null;
        }
        final Map<String, T> map = new HashMap<String, T>();
        for (T c : (T[]) type.getRawType().getEnumConstants()) {
            map.put(c.toString().toLowerCase(Locale.US), c);
        }

        return new TypeAdapter<T>() {
            @Override
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                String name = reader.nextString();
                if (name == null) {
                    return null;
                }
                return map.get(name.toLowerCase(Locale.US));
            }

            @Override
            public void write(JsonWriter writer, T value) throws IOException {
                if (value == null) {
                    writer.nullValue();
                } else {
                    writer.value(value.toString().toLowerCase(Locale.US));
                }
            }
        };
    }
}
