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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.analytics.AnalyticsApiResponse;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class Analytics {
    private static List<AnalyticsEvent> events = new ArrayList<>();
    private static String sessionId = UUID.randomUUID().toString();
    private static boolean sessionInitialised = false;

    public static void startSession() {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", App.settings.analyticsClientId);
        body.put("sessionId", sessionId);
        body.put("javaVersion", Java.getLauncherJavaVersion());
        body.put("majorJavaVersion", Java.getLauncherJavaVersionNumber());
        body.put("osName", OS.getAnalyticsOSName());
        body.put("osArch", OS.getAnalyticsOSArch());
        body.put("osVersion", OS.getVersion());
        body.put("launcherVersion", Constants.VERSION.toStringForLogging());
        body.put("launcherHash", Constants.VERSION.getSha1Revision().toString());
        body.put("launcherInstallMethod", OS.getInstallMethodForAnalytics());

        CompletableFuture<AnalyticsApiResponse> responseFuture = makeApiCall("/session", body);

        responseFuture.thenApply((AnalyticsApiResponse response) -> {
            Analytics.sessionInitialised = response.statusCode >= 200 && response.statusCode < 300;

            Runtime.getRuntime().addShutdownHook(new Thread(Analytics::endSession));

            return response;
        });
    }

    private static void addEventToQueue(AnalyticsEvent event) {
        events.add(event);

        if (events.size() >= 10) {
            synchronized (events) {
                sendEvents(events);
                events.clear();
            }
        }
    }

    private static void sendEvents(List<AnalyticsEvent> events) {
        if (!sessionInitialised) {
            return;
        }

        sendEvents(events, false);
    }

    private static void sendEvents(List<AnalyticsEvent> events, boolean wait) {
        if (!sessionInitialised) {
            return;
        }

        LogManager.debug(String.format("Sending %d batched events", events.size()));

        Map<String, Object> body = new HashMap<>();
        body.put("userId", App.settings.analyticsClientId);
        body.put("sessionId", sessionId);
        body.put("events", events);

        final CompletableFuture<AnalyticsApiResponse> responseFuture = makeApiCall("/events", body);

        if (wait) {
            try {
                responseFuture.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            }
        }
    }

    public static CompletableFuture<AnalyticsApiResponse> makeApiCall(String path, Map<String, Object> body) {
        LogManager.debug(String.format("Calling %s analytics call", path));

        LogManager.debug(Gsons.DEFAULT.toJson(body));

        CompletableFuture<AnalyticsApiResponse> completableFuture = new CompletableFuture<>();

        Request request = new Request.Builder()
                .url(String.format("%s/api%s", Constants.ANALYTICS_BASE_URL, path))
                .post(RequestBody.create(Gsons.DEFAULT.toJson(body), MediaType.get("application/json; charset=utf-8")))
                .build();

        Network.CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogManager.logStackTrace(String.format("Failed to call analytics api for path %s", path), e);
                completableFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response okResp) {
                AnalyticsApiResponse response = new AnalyticsApiResponse();
                response.statusCode = okResp.code();

                okResp.close();
                completableFuture.complete(response);
            }
        });

        return completableFuture;
    }

    public static void sendScreenView(String title) {
        addEventToQueue(AnalyticsEvent.forScreenView(title));
    }

    public static void sendOutboundLink(String url) {
        addEventToQueue(AnalyticsEvent.forLinkClick(url));
    }

    /**
     * @deprecated No longer used, use
     *             Analytics.addEventToQueue(AnalyticsEvent.forX())
     */
    public static void sendEvent(Integer value, String label, String action, String category) {
        // final Map<String, Object> payload = new HashMap<>();
        // payload.put("url", url);

        // addEventToQueue(new AnalyticsEvent("link_click", payload));
    }

    /**
     * @deprecated No longer used, use
     *             Analytics.addEventToQueue(AnalyticsEvent.forX())
     */
    public static void sendEvent(String label, String action, String category) {
        sendEvent(null, label, action, category);
    }

    /**
     * @deprecated No longer used, use
     *             Analytics.addEventToQueue(AnalyticsEvent.forX())
     */
    public static void sendEvent(String action, String category) {
        sendEvent(null, null, action, category);
    }

    public static void endSession() {
        if (events.size() != 0) {
            sendEvents(events, true);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("userId", App.settings.analyticsClientId);
        body.put("sessionId", sessionId);

        makeApiCall("/session/end", body);
    }
}
