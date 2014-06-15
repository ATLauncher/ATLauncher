package com.atlauncher;

import com.atlauncher.event.LogEvent;
import com.atlauncher.thread.LoggingThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class LogManager{
    private static final BlockingQueue<LogEvent> queue = new ArrayBlockingQueue<LogEvent>(64);

    private LogManager(){}

    public static void start(){
        new LoggingThread(queue).start();
    }

    public static void log(LogEvent event){
        queue.offer(event);
    }

    public static void info(String message){
        queue.offer(new LogEvent(LogEvent.LogType.INFO, message));
    }

    public static void debug(String message){
        queue.offer(new LogEvent(LogEvent.LogType.DEBUG, message));
    }

    public static void warn(String message){
        queue.offer(new LogEvent(LogEvent.LogType.WARN, message));
    }

    public static void error(String message){
        queue.offer(new LogEvent(LogEvent.LogType.ERROR, message));
    }
}