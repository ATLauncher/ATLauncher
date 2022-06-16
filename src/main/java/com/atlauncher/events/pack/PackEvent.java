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
package com.atlauncher.events.pack;

import com.atlauncher.data.Instance;
import com.atlauncher.events.AbstractAnalyticsEvent;
import com.atlauncher.events.AnalyticsAction;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategory;

public abstract class PackEvent extends AbstractAnalyticsEvent{
    private static String getPackName(final Instance instance){
        return instance.launcher.pack;
    }

    private static String getPackVersion(final Instance instance){
        return instance.launcher.version;
    }

    private static String getLabel(final String packName, final String packVersion){
        return String.format("%s - %s", packName, packVersion);
    }

    private static String getLabel(final Instance instance){
        return getLabel(getPackName(instance), getPackVersion(instance));
    }

    protected PackEvent(final String packName, final String packVersion, final AnalyticsAction action, final AnalyticsCategory category){
        super(getLabel(packName, packVersion), action, category);
    }

    protected PackEvent(final Instance instance, final AnalyticsAction action, final AnalyticsCategory category){
        super(getLabel(instance), action, category);
    }

    protected PackEvent(final Instance instance, final AnalyticsAction action){
        this(instance, action, instance);
    }
}