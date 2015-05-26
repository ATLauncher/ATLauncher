package com.atlauncher.nio;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class JsonFile{
    public static <T> T of(String name, Class<T> tClass)
    throws Exception{
        return create(name).convert(tClass);
    }

    public static <T> T of(String name, Type t)
    throws Exception{
        return create(name).convert(t);
    }

    public static JsonFile create(String name)
    throws Exception{
        return new JsonFile(FileSystem.JSON.resolve(name));
    }

    private final Path p;

    public JsonFile(Path p)
    throws FileNotFoundException{
        if(!Files.exists(p)){
            throw new FileNotFoundException("File " + p + " wasn't found");
        }

        this.p = p;
    }

    public void write(Object obj)
    throws Exception{
        try(OutputStream os = Files.newOutputStream(this.p, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW)){
            Gsons.DEFAULT.toJson(obj, new OutputStreamWriter(os));
        }
    }

    public <T> T convert(Class<T> tClass)
    throws Exception{
        return Gsons.DEFAULT.fromJson(new InputStreamReader(Files.newInputStream(this.p)), tClass);
    }

    public <T> T convert(Type t)
    throws Exception{
        return Gsons.DEFAULT.fromJson(new InputStreamReader(Files.newInputStream(this.p)), t);
    }
}