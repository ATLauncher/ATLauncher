package com.atlauncher.events;

import javax.swing.*;

public final class ImportInstanceEvent extends AbstractAnalyticsEvent{
    ImportInstanceEvent(final String source, final AnalyticsActions action){
        super(source, action, AnalyticsCategories.IMPORT_INSTANCE);
    }

    public static ImportInstanceEvent forUrl(final String url){
        return new ImportInstanceEvent(url, AnalyticsActions.ADD_FROM_URL);
    }

    public static ImportInstanceEvent forUrl(final JTextField field){
        return forUrl(field.getText());
    }

    public static ImportInstanceEvent forZip(final String path){
        return new ImportInstanceEvent(path, AnalyticsActions.ADD_FROM_ZIP);
    }

    public static ImportInstanceEvent forZip(final JTextField field){
        return forZip(field.getText());
    }
}