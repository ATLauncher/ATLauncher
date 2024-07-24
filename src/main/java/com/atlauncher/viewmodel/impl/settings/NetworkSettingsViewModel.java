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
        _addOnConcurrentConnectionsChanged = BehaviorSubject.create(),
        _addOnConnectionTimeoutChanged = BehaviorSubject.create(),
        _addOnProxyPortChanged = BehaviorSubject.create(),
        _addOnProxyTypeChanged = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _addOnEnableProxyChanged = BehaviorSubject.create();

    private final BehaviorSubject<String>
        _addOnProxyHostChanged = BehaviorSubject.create(),
        modrinthAPIKey = BehaviorSubject.create();

    public NetworkSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _addOnConcurrentConnectionsChanged.onNext(App.settings.concurrentConnections);
        _addOnConnectionTimeoutChanged.onNext(App.settings.connectionTimeout);
        _addOnProxyPortChanged.onNext(App.settings.proxyPort);
        _addOnEnableProxyChanged.onNext(App.settings.enableProxy);
        _addOnProxyHostChanged.onNext(App.settings.proxyHost);
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
        return _addOnConcurrentConnectionsChanged.observeOn(SwingSchedulers.edt());
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
        return _addOnConnectionTimeoutChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableProxy(Boolean b) {
        App.settings.enableProxy = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getEnableProxy() {
        return _addOnEnableProxyChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyHost(String host) {
        App.settings.proxyHost = host;
        setProxyHostPending();
    }

    public void setProxyHostPending() {
        SettingsValidityManager.setValidity("proxy", false);
    }

    @Override
    public Observable<String> getProxyHost() {
        return _addOnProxyHostChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyPort(int port) {
        App.settings.proxyPort = port;
        setProxyHostPending();
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getProxyPort() {
        return _addOnProxyPortChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setProxyType(ProxyType type) {
        App.settings.proxyType = type.name();
        setProxyHostPending();
        SettingsManager.post();
    }

    @Override
    public Observable<Integer> getProxyType() {
        return _addOnProxyTypeChanged.observeOn(SwingSchedulers.edt());
    }

    private void pushProxyType() {
        switch (App.settings.proxyType) {
            case "HTTP":
                _addOnProxyTypeChanged.onNext(0);
                break;
            case "SOCKS":
                _addOnProxyTypeChanged.onNext(1);
                break;
            default:
                _addOnProxyTypeChanged.onNext(2);
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
    public boolean checkProxy() {
        setProxyHostPending();
        if (!App.settings.enableProxy) {
            return true;
        }

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
                return false;
        }

        boolean result = Utils.testProxy(
            new Proxy(type, new InetSocketAddress(App.settings.proxyHost, App.settings.proxyPort))
        );

        SettingsValidityManager.setValidity("proxy", result);

        return result;
    }
}