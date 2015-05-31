package com.atlauncher.evnt;

import com.atlauncher.collection.Caching;
import com.atlauncher.managers.LogManager;

import java.lang.reflect.Constructor;

public final class EventHandler{
    private static final Caching.Cache<Class<?>, Event> eventCache = Caching.newLRU();

    public static final EventBus EVENT_BUS = new EventBus();

    public static <T extends Event> Event get(Class<T> tClass){
        Event e = eventCache.get(tClass);
        if(e != null){
            return e;
        }

        e = createEvent(tClass);
        eventCache.put(tClass, e);
        return e;
    }

    public static <T extends Event> Event createEvent(Class<T> tClass, Object... args){
        try{
            Constructor<T> tConstructor = tClass.getDeclaredConstructor();
            tConstructor.setAccessible(true);
            return tConstructor.newInstance(args);
        } catch(Exception e){
            LogManager.logStackTrace(e);
            return null;
        }
    }

    private interface Event{}

    // Changes
    public static final class AccountsChangeEvent implements Event{}
    public static final class InstancesChangeEvent implements Event{}

    public static final class PacksChangeEvent implements Event{
        public final boolean reload;

        public PacksChangeEvent(boolean reload){
            this.reload = reload;
        }
    }

    // Console
    public static final class ConsoleCloseEvent implements Event{}
    public static final class ConsoleOpenEvent implements Event{}

    // Minecraft
    public static final class MinecraftLaunchEvent implements Event{}
    public static final class MinecraftLandEvent implements Event{}

    // Misc
    public static final class TabChangeEvent implements Event{}
}