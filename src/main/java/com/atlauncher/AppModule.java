package com.atlauncher;

import com.atlauncher.data.Settings;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppModule
extends AbstractModule{
    @Override
    protected void configure(){
    }

    @Provides
    public Settings getAppSettings(){//TODO: refactor this
        return App.settings;
    }

    @Provides
    @Named("taskPool")
    public ExecutorService getTaskPool(){
        return Executors.newWorkStealingPool();
    }

    @Provides
    @Named("CurrentTheme")
    public String getCurrentTheme(){
        return App.settings.theme;
    }
}