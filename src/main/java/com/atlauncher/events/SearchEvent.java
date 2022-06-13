package com.atlauncher.events;

import javax.swing.*;

public final class SearchEvent extends AbstractAnalyticsEvent {
    SearchEvent(final String query, final AnalyticsCategory category){
        super(query, AnalyticsActions.SEARCH, category);
    }

    public static SearchEvent forQuery(final String query, final AnalyticsCategory category){
        return new SearchEvent(query, category);
    }

    public static SearchEvent forTextField(final JTextField field, final AnalyticsCategory category){
        return forQuery(field.getText(), category);
    }

    public static SearchEvent forCurseForgeMod(final JTextField field){
        return forTextField(field, AnalyticsCategories.CURSE_FORGE_MOD);
    }
}