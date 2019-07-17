/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Constants;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.AddModsDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.InstanceSettingsDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 */
@SuppressWarnings("serial")
public class InstanceV2Card extends CollapsiblePanel implements RelocalizationListener {
    private final JSplitPane splitter = new JSplitPane();
    private final InstanceV2 instance;
    private final JPanel rightPanel = new JPanel();
    private final JTextArea descArea = new JTextArea();
    private final ImagePanel image;
    private final JButton playButton = new JButton(GetText.tr("Play"));
    private final JButton reinstallButton = new JButton(GetText.tr("Reinstall"));
    private final JButton updateButton = new JButton(GetText.tr("Update"));
    private final JButton renameButton = new JButton(GetText.tr("Rename"));
    private final JButton backupButton = new JButton(GetText.tr("Backup"));
    private final JButton cloneButton = new JButton(GetText.tr("Clone"));
    private final JButton deleteButton = new JButton(GetText.tr("Delete"));
    private final JButton addButton = new JButton(GetText.tr("Add Mods"));
    private final JButton editButton = new JButton(GetText.tr("Edit Mods"));
    private final JButton serversButton = new JButton(GetText.tr("Servers"));
    private final JButton openButton = new JButton(GetText.tr("Open Folder"));
    private final JButton settingsButton = new JButton(GetText.tr("Settings"));

