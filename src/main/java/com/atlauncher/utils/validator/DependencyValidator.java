package com.atlauncher.utils.validator;

import com.atlauncher.collection.ModList;
import com.atlauncher.data.json.Mod;

public final class DependencyValidator {
    private final String depends;

    public DependencyValidator(String depends){
        this.depends = depends;
    }

    public boolean find(ModList mods){
        for(Mod mod : mods){
            if(mod.name.equals(this.depends)){
                return true;
            }
        }

        return false;
    }
}