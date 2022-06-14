package com.atlauncher.events.launcher;

import com.atlauncher.events.AnalyticsActions;

public final class UpdateDataEvent extends LauncherEvent {
    UpdateDataEvent(){
        super(AnalyticsActions.UPDATE_DATA);
    }

    public static UpdateDataEvent of(){
        return new UpdateDataEvent();
    }
}