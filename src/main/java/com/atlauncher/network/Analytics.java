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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.managers.ConfigManager;
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
    private static Timer timer = new Timer();

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

            Double autoSendInSeconds = ConfigManager.getConfigItem("analytics.autoSendInSeconds", 60d);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (events.size() != 0) {
                        sendAllStoredEvents(false);
                    }
                }
            }, (int) (autoSendInSeconds * 1000), (int) (autoSendInSeconds * 1000));

            Runtime.getRuntime().addShutdownHook(new Thread(Analytics::endSession));

            return response;
        });
    }

    public static void trackEvent(AnalyticsEvent event) {
        events.add(event);

        if (events.size() >= 10 && sessionInitialised) {
            sendAllStoredEvents(false);
        }
    }

    public static synchronized void sendAllStoredEvents(boolean wait) {
        synchronized (events) {
            sendEvents(events, wait);
            events.clear();
        }
    }

    private static void sendEvents(List<AnalyticsEvent> events, boolean wait) {
        LogManager.debug(String.format(Locale.ENGLISH, "Sending %d batched events", events.size()));

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
        trackEvent(AnalyticsEvent.forScreenView(title));
    }

    public static void sendOutboundLink(String url) {
        trackEvent(AnalyticsEvent.forLinkClick(url));
    }

    public static void endSession() {
        if (sessionInitialised) {
            timer.cancel();

            if (events.size() != 0) {
                sendAllStoredEvents(true);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("userId", App.settings.analyticsClientId);
            body.put("sessionId", sessionId);

            try {
                CompletableFuture<AnalyticsApiResponse> response = makeApiCall("/session/end", body);
                response.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignored) {
            }

            sessionInitialised = false;
        }
    }

    public static boolean isEnabled() {
        if (!ConfigManager.getConfigItem("analytics.enabledVersion", "None")
                .equals(Constants.VERSION.toStringForLogging())) {
            return false;
        }

        try {
            Double enrolledPercentage = ConfigManager.getConfigItem("analytics.percentage", 0d);

            if (enrolledPercentage == 0) {
                return false;
            }

            return new Random().nextInt(100) <= enrolledPercentage;
        } catch (Exception ignored) {
            return false;
        }
    }
}
