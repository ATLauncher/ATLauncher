package com.atlauncher.data;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.utils.Base64;
import com.atlauncher.utils.Utils;

/**
 * @author Kihira
 */
public class DropboxSync extends SyncAbstract {
    private File dropboxLocation = null;
    private String backupFolder = "ATLauncherBackup";

    public DropboxSync() {
        super("Dropbox");
        if (App.settings.getDropboxLocation().length() > 1)
            dropboxLocation = new File(App.settings.getDropboxLocation());
    }

    public void findDropboxLocation() {
        File dropboxData = null;
        // host.db sometimes disappears for some reason
        if (Utils.isWindows())
            dropboxData = new File(System.getProperty("user.home"),
                    "/AppData/Roaming/Dropbox/host.db");
        else if (Utils.isMac() || Utils.isLinux())
            dropboxData = new File(System.getProperty("user.home"), "/.dropbox/host.db");

        if (dropboxData == null) {
            promptUserDropboxLocation();
        } else {
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new FileReader(dropboxData));
                String line;
                File dropboxLoc = null;
                while ((line = bufferedReader.readLine()) != null) {
                    dropboxLoc = new File(new String(Base64.decode(line)));
                    if (dropboxLoc.exists())
                        break;
                }
                dropboxLocation = dropboxLoc;
            } catch (IOException e) {
                LogManager.info("Couldn't auto find the dropbox settings location!");
                promptUserDropboxLocation();
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        App.settings.logStackTrace(e);
                    }
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
        if (dropboxLocation == null)
            findDropboxLocation();
        File backupDir = new File(dropboxLocation, backupFolder + File.separator
                + instance.getName());
        File backup = new File(backupDir, backupName + ".zip");
        if (!backupDir.exists())
            backupDir.mkdirs();
        if (backup.exists()) {
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("backup.message.backupexists", backupName),
                    App.settings.getLocalizedString("backup.message.backupexists.title"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            Utils.zip(worldData, backup);
            if (App.settings.getNotifyBackup())
                JOptionPane.showMessageDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("backup.complete"),
                        App.settings.getLocalizedString("backup.complete"),
                        JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public List<String> getBackupsForInstance(Instance instance) {
        if (dropboxLocation == null)
            findDropboxLocation();
        File backupDir = new File(dropboxLocation, backupFolder + File.separator
                + instance.getName());
        if (backupDir.exists()) {
            File[] files = backupDir.listFiles();
            if (files != null) {
                List<String> backupList = new ArrayList<String>();
                for (File file : files) {
                    if (file.getName().matches(".*\\.zip"))
                        backupList.add(file.getName());
                }
                return backupList;
            }
        }
        return null;
    }

    @Override
    public void restoreBackup(String backupName, Instance instance) {
        File target = new File(instance.getSavesDirectory(), backupName.replace(".zip", ""));

        if (target.exists()) {
            if (JOptionPane.showConfirmDialog(
                    App.settings.getParent(),
                    App.settings.getLocalizedString("backup.message.backupoverwrite",
                            backupName.replace(".zip", "")),
                    App.settings.getLocalizedString("backup.message.backupoverwrite.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION)
                Utils.unzip(
                        new File(dropboxLocation, backupFolder + File.separator
                                + instance.getName() + File.separator + backupName), target);
        } else
            Utils.unzip(
                    new File(dropboxLocation, backupFolder + File.separator + instance.getName()
                            + File.separator + backupName), target);

        if (App.settings.getNotifyBackup())
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("backup.message.restoresuccess"),
                    App.settings.getLocalizedString("backup.message.restoresuccess.title"),
                    JOptionPane.INFORMATION_MESSAGE);
        App.settings.clearTempDir();
    }

    @Override
    public void deleteBackup(String backupName, Instance instance) {
        File backupData = new File(dropboxLocation, backupFolder + File.separator
                + instance.getName() + File.separator + backupName);
        if (backupData.exists())
            backupData.delete();
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
            super(App.settings.getParent(), App.settings
                    .getLocalizedString("dropbox.notfound.title"));

            setResizable(false);
            setSize(230, 90);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(App.settings.getParent());

            folderChooseButton = new JButton(
                    App.settings.getLocalizedString("dropbox.label.location"));
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
                    File selectedFolder = fileChooser.getSelectedFile();
                    LogManager.info("User selected folder " + selectedFolder);
                    dropboxLocation = selectedFolder;
                    App.settings.setDropboxLocation(dropboxLocation.toString());
                    dispose();
                } else
                    dropboxLocation = null;
            }
        }
    }
}
