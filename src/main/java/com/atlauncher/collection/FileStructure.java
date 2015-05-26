package com.atlauncher.collection;

import com.atlauncher.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

public final class FileStructure
extends LinkedList<Path>{
    public static FileStructure of(Path... paths){
        return new FileStructure(paths);
    }

    private FileStructure(Path... paths){
        super(Arrays.asList(paths));
    }

    public FileStructure(){}

    public void setup(){
        for(Path p : this){
            if(!Files.exists(p)){
                FileUtils.createDirectory(p);
            }
        }
    }
}