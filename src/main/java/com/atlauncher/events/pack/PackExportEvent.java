package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackExportEvent extends PackEvent{
    PackExportEvent(final Instance instance){
        super(instance, AnalyticsActions.EXPORT);
    }

    public static PackExportEvent of(final Instance instance){
        return new PackExportEvent(instance);
    }
}