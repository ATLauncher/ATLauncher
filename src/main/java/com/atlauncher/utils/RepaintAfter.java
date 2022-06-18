package com.atlauncher.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a function to cause a {@link javax.swing.JComponent} to repaint after execution.
 *
 * Note(s):
 *   - Only safe to use on functions that are executed on the Swing thread.
 *   - Only safe to use on non-static functions that are members to a {@link javax.swing.JComponent}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RepaintAfter{
}