    public InstanceV2Card(InstanceV2 instance) {
        super(instance);
        this.instance = instance;
        this.image = new ImagePanel(instance.getImage().getImage());
        this.splitter.setLeftComponent(this.image);
        this.splitter.setRightComponent(this.rightPanel);
        this.splitter.setEnabled(false);

        this.descArea.setText(instance.getPackDescription());
        this.descArea.setBorder(BorderFactory.createEmptyBorder());
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setLineWrap(true);
        this.descArea.setWrapStyleWord(true);
        this.descArea.setEditable(false);

        JPanel top = new JPanel(new FlowLayout());
        JPanel bottom = new JPanel(new FlowLayout());
        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        top.add(this.playButton);
        top.add(this.reinstallButton);
        top.add(this.updateButton);
        top.add(this.renameButton);
        top.add(this.backupButton);
        top.add(this.settingsButton);
        bottom.add(this.cloneButton);
        bottom.add(this.deleteButton);

        if (instance.launcher.enableCurseIntegration) {
            bottom.add(this.addButton);
        }

        if (instance.launcher.enableEditingMods) {
            bottom.add(this.editButton);
        }

        bottom.add(this.serversButton);
        bottom.add(this.openButton);

        this.rightPanel.setLayout(new BorderLayout());
        this.rightPanel.setPreferredSize(new Dimension(this.rightPanel.getPreferredSize().width, 180));
        this.rightPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(this.splitter, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        if (!instance.hasUpdate()) {
            this.updateButton.setVisible(false);
        }

        this.addActionListeners();
        this.addMouseListeners();
        this.validatePlayable();
    }

    private void validatePlayable() {
        if (!instance.launcher.isPlayable) {
            for (ActionListener al : playButton.getActionListeners()) {
                playButton.removeActionListener(al);
            }
            playButton.addActionListener(e -> DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot play instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show());
            for (ActionListener al : backupButton.getActionListeners()) {
                backupButton.removeActionListener(al);
            }
            backupButton.addActionListener(e -> DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot backup instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show());
            for (ActionListener al : cloneButton.getActionListeners()) {
                cloneButton.removeActionListener(al);
            }
            cloneButton.addActionListener(e -> DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot clone instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show());
        }
    }

    private void addActionListeners() {
        this.playButton.addActionListener(e -> {
            if (!App.settings.ignoreJavaOnInstanceLaunch() && instance.launcher.java != null
                    && !Java.getMinecraftJavaVersion().equalsIgnoreCase("Unknown")
                    && !instance.launcher.java.conforms()) {
                DialogManager.okDialog().setTitle(GetText.tr("Cannot launch instance due to your Java version"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "There was an issue launching this instance.<br/><br/>This version of the pack requires a Java version which you are not using.<br/><br/>Please install that version of Java and try again.<br/><br/>Java version needed: {0}",
                                "<br/><br/>", instance.launcher.java.getVersionString())).build())
                        .setType(DialogManager.ERROR).show();
                return;
            }

            if (instance.hasUpdate()) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                        .setContent(new HTMLBuilder().center().text(GetText
                                .tr("An update is available for this instance.<br/><br/>Do you want to update now?"))
                                .build())
                        .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                if (ret == 0) {
                    if (App.settings.getAccount() == null) {
                        DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                                .setType(DialogManager.ERROR).show();
                    } else {
                        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version,
                                "UpdateFromPlay", "InstanceV2");
                        new InstanceInstallerDialog(instance, true, false, null, null, true);
                    }
                } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION || ret == 2) {
                    if (ret == 2) {
                        instance.ignoreUpdate();
                    }

                    if (!App.settings.isMinecraftLaunched()) {
                        if (instance.launch()) {
                            App.settings.setMinecraftLaunched(true);
                        }
                    }
                }
            } else {
                if (!App.settings.isMinecraftLaunched()) {
                    if (instance.launch()) {
                        App.settings.setMinecraftLaunched(true);
                    }
                }
            }
        });
        this.reinstallButton.addActionListener(e -> {
            if (App.settings.getAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot reinstall pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Reinstall",
                        "InstanceV2");
                new InstanceInstallerDialog(instance);
            }
        });
        this.updateButton.addActionListener(e -> {
            if (App.settings.getAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
            } else {
                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Update", "InstanceV2");
                new InstanceInstallerDialog(instance, true, false, null, null, true);
            }
        });
        this.renameButton.addActionListener(e -> {
            Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Rename", "InstanceV2");
            new RenameInstanceDialog(instance);
        });
        this.backupButton.addActionListener(e -> {
            if (Files.isDirectory(instance.getRoot().resolve("saves"))) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Backing Up {0}", instance.launcher.name))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "Backups saves all your worlds as well as some other files<br/>such as your configs, so you can restore them later.<br/>Once backed up you can find the zip file in the Backups/ folder.<br/>Do you want to backup this instance?"))
                                .build())
                        .setType(DialogManager.INFO).show();

                if (ret == DialogManager.YES_OPTION) {
                    final JDialog dialog = new JDialog(App.settings.getParent(),
                            GetText.tr("Backing Up {0}", instance.launcher.name), ModalityType.APPLICATION_MODAL);
                    dialog.setSize(300, 100);
                    dialog.setLocationRelativeTo(App.settings.getParent());
                    dialog.setResizable(false);

                    JPanel topPanel = new JPanel();
                    topPanel.setLayout(new BorderLayout());
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

                    Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Backup",
                            "InstanceV2");

                    final Thread backupThread = new Thread(() -> {
                        Timestamp timestamp = new Timestamp(new Date().getTime());
                        String time = timestamp.toString().replaceAll("[^0-9]", "_");
                        String filename = instance.getSafeName() + "-" + time.substring(0, time.lastIndexOf("_"))
                                + ".zip";
                        ZipUtil.pack(instance.getRoot().toFile(), FileSystem.BACKUPS.resolve(filename).toFile(),
                                ZipNameMapper.INSTANCE_BACKUP);
                        dialog.dispose();
                        App.TOASTER.pop(GetText.tr(
                                "Backup is complete. Your backup was saved to the following location:<br/><br/>{0}",
                                filename));
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
            } else {
                DialogManager.okDialog().setType(DialogManager.WARNING).setTitle(GetText.tr("No saves found"))
                        .setContent(GetText.tr("No saves were found for this instance")).show();
            }
        });
        this.addButton.addActionListener(e -> {
            Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "AddMods", "InstanceV2");
            new AddModsDialog(instance);
        });
        this.editButton.addActionListener(e -> {
            Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "EditMods", "InstanceV2");
            new EditModsDialog(instance);
        });
        this.serversButton.addActionListener(e -> OS.openWebBrowser(
                String.format("%s/%s?utm_source=launcher&utm_medium=button&utm_campaign=instance_v2_button",
                        Constants.SERVERS_LIST_PACK, instance.getSafePackName())));
        this.openButton.addActionListener(e -> OS.openFileExplorer(instance.getRoot()));
        this.settingsButton.addActionListener(e -> {
            Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Settings", "InstanceV2");
            new InstanceSettingsDialog(instance);
        });
        this.cloneButton.addActionListener(e -> {
            String clonedName = JOptionPane.showInputDialog(App.settings.getParent(),
                    GetText.tr("Enter a new name for this cloned instance."), GetText.tr("Cloning Instance"),
                    JOptionPane.INFORMATION_MESSAGE);
            if (clonedName != null && clonedName.length() >= 1 && App.settings.getInstanceByName(clonedName) == null
                    && App.settings.getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
                    && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1) {
                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Clone", "InstanceV2");

                final String newName = clonedName;
                final ProgressDialog dialog = new ProgressDialog(GetText.tr("Cloning Instance"), 0,
                        GetText.tr("Cloning Instance. Please wait..."), null);
                dialog.addThread(new Thread(() -> {
                    App.settings.cloneInstance(instance, newName);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Cloned Instance Successfully"));
                }));
                dialog.start();
            } else if (clonedName == null || clonedName.equals("")) {
                LogManager.error("Error Occurred While Cloning Instance! Dialog Closed/Cancelled!");
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                                .build())
                        .setType(DialogManager.ERROR).show();
            } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                LogManager.error("Error Occurred While Cloning Instance! Invalid Name!");
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occurred while cloning the instance.<br/><br/>Please check the console and try again"))
                                .build())
                        .setType(DialogManager.ERROR).show();
            } else {
                LogManager.error("Error Occurred While Cloning Instance! Instance With That Name Already Exists!");
                DialogManager.okDialog().setTitle(GetText.tr("Error"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "An error occurred while cloning the instance.<br/><br/>Please check the console and try again"))
                                .build())
                        .setType(DialogManager.ERROR).show();
            }
        });
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Delete Instance"))
                    .setContent(GetText.tr("Are you sure you want to delete this instance?"))
                    .setType(DialogManager.ERROR).show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "Delete", "InstanceV2");
                final ProgressDialog dialog = new ProgressDialog(GetText.tr("Deleting Instance"), 0,
                        GetText.tr("Deleting Instance. Please wait..."), null);
                dialog.addThread(new Thread(() -> {
                    App.settings.removeInstance(instance);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Deleted Instance Successfully"));
                }));
                dialog.start();
            }
        });
    }

    private void addMouseListeners() {
        this.image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (instance.hasUpdate()
                            && !instance.hasUpdateBeenIgnored(instance.launcher.isDev ? instance.getLatestVersion().hash
                                    : instance.getLatestVersion().version)) {
                        int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "An update is available for this instance.<br/><br/>Do you want to update now?"))
                                        .build())
                                .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                        if (ret == 0) {
                            if (App.settings.getAccount() == null) {
                                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                                        .setType(DialogManager.ERROR).show();
                            } else {
                                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version,
                                        "UpdateFromPlay", "InstanceV2");
                                new InstanceInstallerDialog(instance, true, false, null, null, true);
                            }
                        } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION) {
                            if (!App.settings.isMinecraftLaunched()) {
                                if (instance.launch()) {
                                    App.settings.setMinecraftLaunched(true);
                                }
                            }
                        } else if (ret == 2) {
                            instance.ignoreUpdate();
                            if (!App.settings.isMinecraftLaunched()) {
                                if (instance.launch()) {
                                    App.settings.setMinecraftLaunched(true);
                                }
                            }
                        }
                    } else {
                        if (!App.settings.isMinecraftLaunched()) {
                            if (instance.launch()) {
                                App.settings.setMinecraftLaunched(true);
                            }
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu rightClickMenu = new JPopupMenu();

                    JMenuItem changeImageItem = new JMenuItem(GetText.tr("Change Image"));
                    rightClickMenu.add(changeImageItem);

                    JMenuItem updateItem = new JMenuItem(GetText.tr("Update"));
                    rightClickMenu.add(updateItem);

                    if (!instance.hasUpdate()) {
                        updateItem.setEnabled(false);
                    }

                    rightClickMenu.show(image, e.getX(), e.getY());

                    changeImageItem.addActionListener(e13 -> {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        chooser.setAcceptAllFileFilterUsed(false);
                        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                        int ret = chooser.showOpenDialog(App.settings.getParent());
                        if (ret == JFileChooser.APPROVE_OPTION) {
                            File img = chooser.getSelectedFile();
                            if (img.getAbsolutePath().endsWith(".png")) {
                                Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version,
                                        "ChangeImage", "InstanceV2");
                                try {
                                    Utils.safeCopy(img, instance.getRoot().resolve("instance.png").toFile());
                                    image.setImage(instance.getImage().getImage());
                                    instance.save();
                                } catch (IOException ex) {
                                    LogManager.logStackTrace("Failed to set instance image", ex);
                                }
                            }
                        }
                    });

                    updateItem.addActionListener(e12 -> {
                        if (instance.hasUpdate() && !instance
                                .hasUpdateBeenIgnored(instance.launcher.isDev ? instance.getLatestVersion().hash
                                        : instance.getLatestVersion().version)) {
                            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                                            "An update is available for this instance.<br/><br/>Do you want to update now?"))
                                            .build())
                                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO).show();

                            if (ret == 0) {
                                if (App.settings.getAccount() == null) {
                                    DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                                            .setContent(
                                                    GetText.tr("Cannot update pack as you have no account selected."))
                                            .setType(DialogManager.ERROR).show();
                                } else {
                                    Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version,
                                            "Update", "InstanceV2");
                                    new InstanceInstallerDialog(instance, true, false, null, null, true);
                                }
                            } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION) {
                                if (!App.settings.isMinecraftLaunched()) {
                                    if (instance.launch()) {
                                        App.settings.setMinecraftLaunched(true);
                                    }
                                }
                            } else if (ret == 2) {
                                instance.ignoreUpdate();
                                if (!App.settings.isMinecraftLaunched()) {
                                    if (instance.launch()) {
                                        App.settings.setMinecraftLaunched(true);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.playButton.setText(GetText.tr("Play"));
        this.reinstallButton.setText(GetText.tr("Reinstall"));
        this.updateButton.setText(GetText.tr("Update"));
        this.renameButton.setText(GetText.tr("Rename"));
        this.backupButton.setText(GetText.tr("Backup"));
        this.cloneButton.setText(GetText.tr("Clone"));
        this.deleteButton.setText(GetText.tr("Delete"));
        this.addButton.setText(GetText.tr("Add Mods"));
        this.editButton.setText(GetText.tr("Edit Mods"));
        this.serversButton.setText(GetText.tr("Servers"));
        this.openButton.setText(GetText.tr("Open Folder"));
        this.settingsButton.setText(GetText.tr("Settings"));
    }
}
