package com.atlauncher.utils.validator;

import com.atlauncher.collection.ModList;
import com.atlauncher.data.json.Mod;

public final class GroupValidator{
    private final String depends;

    public GroupValidator(String depends){
        this.depends = depends;
    }

    public boolean find(ModList mods){
        for(Mod mod : mods){
            if(!mod.hasGroup()){
                continue;
            }

            if(mod.group.equals(this.depends)){
                return true;
            }
        }

        return false;
    }
}