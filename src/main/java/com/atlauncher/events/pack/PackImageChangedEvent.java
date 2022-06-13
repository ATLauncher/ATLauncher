package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackImageChangedEvent extends PackEvent{
    PackImageChangedEvent(final Instance instance){
        super(instance, AnalyticsActions.CHANGE_IMAGE);
    }

    public static PackImageChangedEvent of(final Instance instance){
        return new PackImageChangedEvent(instance);
    }
}