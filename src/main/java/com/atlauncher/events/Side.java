package com.atlauncher.events;

/**
 * The "{@link Side}" that an event should be executed on.
 *
 * Essentially, a {@link Side} should map to a specific thread.
 */
public enum Side{
    /**
     * Default: The event is executed on the calling thread - or whichever thread published the event.
     */
    DEFAULT,
    /**
     * Async: The event is executed on a worker thread.
     */
    WORKER,
    /**
     * Main: The event is executed on the "main" thread.
     */
    MAIN,
    /**
     * UI: The event is executed on the Swing thread.
     */
    UI;
}