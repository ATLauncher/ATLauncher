package com.atlauncher.events;

public final class UpdateLauncherEvent extends AbstractAnalyticsEvent {
    UpdateLauncherEvent(){
        super(AnalyticsActions.UPDATE, AnalyticsCategories.LAUNCHER);
    }

    public static UpdateLauncherEvent newInstance(){
        return new UpdateLauncherEvent();
    }
}