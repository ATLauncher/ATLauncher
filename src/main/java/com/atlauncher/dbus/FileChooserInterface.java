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
package com.atlauncher.dbus;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

import java.util.Map;

@DBusInterfaceName(value = "org.freedesktop.portal.FileChooser")
public interface FileChooserInterface extends DBusInterface {
    DBusPath OpenFile(String parentWindow, String title, Map<String, Variant> options);

    DBusPath SaveFile(String parentWindow, String title, Map<String, Variant> options);

    DBusPath SaveFiles(String parentWindow, String title, Map<String, Variant> options);
}
