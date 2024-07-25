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
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.viewmodel.base.settings.ILoggingSettingsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 17
 */
public class LoggingSettingsViewModel implements ILoggingSettingsViewModel {
    private final BehaviorSubject<String>
        _addOnLoggingLevelChanged = BehaviorSubject.create();

    private final BehaviorSubject<Boolean>
        _addOnEnableLoggingChanged = BehaviorSubject.create(),
        _addOnEnableAnonAnalyticsChanged = BehaviorSubject.create();

    public LoggingSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _addOnLoggingLevelChanged.onNext(App.settings.forgeLoggingLevel);
        _addOnEnableLoggingChanged.onNext(App.settings.enableLogs);
        _addOnEnableAnonAnalyticsChanged.onNext(App.settings.enableAnalytics);
    }

    @Override
    public void setLoggingLevel(String level) {
        App.settings.forgeLoggingLevel = level;
        SettingsManager.post();
    }

    @Override
    public Observable<String> getLoggingLevel() {
        return _addOnLoggingLevelChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableLogging(Boolean b) {
        App.settings.enableLogs = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getEnableLogging() {
        return _addOnEnableLoggingChanged.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void setEnableAnonAnalytics(Boolean b) {
        App.settings.enableAnalytics = b;
        SettingsManager.post();
    }

    @Override
    public Observable<Boolean> getEnableAnonAnalytics() {
        return _addOnEnableAnonAnalyticsChanged.observeOn(SwingSchedulers.edt());
    }
}