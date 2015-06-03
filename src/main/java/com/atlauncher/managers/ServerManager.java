/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.data.Constants;
import com.atlauncher.data.Server;

public class ServerManager {
    /**
     * Finds if a server is available
     *
     * @param name The name of the Server
     * @return true if found, false if not
     */
    public static boolean isServerByName(String name) {
        for (Server server : Constants.SERVERS) {
            if (server.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a Server from the given name
     *
     * @param name Name of the Server to find
     * @return Server if the server is found from the name
     */
    public static Server getServerByName(String name) {
        for (Server server : Constants.SERVERS) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }
}
