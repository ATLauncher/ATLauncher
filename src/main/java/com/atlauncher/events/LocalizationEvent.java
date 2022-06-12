package com.atlauncher.events;

public abstract class LocalizationEvent extends Event{
    protected LocalizationEvent(){
        super();
    }

    public static final class LocalizationLoadedEvent extends LocalizationEvent{
        public LocalizationLoadedEvent(){
            super();
        }
    }

    public static final class LocalizationChangedEvent extends LocalizationEvent{
        public LocalizationChangedEvent(){
            super();
        }
    }
}