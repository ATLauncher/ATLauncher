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
package com.atlauncher.network;

import java.io.IOException;

@SuppressWarnings("serial")
public class DownloadException extends IOException {
    public Download download;
    public String response = null;
    public Integer statusCode = null;

    public DownloadException(Download download) {
        super(download.url + " request wasn't successful: " + download.response);

        this.download = download;

        if (download.response != null) {
            try {
                this.response = download.response.body().string();
                this.statusCode = download.response.code();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getResponse() {
        return this.response;
    }

    public boolean hasResponse() {
        if (this.download.response == null || this.response == null)
            return false;
        final String contentType = this.download.response.header("Content-Type");
        return contentType.equalsIgnoreCase("application/json")
                || contentType.equalsIgnoreCase("application/xml")
                || contentType.startsWith("text/");
    }
}
