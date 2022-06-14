package com.atlauncher.events;

public abstract class AbstractAnalyticsEvent implements AnalyticsEvent{
    private final String label;
    private final AnalyticsAction action;
    private final AnalyticsCategory category;

    protected AbstractAnalyticsEvent(final String label, final AnalyticsAction action, final AnalyticsCategory category){
        super();
        this.label = label;
        this.action = action;
        this.category = category;
    }

    protected AbstractAnalyticsEvent(final AnalyticsAction action, final AnalyticsCategory category){
        this(null, action, category);
    }

    protected AbstractAnalyticsEvent(){
        this(null, null);
    }

    public final String getLabel(){
        return this.label;
    }

    public final String getAction(){
        return this.action.getAnalyticsValue();
    }

    public final String getCategory(){
        return this.category.getAnalyticsCategory();
    }
}