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
package com.atlauncher;

import com.atlauncher.listener.ProgressListener;
import com.atlauncher.utils.ProgressResponseBody;
import com.atlauncher.workers.InstanceInstaller;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;

public final class Network {
    public static final OkHttpClient CLIENT = new OkHttpClient();
    public static final OkHttpClient PROGRESS_CLIENT = new OkHttpClient();

    public static void setupProgressClient(final InstanceInstaller installer) {
        Network.PROGRESS_CLIENT.networkInterceptors().clear();

        final ProgressListener progressListener = new ProgressListener() {
            public void update(long bytesRead, long contentLength, boolean done) {
                if (bytesRead > 0 && installer != null) {
                    installer.addDownloadedBytes((int) bytesRead);
                }
            }
        };

        Network.PROGRESS_CLIENT.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body(),
                        progressListener)).build();
            }
        });
    }
}