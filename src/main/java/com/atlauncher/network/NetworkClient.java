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
import java.lang.reflect.Type;

import javax.annotation.Nullable;

import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.managers.LogManager;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class NetworkClient {
    @Nullable
    public static <T> T get(String url, Class<T> tClass) {
        try {
            return makeRequest(url, null, null, tClass, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T get(String url, Headers headers, Class<T> tClass) {
        try {
            return makeRequest(url, headers, null, tClass, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T get(String url, Type type) {
        try {
            return makeRequest(url, null, null, type, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T get(String url, Headers headers, Type type) {
        try {
            return makeRequest(url, headers, null, type, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getWithThrow(String url, Headers headers, Class<T> tClass) throws DownloadException {
        return makeRequest(url, headers, null, tClass, null);
    }

    @Nullable
    public static <T> T getWithThrow(String url, Headers headers, Type type) throws DownloadException {
        return makeRequest(url, headers, null, type, null);
    }

    @Nullable
    public static <T> T getCached(String url, Class<T> tClass, @Nullable CacheControl cacheControl) {
        try {
            return makeRequest(url, null, null, tClass, cacheControl);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getCached(String url, Type type, @Nullable CacheControl cacheControl) {
        try {
            return makeRequest(url, null, null, type, cacheControl);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getCached(String url, @Nullable Headers headers, Class<T> tClass,
            @Nullable CacheControl cacheControl) {
        try {
            return makeRequest(url, headers, null, tClass, cacheControl);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getCached(String url, @Nullable Headers headers, Type type,
            @Nullable CacheControl cacheControl) {
        try {
            return makeRequest(url, headers, null, type, cacheControl);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T getCachedWithThrow(String url, Class<T> tClass, @Nullable CacheControl cacheControl)
            throws DownloadException {
        return makeRequest(url, null, null, tClass, cacheControl);
    }

    @Nullable
    public static <T> T getCachedWithThrow(String url, Type type, @Nullable CacheControl cacheControl)
            throws DownloadException {
        return makeRequest(url, null, null, type, cacheControl);
    }

    @Nullable
    public static <T> T post(String url, RequestBody body, Class<T> tClass) {
        try {
            return makeRequest(url, null, body, tClass, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T post(String url, RequestBody body, Type type) {
        try {
            return makeRequest(url, null, body, type, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T post(String url, @Nullable Headers headers, RequestBody body, Class<T> tClass) {
        try {
            return makeRequest(url, headers, body, tClass, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T post(String url, @Nullable Headers headers, RequestBody body, Type type) {
        try {
            return makeRequest(url, headers, body, type, null);
        } catch (DownloadException e) {
            return null;
        }
    }

    @Nullable
    public static <T> T postWithThrow(String url, @Nullable Headers headers, RequestBody body, Class<T> tClass)
            throws DownloadException {
        return makeRequest(url, headers, body, tClass, null);
    }

    @Nullable
    public static <T> T postWithThrow(String url, @Nullable Headers headers, RequestBody body, Type type)
            throws DownloadException {
        return makeRequest(url, headers, body, type, null);
    }

    @Nullable
    private static <T> T makeRequest(String url, @Nullable Headers headers, @Nullable RequestBody requestBody,
            Class<T> tClass, @Nullable CacheControl cacheControl) throws DownloadException {
        return makeRequest(url, headers, requestBody, TypeToken.get(tClass).getType(), cacheControl);
    }

    @Nullable
    private static <T> T makeRequest(String url, @Nullable Headers headers, @Nullable RequestBody requestBody,
            Type type, @Nullable CacheControl cacheControl) throws DownloadException {
        Request.Builder builder = new Request.Builder().url(url);

        if (headers != null) {
            builder.headers(headers);
        }

        if (requestBody != null) {
            builder.post(requestBody);
        }

        if (cacheControl != null) {
            builder.cacheControl(cacheControl);
        }

        OkHttpClient httpClient = (requestBody != null || cacheControl == null) ? Network.CLIENT
                : Network.CACHED_CLIENT;
        Request request = builder.build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response != null && response.code() == 429) {
                    LogManager.info(response.headers().toString());
                }

                throw new DownloadException(response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                return null;
            }

            return Gsons.DEFAULT.fromJson(body.string(), type);
        } catch (DownloadException e) {
            throw e; // Re-throw DownloadException
        } catch (IOException e) {
            LogManager.logStackTrace(String.format("Error calling %s", url), e);
            return null;
        }
    }
}
