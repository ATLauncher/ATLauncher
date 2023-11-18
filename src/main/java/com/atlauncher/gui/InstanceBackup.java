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
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.BackupMode;
import com.atlauncher.data.Instance;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.ZipNameMapper;

/**
 * @since 2023 / 11 / 18
 */
public class InstanceBackup {
    public static void backup(Instance instance) {
        backup(instance, App.settings.backupMode);
    }

    public static void backup(Instance instance, BackupMode backupMode) {
        // #. {0} is the name of the instance
        final JDialog dialog = new JDialog(App.launcher.getParent(), GetText.tr("Backing Up {0}", instance.launcher.name),
            Dialog.ModalityType.DOCUMENT_MODAL);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        // #. {0} is the name of the instance
        JLabel doing = new JLabel(GetText.tr("Backing Up {0}", instance.launcher.name));
        doing.setHorizontalAlignment(JLabel.CENTER);
        doing.setVerticalAlignment(JLabel.TOP);
        topPanel.add(doing);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);

        dialog.add(topPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_backup", instance));

        final Thread backupThread = new Thread(() -> {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().replaceAll("[^0-9]", "_");
            String filename = instance.getSafeName() + "-" + time.substring(0, time.lastIndexOf("_")) + ".zip";

            ArchiveUtils.createZip(instance.getRoot(), FileSystem.BACKUPS.resolve(filename),
                ZipNameMapper.getMapperForBackupMode(backupMode));

            dialog.dispose();
            App.TOASTER.pop(GetText.tr("Backup is complete"));
        });
        backupThread.start();
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                backupThread.interrupt();
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }
}
