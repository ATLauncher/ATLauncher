package com.atlauncher.events;

public interface AnalyticsEvent extends Event{
    default String getLabel(){
        return null;
    }

    default String getCategory(){
        return null;
    }
    default String getAction(){
        return null;
    }
}