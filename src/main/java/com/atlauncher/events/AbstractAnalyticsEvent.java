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
package com.atlauncher.events;

public abstract class AbstractAnalyticsEvent implements AnalyticsEvent{
    private final String label;
    private final AnalyticsAction action;
    private final AnalyticsCategory category;

    protected AbstractAnalyticsEvent(final String label, final AnalyticsAction action, final AnalyticsCategory category){
        super();
        this.label = label;
        this.action = action;
        this.category = category;
    }

    protected AbstractAnalyticsEvent(final AnalyticsAction action, final AnalyticsCategory category){
        this(null, action, category);
    }

    protected AbstractAnalyticsEvent(){
        this(null, null);
    }

    public final String getLabel(){
        return this.label;
    }

    public final String getAction(){
        return this.action.getAnalyticsValue();
    }

    public final String getCategory(){
        return this.category.getAnalyticsCategory();
    }
}