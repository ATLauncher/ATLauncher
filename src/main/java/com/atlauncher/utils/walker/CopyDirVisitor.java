package com.atlauncher.utils.walker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public final class CopyDirVisitor
extends SimpleFileVisitor<Path>{
    private final Path from;
    private final Path to;
    private final StandardCopyOption option;

    public CopyDirVisitor(Path from, Path to, StandardCopyOption option) {
        this.from = from;
        this.to = to;
        this.option = option;
    }

    public CopyDirVisitor(Path from, Path to){
        this(from, to, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
    throws IOException {
        Path target = this.to.resolve(this.from.relativize(dir));
        if(!Files.exists(target)){
            Files.createDirectory(target);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
    throws IOException{
        Files.copy(file, this.to.resolve(this.from.relativize(file)), this.option);
        return FileVisitResult.CONTINUE;
    }
}