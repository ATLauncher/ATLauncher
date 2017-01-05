/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

package com.atlauncher.adapter;

import java.awt.Color;
import java.io.IOException;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * This class will ensure that colors are serialized to a hex value for easy editing
 */
public final class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter writer, Color c) throws IOException {
        if (c == null) {
            writer.nullValue();
        } else {
            writer.beginObject().name("value").value("#" + toHex(c)).endObject();
        }
    }

    @Override
    public Color read(JsonReader reader) throws IOException {
        if (JsonToken.NULL.equals(reader.peek())) {
            return null;
        }
        
        Color ret = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String next = reader.nextName();
            if ("value".equals(next)) {
                String value = reader.nextString();
                if (value.startsWith("#")) {
                    // Custom color format in hex form
                    reader.endObject();
                    int[] rgb = toRGB(clamp(value.substring(1)));
                    ret = new Color(rgb[0], rgb[1], rgb[2]);
                } else {
                    // For compatibility with old java.awt.Color-based format
                    ret = new Color(Integer.parseInt(value));
                }
            } else if ("frgbvalue".equals(next) || "fvalue".equals(next) || "falpha".equals(next) || "cs".equals(next)) {
                // Ignore these, for compatibility with old java.awt.Color-based format
                reader.nextString();
            } else {
                throw new JsonParseException("Key " + next + " isn't a valid key");
            }
        }
        reader.endObject();
        if (ret == null) {
            throw new JsonParseException("Color object must contain a \"value\" key");
        } else {
            return ret;
        }
    }

    /**
     * Adds 0's to the end of a given hex code until it's length is 6 for use as a HTML colour code.
     *
     * @param hex The hex code to clamp to a length of 6 characters
     * @return The hex after claming to 6 characters length
     */
    private String clamp(String hex) {
        while (hex.length() < 6) {
            hex += '0';
        }
        return hex;
    }

    /**
     * Turns a hex colour code into a int array of length 3 with each element being the Red, Green and Blue individual
     * hex codes.
     * <p/>
     * <p/>
     * For example an input of "#FF00FF" returns {255, 0, 255}
     *
     * @param hex The hex code to convert to RGB format
     * @return The int array containing the RGB individual hex codes
     */
    private int[] toRGB(String hex) {
        int[] ret = new int[3];

        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }

        return ret;
    }

    /**
     * Converts a {@link Color} object into a hex code.
     * <p/>
     * <p/>
     * For example an input of Color.BLACK returns #FF00FF
     *
     * @param c The {@link Color} object to convert to hex
     * @return The hex string representing the given Color
     */
    private String toHex(Color c) {
        return Integer.toHexString(c.getRGB() & 0xFFFFFF);
    }
}
