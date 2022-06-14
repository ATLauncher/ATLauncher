package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackReinstallEvent extends PackEvent{
    PackReinstallEvent(final Instance instance){
        super(instance, AnalyticsActions.REINSTALL);
    }

    public static PackReinstallEvent of(final Instance instance){
        return new PackReinstallEvent(instance);
    }
}