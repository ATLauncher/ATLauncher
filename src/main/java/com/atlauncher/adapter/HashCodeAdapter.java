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

import com.atlauncher.utils.Hashing;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public final class HashCodeAdapter extends TypeAdapter<Hashing.HashCode> {
    @Override
    public void write(JsonWriter out, Hashing.HashCode value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public Hashing.HashCode read(JsonReader in) throws IOException {
        if (!in.hasNext()) {
            return Hashing.HashCode.EMPTY;
        }

        String next = in.nextString();
        if (next != null && !next.isEmpty()) {
            return Hashing.HashCode.fromString(next);
        }

        return Hashing.HashCode.EMPTY;
    }
}