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
package com.atlauncher.data.mojang;

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FileTypeAdapter extends TypeAdapter<File> {

    @Override
    public File read(JsonReader json) throws IOException {
        if (json.hasNext()) {
            String value = json.nextString();
            return value == null ? null : new File(value);
        }
        return null;
    }

    @Override
    public void write(JsonWriter json, File value) throws IOException {
        if (value == null) {
            json.nullValue();
        } else {
            json.value(value.getAbsolutePath());
        }
    }
}