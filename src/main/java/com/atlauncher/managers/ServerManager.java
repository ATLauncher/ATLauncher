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

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Server;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class ServerManager {
    /**
     * Data holder for Servers.
     * <p>
     * Automatically updates subscribed entities downstream.
     */
    private static final BehaviorSubject<List<Server>> SERVERS = BehaviorSubject.createDefault(new ArrayList<>());

    /**
     * @return Observable list of servers.
     */
    public static Observable<List<Server>> getServersObservable() {
        return SERVERS;
    }

    /**
     * Loads the user installed servers
     */
    public static void loadServers() {
        PerformanceManager.start();
        LogManager.debug("Loading servers");
        ArrayList<Server> servers = new ArrayList<>();

        for (String folder : Optional.ofNullable(FileSystem.SERVERS.toFile().list(Utils.getServerFileFilter()))
                .orElse(new String[0])) {
            Path serverDir = FileSystem.SERVERS.resolve(folder);

            Server server;

            try (InputStreamReader fileReader = new InputStreamReader(
                    Files.newInputStream(serverDir.resolve("server.json")),
                    StandardCharsets.UTF_8)) {
                server = Gsons.DEFAULT.fromJson(fileReader, Server.class);
                server.ROOT = serverDir;
                LogManager.debug("Loaded server from " + serverDir);
            } catch (Exception e) {
                LogManager.logStackTrace("Failed to load server in the folder " + serverDir, e);
                continue;
            }

            servers.add(server);
        }

        SERVERS.onNext(servers);
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

    /**
     * Note, this method ignores ConstantConditions warning, as it always has.
     *
     * @param server
     *            server to add
     * @return if the server was added or not
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean addServer(Server server) {
        List<Server> servers = SERVERS.getValue();
        boolean added = servers.add(server);
        if (added) {
            SERVERS.onNext(servers);
        }
        return added;
    }

    public static void removeServer(Server server) {
        List<Server> servers = SERVERS.getValue();

        if (servers.remove(server)) {
            FileUtils.delete(server.getRoot(), true);
            SERVERS.onNext(servers);
        }
    }

    public static boolean isServer(String name) {
        return SERVERS.getValue().stream()
                .anyMatch(s -> s.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }
}
