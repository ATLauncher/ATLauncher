package com.atlauncher.events;

public final class ExceptionEvent implements AnalyticsEvent{
    private final Throwable cause;

    private ExceptionEvent(final Throwable cause){
        super();
        this.cause = cause;
    }

    public Throwable getCause(){
        return this.cause;
    }

    public String getCauseMessage(){
        return this.getCause().getMessage();
    }

    public static AnalyticsEvent forException(final Throwable th){
        return new ExceptionEvent(th);
    }
}