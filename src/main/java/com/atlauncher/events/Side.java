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