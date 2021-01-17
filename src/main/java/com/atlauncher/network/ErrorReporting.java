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
package com.atlauncher.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.utils.Java;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.event.Breadcrumb;
import io.sentry.event.BreadcrumbBuilder;

public final class ErrorReporting {
    public static SentryClient client;
    public static List<String> sentEvents = new ArrayList<>();

    public static List<String> ignoredMessages = new ArrayList<>();

    static {
        ignoredMessages.add("Network is unreachable: connect");
        ignoredMessages.add("Permission denied: connect");
        ignoredMessages.add("Connection timed out: connect");
        ignoredMessages.add("Connection refused: connect");
        ignoredMessages.add("failed to delete");
        ignoredMessages.add("failed to rename");
        ignoredMessages.add("Failed to connect to");
        ignoredMessages.add("Connection reset");
        ignoredMessages.add("timeout");
        ignoredMessages.add("Read timed out");
        ignoredMessages.add("Access is denied");
        ignoredMessages.add("request wasn't successful");
        ignoredMessages.add("There is not enough space on the disk");
        ignoredMessages.add("The system cannot find the file specified");
        ignoredMessages.add("being used by another process");
        ignoredMessages.add("The cloud file provider is not running");
        ignoredMessages.add("Name or service not known");
        ignoredMessages.add("Received fatal alert: handshake_failure");
        ignoredMessages.add("not verified (no certificates)");
        ignoredMessages.add("No such host is known");
        ignoredMessages.add("nodename nor servname provided, or not known");
        ignoredMessages.add("NoSuchFileException");
        ignoredMessages.add("Account does not own Minecraft");
    }

    public static void init(boolean disable) {
        if (!disable) {
            client = Sentry.init(Constants.SENTRY_DSN);
            client.addShouldSendEventCallback(event -> {
                if (sentEvents.contains(event.getMessage())) {
                    return false;
                }

                if (ignoredMessages.stream().anyMatch(m -> event.getMessage().contains(m))) {
                    return false;
                }

                sentEvents.add(event.getMessage());
                return true;
            });
            client.setRelease(Constants.VERSION.toStringForLogging());
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
