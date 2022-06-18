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
package com.atlauncher.advice;

import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.MonitorPerformance;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public final class MonitorPerformanceAdvice{
    @Around("@annotation(com.atlauncher.utils.MonitorPerformance) && execution(* *(..))")
    public Object aroundMonitorPerformance(final ProceedingJoinPoint joinPoint) throws Throwable{
        final MonitorPerformance monitor = ((MethodSignature) joinPoint.getSignature()).getMethod()
            .getDeclaredAnnotation(MonitorPerformance.class);
        final String monitorLabel = monitor.value().trim();
        final String label = monitorLabel.isEmpty()
            ? ((MethodSignature) joinPoint.getSignature()).getMethod().getName()
            : monitorLabel;

        PerformanceManager.start(label);
        final Object result = joinPoint.proceed();
        PerformanceManager.end(label);
        return result;
    }
}