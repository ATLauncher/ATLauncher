package com.atlauncher.events.theme;

public final class ThemeLoadedEvent extends ThemeEvent{
    ThemeLoadedEvent(){
        super();
    }

    public static ThemeLoadedEvent newInstance(){
        return new ThemeLoadedEvent();
    }
}