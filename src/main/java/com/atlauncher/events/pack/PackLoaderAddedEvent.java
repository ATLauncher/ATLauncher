package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackLoaderAddedEvent extends PackEvent{
    PackLoaderAddedEvent(final Instance instance){
        super(instance, AnalyticsActions.ADD_LOADER);
    }

    public static PackLoaderAddedEvent of(final Instance instance){
        return new PackLoaderAddedEvent(instance);
    }
}