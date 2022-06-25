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
package com.atlauncher.evnt.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 19 / 06 / 2022
 */
public final class SettingsValidityManager {
    private static final Logger LOG = LogManager.getLogger(SettingsValidityManager.class);
    private static final List<Consumer<Boolean>> listeners = new LinkedList<>();

    public static HashMap<String, Boolean> validities = new HashMap<>();

    private static boolean isValidAtAll() {
        if (!validities.isEmpty())
            for (boolean validity : validities.values()) {
                if (!validity)
                    return false;
            }
        return true;
    }

    public static synchronized void addListener(@NotNull Consumer<Boolean> listener) {
        listener.accept(isValidAtAll());
        listeners.add(listener);
    }

    public static synchronized void removeListener(@NotNull Consumer<Boolean> listener) {
        listeners.remove(listener);
    }

    public static synchronized void post(String setting, boolean validity) {
        validities.put(setting, validity);
        new Thread(() -> {
            for (Consumer<Boolean> listener : listeners) {
                listener.accept(isValidAtAll());
            }
        }).start();
    }
}
