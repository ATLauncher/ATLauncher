package com.atlauncher.logging;

import com.atlauncher.exceptions.LocalException;
import com.atlauncher.network.ErrorReporting;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name=LauncherReportingAppender.PLUGIN_NAME,
        category=LauncherReportingAppender.PLUGIN_CATEGORY,
        elementType=LauncherReportingAppender.ELEMENT_TYPE,
        printObject = true)
public final class LauncherReportingAppender extends AbstractAppender{
    public static final String PLUGIN_NAME = "LauncherReporter";
    public static final String PLUGIN_CATEGORY = Core.CATEGORY_NAME;

    public LauncherReportingAppender(final String name, final Filter filter){
        super(name, filter, null, false, null);
    }

    @Override
    public void append(LogEvent event){
        final Throwable th = event.getThrown();
        if(!(th instanceof LocalException)){ // don't report LocalExceptions
            // fallthrough
        }
    }

    @PluginFactory
    public static LauncherReportingAppender createAppender(@PluginAttribute("name") final String name,
                                                           @PluginElement("Filter") final Filter filter){
        return new LauncherReportingAppender(name, filter);
    }
}
