/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

import java.net.InetSocketAddress;

import de.zh32.pingtest.QueryVersion;
import de.zh32.pingtest.ServerListPing14;
import de.zh32.pingtest.ServerListPing16;
import de.zh32.pingtest.ServerListPing17;
import de.zh32.pingtest.ServerListPing17.StatusResponse;
import de.zh32.pingtest.ServerListPingB18;

public class MCQuery {

    public static QueryVersion getMinecraftServerQueryVersion(String host, int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

        try {
            ServerListPing16 ping16 = new ServerListPing16();
            ping16.setAddress(inetSocketAddress);
            ping16.fetchData();
            return QueryVersion.mc16;
        } catch (Exception ex) {
        }

        try {
            ServerListPing14 ping14 = new ServerListPing14();
            ping14.setAddress(inetSocketAddress);
            ping14.fetchData();
            return QueryVersion.mc14;
        } catch (Exception ex) {
        }

        try {
            ServerListPingB18 pingb18 = new ServerListPingB18();
            pingb18.setAddress(inetSocketAddress);
            pingb18.fetchData();
            return QueryVersion.mc18b;
        } catch (Exception ex) {
        }

        // 1.7 must be queried last because if not, the rest of the checks will ALWAYS fail for some
        // reason of which I'm not sure of yet
        try {
            ServerListPing17 ping17 = new ServerListPing17();
            ping17.setAddress(inetSocketAddress);
            ping17.fetchData();
            return QueryVersion.mc17;
        } catch (Exception ex) {
        }

        return null; // Couldn't determine version so return null
    }

    public static int getNumberOfPlayers(String host, int port, QueryVersion qv) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

        switch (qv) {
            case mc14:
                try {
                    ServerListPing14 ping14 = new ServerListPing14();
                    ping14.setAddress(inetSocketAddress);
                    ping14.fetchData();
                    return ping14.getPlayersOnline();
                } catch (Exception ex) {
                }
                return -1; // Server not reachable so send -1 for offline
            case mc16:
                try {
                    ServerListPing16 ping16 = new ServerListPing16();
                    ping16.setAddress(inetSocketAddress);
                    ping16.fetchData();
                    return ping16.getPlayersOnline();
                } catch (Exception ex) {
                }
                return -1; // Server not reachable so send -1 for offline
            case mc17:
                try {
                    ServerListPing17 ping17 = new ServerListPing17();
                    ping17.setAddress(inetSocketAddress);
                    StatusResponse response = ping17.fetchData();
                    return response.getPlayers().getOnline();
                } catch (Exception ex) {
                }
                return -1; // Server not reachable so send -1 for offline
            case mc18b:
                try {
                    ServerListPingB18 pingb18 = new ServerListPingB18();
                    pingb18.setAddress(inetSocketAddress);
                    pingb18.fetchData();
                    return pingb18.getPlayersOnline();
                } catch (Exception ex) {
                }
                return -1; // Server not reachable so send -1 for offline
            default:
                return -1; // Meaning server unreachable since there was no match to the qv
        }
    }
}