package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackDeleteEvent extends PackEvent{
    PackDeleteEvent(final Instance instance){
        super(instance, AnalyticsActions.DELETE);
    }

    public static PackDeleteEvent of(final Instance instance){
        return new PackDeleteEvent(instance);
    }
}