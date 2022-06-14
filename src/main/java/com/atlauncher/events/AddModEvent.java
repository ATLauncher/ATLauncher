package com.atlauncher.events;

import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthSearchHit;

public final class AddModEvent extends AbstractAnalyticsEvent{
    private AddModEvent(final String name, final AnalyticsCategories category){
        super(name, AnalyticsActions.ADD, category);
    }

    public static AddModEvent forMod(final String name, final AnalyticsCategories category){
        return new AddModEvent(name, category);
    }

    public static AddModEvent forCurseForgeMod(final CurseForgeProject mod){
        return forMod(mod.name, AnalyticsCategories.CURSE_FORGE_MOD);
    }

    public static AddModEvent forModrinthMod(final ModrinthSearchHit mod){
        return forMod(mod.title, AnalyticsCategories.MODRINTH);
    }
}