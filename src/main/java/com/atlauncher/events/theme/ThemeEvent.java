package com.atlauncher.events.theme;

import com.atlauncher.events.Event;
import com.atlauncher.themes.ATLauncherLaf;

public abstract class ThemeEvent implements Event {
    private final ATLauncherLaf theme;

    protected ThemeEvent(ATLauncherLaf theme){
        super();
        this.theme = theme;
    }

    public ATLauncherLaf getTheme(){
        return this.theme;
    }
}