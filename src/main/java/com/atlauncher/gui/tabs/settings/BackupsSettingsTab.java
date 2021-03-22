/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.BackupMode;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.ComboItem;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class BackupsSettingsTab extends AbstractSettingsTab {
    private final JComboBox<ComboItem<BackupMode>> backupMode;
    private final JCheckBox enableAutomaticBackupAfterLaunch;

    public BackupsSettingsTab() {
        // Backup mode

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover backupModeLabel = new JLabelWithHover(GetText.tr("Backup Mode") + ":", HELP_ICON, GetText.tr(
                "When backing up an instance, what should get backed up? Mainly used for when doing automated backups."));

        add(backupModeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        backupMode = new JComboBox<>();
        backupMode.addItem(new ComboItem<>(BackupMode.NORMAL, GetText.tr("Backup saves, configs and options only")));
        backupMode.addItem(new ComboItem<>(BackupMode.NORMAL_PLUS_MODS,
                GetText.tr("Backup saves, mods, configs and options only")));
        backupMode.addItem(new ComboItem<>(BackupMode.FULL, GetText.tr("Backup everything in the instance folder")));

        for (int i = 0; i < backupMode.getItemCount(); i++) {
            ComboItem<BackupMode> item = backupMode.getItemAt(i);

            if (item.getValue() == App.settings.backupMode) {
                backupMode.setSelectedIndex(i);
                break;
            }
        }

        add(backupMode, gbc);

        // Enable automatic backup after launch

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableAutomaticBackupAfterLaunchLabel = new JLabelWithHover(
                GetText.tr("Enable Automatic Backup After Launch") + "?", HELP_ICON,
                GetText.tr("If a backup should run after launching an instance."));
        add(enableAutomaticBackupAfterLaunchLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableAutomaticBackupAfterLaunch = new JCheckBox();
        enableAutomaticBackupAfterLaunch.setSelected(App.settings.enableAutomaticBackupAfterLaunch);
        add(enableAutomaticBackupAfterLaunch, gbc);
    }

    public void save() {
        App.settings.backupMode = ((ComboItem<BackupMode>) backupMode.getSelectedItem()).getValue();
        App.settings.enableAutomaticBackupAfterLaunch = enableAutomaticBackupAfterLaunch.isSelected();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Backups");
    }
}
