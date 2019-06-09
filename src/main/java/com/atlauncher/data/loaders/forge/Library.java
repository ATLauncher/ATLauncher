/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import java.util.List;

import com.atlauncher.annot.Json;

@Json
public class Library {
    private String name;
    private String url; // in <= 1.12.3
    private List<String> checksums; // in <= 1.12.3
    private boolean clientreq = true; // in <= 1.12.3
    private boolean serverreq = true; // in <= 1.12.3
    private Downloads downloads;

    public String getName() {
        return this.name;
    }

    public List<String> getChecksums() {
        return this.checksums;
    }

    public String getUrl() {
        return this.url;
    }

    public boolean isUsingPackXz() {
        return this.url != null && this.checksums != null && this.checksums.size() == 2;
    }

    public boolean hasUrl() {
        return this.url != null;
    }

    public boolean isClientReq() {
        return this.clientreq;
    }

    public boolean isServerReq() {
        return this.serverreq;
    }

    public Downloads getDownloads() {
        return this.downloads;
    }
}
