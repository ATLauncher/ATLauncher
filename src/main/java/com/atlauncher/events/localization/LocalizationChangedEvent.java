package com.atlauncher.events.localization;

public final class LocalizationChangedEvent extends LocalizationEvent{
    LocalizationChangedEvent(){
        super();
    }

    public static LocalizationChangedEvent newInstance(){
        return new LocalizationChangedEvent();
    }
}