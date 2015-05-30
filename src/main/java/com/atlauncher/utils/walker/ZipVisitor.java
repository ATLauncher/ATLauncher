package com.atlauncher.utils.walker;

import com.atlauncher.utils.FileUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class ZipVisitor
extends SimpleFileVisitor<Path>{
    private final FileSystem fs;
    private final Path root;
    private final Path dir;

    public ZipVisitor(FileSystem fs, Path dir)
    throws IOException {
        this.fs = fs;
        this.dir = dir;
        this.root = fs.getPath("/");
        if(Files.notExists(this.root)){
            FileUtils.createDirectory(this.root);
        }
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException {
        String name = this.dir.relativize(file).toString();
        if(name.endsWith("aux_class")){
            name = "aux.class";
        }
        Path dest = this.fs.getPath(name);
        Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    throws IOException {
        String path = this.dir.relativize(dir).toString();
        if(path.isEmpty()){
            return FileVisitResult.CONTINUE;
        }

        Path d = this.fs.getPath(this.dir.relativize(dir).toString());
        if(Files.notExists(d)){
            FileUtils.createDirectory(d);
        }

        return FileVisitResult.CONTINUE;
    }
}