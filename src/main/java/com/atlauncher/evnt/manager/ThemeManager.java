/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import com.atlauncher.evnt.listener.ThemeListener;

public final class ThemeManager {
    private static final List<ThemeListener> listeners = new LinkedList<>();

    public static synchronized void addListener(ThemeListener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(ThemeListener listener) {
        listeners.remove(listener);
    }

    public static synchronized void post() {
        SwingUtilities.invokeLater(() -> {
            for (ThemeListener listener : listeners) {
                listener.onThemeChange();
            }
        });
    }
}
