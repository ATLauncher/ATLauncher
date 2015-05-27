package com.atlauncher.collection;

import com.atlauncher.data.json.DecompType;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;
import com.atlauncher.workers.InstanceInstaller;

import java.util.LinkedList;

public final class ModList
extends LinkedList<Mod>{
    public boolean hasDependency(Mod mod){
        for(Mod modd : this){
            if(!modd.hasDepends()){
                continue;
            }

            if(modd.isDependencyOf(mod)){
                return true;
            }
        }

        return false;
    }

    public void as(ModList mods){
        this.clear();
        this.addAll(mods);
    }

    public boolean hasOptional(){
        for(Mod mod : this){
            if(mod.optional){
                return true;
            }
        }

        return false;
    }

    public boolean hasRecommended(){
        for(Mod mod : this){
            if(mod.recommended){
                return true;
            }
        }

        return false;
    }

    public ModList sort(){
        ModList mods = new ModList();
        mods.as(this);

        for(Mod mod : this){
            if(mod.optional){
                if(mod.hasLinked()){
                    for(Mod mod1 : this){
                        if(mod1.name.equalsIgnoreCase(mod.linked)){
                            mods.remove(mod);
                            int index = mods.indexOf(mod1) + 1;
                            mods.add(index, mod);
                        }
                    }
                }
            }
        }

        ModList modss = new ModList();

        for(Mod mod : mods){
            if(!mod.optional){
                modss.add(mod);
            }
        }

        for(Mod mod : mods){
            if(!modss.contains(mod)){
                modss.add(mod);
            }
        }

        return modss;
    }

    public ModList server(){
        ModList mods = new ModList();

        for(Mod mod : this){
            if(mod.server){
                mods.add(mod);
            }
        }

        return mods;
    }

    public ModList client(){
        ModList mods = new ModList();

        for(Mod mod : this){
            if(mod.client){
                mods.add(mod);
            }
        }

        return mods;
    }

    public ModList dependencies(Mod mod){
        ModList mods = new ModList();
        for(Mod modd : this){
            if(!modd.hasDepends()){
                continue;
            }

            if(modd.isDependencyOf(mod)){
                mods.add(modd);
            }
        }
        return mods;
    }

    public ModList depandencies(Mod mod){
        ModList mods = new ModList();

        for(String name : mod.depends){
            for(Mod modd : this){
                if(modd.name.equals(name)){
                    mods.add(modd);
                    break;
                }
            }
        }

        return mods;
    }

    public ModList grouped(Mod mod) {
        ModList groupedMods = new ModList();
        for (Mod modd : this) {
            if (!modd.hasGroup()) {
                continue;
            }
            if (modd.group.equalsIgnoreCase(mod.group)) {
                if (modd != mod) {
                    groupedMods.add(modd);
                }
            }
        }
        return groupedMods;
    }

    public Mod byName(String name){
        for(Mod mod : this){
            if(mod.name.equalsIgnoreCase(name)){
                return mod;
            }
        }

        return null;
    }

    public ModList linked(Mod mod){
        ModList mods = new ModList();
        for(Mod modd : this){
            if(!modd.hasLinked()){
                continue;
            }

            if(modd.linked.equalsIgnoreCase(mod.name)){
                mods.add(mod);
            }
        }
        return mods;
    }

    public boolean hasJarMod(InstanceInstaller installer){
        for(Mod mod : this){
            if(!mod.server && installer.server){
                continue;
            }

            if(mod.type == ModType.JAR){
                return true;
            } else if(mod.type == ModType.DECOMP && mod.decompType == DecompType.jar){
                return true;
            }
        }

        return false;
    }

    public Mod getByType(ModType type){
        for(Mod mod : this){
            if(mod.type == type){
                return mod;
            }
        }

        return null;
    }

    public DownloadPool downloadPool(InstanceInstaller installer){
        DownloadPool pool = new DownloadPool();

        for(Mod mod : this){
            if(mod.download == DownloadType.SERVER){
                pool.add(mod.generateDownloadable(installer));
            }
        }

        return pool;
    }
}