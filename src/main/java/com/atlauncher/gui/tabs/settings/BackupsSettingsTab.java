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
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.io.File;

import javax.swing.BorderFactory;
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

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        add(Box.createVerticalStrut(18), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JPanel s3Panel = new JPanel();
        s3Panel.setLayout(new GridBagLayout());
        s3Panel.setBorder(BorderFactory.createTitledBorder(GetText.tr("S3 Backup Sync")));

        GridBagConstraints s3gbc = new GridBagConstraints();

        s3gbc.insets = UIConstants.LABEL_INSETS;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3gbc.gridx = 0;
        s3gbc.gridy = 0;
        s3Panel.add(new JLabelWithHover(GetText.tr("S3 Endpoint (If Not Using AWS S3)") + ":", HELP_ICON, GetText.tr("The endpoint URL for your S3-compatible storage. Leave blank if using AWS S3.")), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField s3EndpointField = new JTextField(24);
        addDisposable(viewModel.getS3Endpoint().subscribe(s3EndpointField::setText));
        s3EndpointField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setS3Endpoint(s3EndpointField.getText()), null));
        s3Panel.add(s3EndpointField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3Panel.add(new JLabelWithHover(GetText.tr("S3 Bucket Name") + ":", HELP_ICON, GetText.tr("The name of your S3 bucket where backups will be stored.")), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField s3BucketField = new JTextField(18);
        addDisposable(viewModel.getS3Bucket().subscribe(s3BucketField::setText));
        s3BucketField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setS3Bucket(s3BucketField.getText()), null));
        s3Panel.add(s3BucketField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3Panel.add(new JLabelWithHover(GetText.tr("S3 Region") + ":", HELP_ICON, GetText.tr("The region for your S3 bucket.")), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField s3RegionField = new JTextField(16);
        addDisposable(viewModel.getS3Region().subscribe(s3RegionField::setText));
        s3RegionField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setS3Region(s3RegionField.getText()), null));
        s3Panel.add(s3RegionField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3Panel.add(new JLabelWithHover(GetText.tr("Access Key") + ":", HELP_ICON, GetText.tr("Your access key.")), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField awsAccessKeyField = new JTextField(16);
        addDisposable(viewModel.getAWSAccessKey().subscribe(awsAccessKeyField::setText));
        awsAccessKeyField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setAWSAccessKey(awsAccessKeyField.getText()), null));
        s3Panel.add(awsAccessKeyField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3Panel.add(new JLabelWithHover(GetText.tr("Secret Access Key") + ":", HELP_ICON, GetText.tr("Your secret access key.")), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField awsSecretAccessKeyField = new JTextField(16);
        addDisposable(viewModel.getAWSSecretAccessKey().subscribe(awsSecretAccessKeyField::setText));
        awsSecretAccessKeyField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setAWSSecretAccessKey(awsSecretAccessKeyField.getText()), null));
        s3Panel.add(awsSecretAccessKeyField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        s3Panel.add(new JLabelWithHover(GetText.tr("S3 Path") + ":", HELP_ICON, new HTMLBuilder().center().split(100).text(GetText.tr("The path in your S3 bucket to store backups. You can use $INST_NAME to substitute the instance name.")).build()), s3gbc);
        s3gbc.gridx = 1;
        s3gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JTextField s3PathField = new JTextField(24);
        addDisposable(viewModel.getS3Path().subscribe(s3PathField::setText));
        s3PathField.addKeyListener(new DelayedSavingKeyListener(500, () -> viewModel.setS3Path(s3PathField.getText()), null));
        s3Panel.add(s3PathField, s3gbc);

        s3gbc.gridx = 0;
        s3gbc.gridy++;
        s3gbc.gridwidth = 2;
        s3gbc.anchor = GridBagConstraints.CENTER;
        javax.swing.JButton checkS3Button = new javax.swing.JButton(GetText.tr("Check S3 Connection"));
        checkS3Button.addActionListener(e -> {
            String endpoint = s3EndpointField.getText();
            String region = s3RegionField.getText();
            String accessKey = awsAccessKeyField.getText();
            String secretAccessKey = awsSecretAccessKeyField.getText();
            String bucket = s3BucketField.getText();
            boolean ok = com.atlauncher.utils.S3Utils.checkConnection(endpoint, region, accessKey, secretAccessKey, bucket);
            if (ok) {
                DialogManager.okDialog()
                    .setTitle(GetText.tr("S3 Connection"))
                    .setContent(GetText.tr("Successfully connected to S3 bucket: ") + bucket)
                    .setType(DialogManager.INFO)
                    .show();
            } else {
                DialogManager.okDialog()
                    .setTitle(GetText.tr("S3 Connection"))
                    .setContent(GetText.tr("Failed to connect to S3 bucket: ") + bucket)
                    .setType(DialogManager.ERROR)
                    .show();
            }
        });
        s3Panel.add(Box.createVerticalStrut(12), s3gbc);
        s3gbc.gridy++;
        s3Panel.add(checkS3Button, s3gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = UIConstants.PANEL_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;
        add(s3Panel, gbc);
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
