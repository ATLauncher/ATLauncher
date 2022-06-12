package com.atlauncher.events;

public abstract class ThemeEvent extends Event{
    protected ThemeEvent(){
        super();
    }

    public static final class ThemeLoadedEvent extends ThemeEvent{//TODO: use
        public ThemeLoadedEvent(){
            super();
        }
    }

    @SwingEvent
    public static final class ThemeChangedEvent extends ThemeEvent{
        public ThemeChangedEvent(){
            super();
        }
    }
}