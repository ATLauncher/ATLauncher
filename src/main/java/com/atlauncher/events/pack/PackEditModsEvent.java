package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackEditModsEvent extends PackEvent{
    PackEditModsEvent(final Instance instance){
        super(instance, AnalyticsActions.EDIT_MODS);
    }

    public static PackEditModsEvent of(final Instance instance){
        return new PackEditModsEvent(instance);
    }
}