package com.atlauncher.utils.walker;

import com.atlauncher.utils.FileUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class UnzipVisitor
extends SimpleFileVisitor<Path>{
    private final Path dest;

    public UnzipVisitor(Path dest){
        this.dest = dest;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException {
        Path out = Paths.get(this.dest.toString(), file.toString()); // this.dest.resolve() doesn't work for some reason
        Files.copy(file, out, StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    throws IOException {
        Path d = this.dest.resolve(dir.toString());

        if(Files.notExists(d)){
            FileUtils.createDirectory(d);
        }

        return FileVisitResult.CONTINUE;
    }
}