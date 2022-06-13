package com.atlauncher.events;

public final class ScreenViewEvent implements AnalyticsEvent {
    private final String title;

    private ScreenViewEvent(final String title){
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public static ScreenViewEvent forScreen(final String title){
        return new ScreenViewEvent(title);
    }
}