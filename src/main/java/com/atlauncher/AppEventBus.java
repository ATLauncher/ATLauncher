package com.atlauncher;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.concurrent.Executor;

public final class AppEventBus{
    private static final EventBus defaultBus = new EventBus();
    private static final EventBus uiBus = new AsyncEventBus("ATLauncher", new SwingExecutor());

    public static EventBus getDefaultBus(){
        return defaultBus;
    }

    public static EventBus getUIBus(){
        return uiBus;
    }

    public static void registerToDefaultOnly(@Nonnull final Object o){
        getDefaultBus().register(o);
    }

    public static void registerToUIOnly(@Nonnull final Object o){
        getUIBus().register(o);
    }

    public static void register(@Nonnull final Object o){
        registerToDefaultOnly(o);
        registerToUIOnly(o);
    }

    public static void unregisterFromDefault(@Nonnull final Object o){
        getDefaultBus().unregister(o);
    }

    public static void unregisterFromUI(@Nonnull final Object o){
        getUIBus().unregister(o);
    }

    public static void unregister(@Nonnull final Object o){
        unregisterFromDefault(o);
        unregisterFromUI(o);
    }

    public static void postToDefault(@Nonnull final Object event){
        getDefaultBus().post(event);
    }

    public static void postToUI(@Nonnull final Object event){
        getUIBus().post(event);
    }

    public static void post(@Nonnull final Object event){
        postToDefault(event);
        postToUI(event);
    }

    private static final class SwingExecutor implements Executor{
        @Override
        public void execute(@NotNull Runnable runnable) {
            SwingUtilities.invokeLater(runnable);
        }
    }
}