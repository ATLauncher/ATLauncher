package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackSettingsEvent extends PackEvent{
    PackSettingsEvent(final Instance instance){
        super(instance, AnalyticsActions.SETTINGS);
    }

    public static PackSettingsEvent of(final Instance instance){
        return new PackSettingsEvent(instance);
    }
}