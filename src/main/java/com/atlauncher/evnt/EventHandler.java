package com.atlauncher.evnt;

import com.atlauncher.App;

public final class EventHandler{
    public static final EventBus EVENT_BUS = new EventBus();

    public static <T extends Event> Event get(Class<T> tClass){
        return App.INJECTOR.getInstance(tClass);
    }

    private interface Event{}

    // Changes
    public static final class AccountsChangeEvent implements Event{}
    public static final class InstancesChangeEvent implements Event{}
    public static final class SettingsChangeEvent implements Event{}

    public static final class PacksChangeEvent implements Event{
        public final boolean reload;

        public PacksChangeEvent(boolean reload){
            this.reload = reload;
        }
    }

    // Console
    public static final class ConsoleCloseEvent implements Event{}
    public static final class ConsoleOpenEvent implements Event{}

    // Misc
    public static final class TabChangeEvent implements Event{}
    public static final class RelocalizationEvent implements Event{}
}