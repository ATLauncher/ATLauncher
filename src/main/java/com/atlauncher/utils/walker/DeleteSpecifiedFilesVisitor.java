package com.atlauncher.utils.walker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public final class DeleteSpecifiedFilesVisitor extends SimpleFileVisitor<Path> {
    List<String> files;

    public DeleteSpecifiedFilesVisitor(List<String> files) {
        this.files = files;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (files.contains(file.getFileName().toString())) {
            Files.delete(file);
        }
        
        return FileVisitResult.CONTINUE;
    }
}