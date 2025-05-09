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
package com.atlauncher;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.apollographql.apollo.ApolloClientAwarenessInterceptor;
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
import okhttp3.tls.HandshakeCertificates;

public final class Network {
    public static final Cache CACHE = new Cache(FileSystem.HTTP_CACHE.toFile(), 100 * 1024 * 1024); // 100MB cache

    public static OkHttpClient CLIENT;
    public static OkHttpClient GRAPHQL_CLIENT;
    public static OkHttpClient CACHED_CLIENT;

    public static final String ANALYTICS_USER_AGENT = String.format(
            "Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36 %s/%s Java/%s",
            OS.getUserAgentString(), Constants.LAUNCHER_NAME, Constants.VERSION.toStringForUserAgent(),
            Java.getLauncherJavaVersion());

    public static final String USER_AGENT = String.format(
            "%s/%s (+%s)", Constants.LAUNCHER_NAME, Constants.VERSION.toStringForUserAgent(),
            Constants.LAUNCHER_WEBSITE);

    public static final String API_USER_AGENT = String.format(
            "%s/%s [%s/%s] (+%s)", Constants.LAUNCHER_NAME, Constants.VERSION.toStringForLogging(),
            Constants.VERSION.getSha1Revision(),
            OS.getInstallMethod().toString(),
            Constants.LAUNCHER_WEBSITE);

    static {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINEST);

        OkHttpClient baseClient = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .addNetworkInterceptor(new UserAgentInterceptor())
                .addInterceptor(new DebugLoggingInterceptor())
                .addNetworkInterceptor(new ErrorReportingInterceptor())
                .connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .build();

        CLIENT = baseClient;

        GRAPHQL_CLIENT = baseClient.newBuilder()
                .addInterceptor(
                        new ApolloClientAwarenessInterceptor("Launcher", Constants.VERSION.toStringForLogging()))
                .build();

        CACHED_CLIENT = baseClient.newBuilder().cache(CACHE).build();
    }

    public static void setConnectionTimeouts() {
        CLIENT = CLIENT.newBuilder().connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();

        GRAPHQL_CLIENT = GRAPHQL_CLIENT.newBuilder().connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();

        CACHED_CLIENT = CACHED_CLIENT.newBuilder().connectTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .readTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS)
                .writeTimeout(App.settings.connectionTimeout, TimeUnit.SECONDS).build();
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

    public static void addTrustedCertificate(X509Certificate certificate) {
        HandshakeCertificates certificates = new HandshakeCertificates.Builder().addPlatformTrustedCertificates()
                .addTrustedCertificate(certificate).build();

        CLIENT = CLIENT.newBuilder().sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
                .build();

        GRAPHQL_CLIENT = GRAPHQL_CLIENT.newBuilder()
                .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager()).build();

        CACHED_CLIENT = CACHED_CLIENT.newBuilder()
                .sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager()).build();
    }

    public static void allowAllSslCerts() {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {}

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
                        throws CertificateException {}

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[] {};
                }
            } };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            CLIENT = CLIENT.newBuilder().sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true).build();

            GRAPHQL_CLIENT = GRAPHQL_CLIENT.newBuilder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true).build();

            CACHED_CLIENT = CACHED_CLIENT.newBuilder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeUrlFromCache(String url) {
        try {
            Iterator<String> urlIterator = Network.CACHE.urls();
            while (urlIterator.hasNext()) {
                if (urlIterator.next().equals(url)) {
                    urlIterator.remove();
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }
}
