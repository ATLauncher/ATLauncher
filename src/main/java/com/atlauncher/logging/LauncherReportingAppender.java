/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.logging;

import com.atlauncher.AppEventBus;
import com.atlauncher.events.ExceptionEvent;
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

@Plugin(name = LauncherReportingAppender.PLUGIN_NAME, category = LauncherReportingAppender.PLUGIN_CATEGORY, elementType = LauncherReportingAppender.ELEMENT_TYPE, printObject = true)
public final class LauncherReportingAppender extends AbstractAppender {
    public static final String PLUGIN_NAME = "LauncherReporter";
    public static final String PLUGIN_CATEGORY = Core.CATEGORY_NAME;

    public LauncherReportingAppender(final String name, final Filter filter) {
        super(name, filter, null, false, null);
    }

    @Override
    public void append(LogEvent event) {
        final Throwable th = event.getThrown();
        if (th != null && !(th instanceof LocalException)) { // don't report LocalExceptions
            AppEventBus.postToDefault(ExceptionEvent.forException(th));
            ErrorReporting.captureException(th);
        }
    }

    @PluginFactory
    public static LauncherReportingAppender createAppender(@PluginAttribute("name") final String name,
                                                           @PluginElement("Filter") final Filter filter) {
        return new LauncherReportingAppender(name, filter);
    }
}
