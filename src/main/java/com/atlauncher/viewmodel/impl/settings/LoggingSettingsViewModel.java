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

import com.atlauncher.App;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.tabs.settings.LoggingSettingsTab;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 17
 * <p>
 * View model for {@link LoggingSettingsTab}
 */
public class LoggingSettingsViewModel implements SettingsListener {
    private final BehaviorSubject<String>
        _forgeLoggingLevel = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _enableLogging = BehaviorSubject.create(),
        _enableAnalytics = BehaviorSubject.create();

    public LoggingSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _forgeLoggingLevel.onNext(App.settings.forgeLoggingLevel);
        _enableLogging.onNext(App.settings.enableLogs);
        _enableAnalytics.onNext(App.settings.enableAnalytics);
    }

    public void setLoggingLevel(String level) {
        if (App.settings.forgeLoggingLevel.equals(level)) return;
        App.settings.forgeLoggingLevel = level;
        SettingsManager.post();
    }

    public Observable<String> getForgeLoggingLevel() {
        return _forgeLoggingLevel.observeOn(SwingSchedulers.edt());
    }

    public Observable<Boolean> getEnableLogging() {
        return _enableLogging.observeOn(SwingSchedulers.edt());
    }

    public void setEnableLogging(Boolean b) {
        App.settings.enableLogs = b;
        SettingsManager.post();
    }

    public Observable<Boolean> getEnableAnalytics() {
        return _enableAnalytics.observeOn(SwingSchedulers.edt());
    }

    public void setEnableAnalytics(Boolean b) {
        App.settings.enableAnalytics = b;
        SettingsManager.post();
    }
}