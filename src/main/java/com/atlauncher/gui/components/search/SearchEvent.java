package com.atlauncher.gui.components.search;

import com.atlauncher.events.AnalyticsAction;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsCategory;
import com.atlauncher.events.AnalyticsEvent;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.util.EventObject;
import java.util.regex.Pattern;

public abstract class SearchEvent extends EventObject implements AnalyticsEvent {
    private final AnalyticsAction action;
    private final AnalyticsCategory category;
    private final Pattern pattern;

    protected SearchEvent(@Nonnull final Object source,
                          @Nonnull final Pattern pattern,
                          @Nonnull final AnalyticsAction action,
                          @Nonnull final AnalyticsCategory category){
        super(source);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(pattern);
        Preconditions.checkNotNull(action);
        Preconditions.checkNotNull(category);
        this.pattern = pattern;
        this.action = action;
        this.category = category;
    }

    public final Pattern getSearchPattern(){
        return this.pattern;
    }

    @Override
    public final String getLabel(){
        return this.getSearchPattern().pattern();
    }

    @Override
    public final String getCategory(){
        return this.category.getAnalyticsCategory();
    }

    @Override
    public final String getAction() {
        return this.action.getAnalyticsValue();
    }
}