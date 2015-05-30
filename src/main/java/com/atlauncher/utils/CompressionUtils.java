package com.atlauncher.utils;

import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.walker.UnzipVisitor;
import com.atlauncher.utils.walker.ZipVisitor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class CompressionUtils{
    public static void unzip(Path zip, Path dest)
    throws IOException {
        if(Files.exists(dest)){
            FileUtils.delete(dest);
        }

        if(Files.notExists(dest)){
            LogManager.warn(dest.toString() + " doesn't exist, Creating");
            FileUtils.createDirectory(dest);
        }

        try(FileSystem zipfs = createZipFileSystem(zip, false)){
            Path root = zipfs.getPath("/");
            Files.walkFileTree(root, new UnzipVisitor(dest));
        }
    }

    public static void zip(Path zip, Path dir)
    throws IOException {
        if(Files.exists(zip)){
            FileUtils.delete(zip);
        }

        if(!Files.isDirectory(dir)){
            throw new IllegalStateException("File " + dir.toString() + " isnt a directory");
        }

        try(FileSystem zipfs = createZipFileSystem(zip, true)){
            Files.walkFileTree(dir, new ZipVisitor(zipfs, dir));
        }
    }

    private static FileSystem createZipFileSystem(Path zip, boolean create)
    throws IOException {
        URI uri = URI.create("jar:file:" + zip.toUri().getPath());

        Map<String, String> env = new HashMap<>();

        if(create){
            env.put("create", "true");
        }

        return FileSystems.newFileSystem(uri, env);
    }
}