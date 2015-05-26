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

package com.atlauncher;

import com.atlauncher.evnt.LogEvent;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.thread.LoggingThread;
import com.atlauncher.utils.Utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LogManager {
    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<LogEvent>(128);
    public static boolean showDebug = false;

    /**
     * The level of debug logs to show. 1 being lowest, 2 being meh, 3 being EXTREEEEEEEME
     */
    public static int debugLevel = 0;

    private LogManager() {
    }

    public static void start() {
        new LoggingThread(queue).start();
    }

    public static void log(LogEvent event) {
        queue.offer(event);
    }

    public static void info(String message) {
        queue.offer(new LogEvent(LogType.INFO, message));
    }

    public static void debug(String message) {
        debug(message, false);
    }

    public static void debug(String message, boolean force) {
        if (showDebug || force) {
            queue.offer(new LogEvent(LogType.DEBUG, message));
        }
    }

    public static void debug(String message, int level) {
        if (showDebug && debugLevel >= level) {
            queue.offer(new LogEvent(LogType.DEBUG, message));
        }
    }

    public static void warn(String message) {
        queue.offer(new LogEvent(LogEvent.LogType.WARN, message));
    }

    public static void error(String message) {
        queue.offer(new LogEvent(LogType.ERROR, message));
    }

    public static void minecraft(String message) {
        Object[] value = Utils.prepareMessageForMinecraftLog(message);
        queue.offer(new LogEvent((LogType) value[0], (String) value[1], 10));
    }

    public static void logStackTrace(Exception exception) {
        exception.printStackTrace();

        LogManager.error(exception.getMessage());

        for (StackTraceElement element : exception.getStackTrace()) {
            LogManager.error(element.toString());
        }
    }

    public static void logStackTrace(String message, Exception exception) {
        LogManager.error(message);
        LogManager.logStackTrace(exception);
    }
}
