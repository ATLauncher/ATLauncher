/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.log4j2;

import com.atlauncher.App;
import com.atlauncher.utils.HTMLifier;
import com.atlauncher.utils.Timestamper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Plugin(name = "ATLauncher-Console", category = "Core", elementType = "appender", printObject = true)
public final class ConsoleAppender extends AbstractAppender{
    private final Map<Level, String> LEVEL_COLOURS = new HashMap<Level, String>() {
        {
            this.put(Level.INFO, App.THEME.getLogInfoTextColourHTML());
            this.put(Level.WARN, App.THEME.getLogWarnTextColourHTML());
            this.put(Level.ERROR, App.THEME.getLogErrorTextColourHTML());
        }
    };

    protected ConsoleAppender(String name, Layout<? extends Serializable> layout, Filter filter) {
        super(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        App.settings.getConsole().log(
                String.format(
                        "%s %s<br/>",
                        HTMLifier
                                .wrap("[" + Timestamper.now() + "] [" + event.getLevel().name()
                                        + "]").bold()
                                .font(this.LEVEL_COLOURS.get(event.getLevel())), event.getMessage()
                                .getFormattedMessage()));
    }

    @PluginFactory()
    public static ConsoleAppender createAppender() {
        return new ConsoleAppender("ATLauncher-Console", null, null);
    }
}
