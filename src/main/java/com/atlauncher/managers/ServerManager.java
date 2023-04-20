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
package com.atlauncher.managers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingUtilities;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Server;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;

public class ServerManager {
    private static final List<Listener> listeners = new LinkedList<>();

    public static synchronized void addListener(Listener listener) {
        listeners.add(listener);
    }

    public static synchronized void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public static synchronized void post() {
        SwingUtilities.invokeLater(() -> {
            for (Listener listener : listeners) {
                listener.onServersChanged();
            }
        });
    }

    public static List<Server> getServers() {
        return Data.SERVERS;
    }

    /**
     * Loads the user installed servers
     */
    public static void loadServers() {
        PerformanceManager.start();
        LogManager.debug("Loading servers");
        Data.SERVERS.clear();

        for (String folder : Optional.of(FileSystem.SERVERS.toFile().list(Utils.getServerFileFilter()))
                .orElse(new String[0])) {
            File serverDir = FileSystem.SERVERS.resolve(folder).toFile();

            Server server;

            try (FileReader fileReader = new FileReader(new File(serverDir, "server.json"))) {
                server = Gsons.DEFAULT.fromJson(fileReader, Server.class);
                LogManager.debug("Loaded server from " + serverDir);
            } catch (Exception e) {
                LogManager.logStackTrace("Failed to load server in the folder " + serverDir, e);
                continue;
            }

            if (server == null) {
                LogManager.error("Failed to load server in the folder " + serverDir);
                continue;
            }

            Data.SERVERS.add(server);
        }

        LogManager.debug("Finished loading servers");
        PerformanceManager.end();
    }

    public static void setServerVisibility(Server server, boolean collapsed) {
        if (server != null) {
            if (collapsed) {
                // Closed It
                if (!AccountManager.getSelectedAccount().collapsedServers.contains(server.name)) {
                    AccountManager.getSelectedAccount().collapsedServers.add(server.name);
                }
            } else {
                // Opened It
                AccountManager.getSelectedAccount().collapsedServers.remove(server.name);
            }
            AccountManager.saveAccounts();
        }
    }

    public static ArrayList<Server> getServersSorted() {
        ArrayList<Server> servers = new ArrayList<>(Data.SERVERS);
        servers.sort(Comparator.comparing(s -> s.name));
        return servers;
    }

    /**
     * Note, this method ignores ConstantConditions warning, as it always has.
     *
     * @param server server to add
     * @return if the server was added or not
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean addServer(Server server) {
        boolean added = Data.SERVERS.add(server);
        if (added)
            post();
        return added;
    }

    public static void removeServer(Server server) {
        if (Data.SERVERS.remove(server)) {
            FileUtils.delete(server.getRoot(), true);
            post();
        }
    }

    public static boolean isServer(String name) {
        return Data.SERVERS.stream()
                .anyMatch(s -> s.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    public interface Listener {
        void onServersChanged();
    }
}
