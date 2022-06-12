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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemOutInterceptor extends PrintStream {
    private final Logger logger;

    private SystemOutInterceptor(final String name, final OutputStream os) {
        super(os, true);
        this.logger = LogManager.getLogger(name);
    }

    protected Logger getLogger() {
        return this.logger;
    }

    public static SystemOutInterceptor asDebug(final OutputStream os) {
        return new DebugOutInterceptor(os);
    }

    public static SystemOutInterceptor asInfo(final OutputStream os) {
        return new InfoOutInterceptor(os);
    }

    public static SystemOutInterceptor asError(final OutputStream os) {
        return new ErrorOutInterceptor(os);
    }

    private static final class DebugOutInterceptor extends SystemOutInterceptor {
        DebugOutInterceptor(final OutputStream os) {
            super("debug-osi", os);
        }

        @Override
        public void print(@Nullable String val) {
            this.getLogger().debug(val);
        }
    }

    private static final class InfoOutInterceptor extends SystemOutInterceptor {
        InfoOutInterceptor(final OutputStream os) {
            super("default-osi", os);
        }

        @Override
        public void print(@Nullable String val) {
            this.getLogger().info(val);
        }
    }

    private static final class ErrorOutInterceptor extends SystemOutInterceptor {
        ErrorOutInterceptor(final OutputStream os) {
            super("error-osi", os);
        }

        @Override
        public void print(@Nullable String val) {
            this.getLogger().error(val);
        }
    }
}
