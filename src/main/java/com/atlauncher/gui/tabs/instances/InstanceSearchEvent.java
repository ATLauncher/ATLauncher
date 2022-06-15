package com.atlauncher.gui.tabs.instances;

import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.gui.components.search.SearchEvent;
import com.atlauncher.gui.tabs.servers.ServerSearchField;
import com.google.common.base.Preconditions;

import java.util.regex.Pattern;

final class InstanceSearchEvent extends SearchEvent {
    InstanceSearchEvent(final Object source, final Pattern pattern){
        super(source, pattern, AnalyticsActions.SEARCH, AnalyticsCategories.SERVER);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(pattern);
    }

    static InstanceSearchEvent forQuery(final Object source, final Pattern pattern){
        return new InstanceSearchEvent(source, pattern);
    }

    static InstanceSearchEvent forSearchField(final InstanceSearchField field){
        return forQuery(field, field.getSearchPattern());
    }
}