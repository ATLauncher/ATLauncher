package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsAction;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategory;

public abstract class PackEvent extends AbstractAnalyticsEvent{
    private static String getPackName(final Instance instance){
        return instance.launcher.pack;
    }

    private static String getPackVersion(final Instance instance){
        return instance.launcher.version;
    }

    private static String getLabel(final String packName, final String packVersion){
        return String.format("%s - %s", packName, packVersion);
    }

    private static String getLabel(final Instance instance){
        return getLabel(getPackName(instance), getPackVersion(instance));
    }

    protected PackEvent(final String packName, final String packVersion, final AnalyticsAction action, final AnalyticsCategory category){
        super(getLabel(packName, packVersion), action, category);
    }

    protected PackEvent(final Instance instance, final AnalyticsAction action, final AnalyticsCategory category){
        super(getLabel(instance), action, category);
    }

    protected PackEvent(final Instance instance, final AnalyticsAction action){
        this(instance, action, instance);
    }
}