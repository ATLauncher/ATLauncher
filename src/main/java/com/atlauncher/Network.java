/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.atlauncher.constants.Constants;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.listener.ProgressListener;
import com.atlauncher.network.DebugLoggingInterceptor;
import com.atlauncher.network.ErrorReportingInterceptor;
import com.atlauncher.network.UserAgentInterceptor;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.ProgressResponseBody;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;

public final class Network {
    public static Cache CACHE = new Cache(FileSystem.CACHE.toFile(), 100 * 1024 * 1024); // 100MB cache

    private static List<Protocol> protocols = App.settings.dontUseHttp2 ? Arrays.asList(Protocol.HTTP_1_1)
            : Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

    public static OkHttpClient CLIENT = new OkHttpClient.Builder().protocols(protocols)
            .addNetworkInterceptor(new UserAgentInterceptor()).addInterceptor(new DebugLoggingInterceptor())
            .addNetworkInterceptor(new ErrorReportingInterceptor())
            .connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
            .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
            .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();

    public static OkHttpClient CACHED_CLIENT = CLIENT.newBuilder().cache(CACHE).build();

    public static final String USER_AGENT = String.format(
            "Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36 %s/%s Java/%s",
            OS.getUserAgentString(), Constants.LAUNCHER_NAME, Constants.VERSION.toStringForLogging(),
            Java.getLauncherJavaVersion());

    static {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    public static void setConnectionTimeouts() {
        CLIENT = CLIENT.newBuilder().connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();

        CACHED_CLIENT = CACHED_CLIENT.newBuilder().connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();
    }

    public static void setProtocols() {
        protocols = App.settings.dontUseHttp2 ? Arrays.asList(Protocol.HTTP_1_1)
                : Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1);

        CLIENT = CLIENT.newBuilder().protocols(protocols).build();
        CACHED_CLIENT = CACHED_CLIENT.newBuilder().protocols(protocols).build();
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

    public static void allowAllSslCerts() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            CLIENT = CLIENT.newBuilder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

            CACHED_CLIENT = CACHED_CLIENT.newBuilder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
