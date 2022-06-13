package com.atlauncher.events;

import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthSearchHit;

public final class AddModEvent extends AnalyticsEvent.AppEvent{
    public static final String ACTION = "Add";

    private AddModEvent(final String name, final String category){
        super(name, ACTION, category);
    }

    public static AddModEvent forModrinth(final ModrinthSearchHit mod){
        return new AddModEvent(mod.title, "ModrinthMod");
    }

    public static AddModEvent forCurseForge(final CurseForgeProject mod){
        return new AddModEvent(mod.name, "CurseForgeMod");
    }
}