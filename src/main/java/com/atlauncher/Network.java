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
package com.atlauncher;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atlauncher.data.Constants;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.listener.ProgressListener;
import com.atlauncher.network.DebugLoggingInterceptor;
import com.atlauncher.network.ErrorReportingInterceptor;
import com.atlauncher.network.UserAgentInterceptor;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ProgressResponseBody;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public final class Network {
    private static Cache cache = new Cache(FileSystem.CACHE.toFile(), 100 * 1024 * 1024); // 100MB cache

    public static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .addNetworkInterceptor(new UserAgentInterceptor()).addInterceptor(new DebugLoggingInterceptor())
            .addNetworkInterceptor(new ErrorReportingInterceptor()).build();

    public static final OkHttpClient CACHED_CLIENT = CLIENT.newBuilder().cache(cache).build();

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like "
            + "Gecko) Chrome/28.0.1500.72 Safari/537.36 " + Constants.LAUNCHER_NAME + "/" + Constants.VERSION + " Java/"
            + Java.getLauncherJavaVersion();

    static {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    public static OkHttpClient createProgressClient(final NetworkProgressable progressable) {
        final ProgressListener progressListener = (bytesRead, contentLength, done) -> {
            if (bytesRead > 0 && progressable != null) {
                progressable.addDownloadedBytes(bytesRead);
            }
        };

        return Network.CLIENT.newBuilder().addNetworkInterceptor(chain -> {
            Response originalResponse = chain.proceed(chain.request());
            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), progressListener)).build();
        }).build();
    }
}
