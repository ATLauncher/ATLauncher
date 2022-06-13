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

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.events.OnSide;
import com.atlauncher.events.OutboundLinkEvent;
import com.atlauncher.events.ScreenViewEvent;
import com.atlauncher.events.Side;
import com.atlauncher.events.ExceptionEvent;
import com.atlauncher.events.settings.SettingsSavedEvent;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.brsanthu.googleanalytics.GoogleAnalytics;
import com.brsanthu.googleanalytics.GoogleAnalyticsConfig;
import com.brsanthu.googleanalytics.request.DefaultRequest;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

public final class Analytics {
    private static final Logger LOG = LogManager.getLogger(Analytics.class);
    private static GoogleAnalytics session;

    private static GoogleAnalytics createSession(){
        return GoogleAnalytics.builder()
            .withConfig(buildConfig())
            .withDefaultRequest(buildDefaultRequest())
            .withTrackingId(Constants.GA_TRACKING_ID)
            .withAppName(Constants.LAUNCHER_NAME)
            .withAppVersion(Constants.VERSION.toStringForLogging())
            .build();
    }

    public static void startSession() {
        session = createSession();
        session.screenView().sessionControl("start").sendAsync();
        Runtime.getRuntime().addShutdownHook(new Thread(Analytics::endSession));
    }

    private static GoogleAnalyticsConfig buildConfig() {
        return new GoogleAnalyticsConfig()
            .setDiscoverRequestParameters(true)
            .setProxyHost(App.settings.proxyHost)
            .setProxyPort(App.settings.proxyPort)
            .setEnabled(!App.disableAnalytics && App.settings.enableAnalytics);
    }

    private static DefaultRequest buildDefaultRequest() {
        Rectangle screenBounds = OS.getScreenVirtualBounds();
        String screenResolution = String.format("%dx%d", screenBounds.width, screenBounds.height);
        return new DefaultRequest().userAgent(Network.USER_AGENT).clientId(App.settings.analyticsClientId)
            .customDimension(1, Java.getLauncherJavaVersion()).customDimension(2, System.getProperty("os.name"))
            .customDimension(3, System.getProperty("os.arch")).screenResolution(screenResolution);
    }

    private static void sendScreenView(final String title){
        if(session == null)
            return;
        session.screenView(Constants.LAUNCHER_NAME, title).sendAsync();
    }

    private static void sendOutboundLink(String url) {
        if (session == null)
            return;
        session.event()
            .eventLabel(url)
            .eventAction("Outbound")
            .eventCategory("Link")
            .sendAsync();
    }

    public static void sendEvent(Integer value, String label, String action, String category) {
        if (session == null)
            return;
        session.event()
            .eventValue(value)
            .eventLabel(label)
            .eventAction(action)
            .eventCategory(category)
            .sendAsync();
    }

    public static void sendEvent(String label, String action, String category) {
        sendEvent(null, label, action, category);
    }

    public static void sendEvent(String action, String category) {
        sendEvent(null, null, action, category);
    }

    private static void sendException(String message) {
        if(session == null)
            return;
        session.exception().exceptionDescription(message).sendAsync();
    }

    public static void endSession() {
        if (session == null)
            return;
        try {
            session.screenView()
                .sessionControl("end")
                .send();
            session.close();
        } catch (Exception exc) {
            LOG.error("Error closing analytics session:", exc);
        }
    }

    @Subscribe
    @OnSide(Side.UI)
    public void onSettingsSaved(final SettingsSavedEvent event) {
        session.getConfig()
            .setProxyHost(App.settings.proxyHost)
            .setProxyPort(App.settings.proxyPort)
            .setEnabled(!App.disableAnalytics && App.settings.enableAnalytics);
    }

    @Subscribe
    @OnSide(Side.WORKER)
    public void onScreenView(final ScreenViewEvent event){
        sendScreenView(event.getTitle());
    }

    @Subscribe
    @OnSide(Side.WORKER)
    public void onOutboundLink(final OutboundLinkEvent event){
        sendOutboundLink(event.getDestination());
    }

    @Subscribe
    @OnSide(Side.WORKER)
    public void onAppException(final ExceptionEvent event){
        sendException(event.getCauseMessage());
    }
}
