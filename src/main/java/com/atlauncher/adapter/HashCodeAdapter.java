package com.atlauncher.adapter;

import com.atlauncher.utils.Hashing;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class HashCodeAdapter
extends TypeAdapter<Hashing.HashCode> {
    @Override
    public void write(JsonWriter out, Hashing.HashCode value)
    throws IOException {
        out.value(value.toString());
    }

    @Override
    public Hashing.HashCode read(JsonReader in)
    throws IOException {
        if(!in.hasNext()){
            return Hashing.HashCode.EMPTY;
        }

        String next = in.nextString();
        if(next != null && !next.isEmpty()){
            return Hashing.HashCode.fromString(next);
        }

        return Hashing.HashCode.EMPTY;
    }
}