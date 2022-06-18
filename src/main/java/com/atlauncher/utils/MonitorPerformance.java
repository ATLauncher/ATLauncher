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
package com.atlauncher.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to wrap code for performance monitoring.
 *
 * Rather than doing such:
 * ```java
 * public void myFunction(){
 *   PerformanceManager.start();
 *
 *   // do some things
 *
 *   PerformanceManager.stop();
 * }
 * ```
 *
 * This can be abbreviated to:
 * ```java
 * @MonitorPerformance(
 *   value = "" // or some other value
 * )
 * public void myFunction(){
 *   // do some things
 * }
 * ```
 *
 * After compilation, they will be effectively the same.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface MonitorPerformance{
    /**
     * The label to supply to the {@link com.atlauncher.managers.PerformanceManager}.
     *
     * Note(s)
     *  - default, or empty string, for the value will use the name of the function this is attached to.
     *
     * @return The label to use for the telemetry span.
     */
    String value() default "";
}