package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackPlayOfflineEvent extends PackEvent{
    PackPlayOfflineEvent(final Instance instance){
        super(instance, AnalyticsActions.PLAY_OFFLINE);
    }

    public static PackPlayOfflineEvent of(final Instance instance){
        return new PackPlayOfflineEvent(instance);
    }
}