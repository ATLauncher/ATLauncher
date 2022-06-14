package com.atlauncher.events.theme;

import com.atlauncher.App;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsEvent;
import com.atlauncher.themes.ATLauncherLaf;

public final class ThemeChangedEvent extends ThemeEvent implements AnalyticsEvent {
    ThemeChangedEvent(final ATLauncherLaf theme){
        super(theme);
    }

    @Override
    public String getLabel() {
        return this.getTheme().getName();
    }

    @Override
    public String getCategory() {
        return AnalyticsCategories.LAUNCHER.getAnalyticsCategory();
    }

    @Override
    public String getAction() {
        return AnalyticsActions.CHANGE_THEME.getAnalyticsValue();
    }

    public static ThemeChangedEvent forTheme(final ATLauncherLaf theme){
        return new ThemeChangedEvent(theme);
    }

    public static ThemeChangedEvent forCurrentTheme(){
        return forTheme(App.THEME);
    }
}