package com.atlauncher.events.settings;

public final class SettingsSavedEvent extends SettingsEvent{
    SettingsSavedEvent(){
        super();
    }

    public static SettingsSavedEvent newInstance(){
        return new SettingsSavedEvent();
    }
}