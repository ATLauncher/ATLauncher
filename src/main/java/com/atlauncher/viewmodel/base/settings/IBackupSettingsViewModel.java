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

import com.atlauncher.data.BackupMode;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.BackupsSettingsTab;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 15
 * <p>
 * View model for {@link BackupsSettingsTab}
 */
public interface IBackupSettingsViewModel extends SettingsListener {
    /**
     * Listen to back up mode changes
     */
    Observable<Integer> getBackupMode();

    /**
     * Set the backup mode
     *
     * @param item backup mode
     */
    void setBackupMode(BackupMode item);

    /**
     * Listen to auto backup changes
     */
    Observable<Boolean> getEnableAutoBackup();

    /**
     * Set if auto backup is available
     */
    void setEnableAutoBackup(boolean enabled);
}
