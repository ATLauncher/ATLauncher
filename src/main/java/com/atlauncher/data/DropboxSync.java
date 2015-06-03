/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.Base64;
import com.atlauncher.utils.CompressionUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kihira
 */
public class DropboxSync extends SyncAbstract {
    private Path dropboxLocation = null;
    private String backupFolder = Constants.LAUNCHER_NAME + "Backup";

    public DropboxSync() {
        super("Dropbox");
        if (SettingsManager.getDropboxLocation().length() > 1) {
            dropboxLocation = Paths.get(SettingsManager.getDropboxLocation());
        }
    }

    public void findDropboxLocation() {
        File dropboxData = null;

        // host.db sometimes disappears for some reason
        if (Utils.isWindows()) {
            dropboxData = new File(System.getProperty("user.home"), "/AppData/Roaming/Dropbox/host.db");
        } else {
            dropboxData = new File(System.getProperty("user.home"), "/.dropbox/host.db");
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(dropboxData));
            String line;
            Path dropboxLoc = null;
            while ((line = bufferedReader.readLine()) != null) {
                dropboxLoc = Paths.get(new String(Base64.decode(line)));
                if (Files.exists(dropboxLoc)) {
                    break;
                }
            }
            dropboxLocation = dropboxLoc;
        } catch (IOException e) {
            LogManager.info("Couldn't auto find the Dropbox settings location!");
            promptUserDropboxLocation();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }
    }

    private void promptUserDropboxLocation() {
        JDialog dialog = new FileChooseDialog();
        dialog.setVisible(true);
    }

    @Override
    public void backupWorld(String backupName, File worldData, Instance instance) {
        if (dropboxLocation == null) {
            findDropboxLocation();
        }

        Path backupDir = dropboxLocation.resolve(backupFolder).resolve(instance.getName());
        Path backup = backupDir.resolve(backupName + ".zip");

        if (!Files.exists(backupDir)) {
            FileUtils.createDirectory(backupDir);
        }

        if (Files.exists(backup)) {
            JOptionPane.showMessageDialog(App.settings.getParent(), Language.INSTANCE.localizeWithReplace("backup" +
                    ".message" + ".backupexists", backupName), Language.INSTANCE.localize("backup.message" + "" +
                    ".backupexists.title"), JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                CompressionUtils.zip(worldData.toPath(), backup);
            } catch (IOException e) {
                LogManager.logStackTrace("Error compressing " + worldData, e);
            }

            if (SettingsManager.getNotifyBackup()) {
                JOptionPane.showMessageDialog(App.settings.getParent(), Language.INSTANCE.localize("backup" + "" +
                        ".complete"), Language.INSTANCE.localize("backup.complete"), JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public List<String> getBackupsForInstance(Instance instance) {
        if (dropboxLocation == null) {
            findDropboxLocation();
        }

        Path backupDir = dropboxLocation.resolve(backupFolder).resolve(instance.getName());

        if (Files.exists(backupDir)) {
            File[] files = backupDir.toFile().listFiles();
            if (files != null) {
                List<String> backupList = new ArrayList<>();
                for (File file : files) {
                    if (file.getName().matches(".*\\.zip")) {
                        backupList.add(file.getName());
                    }
                }
                return backupList;
            }
        }

        return null;
    }

    @Override
    public void restoreBackup(String backupName, Instance instance) {
        Path target = instance.getSavesDirectory().resolve(backupName.replace(".zip", ""));

        if (Files.exists(target)) {
            if (JOptionPane.showConfirmDialog(App.settings.getParent(), Language.INSTANCE.localizeWithReplace
                    ("backup" + ".message.backupoverwrite", backupName.replace(".zip", "")), Language.INSTANCE
                    .localize("backup.message.backupoverwrite.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane
                    .WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                FileUtils.unzip(dropboxLocation.resolve(backupFolder).resolve(instance.getName()).resolve(backupName)
                        , target);
            }
        } else {
            FileUtils.unzip(dropboxLocation.resolve(backupFolder).resolve(instance.getName()).resolve(backupName),
                    target);
        }

        if (SettingsManager.getNotifyBackup()) {
            JOptionPane.showMessageDialog(App.settings.getParent(), Language.INSTANCE.localize("backup.message" + "" +
                            ".restoresuccess"), Language.INSTANCE.localize("backup.message.restoresuccess.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        App.settings.clearTempDir();
    }

    @Override
    public void deleteBackup(String backupName, Instance instance) {
        Path backupData = dropboxLocation.resolve(backupFolder).resolve(instance.getName()).resolve(backupName);

        if (Files.exists(backupData)) {
            FileUtils.delete(backupData);
        }
    }

    @Override
    public CollapsiblePanel getSettingsPanel() {
        return null;
    }

    private class FileChooseDialog extends JDialog implements ActionListener {

        private static final long serialVersionUID = 1417439005699532910L;
        private final JButton folderChooseButton;
        private final JFileChooser fileChooser = new JFileChooser();

        public FileChooseDialog() {
            super(App.settings.getParent(), Language.INSTANCE.localize("dropbox.notfound.title"));

            setResizable(false);
            setSize(230, 90);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(App.settings.getParent());

            folderChooseButton = new JButton(Language.INSTANCE.localize("dropbox.label.location"));
            folderChooseButton.addActionListener(this);
            folderChooseButton.setHorizontalAlignment(SwingConstants.CENTER);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            JLabel label = new JLabel(Language.INSTANCE.localize("dropbox.label.location"));
            label.setBorder(BorderFactory.createEmptyBorder());
            label.setHorizontalAlignment(SwingConstants.CENTER);

            add(label, BorderLayout.CENTER);
            add(folderChooseButton, BorderLayout.SOUTH);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == folderChooseButton) {
                int returnVal = fileChooser.showOpenDialog(this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedFolder = fileChooser.getSelectedFile();
                    LogManager.info("User selected folder " + selectedFolder);
                    dropboxLocation = selectedFolder.toPath();
                    SettingsManager.setDropboxLocation(dropboxLocation.toString());
                    dispose();
                } else {
                    dropboxLocation = null;
                }
            }
        }
    }
}
