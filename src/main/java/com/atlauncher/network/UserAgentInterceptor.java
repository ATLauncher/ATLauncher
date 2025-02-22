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

import com.atlauncher.Network;
import com.atlauncher.constants.Constants;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class UserAgentInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        boolean internalHost = originalRequest.url().host().equals(Constants.API_HOST)
                || originalRequest.url().host().equals(Constants.PASTE_HOST)
                || originalRequest.url().host().equals(Constants.DOWNLOAD_HOST);
        String userAgent = internalHost ? Network.API_USER_AGENT
                : Network.USER_AGENT;
        Request requestWithUserAgent = originalRequest.newBuilder().removeHeader("User-Agent")
                .addHeader("User-Agent", userAgent).build();

        return chain.proceed(requestWithUserAgent);
    }
}
