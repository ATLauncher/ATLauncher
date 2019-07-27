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

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.data.Constants;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.utils.Java;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;
import com.brsanthu.googleanalytics.request.DefaultRequest;

public final class Analytics implements SettingsListener {
    private static GoogleAnalytics ga = GoogleAnalytics.builder()
            .withConfig(new GoogleAnalyticsConfig().setDiscoverRequestParameters(true)
                    .setProxyHost(App.settings.getProxyHost()).setProxyPort(App.settings.getProxyPort())
                    .setEnabled(App.settings.enableAnalytics()))
            .withDefaultRequest(new DefaultRequest().userAgent(Network.USER_AGENT)
                    .clientId(App.settings.getAnalyticsClientId()).customDimension(1, Java.getLauncherJavaVersion()))
            .withTrackingId(Constants.GA_TRACKING_ID).withAppName(Constants.LAUNCHER_NAME)
            .withAppVersion(Constants.VERSION.toString()).build();

    public static void startSession() {
        ga.screenView().sessionControl("start").sendAsync();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> endSession()));
    }

    public static void sendScreenView(String title) {
        ga.screenView(Constants.LAUNCHER_NAME, title).sendAsync();
    }

    public static void sendOutboundLink(String url) {
        ga.event().eventLabel(url).eventAction("Outbound").eventCategory("Link").sendAsync();
    }

    public static void sendEvent(Integer value, String label, String action, String category) {
        ga.event().eventValue(value).eventLabel(label).eventAction(action).eventCategory(category).sendAsync();
    }

    public static void sendEvent(String label, String action, String category) {
        sendEvent(null, label, action, category);
    }

    public static void sendEvent(String action, String category) {
        sendEvent(null, null, action, category);
    }

    public static void sendException(String message) {
        ga.exception().exceptionDescription(message).sendAsync();
    }

    public static void endSession() {
        try {
            ga.screenView().sessionControl("end").send();
            ga.close();
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public void onSettingsSaved() {
        ga.getConfig().setEnabled(App.settings.enableAnalytics());
    }
}
