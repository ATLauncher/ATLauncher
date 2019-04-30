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
package com.atlauncher.data.loaders.forge;

import com.atlauncher.App;
import com.atlauncher.annot.Json;
import com.atlauncher.utils.Utils;

@Json
public class Data {
    private String client;
    private String server;

    public Data(String client, String server) {
        this.client = client;
        this.server = server;
    }

    public String getClient() {
        char start = this.client.charAt(0);
        char end = this.client.charAt(this.client.length() - 1);

        if (start == '[' && end == ']') {
            return Utils.convertMavenIdentifierToFile(this.client.substring(1, this.client.length() - 1),
                    App.settings.getGameLibrariesDir()).getAbsolutePath();
        }

        return this.client;
    }

    public String getServer() {
        char start = this.server.charAt(0);
        char end = this.server.charAt(this.server.length() - 1);

        if (start == '[' && end == ']') {
            return Utils.convertMavenIdentifierToFile(this.server.substring(1, this.server.length() - 1),
                    App.settings.getGameLibrariesDir()).getAbsolutePath();
        }

        return this.server;
    }
}
