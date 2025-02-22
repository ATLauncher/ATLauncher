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
package com.atlauncher.managers;

import java.io.CharArrayWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlauncher.Gsons;
import com.atlauncher.evnt.LogEvent;
import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.exceptions.LocalException;
import com.atlauncher.network.DownloadException;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.thread.LoggingThread;
import com.atlauncher.utils.SystemOutInterceptor;

public final class LogManager {
    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<>(128);
    public static boolean showDebug = false;

    private static final Pattern LOG4J_THREAD_REGEX = Pattern.compile("<log4j:Event.*?thread=\"(.*?)\".*?>");
    private static final Pattern LOG4J_LEVEL_REGEX = Pattern.compile("<log4j:Event.*?level=\"(.*?)\".*?>");
    private static final Pattern LOG4J_MESSAGE_REGEX = Pattern
            .compile("<log4j:Message><!\\[CDATA\\[(.*?)\\]\\]></log4j:Message>");

    public static void start() {
        new LoggingThread(queue).start();

        redirectSystemOutLogs();
    }

    private static void redirectSystemOutLogs() {
        PrintStream origOut = System.out;
        PrintStream origErr = System.err;

        System.setOut(new SystemOutInterceptor(origOut, LogType.DEBUG));
        System.setErr(new SystemOutInterceptor(origErr, LogType.ERROR));
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
            if (!(t instanceof LocalException) && sendRemote) {
                ErrorReporting.captureException(t);
            }

            t.printStackTrace(new PrintWriter(writer));
            error(writer.toString());
        }

        if (t instanceof DownloadException) {
            DownloadException exception = ((DownloadException) t);

            try {
                if (exception.contentType != null
                        && (exception.contentType.equalsIgnoreCase("application/json")
                                || exception.contentType
                                        .equalsIgnoreCase("application/xml")
                                || exception.contentType.startsWith("text/"))) {
                    debug(exception.response, 5);
                }
            } catch (Exception e) {
                // ignored
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

    public static void minecraftLog4j(String string) {
        String thread = "";
        String message = "";
        String levelString = "";
        LogType level = LogType.INFO;

        Matcher threadMatcher = LOG4J_THREAD_REGEX.matcher(string);
        if (threadMatcher.find()) {
            thread = threadMatcher.group(1);
        }

        Matcher levelMatcher = LOG4J_LEVEL_REGEX.matcher(string);
        if (levelMatcher.find()) {
            levelString = levelMatcher.group(1);

            if (levelString.equalsIgnoreCase("INFO")) {
                level = LogType.INFO;
            } else if (levelString.equalsIgnoreCase("ERROR") || levelString.equalsIgnoreCase("SEVERE")) {
                level = LogType.ERROR;
            } else if (levelString.equalsIgnoreCase("WARN")) {
                level = LogType.WARN;
            }
        }

        Matcher messageMatcher = LOG4J_MESSAGE_REGEX.matcher(string);
        if (messageMatcher.find()) {
            message = messageMatcher.group(1);
        }

        queue.offer(new LogEvent(level, String.format("[%s/%s] %s", thread, levelString, message),
                LogEvent.CONSOLE));
    }
}
