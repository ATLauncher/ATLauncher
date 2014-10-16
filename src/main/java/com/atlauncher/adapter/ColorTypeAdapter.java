package com.atlauncher.adapter;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.Color;
import java.io.IOException;

/**
 * This class will ensure that colors are serialized to a hex value for easy editing
 */
public final class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter writer, Color c) throws IOException {
        writer.beginObject().name("value").value("#" + toHex(c)).endObject();
    }

    @Override
    public Color read(JsonReader reader) throws IOException {
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
