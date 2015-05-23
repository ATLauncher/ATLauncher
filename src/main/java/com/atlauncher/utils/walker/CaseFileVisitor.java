package com.atlauncher.utils.walker;

import com.atlauncher.data.json.CaseType;
import com.atlauncher.utils.Utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public final class CaseFileVisitor extends SimpleFileVisitor<Path> {
    private CaseType caseType;
    private List<String> filesToIgnore;

    public CaseFileVisitor(CaseType caseType) {
        this.caseType = caseType;
        this.filesToIgnore = new ArrayList<>();
    }

    public CaseFileVisitor(CaseType caseType, List<String> filesToIgnore) {
        this.caseType = caseType;
        this.filesToIgnore = filesToIgnore;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (this.filesToIgnore.contains(path.getFileName().toString())) {
            return FileVisitResult.CONTINUE;
        }

        if (!Files.isRegularFile(path) || (!path.getFileName().endsWith("jar") && !path.getFileName().endsWith("zip") &&
                !path.getFileName().endsWith("litemod"))) {
            if (caseType == CaseType.upper) {
                Path moveTo = path.getParent().resolve(path.getFileName().subpath(0, path.getFileName().toString()
                        .lastIndexOf(".")));
                Utils.moveFile(path, moveTo);
            } else if (caseType == CaseType.lower) {
                Utils.moveFile(path, path.getParent().resolve(path.getFileName().toString().toLowerCase()));
            }
        }

        return FileVisitResult.CONTINUE;
    }
}