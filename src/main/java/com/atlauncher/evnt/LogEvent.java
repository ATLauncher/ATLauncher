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
    public final boolean logToConsole;
    public final boolean logToFile;

    public LogEvent(LogType type, String body) {
        this(type, body, true, true);
    }

    public LogEvent(LogType type, String body, boolean logToConsole) {
        this(type, body, logToConsole, true);
    }

    public LogEvent(LogType type, String body, boolean logToConsole, boolean logToFile) {
        this.type = type;
        this.body = (!body.endsWith("\n") ? body + "\n" : body);
        this.logToConsole = logToConsole;
        this.logToFile = logToFile;
    }

    public void post() {
        if (this.logToConsole) {
            Console c = App.settings.getConsole().console;
            c.setColor(this.type.color()).setBold(true).write("[" + Timestamper.now() + "] ");
            c.setColor(App.THEME.getConsoleTextColor()).setBold(false).write(this.body);
        }
        if (this.logToFile) {
            // TODO: File logging
        }
    }

    @Override
    public String toString() {
        return "[" + Timestamper.now() + "] " + this.body;
    }

}