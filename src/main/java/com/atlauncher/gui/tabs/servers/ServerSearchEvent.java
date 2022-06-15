package com.atlauncher.gui.tabs.servers;

import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.gui.components.search.SearchEvent;
import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

final class ServerSearchEvent extends SearchEvent{
    ServerSearchEvent(final Object source, final Pattern pattern){
        super(source, pattern, AnalyticsActions.SEARCH, AnalyticsCategories.SERVER);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(pattern);
    }

    static ServerSearchEvent forQuery(final Object source, final Pattern pattern){
        return new ServerSearchEvent(source, pattern);
    }

    static ServerSearchEvent forSearchField(final ServerSearchField field){
        return forQuery(field, field.getSearchPattern());
    }
}