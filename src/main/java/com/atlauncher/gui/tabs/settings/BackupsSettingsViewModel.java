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
