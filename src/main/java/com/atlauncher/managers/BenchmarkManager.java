/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import java.util.HashMap;
import java.util.Map;

public class BenchmarkManager {
    public static final Map<String, Long> benchmarks = new HashMap<>();

    public static void start() {
        BenchmarkManager.start(BenchmarkManager.getCallersName());
    }

    public static void start(String name) {
        benchmarks.put(name, System.currentTimeMillis());
    }

    public static void stop() {
        BenchmarkManager.stop(BenchmarkManager.getCallersName(), false);
    }

    public static void stop(String name) {
        BenchmarkManager.stop(name, false);
    }

    public static void stop(String name, boolean withName) {
        if (benchmarks.containsKey(name)) {
            LogManager.debug("[" + BenchmarkManager.getCallersName() + "] " + (withName ? name + " " : "") + "took " +
                    (System.currentTimeMillis() - benchmarks.get(name)) + " ms to run!", 1);

            benchmarks.remove(name);
        }
    }

    public static String getCallersName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();

        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(BenchmarkManager.class.getName()) && ste.getClassName().indexOf("java" +
                    ".lang" + ".Thread") != 0) {
                return ste.getClassName() + ":" + ste.getMethodName();
            }
        }

        return "";
    }
}
