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

import java.util.ArrayList;
import java.util.List;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.Utils;

import io.sentry.Breadcrumb;
import io.sentry.Sentry;
import io.sentry.SentryLevel;

public final class ErrorReporting {
    public static List<String> sentEvents = new ArrayList<>();
    public static List<String> ignoredMessages = new ArrayList<>();
    public static boolean sentryInitialised = false;

    public static void enable() {
        if (!sentryInitialised) {
            Sentry.init(options -> {
                options.setDsn(Constants.SENTRY_DSN);
                options.setBeforeSend((event, hint) -> {
                    try {
                        Throwable t = event.getThrowable();

                        if (t == null || t.getMessage() == null || sentEvents.contains(t.getMessage())) {
                            return null;
                        }

                        if (ignoredMessages.stream().anyMatch(m -> t.getMessage().contains(m))) {
                            return null;
                        }

                        sentEvents.add(t.getMessage());

                        event.setServerName(null); // Don't send server names, they're useless
                        return event;
                    } catch (Throwable ignored) {
                    }
                    return event;
                });
                options.setDebug(Utils.isDevelopment());
                options.setEnvironment(ErrorReporting.getEnvironmentName());
                options.setRelease(Constants.VERSION.toStringForLogging());
                options.setTag("java.version", Java.getLauncherJavaVersion());
                options.setTag("os.name", System.getProperty("os.name"));
                options.setTag("os.version", System.getProperty("os.version"));
            }, true);

            sentryInitialised = true;
        }
    }

    private static String getEnvironmentName() {
        if (Utils.isDevelopment()) {
            return "development";
        }

        if (!Constants.VERSION.isReleaseStream()) {
            return "staging";
        }

        return "production";
    }

    public static void disable() {
        if (sentryInitialised && Sentry.isEnabled()) {
            try {
                Sentry.close();
            } catch (Exception e) {
                LogManager.logStackTrace("Error disabling error reporting", e);
            }

            sentryInitialised = false;
        }
    }

    public static void setExtra(String name, String value) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Sentry.setExtra(name, value);
        }
    }

    public static void setTag(String name, String value) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Sentry.setTag(name, value);
        }
    }

    public static void addBreadcrumb(String message, String type, SentryLevel level) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Breadcrumb breadcrumb = new Breadcrumb(message);
            breadcrumb.setType(type);
            breadcrumb.setLevel(level);

            Sentry.addBreadcrumb(breadcrumb);
        }
    }

    public static void addBreadcrumb(String message, SentryLevel level) {
        addBreadcrumb(message, "default", level);
    }

    public static void recordBreadcrumb(String message) {
        addBreadcrumb(message, "default", SentryLevel.INFO);
    }

    public static void recordNetworkRequest(String url, String method, int statusCode, String timeTaken) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Breadcrumb breadcrumb = new Breadcrumb();
            breadcrumb.setType("default");
            breadcrumb.setCategory("http");
            breadcrumb.setLevel(SentryLevel.INFO);
            breadcrumb.setData("url", url);
            breadcrumb.setData("method", method);
            breadcrumb.setData("status_code", statusCode);
            breadcrumb.setData("timeTaken", timeTaken);

            Sentry.addBreadcrumb(breadcrumb);
        }
    }

    public static void recordPackInstall(String packName, String packVersion, LoaderVersion loader) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Breadcrumb breadcrumb = new Breadcrumb("Started pack install");
            breadcrumb.setType("user");
            breadcrumb.setCategory("pack.install");
            breadcrumb.setLevel(SentryLevel.INFO);
            breadcrumb.setData("pack.name", packName);
            breadcrumb.setData("pack.version", packVersion);

            if (loader != null) {
                breadcrumb.setData("loader.version", loader.version);
                breadcrumb.setData("loader.type", loader.type);
            }

            Sentry.addBreadcrumb(breadcrumb);
        }
    }

    public static void recordInstancePlay(String packName, String packVersion, LoaderVersion loader,
            int instanceVersion) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Breadcrumb breadcrumb = new Breadcrumb("Playing instance");
            breadcrumb.setType("user");
            breadcrumb.setCategory("instance.play");
            breadcrumb.setLevel(SentryLevel.INFO);
            breadcrumb.setData("pack.name", packName);
            breadcrumb.setData("pack.version", packVersion);
            breadcrumb.setData("instance.version", instanceVersion + "");

            if (loader != null) {
                breadcrumb.setData("loader.version", loader.version);
                breadcrumb.setData("loader.type", loader.type);
            }

            Sentry.addBreadcrumb(breadcrumb);
        }
    }

    public static void captureException(Throwable t) {
        if (sentryInitialised && Sentry.isEnabled()) {
            Sentry.captureException(t);
        }
    }
}
