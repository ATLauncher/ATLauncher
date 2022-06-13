package com.atlauncher.events.pack;

import com.atlauncher.data.Pack;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategory;

public final class PackViewModsEvent extends PackEvent{
    PackViewModsEvent(final String name, final AnalyticsCategory category){
        super(name, "", AnalyticsActions.VIEW_MODS, category);
    }

    public static PackViewModsEvent of(final String name, final AnalyticsCategory category){
        return new PackViewModsEvent(name, category);
    }

    public static PackViewModsEvent of(final Pack pack){
        return PackViewModsEvent.of(pack.name, pack);
    }
}