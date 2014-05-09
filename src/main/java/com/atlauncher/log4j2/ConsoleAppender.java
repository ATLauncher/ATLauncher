/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.log4j2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import com.atlauncher.App;
import com.atlauncher.utils.HTMLifier;
import com.atlauncher.utils.Timestamper;

@Plugin(name = "ATLauncher-Console", category = "Core", elementType = "appender",
        printObject = true)
public final class ConsoleAppender extends AbstractOutputStreamAppender {

    private final Map<Level, String> LEVEL_COLOURS = new HashMap<Level, String>() {
        {
            this.put(Level.INFO, "#89C236");
            this.put(Level.WARN, "#FFFF4C");
            this.put(Level.ERROR, "#EE2222");
        }
    };

    protected ConsoleAppender(String name, Layout<? extends Serializable> layout, Filter filter,
            boolean ignoreExceptions, boolean immediateFlush, OutputStreamManager manager) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
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
        return new ConsoleAppender("ATLauncher-Console", null, null, false, false, null);
    }
}
