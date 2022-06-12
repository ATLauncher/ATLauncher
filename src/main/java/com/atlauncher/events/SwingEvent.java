package com.atlauncher.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotation an {@link Event}, marking the {@link Event} to be posted on the Swing thread
 * rather than the current thread.
 */
@Target({ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SwingEvent{
}