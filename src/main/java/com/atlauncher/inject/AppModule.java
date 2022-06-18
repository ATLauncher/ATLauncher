package com.atlauncher.inject;

import com.atlauncher.App;
import com.atlauncher.data.Settings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public final class AppModule extends AbstractModule{
    @Override
    protected void configure(){
    }

    @Provides
    public Settings getSettings(){ //TODO: remove static access to App.settings by using @Inject in Settings.java
        return App.settings;
    }
}