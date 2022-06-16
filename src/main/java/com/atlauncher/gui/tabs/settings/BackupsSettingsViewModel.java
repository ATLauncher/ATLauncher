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
import com.atlauncher.data.BackupMode;
import com.atlauncher.evnt.manager.SettingsManager;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public class BackupsSettingsViewModel implements IBackupsSettingsViewModel {
    Consumer<Integer> _onBackupModeSelected;
    Consumer<Boolean> _addOnEnableAutoBackupChanged;

    @Override
    public void onSettingsSaved() {
        _onBackupModeSelected.accept(App.settings.backupMode.ordinal());
        _addOnEnableAutoBackupChanged.accept(App.settings.enableAutomaticBackupAfterLaunch);
    }

    @Override
    public void onBackupModeSelected(Consumer<Integer> onSelectedItem) {
        onSelectedItem.accept(App.settings.backupMode.ordinal());
        _onBackupModeSelected = onSelectedItem;
    }

    @Override
    public void setBackupMode(BackupMode item) {
        App.settings.backupMode = item;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableAutoBackupChanged(Consumer<Boolean> onChanged) {
        onChanged.accept(App.settings.enableAutomaticBackupAfterLaunch);
        _addOnEnableAutoBackupChanged = onChanged;
    }

    @Override
    public void setEnableAutoBackup(boolean enabled) {
        App.settings.enableAutomaticBackupAfterLaunch = enabled;
        SettingsManager.post();
    }
}
