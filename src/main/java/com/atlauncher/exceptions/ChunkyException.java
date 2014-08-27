package com.atlauncher.exceptions;

public final class ChunkyException extends RuntimeException {
    public ChunkyException(String ex) {
        super(ex);
    }

    public ChunkyException(String ex, Throwable t) {
        super(ex, t);
    }

    public ChunkyException(Throwable t) {
        super(t);
    }
}