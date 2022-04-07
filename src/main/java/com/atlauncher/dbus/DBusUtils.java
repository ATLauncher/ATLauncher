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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

import com.atlauncher.managers.LogManager;

import org.freedesktop.dbus.DBusMap;
import org.freedesktop.dbus.DBusMatchRule;
import org.freedesktop.dbus.DBusPath;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.interfaces.DBusSigHandler;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.Variant;

public class DBusUtils {
    public static File[] selectFiles() {
        try {
            DBusConnection bus = DBusConnection.getConnection(DBusConnection.DBusBusType.SESSION);
            System.out.printf("Unique name: %s%n", bus.getUniqueName());

            String token = UUID.randomUUID().toString().replaceAll("-", "");
            String sender = bus.getUniqueName().substring(1).replace('.', '_');
            String path = String.format("/org/freedesktop/portal/desktop/request/%s/%s", sender, token);
            TransferQueue<Optional<String[]>> queue = new LinkedTransferQueue<>();

            DBusMatchRule matchRule = new DBusMatchRule("signal", "org.freedesktop.portal.Request", "Response");
            bus.addGenericSigHandler(matchRule, new DBusSigHandler<DBusSignal>() {
                @Override
                public void handle(DBusSignal t) {
                    System.out.println("--- signal received ---");
                    System.out.println(t);
                    if (path.equals(t.getPath())) {
                        try {
                            Object[] params = t.getParameters();
                            System.out.printf("params: size=%d%n", params.length);
                            UInt32 response = (UInt32) params[0];
                            DBusMap<String, Variant> results = (DBusMap) params[1];

                            if (response.intValue() == 0) {
                                System.out.println("Screenshot successfull");
                                Variant vuris = results.get("uris");
                                ArrayList<String> uris = (ArrayList<String>) vuris.getValue();
                                System.out.printf("uris: %s%n", uris);

                                if (uris != null){
                                    queue.add(Optional.of(uris.toArray(new String[uris.size()])));
                                } else {
                                    queue.add(Optional.empty());
                                }
                            } else {
                                System.out.printf("Failed: response=%s%n", response);
                                queue.add(Optional.empty());
                            }

                            bus.removeGenericSigHandler(matchRule, this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            FileChooserInterface iface = bus.getRemoteObject("org.freedesktop.portal.Desktop",
                    "/org/freedesktop/portal/desktop", FileChooserInterface.class);
            Map<String, Variant> options = new HashMap<>();
            options.put("directory", new Variant(Boolean.FALSE));
            options.put("handle_token", new Variant(token));
            DBusPath result = iface.OpenFile("", "Pick File", options);
            System.out.printf("       result: %s%n", result);
            System.out.printf("expected path: %s%n", path);

            Optional<String[]> selectedFiles = queue.take();
            if (selectedFiles.isPresent()) {
                return Arrays.stream(selectedFiles.get()).map(f -> {
                    System.out.println(f);

                    return new File(f);
                }).toArray(size -> new File[selectedFiles.get().length]);
            }
        } catch (Throwable t) {
            LogManager.logStackTrace("Error selecting files using DBus", t);
        }

        return new File[0];
    }
}
