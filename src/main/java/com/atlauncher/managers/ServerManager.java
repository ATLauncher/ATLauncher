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

import com.atlauncher.App;
import com.atlauncher.AppEventBus;
import com.atlauncher.AppTaskEngine;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Server;
import com.atlauncher.events.servers.ServerAddedEvent;
import com.atlauncher.events.servers.ServerRemovedEvent;
import com.atlauncher.task.LoadServersTask;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerManager {
    private static final Logger LOG = LogManager.getLogger(ServerManager.class);
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Set<Server> servers = new TreeSet<>();

    public static Set<Server> getServers(){
        final ReentrantReadWriteLock.ReadLock lock = getReadLock();
        try{
            lock.lock();
            return ImmutableSet.copyOf(servers);
        } finally{
            lock.unlock();
        }
    }

    /**
     * Loads the user installed servers
     */
    public static void loadServers() {
        final LoadServersTask task = LoadServersTask.of(FileSystem.SERVERS)
            .build();
        AppTaskEngine.submit(task);
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

    public static void addServer(@Nonnull final Server server){
        Preconditions.checkNotNull(server);
        registerServer(server);
        AppEventBus.post(ServerAddedEvent.of(server));
    }

    public static void removeServer(@Nonnull final Server server) {
        Preconditions.checkNotNull(server);
        unregisterServer(server);
        AppEventBus.post(ServerRemovedEvent.of(server));
    }

    public static boolean isServer(String name) {
        return Data.SERVERS.stream()
            .anyMatch(s -> s.getSafeName().equalsIgnoreCase(name.replaceAll("[^A-Za-z0-9]", "")));
    }

    private static ReentrantReadWriteLock.WriteLock getWriteLock(){
        return rwLock.writeLock();
    }

    private static ReentrantReadWriteLock.ReadLock getReadLock(){
        return rwLock.readLock();
    }

    private static void registerServer(@Nonnull final Server server){
        Preconditions.checkNotNull(server);

        ReentrantReadWriteLock.WriteLock lock = getWriteLock();
        try{
            lock.lock();
            servers.add(server);
        } finally{
            lock.unlock();
        }
    }

    private static void unregisterServer(@Nonnull final Server server){
        Preconditions.checkNotNull(server);

        ReentrantReadWriteLock.WriteLock lock = getWriteLock();
        try{
            lock.lock();
            servers.remove(server);
        } finally{
            lock.unlock();
        }
    }
}
