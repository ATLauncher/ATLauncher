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

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.evnt.listener.MinecraftLaunchListener;

public final class MinecraftLaunchManager {
    private static final List<MinecraftLaunchListener> listeners = new LinkedList<>();

    private MinecraftLaunchManager() {
    }

    public static synchronized void addListener(MinecraftLaunchListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(MinecraftLaunchListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void minecraftLaunching(Instance instance) {
        SwingUtilities.invokeLater(() -> {
            for (MinecraftLaunchListener listener : listeners) {
                listener.minecraftLaunching(instance);
            }
        });
    }

    public static synchronized void minecraftLaunchFailed(Instance instance, String reason) {
        SwingUtilities.invokeLater(() -> {
            for (MinecraftLaunchListener listener : listeners) {
                listener.minecraftLaunchFailed(instance, reason);
            }
        });
    }

    public static synchronized void minecraftLaunched(Instance instance, AbstractAccount account, Process process) {
        SwingUtilities.invokeLater(() -> {
            for (MinecraftLaunchListener listener : listeners) {
                listener.minecraftLaunched(instance, account, process);
            }
        });
    }

    public static synchronized void minecraftClosed(Instance instance) {
        SwingUtilities.invokeLater(() -> {
            for (MinecraftLaunchListener listener : listeners) {
                listener.minecraftClosed(instance);
            }
        });
    }
}
