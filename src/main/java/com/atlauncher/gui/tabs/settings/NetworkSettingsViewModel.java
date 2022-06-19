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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.data.CheckState;
import com.atlauncher.evnt.manager.SettingsValidityManager;
import com.atlauncher.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 18 / 06 / 2022
 */
public class NetworkSettingsViewModel implements INetworkSettingsViewModel {
    private static final Logger LOG = LogManager.getLogger(NetworkSettingsViewModel.class);

    private Consumer<CheckState> _addOnProxyCheckedListener;
    private boolean proxySettingsChanged = false;
    private long lastChangeToProxy = 0;
    private static final long proxyCheckDelay = 1000;
    private final Runnable threadRunnable = () -> {
        // Ignore if proxy is disabled
        while (App.settings.enableProxy) {
            if (proxySettingsChanged) {
                _addOnProxyCheckedListener.accept(
                    new CheckState.CheckPending()
                );
                if ((lastChangeToProxy + proxyCheckDelay) < System.currentTimeMillis()) {
                    proxySettingsChanged = false;

                    // Do not let user save while checking
                    SettingsValidityManager.post("proxy", false);

                    _addOnProxyCheckedListener.accept(
                        new CheckState.Checking()
                    );

                    boolean valid = checkHost();
                    SettingsValidityManager.post("proxy", valid);
                    _addOnProxyCheckedListener.accept(
                        new CheckState.Checked(valid)
                    );
                    if (valid)
                        SettingsManager.post();
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
    };

    private Thread proxyCheckThread = new Thread(threadRunnable);


    private void changedProxySettings() {
        lastChangeToProxy = System.currentTimeMillis();
        proxySettingsChanged = true;
    }

    public NetworkSettingsViewModel() {
        SettingsManager.addListener(this);
        proxyCheckThread.start();
    }

    @Override
    public void onSettingsSaved() {
        _addOnConcurrentConnectionsChanged.accept(getConcurrentConnections());
        _addOnConnectionTimeoutChanged.accept(getConnectionTimeout());
        _addOnProxyPortChanged.accept(getProxyPort());
        _addOnDoNotUseHTTP2Changed.accept(App.settings.dontUseHttp2);
        _addOnEnableProxyChanged.accept(App.settings.enableProxy);
        _addOnProxyHostChanged.accept(App.settings.proxyHost);
        pushProxyType();
    }

    private Consumer<Integer>
        _addOnConcurrentConnectionsChanged,
        _addOnConnectionTimeoutChanged,
        _addOnProxyPortChanged,
        _addOnProxyTypeChanged;

    private Consumer<Boolean>
        _addOnDoNotUseHTTP2Changed,
        _addOnEnableProxyChanged;

    private Consumer<String> _addOnProxyHostChanged;

    @Override
    public int getConcurrentConnections() {
        return App.settings.concurrentConnections;
    }

    @Override
    public void setConcurrentConnections(int connections) {
        App.settings.concurrentConnections = connections;
        SettingsManager.post();
    }

    @Override
    public void addOnConcurrentConnectionsChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.concurrentConnections);
        _addOnConcurrentConnectionsChanged = onChanged;
    }

    @Override
    public int getConnectionTimeout() {
        return App.settings.connectionTimeout;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        App.settings.connectionTimeout = timeout;
        SettingsManager.post();
        Network.setConnectionTimeouts();
    }

    @Override
    public void addOnConnectionTimeoutChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.connectionTimeout);
        _addOnConnectionTimeoutChanged = onChanged;
    }

    @Override
    public void setDoNotUseHTTP2(Boolean b) {
        App.settings.dontUseHttp2 = b;
        SettingsManager.post();
        Network.setProtocols();
    }

    @Override
    public void addOnDoNotUseHTTP2Changed(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.dontUseHttp2);
        _addOnDoNotUseHTTP2Changed = onChanged;
    }

    @Override
    public void setEnableProxy(Boolean b) {
        App.settings.enableProxy = b;
        SettingsManager.post();
        // Ensure proxy validity is set to true when disabled
        if (!b)
            SettingsValidityManager.post("proxy", true);

        if (!b && proxyCheckThread.isAlive()) {
            // Stop the proxy check if it is running
            proxyCheckThread.interrupt();
            _addOnProxyCheckedListener.accept(new CheckState.NotChecking());
        } else {
            // Restart the proxy check thread
            if (!proxyCheckThread.isAlive() || proxyCheckThread.isInterrupted()) {
                proxyCheckThread = new Thread(threadRunnable);
                proxyCheckThread.start();
            }
        }
    }


    @Override
    public void addOnEnableProxyChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableProxy);
        _addOnEnableProxyChanged = onChanged;
    }

    @Override
    public void setProxyHost(String host) {
        changedProxySettings();
        App.settings.proxyHost = host;
    }

    @Override
    public void addOnProxyHostChanged(Consumer<String> onChanged) {
        onChanged.accept(App.settings.proxyHost);
        _addOnProxyHostChanged = onChanged;
    }

    @Override
    public int getProxyPort() {
        return App.settings.proxyPort;
    }

    @Override
    public void setProxyPort(int port) {
        changedProxySettings();
        App.settings.proxyPort = port;
        SettingsManager.post();
    }

    @Override
    public void addOnProxyPortChanged(Consumer<Integer> onChanged) {
        onChanged.accept(App.settings.proxyPort);
        _addOnProxyPortChanged = onChanged;
    }

    @Override
    public void setProxyType(ProxyType type) {
        changedProxySettings();
        App.settings.proxyType = type.name();
        SettingsManager.post();
    }

    @Override
    public void addOnProxyTypeChanged(Consumer<Integer> onChanged) {
        _addOnProxyTypeChanged = onChanged;
        pushProxyType();
    }

    private boolean checkHost() {
        Proxy.Type theType;
        switch (App.settings.proxyType) {
            case "HTTP":
                theType = Proxy.Type.HTTP;
                break;
            case "SOCKS":
                theType = Proxy.Type.SOCKS;
                break;
            case "DIRECT":
                theType = Proxy.Type.DIRECT;
                break;
            default:
                return false;
        }

        try {
            return Utils.testProxy(
                new Proxy(theType, new InetSocketAddress(App.settings.proxyHost, getProxyPort()))
            );
        } catch (Exception e) {
            LOG.error("Error checking proxy", e);
            return false;
        }
    }

    @Override
    public void addOnProxyCheckListener(Consumer<CheckState> onChecked) {
        onChecked.accept(new CheckState.NotChecking());
        _addOnProxyCheckedListener = onChecked;
    }

    private void pushProxyType() {
        switch (App.settings.proxyType) {
            case "HTTP":
                _addOnProxyTypeChanged.accept(0);
                break;
            case "SOCKS":
                _addOnProxyTypeChanged.accept(1);
                break;
            default:
                _addOnProxyTypeChanged.accept(2);
                break;
        }
    }
}
