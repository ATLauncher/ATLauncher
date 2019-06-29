/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.atlauncher.evnt.LogEvent;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.thread.LoggingThread;
import com.atlauncher.utils.Utils;

public final class LogManager {
    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<>(128);
    public static boolean showDebug = false;

    /**
     * The level of debug logs to show. 1 being lowest, 2 being meh, 3 being
     * EXTREEEEEEEME and 5 being OMG WTF SO MUCH LOGS
     */
    public static int debugLevel = 0;

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

    public static void debugObject(Object object) {
        debug(Gsons.DEFAULT.toJson(object), false);
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

    /**
     * Logs a stack trace to the console window
     *
     * @param t The throwable to show in the console
     */

    public static void logStackTrace(Throwable t) {
        t.printStackTrace();

        CharArrayWriter writer = new CharArrayWriter();
        try {
            t.printStackTrace(new PrintWriter(writer));
            error(writer.toString());
        } finally {
            writer.close();
        }
    }

    /**
     * Logs a stack trace to the console window with a custom message before it
     *
     * @param message A message regarding the stack trace to show before it
     *                providing more insight
     * @param t       The throwable to show in the console
     */
    public static void logStackTrace(String message, Throwable t) {
        error(message);
        logStackTrace(t);
    }
}
