package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackRenameEvent extends PackEvent{
    PackRenameEvent(final Instance instance){
        super(instance, AnalyticsActions.RENAME);
    }

    public static PackRenameEvent of(final Instance instance){
        return new PackRenameEvent(instance);
    }
}