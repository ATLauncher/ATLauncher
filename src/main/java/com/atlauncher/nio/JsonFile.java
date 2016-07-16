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
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static <T> T of(String name, Type type) throws Exception {
        return create(name).convert(type);
    }

    public static JsonFile create(String name) throws Exception {
        return new JsonFile(FileSystem.JSON.resolve(name));
    }

    private final Path path;

    public JsonFile(Path path) throws FileNotFoundException {
        this(path, false);
    }

    public JsonFile(Path path, boolean write) throws FileNotFoundException {
        if (!Files.exists(path) && !write) {
            throw new FileNotFoundException("File " + path + " wasn't found");
        }

        this.path = path;
    }

    public void write(Object obj) throws Exception {
        write(Gsons.DEFAULT, obj);
    }

    public void write(Gson gson, Object obj) throws Exception {
        OutputStream os = Files.newOutputStream(this.path, StandardOpenOption.WRITE);
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

        gson.toJson(obj, osw);
        
        osw.flush();
        osw.close();

        os.flush();
        os.close();
    }

    public <T> T convert(Class<T> tClass) throws Exception {
        return convert(Gsons.DEFAULT, tClass);
    }

    public <T> T convert(Type type) throws Exception {
        return convert(Gsons.DEFAULT, type);
    }

    public <T> T convert(Gson gson, Class<T> tClass) throws Exception {
        try (InputStream stream = Files.newInputStream(this.path)) {
            return gson.fromJson(new InputStreamReader(stream, "UTF-8"), tClass);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    public <T> T convert(Gson gson, Type type) throws Exception {
        try (InputStream stream = Files.newInputStream(this.path)) {
            return gson.fromJson(new InputStreamReader(stream, "UTF-8"), type);
        }
    }
}