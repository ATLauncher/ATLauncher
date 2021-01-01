/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.MinecraftServer;

public class CheckingServersManager {
    // timer for server checking tool
    private static Timer checkingServersTimer = null; // Timer used for checking servers

    public static List<MinecraftServer> getCheckingServers() {
        return Data.CHECKING_SERVERS;
    }

    /**
     * Loads the user servers added for checking
     */
    public static void loadCheckingServers() {
        PerformanceManager.start();
        LogManager.debug("Loading servers to check");
        Data.CHECKING_SERVERS.clear();

        if (Files.exists(FileSystem.CHECKING_SERVERS_JSON)) {
            FileReader fileReader;
            try {
                fileReader = new FileReader(FileSystem.CHECKING_SERVERS_JSON.toFile());
            } catch (FileNotFoundException e) {
                LogManager.logStackTrace(e);
                return;
            }

            Data.CHECKING_SERVERS.addAll(Gsons.DEFAULT.fromJson(fileReader, MinecraftServer.LIST_TYPE));

            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    LogManager
                            .logStackTrace("Exception while trying to close FileReader when loading servers for server "
                                    + "checker" + " tool.", e);
                }
            }
        }
        LogManager.debug("Finished loading servers to check");
        PerformanceManager.end();
    }

    public static void saveCheckingServers() {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (!Files.exists(FileSystem.CHECKING_SERVERS_JSON)) {
                Files.createFile(FileSystem.CHECKING_SERVERS_JSON);
            }

            fw = new FileWriter(FileSystem.CHECKING_SERVERS_JSON.toFile());
            bw = new BufferedWriter(fw);
            bw.write(Gsons.DEFAULT.toJson(Data.CHECKING_SERVERS));
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Exception while trying to close FileWriter/BufferedWriter when saving servers for "
                                + "server checker tool.",
                        e);
            }
        }
    }

    public static void startCheckingServers() {
        PerformanceManager.start();
        if (checkingServersTimer != null) {
            // If it's not null, cancel and purge tasks left
            checkingServersTimer.cancel();
            checkingServersTimer.purge(); // not sure if needed or not
        }

        if (App.settings.enableServerChecker) {
            checkingServersTimer = new Timer();
            checkingServersTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    for (MinecraftServer server : Data.CHECKING_SERVERS) {
                        server.checkServer();
                    }
                }
            }, 0, App.settings.serverCheckerWait * 1000);
        }
        PerformanceManager.end();
    }

    public static void addCheckingServer(MinecraftServer server) {
        Data.CHECKING_SERVERS.add(server);
        saveCheckingServers();
    }

    public static void removeCheckingServer(MinecraftServer server) {
        Data.CHECKING_SERVERS.remove(server);
        saveCheckingServers();
        startCheckingServers();
    }
}
