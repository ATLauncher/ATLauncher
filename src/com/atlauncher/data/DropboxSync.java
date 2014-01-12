package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.gui.CollapsiblePanel;
import com.atlauncher.utils.Base64;
import com.atlauncher.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kihira
 */
public class DropboxSync extends SyncAbstract {

    private Path dropboxLocation = null;
    private String backupFolder = "ATLauncherBackup";

    public DropboxSync() {
        super("Dropbox");
        if (App.settings.getDropboxLocation().length() > 1) dropboxLocation = Paths.get(App.settings.getDropboxLocation());
    }

    public void findDropboxLocation() {
        Path dropboxData = null;
        //host.db sometimes disappears for some reason
        if (Utils.isWindows()) dropboxData = Paths.get(System.getProperty("user.home"), "/AppData/Roaming/Dropbox/host.db");
        else if (Utils.isMac() || Utils.isLinux()) dropboxData = Paths.get(System.getProperty("user.home"), "/.dropbox/host.db");

        if (dropboxData == null) {
            promptUserDropboxLocation();
        }
        else {
            try {
                BufferedReader bufferedReader = Files.newBufferedReader(dropboxData, Charset.defaultCharset());
                String line;
                Path dropboxLoc = null;
                while ((line = bufferedReader.readLine()) != null) {
                    dropboxLoc = Paths.get(new String(Base64.decode(line)));
                    if (Files.exists(dropboxLoc)) break;
                }
                dropboxLocation = dropboxLoc;
            } catch (IOException e) {
                App.settings.log("Couldn't auto find the dropbox settings location!");
                promptUserDropboxLocation();
            }
        }
    }

    private void promptUserDropboxLocation() {
        JDialog dialog = new FileChooseDialog();
        dialog.setVisible(true);
    }

    @Override
    public void backupWorld(String backupName, Path worldData, String instanceName) {
        if (dropboxLocation == null) findDropboxLocation();
        Path backupDir = dropboxLocation.resolve(Paths.get(backupFolder, instanceName));
        try {
            if (!Files.exists(backupDir)) Files.createDirectories(backupDir);
            if (Files.exists(backupDir.resolve(backupName + ".zip"))) {
                JOptionPane.showMessageDialog(App.settings.getParent(), App.settings.getLocalizedString("backup.message.backupexists", backupName),
                        App.settings.getLocalizedString("backup.message.backupexists.title"), JOptionPane.ERROR_MESSAGE);
            }
            else {
                Utils.zip(worldData.toFile(), backupDir.resolve(backupName + ".zip").toFile());
                if (App.settings.getNotifyBackup()) JOptionPane.showMessageDialog(App.settings.getParent(), App.settings.getLocalizedString("backup.complete"),
                        App.settings.getLocalizedString("backup.complete"), JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(App.settings.getParent(), App.settings.getLocalizedString("backup.message.backupfailed") + "\n\n" + e.fillInStackTrace(),
                    App.settings.getLocalizedString("backup.message.backupfailed.title"), JOptionPane.ERROR_MESSAGE);
            App.settings.logStackTrace(e);
        }
    }

    @Override
    public List<String> getBackupsForInstance(String instanceName) {
        if (dropboxLocation == null) findDropboxLocation();
        Path backupDir = dropboxLocation.resolve(Paths.get(backupFolder, instanceName));
        if (Files.exists(backupDir)) {
            try {
                DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir, "*.zip");
                List<String> backupList = new ArrayList<>();
                for (Path file:stream) {
                    backupList.add(file.getFileName().toString());
                }
                return backupList;
            } catch (IOException | DirectoryIteratorException e) {
                App.settings.logStackTrace(e);
            }
        }
        return null;
    }

    @Override
    public void restoreBackup(String backupName, Instance instance) {
        Path target = instance.getSavesDirectory().toPath().resolve(backupName.replace(".zip", ""));

        if (Files.exists(target)) {
            if (JOptionPane.showConfirmDialog(App.settings.getParent(), App.settings.getLocalizedString("backup.message.backupoverwrite", backupName.replace(".zip", "")),
                    App.settings.getLocalizedString("backup.message.backupoverwrite.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) Utils.unzip(
                    dropboxLocation.resolve(Paths.get(backupFolder, instance.getName(), backupName)).toFile(), target.toFile());
        }
        else Utils.unzip(dropboxLocation.resolve(Paths.get(backupFolder, instance.getName(), backupName)).toFile(), target.toFile());

        if (App.settings.getNotifyBackup()) JOptionPane.showMessageDialog(App.settings.getParent(), App.settings.getLocalizedString("backup.message.restoresuccess"),
                App.settings.getLocalizedString("backup.message.restoresuccess.title"), JOptionPane.INFORMATION_MESSAGE);
        App.settings.clearTempDir();
    }

    @Override
    public void deleteBackup(String backupName, Instance instance) {
        Path backupData = dropboxLocation.resolve(Paths.get(backupFolder, instance.getName(), backupName));
        try {
            Files.deleteIfExists(backupData);
        } catch (IOException e) {
            App.settings.logStackTrace(e);
        }
    }

    @Override
    public CollapsiblePanel getSettingsPanel() {
        return null;
    }

    private class FileChooseDialog extends JDialog implements ActionListener {

        private final JButton folderChooseButton;
        private final JFileChooser fileChooser = new JFileChooser();

        public FileChooseDialog() {
            super(App.settings.getParent(), App.settings.getLocalizedString("dropbox.notfound.title"));

            setResizable(false);
            setSize(230, 90);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(App.settings.getParent());

            folderChooseButton = new JButton(App.settings.getLocalizedString("dropbox.label.location"));
            folderChooseButton.addActionListener(this);
            folderChooseButton.setHorizontalAlignment(SwingConstants.CENTER);

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            JLabel label = new JLabel(App.settings.getLocalizedString("dropbox.label.location"));
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
                    Path selectedFolder = fileChooser.getSelectedFile().toPath();
                    App.settings.log("User selected folder " + selectedFolder);
                    dropboxLocation = selectedFolder;
                    App.settings.setDropboxLocation(dropboxLocation.toString());
                    dispose();
                }
                else dropboxLocation = null;
            }
        }
    }
}
