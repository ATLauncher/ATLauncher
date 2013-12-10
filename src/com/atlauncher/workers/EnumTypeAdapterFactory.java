/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.workers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class EnumTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!type.getRawType().isEnum())
            return null;
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
                if (name == null)
                    return null;
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
