package com.atlauncher.events;

import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;

public final class AddFileEvent extends AbstractAnalyticsEvent{
    AddFileEvent(final String name, final AnalyticsCategories category){
        super(name, AnalyticsActions.ADD_FILE, category);
    }

    public static AddFileEvent forMod(final String name, final AnalyticsCategories category){
        return new AddFileEvent(name, category);
    }

    public static AddFileEvent forCurseForgeMod(final String name, final String file){
        return forMod(String.format("%s - %s", name, file), AnalyticsCategories.CURSE_FORGE_MOD);
    }

    public static AddFileEvent forCurseForgeMod(final CurseForgeProject mod, final CurseForgeFile file){
        return forCurseForgeMod(mod.name, file.displayName);
    }

    public static AddFileEvent forModrinthMod(final String name, final String version){
        return forMod(String.format("%s - %s", name, version), AnalyticsCategories.MODRINTH);
    }

    public static AddFileEvent forModrinthMod(final ModrinthProject mod, final ModrinthVersion version){
        return forModrinthMod(mod.title, version.name);
    }
}