/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

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