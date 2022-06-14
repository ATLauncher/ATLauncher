package com.atlauncher.events;

public final class ToolRunEvent extends AbstractAnalyticsEvent{
    ToolRunEvent(final String tool){
        super(tool, AnalyticsActions.RUN, AnalyticsCategories.TOOL);
    }

    public static ToolRunEvent of(final String tool){
        return new ToolRunEvent(tool);
    }

    public static ToolRunEvent networkChecker(){
        return of("NetworkChecker");
    }

    public static ToolRunEvent skinUpdater(){
        return of("SkinUpdater");
    }
}
