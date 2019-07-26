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
package com.atlauncher.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.Constants;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.utils.Java;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.Event;
import io.sentry.event.helper.ShouldSendEventCallback;

public final class ErrorReporting {
    public static SentryClient client;
    public static List<String> sentEvents = new ArrayList<>();

    public static void init(boolean disable) {
        if (!disable) {
            client = Sentry.init(Constants.SENTRY_DSN);
            client.addShouldSendEventCallback(new ShouldSendEventCallback() {
                @Override
                public boolean shouldSend(Event event) {
                    if (sentEvents.contains(event.getMessage())) {
                        return false;
                    }

                    sentEvents.add(event.getMessage());
                    return true;
                }
            });
            client.setRelease(Constants.VERSION.toString());
            client.addTag("java.version", Java.getLauncherJavaVersion());
            client.addTag("os.name", System.getProperty("os.name"));
            client.addTag("os.version", System.getProperty("os.version"));
        }
    }

    public static void addExtra(String name, String value) {
        if (client != null) {
            client.getContext().addExtra(name, value);
        }
    }

    public static void addTag(String name, String value) {
        if (client != null) {
            client.getContext().addTag(name, value);
        }
    }

    public static void recordBreadcrumb(String message, Breadcrumb.Type type, Breadcrumb.Level level) {
        if (client != null) {
            client.getContext().recordBreadcrumb(
                    new BreadcrumbBuilder().setMessage(message).setType(type).setLevel(level).build());
        }
    }

    public static void recordBreadcrumb(Map<String, String> data, Breadcrumb.Type type, Breadcrumb.Level level) {
        if (client != null) {
            client.getContext()
                    .recordBreadcrumb(new BreadcrumbBuilder().setData(data).setType(type).setLevel(level).build());
        }
    }

    public static void recordBreadcrumb(String message, Breadcrumb.Level level) {
        recordBreadcrumb(message, Breadcrumb.Type.DEFAULT, level);
    }

    public static void recordBreadcrumb(String message) {
        recordBreadcrumb(message, Breadcrumb.Type.DEFAULT, Breadcrumb.Level.INFO);
    }

    public static void recordNetworkRequest(String url, String timeTaken) {
        if (client != null) {
            Map<String, String> data = new HashMap<>();

            data.put("timeTaken", timeTaken);

            client.getContext()
                    .recordBreadcrumb(new BreadcrumbBuilder().setMessage(url).setType(Breadcrumb.Type.DEFAULT)
                            .setLevel(Breadcrumb.Level.INFO).setCategory("http.request").setData(data).build());
        }
    }

    public static void recordPackInstall(String packName, String packVersion, LoaderVersion loader) {
        if (client != null) {
            Map<String, String> data = new HashMap<>();

            data.put("pack.name", packName);
            data.put("pack.version", packVersion);

            if (loader != null) {
                data.put("loader.version", loader.version);
                data.put("loader.type", loader.type);
            }

            client.getContext().recordBreadcrumb(
                    new BreadcrumbBuilder().setMessage("Started pack install").setType(Breadcrumb.Type.USER)
                            .setLevel(Breadcrumb.Level.INFO).setCategory("pack.install").setData(data).build());
        }
    }

    public static void recordInstancePlay(String packName, String packVersion, LoaderVersion loader,
            int instanceVersion) {
        if (client != null) {
            Map<String, String> data = new HashMap<>();

            data.put("pack.name", packName);
            data.put("pack.version", packVersion);
            data.put("instance.version", instanceVersion + "");

            if (loader != null) {
                data.put("loader.version", loader.version);
                data.put("loader.type", loader.type);
            }

            client.getContext()
                    .recordBreadcrumb(new BreadcrumbBuilder().setMessage("Playing instance")
                            .setType(Breadcrumb.Type.USER).setLevel(Breadcrumb.Level.INFO).setCategory("instance.play")
                            .setData(data).build());
        }
    }

    public static void reportError(Throwable t) {
        if (client != null) {
            client.sendException(t);
        }
    }
}
