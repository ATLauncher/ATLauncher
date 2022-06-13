package com.atlauncher.events;

import javax.swing.*;

public final class SearchEvent extends AnalyticsEvent.AppEvent{
    public static final String ACTION = "Search";

    private SearchEvent(final String query, final String category){
        super(query, ACTION, category);
    }

    public static SearchEvent forQuery(final String query, final String category){
        return new SearchEvent(query, category);
    }

    public static SearchEvent forTextField(final JTextField field, final String category){
        return forQuery(field.getText(), category);
    }
}