/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

package com.atlauncher.evnt;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.gui.components.Console;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.writer.LogEventWriter;

import java.awt.Color;
import java.io.IOException;

public final class LogEvent {
    public static final int CONSOLE = 0xA;
    public static final int FILE = 0xB;

    public final LogType type;
    public final String body;
    public final int meta;

    public LogEvent(LogType type, String body) {
        this(type, body, CONSOLE | FILE);
    }

    public LogEvent(LogType type, String body, int meta) {
        this.type = type;
        if (App.settings != null && !LogManager.showDebug) {
            body = body.replace(FileSystem.BASE_DIR.toString(), "**USERSDIR**");
        }
        this.body = (!body.endsWith("\n") ? body + "\n" : body);
        this.meta = meta;
    }

    public void post(LogEventWriter writer) {
        if ((this.meta & CONSOLE) == CONSOLE) {
            Console c = App.settings.getConsole().console;
            c.setColor(this.type.color()).setBold(true).write("[" + Timestamper.now() + "] ");
            c.setColor(App.THEME.getConsoleTextColor()).setBold(false).write(this.body);
        }

        if ((this.meta & FILE) == FILE) {
            try {
                writer.write(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "[" + Timestamper.now() + "] [" + this.type.name() + "]" + this.body;
    }

    public static enum LogType {
        INFO, WARN, ERROR, DEBUG;

        public Color color() {
            switch (this) {
                case INFO: {
                    return App.THEME.getLogInfoColor();
                }
                case WARN: {
                    return App.THEME.getLogWarnColor();
                }
                case ERROR: {
                    return App.THEME.getLogErrorColor();
                }
                case DEBUG: {
                    return App.THEME.getLogDebugColor();
                }
                default: {
                    return App.THEME.getConsoleTextColor();
                }
            }
        }

    }
}