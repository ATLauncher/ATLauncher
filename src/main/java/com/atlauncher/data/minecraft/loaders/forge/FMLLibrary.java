package com.atlauncher.data.minecraft.loaders.forge;

public class FMLLibrary {
    public String name;
    public String sha1Hash;
    public Long size;

    public FMLLibrary(String name, String sha1Hash, Long size) {
        this.name = name;
        this.sha1Hash = sha1Hash;
        this.size = size;
    }
}
