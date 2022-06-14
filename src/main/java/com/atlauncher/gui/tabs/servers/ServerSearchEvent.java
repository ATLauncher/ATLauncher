package com.atlauncher.gui.tabs.servers;

import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsEvent;
import com.google.common.base.Preconditions;

import java.util.EventObject;
import java.util.regex.Pattern;

public final class ServerSearchEvent extends EventObject implements AnalyticsEvent {
    private final Pattern pattern;

    ServerSearchEvent(final Object source, final Pattern pattern){
        super(source);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(pattern);
        this.pattern = pattern;
    }

    public Pattern getSearchPattern(){
        return this.pattern;
    }

    @Override
    public String getLabel(){
        return this.getSearchPattern().pattern();
    }

    @Override
    public String getAction() {
        return AnalyticsActions.SEARCH.getAnalyticsValue();
    }

    @Override
    public String getCategory(){
        return AnalyticsCategories.SERVER.getAnalyticsCategory();
    }

    public static ServerSearchEvent forQuery(final Object source, final Pattern pattern){
        return new ServerSearchEvent(source, pattern);
    }

    public static ServerSearchEvent forSearchField(final ServerSearchField field){
        return forQuery(field, field.getSearchPattern());
    }
}