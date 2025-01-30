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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.BackupMode;
import com.atlauncher.data.CheckState;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.tabs.settings.BackupsSettingsTab;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * @since 2022 / 06 / 15
 *        <p>
 *        View model for {@link BackupsSettingsTab}
 */
public class BackupsSettingsViewModel implements SettingsListener {
    private final BehaviorSubject<Integer> backupMode = BehaviorSubject.create();

    private final BehaviorSubject<Boolean> enableAutomaticBackupAfterLaunch = BehaviorSubject.create();

    private final BehaviorSubject<String> backupsPath = BehaviorSubject.create();

    private final BehaviorSubject<CheckState> backupsPathChecker = BehaviorSubject.create();

    public BackupsSettingsViewModel() {
        onSettingsSaved();
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        backupMode.onNext(App.settings.backupMode.ordinal());
        enableAutomaticBackupAfterLaunch.onNext(App.settings.enableAutomaticBackupAfterLaunch);
        backupsPath.onNext(Optional.ofNullable(App.settings.backupsPath)
                .orElse(FileSystem.BACKUPS.toAbsolutePath().toString()));
    }

    /**
     * Listen to back up mode changes
     */
    public Observable<Integer> getBackupMode() {
        return backupMode.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the backup mode
     *
     * @param item backup mode
     */
    public void setBackupMode(BackupMode item) {
        App.settings.backupMode = item;
        SettingsManager.post();
    }

    /**
     * Listen to auto backup changes
     */
    public Observable<Boolean> getEnableAutoBackup() {
        return enableAutomaticBackupAfterLaunch.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set if auto backup is available
     */
    public void setEnableAutoBackup(boolean enabled) {
        App.settings.enableAutomaticBackupAfterLaunch = enabled;
        SettingsManager.post();
    }

    /**
     * Listen to backups path changes
     */
    public Observable<String> getBackupsPath() {
        return backupsPath.observeOn(SwingSchedulers.edt());
    }

    /**
     * Listen to backups path validation state changes
     */
    public Observable<CheckState> getBackupsPathChecker() {
        return backupsPathChecker.observeOn(SwingSchedulers.edt());
    }

    /**
     * Set the backups path
     */
    public void setBackupsPath(String path) {
        App.settings.backupsPath = path;
        SettingsManager.post();
        validateBackupsPath();
    }

    /**
     * Reset the backups path to default
     */
    public void resetBackupsPath() {
        App.settings.backupsPath = null;
        SettingsManager.post();
        validateBackupsPath();
    }

    /**
     * Mark that the backups path is pending validation
     */
    public void setBackupsPathPending() {
        backupsPathChecker.onNext(CheckState.CheckPending);
    }

    private void validateBackupsPath() {
        if (App.settings.backupsPath == null) {
            backupsPathChecker.onNext(CheckState.NotChecking);
            return;
        }

        Path path = Paths.get(App.settings.backupsPath);
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            backupsPathChecker.onNext(new CheckState.Checked(false));
            return;
        }

        backupsPathChecker.onNext(new CheckState.Checked(true));
    }
}