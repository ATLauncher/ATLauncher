package com.atlauncher.events.settings;

public final class SettingsLoadedEvent extends SettingsEvent{
    SettingsLoadedEvent(){
        super();
    }

    public static SettingsLoadedEvent newInstance(){
        return new SettingsLoadedEvent();
    }
}