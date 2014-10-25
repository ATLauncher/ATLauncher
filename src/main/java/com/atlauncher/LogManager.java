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

    private LogManager() {
    }

    public static void start() {
        new LoggingThread(queue).start();
    }

    public static void log(LogEvent event) {
        queue.offer(event);
    }

    public static void info(String message) {
        queue.offer(new LogEvent(LogEvent.LogType.INFO, message));
    }

    public static void debug(String message) {
        debug(message, false);
    }

    public static void debug(String message, boolean force) {
        if (showDebug || force) {
            queue.offer(new LogEvent(LogEvent.LogType.DEBUG, message));
        }
    }

    public static void warn(String message) {
        queue.offer(new LogEvent(LogEvent.LogType.WARN, message));
    }

    public static void error(String message) {
        queue.offer(new LogEvent(LogEvent.LogType.ERROR, message));
    }

    public static void minecraft(String message) {
        Object[] value = Utils.prepareMessageForMinecraftLog(message);
        queue.offer(new LogEvent((LogType) value[0], (String) value[1], 10));
    }
}
