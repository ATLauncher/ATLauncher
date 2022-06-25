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

import java.util.function.Consumer;

/**
 * 17 / 06 / 2022
 */
public class LoggingSettingsViewModel implements ILoggingSettingsViewModel {
    private Consumer<String> _addOnLoggingLevelChanged;
    private Consumer<Boolean>
        _addOnEnableLoggingChanged,
        _addOnEnableAnonAnalyticsChanged,
        _addOnEnableOpenEyeReportingChanged;

    public LoggingSettingsViewModel(){
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _addOnLoggingLevelChanged.accept(App.settings.forgeLoggingLevel);
        _addOnEnableLoggingChanged.accept(App.settings.enableLogs);
        _addOnEnableAnonAnalyticsChanged.accept(App.settings.enableAnalytics);
        _addOnEnableOpenEyeReportingChanged.accept(App.settings.enableOpenEyeReporting);
    }

    @Override
    public void setLoggingLevel(String level) {
        App.settings.forgeLoggingLevel = level;
        SettingsManager.post();
    }

    @Override
    public void addOnLoggingLevelChanged(Consumer<String> onChanged) {
        onChanged.accept(App.settings.forgeLoggingLevel);
        _addOnLoggingLevelChanged = onChanged;
    }

    @Override
    public void setEnableLogging(Boolean b) {
        App.settings.enableLogs = b;
        App.settings.enableOpenEyeReporting = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableLoggingChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableLogs);
        _addOnEnableLoggingChanged = onChanged;
    }

    @Override
    public void setEnableAnonAnalytics(Boolean b) {
        App.settings.enableAnalytics = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableAnonAnalyticsChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableAnalytics);
        _addOnEnableAnonAnalyticsChanged = onChanged;
    }

    @Override
    public void setEnableOpenEyeReporting(Boolean b) {
        App.settings.enableOpenEyeReporting = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableOpenEyeReportingChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableOpenEyeReporting);
        _addOnEnableOpenEyeReportingChanged = onChanged;
    }
}
