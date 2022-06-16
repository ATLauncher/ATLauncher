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