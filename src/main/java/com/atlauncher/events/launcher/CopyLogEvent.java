package com.atlauncher.events.launcher;

import com.atlauncher.events.AnalyticsActions;

public final class CopyLogEvent extends LauncherEvent {
    CopyLogEvent(){
        super(AnalyticsActions.COPY_LOG);
    }

    public static CopyLogEvent of(){
        return new CopyLogEvent();
    }
}
