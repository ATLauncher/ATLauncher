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
package com.atlauncher.viewmodel.impl.settings;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.data.CheckState;
import com.atlauncher.data.ProxyType;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.managers.SettingsValidityManager;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.base.settings.INetworkSettingsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 18
 */
public class NetworkSettingsViewModel implements INetworkSettingsViewModel {

    private final BehaviorSubject<Integer>
        _concurrentConnections = BehaviorSubject.create(),
        _connectionTimeout = BehaviorSubject.create(),
        _proxyPort = BehaviorSubject.create(),
        _proxyType = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _enableProxy = BehaviorSubject.create();

    private final BehaviorSubject<String>
        _proxyHost = BehaviorSubject.create(),
        modrinthAPIKey = BehaviorSubject.create();

    private final BehaviorSubject<CheckState> proxyCheckState =
        BehaviorSubject.createDefault(CheckState.NotChecking);

    private long lastSetPending = System.currentTimeMillis();
    private boolean isCheckThreadRunning = false;

    public NetworkSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _concurrentConnections.onNext(App.settings.concurrentConnections);
        _connectionTimeout.onNext(App.settings.connectionTimeout);
        _proxyPort.onNext(App.settings.proxyPort);
        _enableProxy.onNext(App.settings.enableProxy);
        _proxyHost.onNext(App.settings.proxyHost);
        modrinthAPIKey.onNext(Optional.ofNullable(App.settings.modrinthApiKey).orElse(""));
        pushProxyType();
    }

    @Override
    public void setConcurrentConnections(int connections) {
        App.settings.concurrentConnections = connections;
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getConcurrentConnections() {
        return _concurrentConnections.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        boolean timeoutChanged = App.settings.connectionTimeout != timeout;

        App.settings.connectionTimeout = timeout;
        SettingsManager.post();

        if (timeoutChanged) {
            Network.setConnectionTimeouts();
        }
    }

    @Override
    public Observable<Integer> getConnectionTimeout() {
        return _connectionTimeout.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableProxy(Boolean b) {
        App.settings.enableProxy = b;
        SettingsManager.post();

        if (b) {
            // If enabled, check the proxy soon
            SettingsValidityManager.setValidity("proxy", false);
            proxyCheckState.onNext(CheckState.CheckPending);
            setProxyHostPending(2000);
        } else {
            // Mark proxy as valid if disabled
            SettingsValidityManager.setValidity("proxy", true);
            proxyCheckState.onNext(CheckState.NotChecking);
        }
    }

    @Override
    public Observable<Boolean> getEnableProxy() {
        return _enableProxy.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyHost(String host) {
        App.settings.proxyHost = host;
        setProxyHostPending();
    }

    /**
     * Basic set, will check in a second
     */
    private void setProxyHostPending() {
        setProxyHostPending(1000);
    }

    /**
     * Custom pending set
     *
     * @param delay how long until checked
     */
    private void setProxyHostPending(long delay) {
        SettingsValidityManager.setValidity("proxy", false);
        proxyCheckState.onNext(CheckState.CheckPending);
        lastSetPending = System.currentTimeMillis();

        if (!isCheckThreadRunning) {
            isCheckThreadRunning = true;

            new Thread(() -> {
                while (lastSetPending + delay > System.currentTimeMillis()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                }

                checkProxy();

                isCheckThreadRunning = false;
            }).start();
        }
    }

    @Override
    public Observable<String> getProxyHost() {
        return _proxyHost.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyPort(int port) {
        App.settings.proxyPort = port;
        setProxyHostPending();
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getProxyPort() {
        return _proxyPort.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyType(ProxyType type) {
        App.settings.proxyType = type.name();
        setProxyHostPending();
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getProxyType() {
        return _proxyType.observeOn(SwingSchedulers.edt());
    }

    private void pushProxyType() {
        switch (App.settings.proxyType) {
            case "HTTP":
                _proxyType.onNext(0);
                break;
            case "SOCKS":
                _proxyType.onNext(1);
                break;
            default:
                _proxyType.onNext(2);
                break;
        }
    }

    @Override
    public Observable<String> getModrinthAPIKey() {
        return modrinthAPIKey.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setModrinthAPIKey(String apiKey) {
        if (!apiKey.isEmpty())
            App.settings.modrinthApiKey = apiKey;
        else App.settings.modrinthApiKey = null;
    }

    @Override
    public Observable<CheckState> getProxyCheckState() {
        return proxyCheckState.observeOn(SwingSchedulers.edt());
    }

    private void checkProxy() {
        if (!App.settings.enableProxy) {
            // If not enabled, we can skip this
            proxyCheckState.onNext(CheckState.NotChecking);
            SettingsValidityManager.setValidity("proxy", true);
            return;
        }

        SettingsValidityManager.setValidity("proxy", false);
        proxyCheckState.onNext(CheckState.Checking);

        Proxy.Type type;

        switch (App.settings.proxyType) {
            case "HTTP":
                type = Proxy.Type.HTTP;
                break;
            case "SOCKS":
                type = Proxy.Type.SOCKS;
                break;
            case "DIRECT":
                type = Proxy.Type.DIRECT;
                break;
            default:
                SettingsValidityManager.setValidity("proxy", false);

                proxyCheckState.onNext(new CheckState.Checked(false));
                return;
        }

        boolean result = Utils.testProxy(
            new Proxy(type, new InetSocketAddress(App.settings.proxyHost, App.settings.proxyPort))
        );

        SettingsValidityManager.setValidity("proxy", result);

        proxyCheckState.onNext(new CheckState.Checked(result));
    }
}