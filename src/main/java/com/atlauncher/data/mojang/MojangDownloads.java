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
package com.atlauncher.data.mojang;

import com.atlauncher.annot.Json;

@Json
public class MojangDownloads {
    private MojangDownload client;
    private MojangDownload server;
    private MojangDownload windows_server;

    public MojangDownload getClient() {
        return this.client;
    }

    public MojangDownload getServer() {
        return this.server;
    }

    public MojangDownload getWindowsServer() {
        return this.windows_server;
    }
}
