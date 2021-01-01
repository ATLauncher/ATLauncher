/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.Color;

import javax.swing.UIManager;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.gui.components.Console;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Timestamper;

import org.apache.logging.log4j.Logger;

public final class LogEvent {
    public static final int CONSOLE = 0xA;
    public static final int LOG4J = 0xB;
    public final LogType type;
    public final String body;
    public final int meta;

    public LogEvent(LogType type, String body) {
        this(type, body, CONSOLE | LOG4J);
    }

    public LogEvent(LogType type, String body, int meta) {
        this.type = type;

        if (App.settings != null && !LogManager.showDebug) {
            body = body.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "**USERSDIR**");
        }

        this.body = (!body.endsWith("\n") ? body + "\n" : body);

        this.meta = meta;
    }

    public void post(Logger logger) {
        if ((this.meta & CONSOLE) == CONSOLE) {
            Console c = App.console.console;
            c.setColor(this.type.color()).setBold(true).write("[" + Timestamper.now() + "] ");
            c.setColor(UIManager.getColor("EditorPane.foreground")).setBold(false).write(this.body);
        }

        if ((this.meta & LOG4J) == LOG4J) {
            switch (type) {
                case WARN: {
                    logger.warn(body);
                    break;
                }
                case ERROR: {
                    logger.error(body);
                    break;
                }
                case DEBUG: {
                    logger.debug(body);
                    break;
                }
                case INFO:
                default: {
                    logger.info(body);
                    break;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "[" + Timestamper.now() + "] [" + this.type.name() + "]" + this.body;
    }

    public enum LogType {
        INFO, WARN, ERROR, DEBUG;

        public Color color() {
            switch (this) {
                case INFO: {
                    return UIManager.getColor("Console.LogType.info");
                }
                case WARN: {
                    return UIManager.getColor("Console.LogType.warn");
                }
                case ERROR: {
                    return UIManager.getColor("Console.LogType.error");
                }
                case DEBUG: {
                    return UIManager.getColor("Console.LogType.debug");
                }
                default: {
                    return UIManager.getColor("Console.LogType.default");
                }
            }
        }

    }
}
