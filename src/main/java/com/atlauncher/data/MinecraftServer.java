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
package com.atlauncher.data;

import java.text.DecimalFormat;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.utils.MCQuery;
import com.google.gson.reflect.TypeToken;

import org.mini2Dx.gettext.GetText;

import de.zh32.pingtest.QueryVersion;

public class MinecraftServer {
    /**
     * The Type used for JSON reading for a List of this type
     */
    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<List<MinecraftServer>>() {
    }.getType();

    /**
     * The name of the server given by the user.
     */
    private String name;

    /**
     * The host/IP of the server to use when connecting to query it.
     */
    private String host;

    /**
     * The port to use when connecting to the server to query it.
     */
    private int port;

    /**
     * The QueryVersion to use when querying the server so we don't have to loop
     * through and try them all everytime.
     */
    private QueryVersion queryVersion;

    /**
     * The players online for the last check. -1 if offline.
     */
    transient private int playersOnline;

    /**
     * If we have run a check before or not.
     */
    transient private boolean hasRun;

    /**
     * Default constructor for creating an instance of this class.
     *
     * @param name         The friendly name of the server shown to the user
     * @param host         The host/IP of the server
     * @param port         The port of the server
     * @param queryVersion The version of Minecraft querying we should use
     */
    public MinecraftServer(String name, String host, int port, QueryVersion queryVersion) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.queryVersion = queryVersion;
    }

    /**
     * Checks this server to see if it's online or not and if a notification should
     * be displayed to the user.
     */
    public void checkServer() {
        int playersOnline = MCQuery.getNumberOfPlayers(this.host, this.port, this.queryVersion);

        if (!this.hasRun) {
            App.TOASTER.pop(String.format("Server Checking on %s has started!", this.name));
            this.hasRun = true;
        } else {
            if (playersOnline == -1 && this.playersOnline >= 0) {
                // The server WAS online and now it isn't
                App.TOASTER.popError(String.format("Server %s is now offline!", this.name));
            } else if (playersOnline >= 0 && this.playersOnline == -1) {
                // The server WAS offline and now it isn't
                App.TOASTER
                        .pop(String.format("Server %s is now online with %d players", this.name, this.playersOnline));
            }
        }

        this.playersOnline = playersOnline; // Set it for the next check
    }

    /**
     * Gets the friendly name of this server as specified by the user.
     *
     * @return The name of this server
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public QueryVersion getQueryVersion() {
        return this.queryVersion;
    }

    public void setQueryVersion(QueryVersion queryVersion) {
        this.queryVersion = queryVersion;
    }

    public String getPrintablePlayersOnline() {
        DecimalFormat df = new DecimalFormat("#,###,###");
        return df.format(this.playersOnline);
    }

    private String getStatusLocalization() {
        if (this.playersOnline == -1) {
            return GetText.tr("Offline");
        } else {
            return GetText.tr("Online") + " - " + this.getPrintablePlayersOnline()
                    + " Players";
        }
    }

    public String toString() {
        return String.format("%s (%s:%d) - %s", this.name, this.host, this.port, this.getStatusLocalization());
    }
}
