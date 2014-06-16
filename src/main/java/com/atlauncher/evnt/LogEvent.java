package com.atlauncher.evnt;

import java.awt.Color;
import java.io.IOException;

import com.atlauncher.App;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.writer.LogEventWriter;

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
                    return App.THEME.getLogDebugColor();
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
        this(type, body, CONSOLE | FILE);
    }

    public LogEvent(LogType type, String body, int meta) {
        this.type = type;
        this.body = (!body.endsWith("\n") ? body + System.getProperty("line.separator") : body);
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
        return "[" + Timestamper.now() + "] " + this.body;
    }
}