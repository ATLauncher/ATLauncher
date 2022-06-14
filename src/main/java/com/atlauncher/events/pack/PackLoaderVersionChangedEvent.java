package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackLoaderVersionChangedEvent extends PackEvent{
    PackLoaderVersionChangedEvent(final Instance instance){
        super(instance, AnalyticsActions.CHANGE_LOADER_VERSION);
    }

    public static PackLoaderVersionChangedEvent of(final Instance instance){
        return new PackLoaderVersionChangedEvent(instance);
    }
}