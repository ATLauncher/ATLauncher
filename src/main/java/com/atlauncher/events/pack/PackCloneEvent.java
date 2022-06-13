package com.atlauncher.events.pack;


import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackCloneEvent extends PackEvent{
    PackCloneEvent(final Instance instance){
        super(instance, AnalyticsActions.CLONE);
    }

    public static PackCloneEvent of(final Instance instance){
        return new PackCloneEvent(instance);
    }
}