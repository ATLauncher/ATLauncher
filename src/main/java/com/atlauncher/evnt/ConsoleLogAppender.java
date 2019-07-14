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
package com.atlauncher.evnt;

import java.io.Serializable;

import com.atlauncher.App;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Timestamper;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "ConsoleLogAppender", category = "Core", elementType = "appender", printObject = true)
public class ConsoleLogAppender extends AbstractAppender {
    protected ConsoleLogAppender(String name, Filter filter, Layout<? extends Serializable> layout,
            boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {
        try {
            if (event.getLevel().equals(Level.INFO)) {
                logInfo(event);
            } else if (event.getLevel().equals(Level.WARN)) {
                logWarn(event);
            } else if (event.getLevel().equals(Level.ERROR)) {
                logError(event);
            } else if (event.getLevel().equals(Level.DEBUG)) {
                logDebug(event);
            }
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        }
    }

    private void logInfo(LogEvent event) {
        Console console = App.console.console;
        console.setColor(App.THEME.getLogInfoColor()).setBold(true).write("[" + Timestamper.now() + "] ");
        console.setColor(App.THEME.getConsoleTextColor()).setBold(false)
                .write(new String(getLayout().toByteArray(event)));
    }

    private void logWarn(LogEvent event) {
        Console console = App.console.console;
        console.setColor(App.THEME.getLogWarnColor()).setBold(true).write("[" + Timestamper.now() + "] ");
        console.setColor(App.THEME.getConsoleTextColor()).setBold(false)
                .write(new String(getLayout().toByteArray(event)));
    }

    private void logError(LogEvent event) {
        Console console = App.console.console;
        console.setColor(App.THEME.getLogErrorColor()).setBold(true).write("[" + Timestamper.now() + "] ");
        console.setColor(App.THEME.getConsoleTextColor()).setBold(false)
                .write(new String(getLayout().toByteArray(event)));
    }

    private void logDebug(LogEvent event) {
        Console console = App.console.console;
        console.setColor(App.THEME.getLogDebugColor()).setBold(true).write("[" + Timestamper.now() + "] ");
        console.setColor(App.THEME.getConsoleTextColor()).setBold(false)
                .write(new String(getLayout().toByteArray(event)));
    }

    @PluginFactory
    public static ConsoleLogAppender createAppender(@PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for ConsoleLogAppender");
            return null;
        }

        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        return new ConsoleLogAppender(name, filter, layout, true, Property.EMPTY_ARRAY);
    }
}
