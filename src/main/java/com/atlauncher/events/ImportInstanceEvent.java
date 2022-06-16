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

import javax.swing.*;

public final class ImportInstanceEvent extends AbstractAnalyticsEvent{
    ImportInstanceEvent(final String source, final AnalyticsActions action){
        super(source, action, AnalyticsCategories.IMPORT_INSTANCE);
    }

    public static ImportInstanceEvent forUrl(final String url){
        return new ImportInstanceEvent(url, AnalyticsActions.ADD_FROM_URL);
    }

    public static ImportInstanceEvent forUrl(final JTextField field){
        return forUrl(field.getText());
    }

    public static ImportInstanceEvent forZip(final String path){
        return new ImportInstanceEvent(path, AnalyticsActions.ADD_FROM_ZIP);
    }

    public static ImportInstanceEvent forZip(final JTextField field){
        return forZip(field.getText());
    }
}