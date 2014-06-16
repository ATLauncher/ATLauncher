package com.atlauncher.evnt;

import java.awt.Color;

import com.atlauncher.App;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Timestamper;

public final class LogEvent {
    public static final int CONSOLE = 0xA;
    public static final int FILE = 0xB;

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
                    return Color.MAGENTA;
                }
                default: {
                    return App.THEME.getConsoleTextColor();
                }
            }
        }

    }

    public final LogType type;
    public final String body;
    public final int meta;

    public LogEvent(LogType type, String body) {
        this(type, body, CONSOLE);
    }

    public LogEvent(LogType type, String body, int meta) {
        this.type = type;
        this.body = (!body.endsWith("\n") ? body + "\n" : body);
        this.meta = meta;
    }

    public void post() {
        if ((this.meta & CONSOLE) == CONSOLE) {
            Console c = App.settings.getConsole().console;
            c.setColor(this.type.color()).setBold(true).write("[" + Timestamper.now() + "] ");
            c.setColor(App.THEME.getConsoleTextColor()).setBold(false).write(this.body);
        }
        if ((this.meta & FILE) == FILE) {
            // TODO: File logging
        }
    }

    @Override
    public String toString() {
        return "[" + Timestamper.now() + "] " + this.body;
    }
}