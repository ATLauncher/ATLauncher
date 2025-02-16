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

import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.BackupMode;
import com.atlauncher.data.CheckState;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.listener.DelayedSavingKeyListener;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.Utils;
import com.atlauncher.viewmodel.impl.settings.BackupsSettingsViewModel;

public class BackupsSettingsTab extends AbstractSettingsTab {
    private final BackupsSettingsViewModel viewModel;
    private JLabelWithHover backupsPathChecker;

    public BackupsSettingsTab(BackupsSettingsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    protected void onShow() {
        // Backup mode

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover backupModeLabel = new JLabelWithHover(GetText.tr("Backup Mode") + ":", HELP_ICON, GetText.tr(
                "When backing up an instance, what should get backed up? Mainly used for when doing automated backups."));

        add(backupModeLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JComboBox<ComboItem<BackupMode>> backupMode = new JComboBox<>();
        backupMode.addItem(new ComboItem<>(BackupMode.NORMAL, GetText.tr("Backup saves, configs and options only")));
        backupMode.addItem(new ComboItem<>(BackupMode.NORMAL_PLUS_MODS,
                GetText.tr("Backup saves, mods, configs and options only")));
        backupMode.addItem(new ComboItem<>(BackupMode.FULL, GetText.tr("Backup everything in the instance folder")));
        backupMode.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED)
                viewModel.setBackupMode(((ComboItem<BackupMode>) itemEvent.getItem()).getValue());
        });
        addDisposable(viewModel.getBackupMode().subscribe(backupMode::setSelectedIndex));

        add(backupMode, gbc);

        // Custom Backups Path

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover backupsPathLabel = new JLabelWithHover(GetText.tr("Backups Folder") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This setting allows you to change the Backups folder that the launcher uses to store instance backups. By default this is the backups folder where the launcher is installed."))
                        .build());
        add(backupsPathLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JPanel backupsPathPanel = new JPanel();
        backupsPathPanel.setLayout(new BoxLayout(backupsPathPanel, BoxLayout.X_AXIS));

        JTextField backupsPath = new JTextField(16);
        backupsPathChecker = new JLabelWithHover("", null, null);
        addDisposable(viewModel.getBackupsPath().subscribe(backupsPath::setText));
        backupsPath.addKeyListener(
                new DelayedSavingKeyListener(
                        500,
                        () -> viewModel.setBackupsPath(backupsPath.getText()),
                        viewModel::setBackupsPathPending));
        addDisposable(viewModel.getBackupsPathChecker().subscribe(this::setBackupsPathCheckState));

        JButton backupsPathResetButton = new JButton(GetText.tr("Reset"));

        backupsPathResetButton.addActionListener(e -> {
            viewModel.resetBackupsPath();
            resetBackupsPathCheckLabel();
        });

        JButton backupsPathBrowseButton = new JButton(GetText.tr("Browse"));
        backupsPathBrowseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(backupsPath.getText()));
            chooser.setDialogTitle(GetText.tr("Select"));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File selectedPath = chooser.getSelectedFile();
                backupsPath.setText(selectedPath.getAbsolutePath());
                viewModel.setBackupsPathPending();
                viewModel.setBackupsPath(selectedPath.getAbsolutePath());
            }
        });

        backupsPathPanel.add(backupsPath);
        backupsPathPanel.add(Box.createHorizontalStrut(5));
        backupsPathPanel.add(backupsPathChecker);
        backupsPathPanel.add(Box.createHorizontalStrut(5));
        backupsPathPanel.add(backupsPathResetButton);
        backupsPathPanel.add(Box.createHorizontalStrut(5));
        backupsPathPanel.add(backupsPathBrowseButton);

        add(backupsPathPanel, gbc);

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
        JCheckBox enableAutomaticBackupAfterLaunch = new JCheckBox();
        enableAutomaticBackupAfterLaunch
                .addItemListener(e -> viewModel.setEnableAutoBackup(e.getStateChange() == ItemEvent.SELECTED));
        addDisposable(viewModel.getEnableAutoBackup().subscribe(enableAutomaticBackupAfterLaunch::setSelected));
        add(enableAutomaticBackupAfterLaunch, gbc);
    }

    private void showBackupsPathWarning() {
        DialogManager.okDialog()
                .setTitle(GetText.tr("Help"))
                .setContent(
                        new HTMLBuilder()
                                .center()
                                .text(
                                        GetText.tr(
                                                "The Backups Path you set is incorrect.<br/><br/>Please verify it points to a folder and try again."))
                                .build())
                .setType(DialogManager.ERROR)
                .show();
    }

    private void setLabelState(String tooltip, String path) {
        try {
            backupsPathChecker.setToolTipText(tooltip);
            ImageIcon icon = Utils.getIconImage(path);
            if (icon != null) {
                backupsPathChecker.setIcon(icon);
                icon.setImageObserver(backupsPathChecker);
            }
        } catch (NullPointerException ignored) {
            // ignored
        }
    }

    private void resetBackupsPathPathCheckLabel() {
        backupsPathChecker.setText("");
        backupsPathChecker.setIcon(null);
        backupsPathChecker.setToolTipText(null);
    }

    private void setBackupsPathCheckState(CheckState state) {
        if (state == CheckState.NotChecking) {
            resetBackupsPathPathCheckLabel();
        } else if (state == CheckState.CheckPending) {
            setLabelState(GetText.tr("Downloads folder path change pending"),
                    "/assets/icon/warning.png");
        } else if (state == CheckState.Checking) {
            setLabelState(GetText.tr("Checking downloads folder path"),
                    "/assets/image/loading-bars-small.gif");
        } else if (state instanceof CheckState.Checked) {
            if (((CheckState.Checked) state).valid) {
                resetBackupsPathPathCheckLabel();
            } else {
                setLabelState(GetText.tr("Invalid!"), "/assets/icon/error.png");
                showBackupsPathWarning();
            }
        }
    }

    private void resetBackupsPathCheckLabel() {
        backupsPathChecker.setIcon(null);
        backupsPathChecker.setToolTipText(null);
    }

    @Override
    public String getTitle() {
        return GetText.tr("Backups");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Backups";
    }

    @Override
    protected void createViewModel() {
    }

    @Override
    protected void onDestroy() {
        removeAll();
    }
}
