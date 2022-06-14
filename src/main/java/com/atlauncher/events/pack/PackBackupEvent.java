package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackBackupEvent extends PackEvent{
    PackBackupEvent(final Instance instance){
        super(instance, AnalyticsActions.PACK_BACKUP);
    }

    public static PackBackupEvent of(final Instance instance){
        return new PackBackupEvent(instance);
    }
}