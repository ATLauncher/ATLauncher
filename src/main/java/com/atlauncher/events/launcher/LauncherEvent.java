package com.atlauncher.events.launcher;

import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;

public abstract class LauncherEvent extends AbstractAnalyticsEvent {
    protected LauncherEvent(final AnalyticsActions action){
        super(action, AnalyticsCategories.LAUNCHER);
    }
}