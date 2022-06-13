package com.atlauncher.events;

import javax.swing.*;

public final class ImportInstanceEvent extends AnalyticsEvent.AppEvent{
    private static final String CATEGORY = "ImportInstance";

    private ImportInstanceEvent(final String source, final String action){
        super(source, action, CATEGORY);
    }

    public static ImportInstanceEvent forUrl(final String url){
        return new ImportInstanceEvent(url, "AddFromUrl");
    }

    public static ImportInstanceEvent forUrl(final JTextField field){
        return forUrl(field.getText());
    }

    public static ImportInstanceEvent forZip(final String path){
        return new ImportInstanceEvent(path, "AddFromZip");
    }

    public static ImportInstanceEvent forZip(final JTextField field){
        return forZip(field.getText());
    }
}