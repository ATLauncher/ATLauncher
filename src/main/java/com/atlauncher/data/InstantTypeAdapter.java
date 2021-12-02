package com.atlauncher.data;

import com.google.common.base.Preconditions;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

public final class InstantTypeAdapter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Preconditions.checkArgument((json instanceof JsonPrimitive));
        final long epochTime = json.getAsLong();
        return Instant.ofEpochMilli(epochTime);
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context){
        final long epochTime = src.toEpochMilli();
        return new JsonPrimitive(epochTime);
    }
}