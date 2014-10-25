package com.atlauncher;

public final class ExceptionStrainer implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        LogManager.error(e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString() != null) {
                LogManager.error(element.toString());
            }
        }
    }
}