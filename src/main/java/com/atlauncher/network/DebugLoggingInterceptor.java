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
import java.net.HttpURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class DebugLoggingInterceptor implements Interceptor {
    private static final Logger LOG = LogManager.getLogger(DebugLoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        LOG.debug("Sending request {}", request.url());

        String debugLogMessage = request.toString();
        if (request.header("Authorization") != null) {
            debugLogMessage = debugLogMessage.replace(request.header("Authorization"), "REDACTED");
        } else if (request.header("x-api-key") != null) {
            debugLogMessage = debugLogMessage.replace(request.header("x-api-key"), "REDACTED");
        }

        LOG.debug(debugLogMessage);

        long t1 = System.nanoTime();
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        if (response.cacheResponse() != null && (response.networkResponse() == null
                || response.networkResponse().code() == HttpURLConnection.HTTP_NOT_MODIFIED)) {
            LOG.debug(String.format("Received cached response code %d for %s in %.1fms", response.code(),
                    response.request().url(), (t2 - t1) / 1e6d), 3);
        } else {
            LOG.debug(String.format("Received response code %d for %s in %.1fms", response.code(),
                    response.request().url(), (t2 - t1) / 1e6d), 3);
        }

        LOG.debug("{}", response);
        return response;
    }
}
