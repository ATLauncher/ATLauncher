package com.atlauncher.evnt;

import com.atlauncher.injector.Module;

public final class EventModule
extends Module {
    @Override
    protected void configure() {
        this.bind(EventHandler.AccountsChangeEvent.class)
            .asSingleton();
        this.bind(EventHandler.InstancesChangeEvent.class)
            .asSingleton();
        this.bind(EventHandler.SettingsChangeEvent.class)
            .asSingleton();
        this.bind(EventHandler.ConsoleCloseEvent.class)
            .asSingleton();
        this.bind(EventHandler.ConsoleOpenEvent.class)
            .asSingleton();
        this.bind(EventHandler.TabChangeEvent.class)
            .asSingleton();
        this.bind(EventHandler.RelocalizationEvent.class)
            .asSingleton();
    }
}