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

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.atlauncher.Gsons;
import com.atlauncher.evnt.LogEvent;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.exceptions.LocalException;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadException;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.thread.LoggingThread;

public final class LogManager {
    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<>(128);
    public static boolean showDebug = false;

    public static void start() {
        new LoggingThread(queue).start();
    }

    /**
     * The level of debug logs to show. 1 being lowest, 2 being meh, 3 being
     * EXTREEEEEEEME and 5 being OMG WTF SO MUCH LOGS
     */
    public static int debugLevel = 0;

    public static void info(String message) {
        queue.offer(new LogEvent(LogType.INFO, message));
    }

    public static void debug(String message) {
        if (showDebug) {
            queue.offer(new LogEvent(LogType.DEBUG, message));
        }
    }

    public static void warn(String message) {
        queue.offer(new LogEvent(LogType.WARN, message));
    }

    public static void error(String message) {
        queue.offer(new LogEvent(LogType.ERROR, message));
    }

    public static void debugObject(Object object) {
        debug(Gsons.DEFAULT.toJson(object));
    }

    public static void debug(String message, int level) {
        if (debugLevel >= level) {
            debug(message);
        }
    }

    public static void minecraft(String message) {
        Object[] value = prepareMessageForMinecraftLog(message);
        queue.offer(new LogEvent((LogType) value[0], (String) value[1], LogEvent.CONSOLE));
    }

    public static void logStackTrace(Throwable t) {
        logStackTrace(t, true);
    }

    public static void logStackTrace(Throwable t, boolean sendRemote) {
        t.printStackTrace();

        try (CharArrayWriter writer = new CharArrayWriter()) {
            Analytics.sendException(t.getMessage());

            if (!(t instanceof LocalException) && sendRemote) {
                ErrorReporting.reportError(t);
            }

            t.printStackTrace(new PrintWriter(writer));
            error(writer.toString());
        }

        if (t instanceof DownloadException) {
            DownloadException exception = ((DownloadException) t);

            if (exception.download.response != null && exception.response != null
                    && (exception.download.response.header("Content-Type").equalsIgnoreCase("application/json")
                            || exception.download.response.header("Content-Type").equalsIgnoreCase("application/xml")
                            || exception.download.response.header("Content-Type").startsWith("text/"))) {
                try {
                    debug(exception.response, 5);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void logStackTrace(String message, Throwable t) {
        logStackTrace(message, t, true);
    }

    public static void logStackTrace(String message, Throwable t, boolean sendRemote) {
        error(message);
        logStackTrace(t, sendRemote);
    }

    public static Object[] prepareMessageForMinecraftLog(String text) {
        LogType type; // The log message type
        String message; // The log message

        if (text.contains("[INFO] [STDERR]")) {
            message = text.substring(text.indexOf("[INFO] [STDERR]"));
            type = LogType.WARN;
        } else if (text.contains("[INFO]")) {
            message = text.substring(text.indexOf("[INFO]"));
            if (message.contains("CONFLICT")) {
                type = LogType.ERROR;
            } else if (message.contains("overwriting existing item")) {
                type = LogType.WARN;
            } else {
                type = LogType.INFO;
            }
        } else if (text.contains("[WARNING]")) {
            message = text.substring(text.indexOf("[WARNING]"));
            type = LogType.WARN;
        } else if (text.contains("WARNING:")) {
            message = text.substring(text.indexOf("WARNING:"));
            type = LogType.WARN;
        } else if (text.contains("INFO:")) {
            message = text.substring(text.indexOf("INFO:"));
            type = LogType.INFO;
        } else if (text.contains("Exception")) {
            message = text;
            type = LogType.ERROR;
        } else if (text.contains("[SEVERE]")) {
            message = text.substring(text.indexOf("[SEVERE]"));
            type = LogType.ERROR;
        } else if (text.contains("[Sound Library Loader/ERROR]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Sound Library Loader/WARN]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Sound Library Loader/INFO]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[Client thread/ERROR]")) {
            message = text.substring(text.indexOf("[Client thread/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Client thread/WARN]")) {
            message = text.substring(text.indexOf("[Client thread/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Client thread/INFO]")) {
            message = text.substring(text.indexOf("[Client thread/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[Server thread/ERROR]")) {
            message = text.substring(text.indexOf("[Server thread/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[Server thread/WARN]")) {
            message = text.substring(text.indexOf("[Server thread/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[Server thread/INFO]")) {
            message = text.substring(text.indexOf("[Server thread/INFO]"));
            type = LogType.INFO;
        } else if (text.contains("[main/ERROR]")) {
            message = text.substring(text.indexOf("[main/ERROR]"));
            type = LogType.ERROR;
        } else if (text.contains("[main/WARN]")) {
            message = text.substring(text.indexOf("[main/WARN]"));
            type = LogType.WARN;
        } else if (text.contains("[main/INFO]")) {
            message = text.substring(text.indexOf("[main/INFO]"));
            type = LogType.INFO;
        } else {
            message = text;
            type = LogType.INFO;
        }

        return new Object[] { type, message };
    }
}
