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
package com.atlauncher.nio;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class JsonFile {
    public static <T> T of(String name, Class<T> tClass) throws Exception {
        return create(name).convert(tClass);
    }

    public static <T> T of(String name, Type t) throws Exception {
        return create(name).convert(t);
    }

    public static JsonFile create(String name) throws Exception {
        return new JsonFile(FileSystem.JSON.resolve(name));
    }

    private final Path p;

    public JsonFile(Path p) throws FileNotFoundException {
        if (!Files.exists(p)) {
            throw new FileNotFoundException("File " + p + " wasn't found");
        }

        this.p = p;
    }

    public void write(Object obj) throws Exception {
        try (OutputStream os = Files.newOutputStream(this.p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)) {
            Gsons.DEFAULT.toJson(obj, new OutputStreamWriter(os));
        }
    }

    public <T> T convert(Class<T> tClass) throws Exception {
        return convert(Gsons.DEFAULT, tClass);
    }

    public <T> T convert(Type t) throws Exception {
        return convert(Gsons.DEFAULT, t);
    }

    public <T> T convert(Gson gson, Class<T> tClass) throws Exception {
        return gson.fromJson(new String(Files.readAllBytes(this.p)), tClass);
    }

    public <T> T convert(Gson gson, Type t) throws Exception {
        return gson.fromJson(new String(Files.readAllBytes(this.p)), t);
    }
}