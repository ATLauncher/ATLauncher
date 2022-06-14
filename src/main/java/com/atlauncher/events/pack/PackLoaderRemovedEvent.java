package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackLoaderRemovedEvent extends PackEvent{
    PackLoaderRemovedEvent(final Instance instance){
        super(instance, AnalyticsActions.REMOVE_LOADER);
    }

    public static PackLoaderRemovedEvent of(final Instance instance){
        return new PackLoaderRemovedEvent(instance);
    }
}