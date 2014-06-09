package com.atlauncher.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.*;
import java.io.IOException;

/**
 * This class will ensure that colors are serialized to a hex value for easy editing
 */
public final class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter writer, Color c)
            throws IOException {
        writer.beginObject()
                .name("value")
                .value("#" + toHex(c))
                .endObject();
    }

    @Override
    public Color read(JsonReader reader)
            throws IOException {
        reader.beginObject();
        String next = reader.nextName();
        if (!next.equalsIgnoreCase("value")) {
            throw new JsonParseException("Key " + next + " isnt a valid key");
        }
        String hex = reader.nextString();
        reader.endObject();
        int[] rgb = toRGB(clamp(hex.substring(1)));
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    private String clamp(String hex){
        while(hex.length() < "000000".length()){
            hex += '0';
        }
        return hex;
    }

    private int[] toRGB(String hex) {
        int[] ret = new int[3];

        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }

        return ret;
    }

    private String toHex(Color c) {
        return Integer.toHexString(c.getRGB() & 0xFFFFFF);
    }
}