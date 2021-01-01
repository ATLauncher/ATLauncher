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
package com.atlauncher.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class PerformanceManager {

    private static final Map<String, Instant> times = new HashMap<>();

    public static void start() {
        start(new Throwable().getStackTrace()[1].getMethodName());
    }

    public static void start(String name) {
        if (LogManager.showDebug) {
            times.put(name, Instant.now());
        }
    }

    public static void end() {
        end(new Throwable().getStackTrace()[1].getMethodName());
    }

    public static void end(String name) {
        if (LogManager.showDebug && times.containsKey(name)) {
            long timeElapsed = Duration.between(times.get(name), Instant.now()).toMillis();

            times.remove(name);

            LogManager.debug(name + " took " + timeElapsed + " ms", 5);
        }
    }
}
