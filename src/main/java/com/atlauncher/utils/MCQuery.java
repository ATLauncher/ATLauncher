/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

import java.io.IOException;
import java.net.InetSocketAddress;

import de.zh32.pingtest.QueryVersion;
import de.zh32.pingtest.ServerListPing14;
import de.zh32.pingtest.ServerListPing16;
import de.zh32.pingtest.ServerListPing17;
import de.zh32.pingtest.ServerListPingB18;

public class MCQuery {

    public static QueryVersion getMinecraftServerQueryVersion(String host, int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

        QueryVersion queryVersion = null;

        try {
            ServerListPingB18 pingb18 = new ServerListPingB18();
            pingb18.setAddress(inetSocketAddress);
            pingb18.fetchData();
            queryVersion = QueryVersion.mc18b;
        } catch (Exception ex) {
        }

        System.out.println("SLP 1.4+");
        try {
            ServerListPing14 ping14 = new ServerListPing14();
            ping14.setAddress(inetSocketAddress);
            ping14.fetchData();
            queryVersion = QueryVersion.mc14;
        } catch (Exception ex) {
        }

        System.out.println("SLP 1.6+");
        try {
            ServerListPing16 ping16 = new ServerListPing16();
            ping16.setAddress(inetSocketAddress);
            ping16.fetchData();
            queryVersion = QueryVersion.mc16;
        } catch (Exception ex) {
        }

        System.out.println("SLP 1.7+");
        try {
            ServerListPing17 ping17 = new ServerListPing17();
            ping17.setAddress(inetSocketAddress);
            ServerListPing17.StatusResponse response = ping17.fetchData();
            queryVersion = QueryVersion.mc17;
        } catch (IOException ex) {
        }

        return queryVersion;
    }

}