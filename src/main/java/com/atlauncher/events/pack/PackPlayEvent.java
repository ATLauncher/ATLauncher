package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackPlayEvent extends PackEvent{
    PackPlayEvent(final Instance instance){
        super(instance, AnalyticsActions.PLAY);
    }

    public static PackPlayEvent of(final Instance instance){
        return new PackPlayEvent(instance);
    }
}