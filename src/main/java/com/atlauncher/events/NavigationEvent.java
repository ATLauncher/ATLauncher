package com.atlauncher.events;

public final class NavigationEvent extends AbstractAnalyticsEvent{
    private final int page;

    NavigationEvent(final AnalyticsActions action, final AnalyticsCategory category, final int page){
        super(action, category);
        this.page = page;
    }

    public int getPage(){
        return this.page;
    }

    public static NavigationEvent nextPage(final int page, final AnalyticsCategory category){
        return new NavigationEvent(AnalyticsActions.NEXT_PAGE, category, page);
    }

    public static NavigationEvent previousPage(final int page, final AnalyticsCategory category){
        return new NavigationEvent(AnalyticsActions.PREVIOUS_PAGE, category, page);
    }
}
