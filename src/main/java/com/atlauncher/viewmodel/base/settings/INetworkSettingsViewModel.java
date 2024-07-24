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
package com.atlauncher.viewmodel.base.settings;

import com.atlauncher.data.CheckState;
import com.atlauncher.data.ProxyType;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.NetworkSettingsTab;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 15
 * <p>
 * View model for {@link NetworkSettingsTab}
 */
public interface INetworkSettingsViewModel extends SettingsListener {

    void setConcurrentConnections(int connections);

    Observable<Integer> getConcurrentConnections();

    void setConnectionTimeout(int timeout);

    Observable<Integer> getConnectionTimeout();

    void setEnableProxy(Boolean b);

    Observable<Boolean> getEnableProxy();

    void setProxyHost(String host);

    Observable<String> getProxyHost();

    void setProxyPort(int port);

    Observable<Integer> getProxyPort();

    void setProxyType(ProxyType type);

    Observable<Integer> getProxyType();

    void setModrinthAPIKey(String apiKey);

    Observable<String> getModrinthAPIKey();

    Observable<CheckState> getProxyCheckState();
}