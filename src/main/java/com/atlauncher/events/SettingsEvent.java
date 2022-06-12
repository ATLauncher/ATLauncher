package com.atlauncher.events;

public abstract class SettingsEvent extends Event{
    protected SettingsEvent(){
        super();
    }

    public static final class SettingsLoadedEvent extends SettingsEvent{
        public SettingsLoadedEvent(){
            super();
        }
    }

    public static final class SettingsSavedEvent extends SettingsEvent{
        public SettingsSavedEvent(){
            super();
        }
    }
}