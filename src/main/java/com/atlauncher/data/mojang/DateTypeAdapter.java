/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTypeAdapter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    private final DateFormat enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
    private final DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Override
    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive)) {
            throw new JsonParseException("The date " + json + " is not a string!");
        }
        if (type != Date.class) {
            throw new IllegalArgumentException(getClass() + " cannot deserialize to " + type);
        }
        String value = json.getAsString();
        synchronized (enUsFormat) {
            try {
                return enUsFormat.parse(value);
            } catch (ParseException e) {
                try {
                    return iso8601Format.parse(value);
                } catch (ParseException e2) {
                    try {
                        String tmp = value.replace("Z", "+00:00");
                        return iso8601Format.parse(tmp.substring(0, 22) + tmp.substring(23));
                    } catch (ParseException e3) {
                        throw new JsonSyntaxException("Invalid date " + value, e3);
                    }
                }
            }
        }
    }

    @Override
    public JsonElement serialize(Date value, Type type, JsonSerializationContext context) {
        synchronized (enUsFormat) {
            String ret = this.iso8601Format.format(value);
            return new JsonPrimitive(ret.substring(0, 22) + ":" + ret.substring(22));
        }
    }
}