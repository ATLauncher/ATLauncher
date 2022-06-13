package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackAddModsEvent extends PackEvent{
    PackAddModsEvent(final Instance instance){
        super(instance, AnalyticsActions.ADD_MODS);
    }

    public static PackAddModsEvent of(final Instance instance){
        return new PackAddModsEvent(instance);
    }
}