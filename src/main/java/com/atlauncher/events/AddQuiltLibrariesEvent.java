package com.atlauncher.events;

public final class AddQuiltLibrariesEvent extends AnalyticsEvent.AppEvent{
    public static final String ACTION = "AddQuiltStandardLibraries";

    private AddQuiltLibrariesEvent(final String category){
        super(ACTION, category);
    }

    public static AddQuiltLibrariesEvent forCategory(final String category){
        return new AddQuiltLibrariesEvent(category);
    }

    public static AddQuiltLibrariesEvent forModrinth(){
        return forCategory("Modrinth");
    }
}