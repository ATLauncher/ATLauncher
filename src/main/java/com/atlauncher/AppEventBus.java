/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppEventBus {
    private static final ExecutorService pool = Executors.newWorkStealingPool();
    private static final EventBus defaultBus = new AsyncEventBus("Workers", pool);
    private static final EventBus uiBus = new AsyncEventBus("Swing", new SwingExecutor());

    public static EventBus getDefaultBus() {
        return defaultBus;
    }

    public static EventBus getUIBus() {
        return uiBus;
    }

    public static void registerToDefaultOnly(@Nonnull final Object o) {
        getDefaultBus().register(o);
    }

    public static void registerToUIOnly(@Nonnull final Object o) {
        getUIBus().register(o);
    }

    public static void register(@Nonnull final Object o) {
        registerToDefaultOnly(o);
        registerToUIOnly(o);
    }

    public static void unregisterFromDefault(@Nonnull final Object o) {
        getDefaultBus().unregister(o);
    }

    public static void unregisterFromUI(@Nonnull final Object o) {
        getUIBus().unregister(o);
    }

    public static void unregister(@Nonnull final Object o) {
        unregisterFromDefault(o);
        unregisterFromUI(o);
    }

    public static void postToDefault(@Nonnull final Object event) {
        getDefaultBus().post(event);
    }

    public static void postToUI(@Nonnull final Object event) {
        getUIBus().post(event);
    }

    public static void post(@Nonnull final Object event) {
        postToDefault(event);
        postToUI(event);
    }

    private static final class SwingExecutor implements Executor {
        @Override
        public void execute(@NotNull Runnable runnable) {
            SwingUtilities.invokeLater(runnable);
        }
    }
}