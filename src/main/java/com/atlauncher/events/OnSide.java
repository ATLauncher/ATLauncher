package com.atlauncher.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotation a function or an {@link Event}, noting which {@link Side} the event should be executing on.
 */
@Target({ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OnSide{
    Side value() default Side.DEFAULT;
}