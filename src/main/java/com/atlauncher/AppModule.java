package com.atlauncher;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import joptsimple.OptionParser;

import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppModule
extends AbstractModule{
    @Override
    protected void configure(){

    }

    @Provides
    @Named("taskPool")
    public ExecutorService getTaskPool(){
        return Executors.newWorkStealingPool();
    }
}