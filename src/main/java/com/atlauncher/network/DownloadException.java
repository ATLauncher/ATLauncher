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

import com.atlauncher.managers.LogManager;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadException extends IOException {
    public String response = null;
    public String contentType = null;
    public Integer statusCode = null;

    public DownloadException(Download download) {
        super(download.url + " request wasn't successful: " + download.response);

        if (download.response != null) {
            try {
                ResponseBody responseBody = download.response.body();

                this.response = responseBody != null ? responseBody.string() : null;
                this.statusCode = download.response.code();
                this.contentType = download.response.header("Content-Type");
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    public DownloadException(Response failedResponse) {
        super(failedResponse.request().url() + " request wasn't successful: " + failedResponse);

        try {
            ResponseBody responseBody = failedResponse.body();

            this.response = responseBody != null ? responseBody.string() : null;
            this.statusCode = failedResponse.code();
            this.contentType = failedResponse.header("Content-Type");
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public String getResponse() {
        return this.response;
    }

    public boolean hasResponse() {
        if (this.contentType == null || this.response == null) {
            return false;
        }

        return contentType.equalsIgnoreCase("application/json")
                || contentType.equalsIgnoreCase("application/xml")
                || contentType.startsWith("text/");
    }
}
