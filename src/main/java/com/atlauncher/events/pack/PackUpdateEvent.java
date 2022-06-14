package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackUpdateEvent extends PackEvent{
    PackUpdateEvent(final AnalyticsActions action, final Instance instance){
        super(instance, action);
    }

    public static PackUpdateEvent of(final Instance instance){
        return new PackUpdateEvent(AnalyticsActions.UPDATE, instance);
    }

    public static PackUpdateEvent fromPlay(final Instance instance){
        return new PackUpdateEvent(AnalyticsActions.UPDATE_FROM_PLAY, instance);
    }
}