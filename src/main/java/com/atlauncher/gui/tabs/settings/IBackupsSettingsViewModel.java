package com.atlauncher.gui.tabs.settings;


import com.atlauncher.data.BackupMode;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public interface IBackupsSettingsViewModel extends IAbstractSettingsViewModel {
    void onBackupModeSelected(Consumer<Integer> onSelectedItem);

    void setBackupMode(BackupMode item);

    void addOnEnableAutoBackupChanged(Consumer<Boolean> onChanged);

    void setEnableAutoBackup(boolean enabled);
}