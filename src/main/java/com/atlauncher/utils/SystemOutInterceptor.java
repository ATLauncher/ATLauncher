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

import java.io.OutputStream;
import java.io.PrintStream;

import com.atlauncher.evnt.LogEvent.LogType;
import com.atlauncher.managers.LogManager;

public class SystemOutInterceptor extends PrintStream {
    private final LogType logType;

    public SystemOutInterceptor(OutputStream out, LogType type) {
        super(out, true);

        logType = type;
    }

    @Override
    public void print(String s) {
        super.print(s);

        if (logType == LogType.ERROR) {
            LogManager.error(s);
        } else {
            LogManager.debug(s);
        }
    }
}
