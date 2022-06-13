package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AnalyticsActions;

public final class PackShareCodeEvent extends PackEvent{
    PackShareCodeEvent(final Instance instance){
        super(instance, AnalyticsActions.MAKE_SHARE_CODE);
    }

    public static PackShareCodeEvent of(final Instance instance){
        return new PackShareCodeEvent(instance);
    }
}