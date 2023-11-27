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

import java.util.Map;

import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.types.Variant;

/**
 * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html">DynamicLauncher</a>
 */
@DBusInterfaceName(value = "org.freedesktop.portal.DynamicLauncher")
public interface DynamicLauncher extends DBusInterface {
    /**
     * @param token           Token proving authorization of the installation
     * @param desktop_file_id The .desktop file name to be used
     * @param desktop_entry   The text of the Desktop Entry file to be installed, see below
     * @param options         Vardict with optional further information
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-install">
     * Install</a>
     */
    void Install(String token,
                 String desktop_file_id,
                 String desktop_entry,
                 Map<String, Variant> options);

    /**
     * @param parentWindow
     * @param name
     * @param icon_v
     * @param options
     * @return
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-prepareinstall">
     * PrepareInstall</a>
     */
    DBusPath PrepareInstall(String parentWindow,
                            String name,
                            Variant icon_v,
                            Map<String, Variant> options);

    /**
     * @param name
     * @param icon_v
     * @param options
     * @return
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-requestinstalltoken">
     * RequestInstallToken</a>
     */
    String RequestInstallToken(String name,
                               Variant icon_v,
                               Map<String, Variant> options);

    /**
     * @param desktop_file_id
     * @param options
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-uninstall">
     * Uninstall</a>
     */
    void Uninstall(String desktop_file_id,
                   Map<String, Variant> options);

    /**
     * @param desktop_file_id
     * @return
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-getdesktopentry">
     * GetDesktopEntry</a>
     */
    String GetDesktopEntry(String desktop_file_id);

    /**
     * @param desktop_file_id
     * @return TODO FIGURE THIS OUT
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-geticon">
     * GetIcon</a>
     */
    void GetIcon(String desktop_file_id);

    /**
     * @param desktop_file_id
     * @param options
     * @see <a href="https://flatpak.github.io/xdg-desktop-portal/docs/doc-org.freedesktop.portal.DynamicLauncher.html#org-freedesktop-portal-dynamiclauncher-launch">
     * Launch</a>
     */
    void Launch(String desktop_file_id,
                Map<String, Variant> options);
}
