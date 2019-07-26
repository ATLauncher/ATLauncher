/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import com.atlauncher.exceptions.LocalException;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.ErrorReporting;

import org.apache.logging.log4j.Logger;

public final class LogManager {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(LogManager.class);
    public static boolean showDebug = false;

    /**
     * The level of debug logs to show. 1 being lowest, 2 being meh, 3 being
     * EXTREEEEEEEME and 5 being OMG WTF SO MUCH LOGS
     */
    public static int debugLevel = 0;

    public static void info(String message) {
        logger.info(message);
    }

    public static void debug(String message) {
        if (showDebug) {
            logger.debug(message);
        }
    }

    public static void debugObject(Object object) {
        debug(Gsons.DEFAULT.toJson(object));
    }

    public static void debug(String message, int level) {
        if (debugLevel >= level) {
            debug(message);
        }
    }

    public static void warn(String message) {
        logger.warn(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

    public static void minecraft(String message) {
        logger.info(message);
    }

    public static void logStackTrace(Throwable t) {
        logStackTrace(t, true);
    }

    public static void logStackTrace(Throwable t, boolean sendRemote) {
        t.printStackTrace();

        CharArrayWriter writer = new CharArrayWriter();
        try {
            Analytics.sendException(t.getMessage());

            if (!(t instanceof LocalException) && sendRemote) {
                ErrorReporting.reportError(t);
            }

            t.printStackTrace(new PrintWriter(writer));
            error(writer.toString());
        } finally {
            writer.close();
        }
    }

    public static void logStackTrace(String message, Throwable t) {
        logStackTrace(message, t, true);
    }

    public static void logStackTrace(String message, Throwable t, boolean sendRemote) {
        error(message);
        logStackTrace(t, sendRemote);
    }
}
