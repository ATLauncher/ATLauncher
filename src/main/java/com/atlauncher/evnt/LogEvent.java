package com.atlauncher.evnt;

import java.awt.Color;

import com.atlauncher.App;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Timestamper;

public final class LogEvent {
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

    public LogEvent(LogType type, String body) {
        this.type = type;
        this.body = (!body.endsWith("\n") ? body + "\n" : body);
    }

    public void post() {
        Console c = App.settings.getConsole().console;
        c.setColor(this.type.color()).setBold(true).write("[" + Timestamper.now() + "] ");
        c.setColor(App.THEME.getConsoleTextColor()).setBold(false).write(this.body);
    }

    @Override
    public String toString() {
        return "[" + Timestamper.now() + "] " + this.body;
    }
}