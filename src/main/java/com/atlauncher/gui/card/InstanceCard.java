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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
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
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.AddModsDialog;
import com.atlauncher.gui.dialogs.BackupDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.InstanceSettingsDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.reflect.TypeToken;

import org.zeroturnaround.zip.ZipUtil;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 */
@SuppressWarnings("serial")
public class InstanceCard extends CollapsiblePanel implements RelocalizationListener {
    private final JSplitPane splitter = new JSplitPane();
    private final Instance instance;
    private final JPanel rightPanel = new JPanel();
    private final JTextArea descArea = new JTextArea();
    private final ImagePanel image;
    private final JButton playButton = new JButton(Language.INSTANCE.localize("common.play"));
    private final JButton reinstallButton = new JButton(Language.INSTANCE.localize("common.reinstall"));
    private final JButton updateButton = new JButton(Language.INSTANCE.localize("common.update"));
    private final JButton renameButton = new JButton(Language.INSTANCE.localize("common.rename"));
    private final JButton backupButton = new JButton(Language.INSTANCE.localize("common.backup"));
    private final JButton cloneButton = new JButton(Language.INSTANCE.localize("instance.clone"));
    private final JButton deleteButton = new JButton(Language.INSTANCE.localize("common.delete"));
    private final JButton addButton = new JButton(Language.INSTANCE.localize("common.addmods"));
    private final JButton editButton = new JButton(Language.INSTANCE.localize("common.editmods"));
    private final JButton openButton = new JButton(Language.INSTANCE.localize("common.openfolder"));
    private final JButton settingsButton = new JButton(Language.INSTANCE.localize("tabs.settings"));

    public InstanceCard(Instance instance) {
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

        if (instance.hasEnabledCurseIntegration()) {
            bottom.add(this.addButton);
        }

        if (instance.hasEnabledEditingMods()) {
            bottom.add(this.editButton);
        }

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
        if (!instance.isPlayable()) {
            for (ActionListener al : playButton.getActionListeners()) {
                playButton.removeActionListener(al);
            }
            playButton.addActionListener(
                    e -> DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.corrupt"))
                            .setContent(Language.INSTANCE.localize("instance.corruptplay")).setType(DialogManager.ERROR)
                            .show());
            for (ActionListener al : backupButton.getActionListeners()) {
                backupButton.removeActionListener(al);
            }
            backupButton.addActionListener(
                    e -> DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.corrupt"))
                            .setContent(Language.INSTANCE.localize("instance.corruptbackup"))
                            .setType(DialogManager.ERROR).show());
            for (ActionListener al : cloneButton.getActionListeners()) {
                cloneButton.removeActionListener(al);
            }
            cloneButton.addActionListener(
                    e -> DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.corrupt"))
                            .setContent(Language.INSTANCE.localize("instance.corruptclone"))
                            .setType(DialogManager.ERROR).show());
        }
    }

    private void addActionListeners() {
        this.playButton.addActionListener(e -> {
            if (!App.settings.ignoreJavaOnInstanceLaunch() && instance.getJava() != null
                    && !Java.getMinecraftJavaVersion().equalsIgnoreCase("Unknown") && !instance.getJava().conforms()) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.incorrectjavatitle"))
                        .setContent(HTMLUtils.centerParagraph(
                                Language.INSTANCE.localizeWithReplace("instance.incorrectjava", "<br/><br/>")
                                        + instance.getJava().getVersionString()))
                        .setType(DialogManager.ERROR).show();
                return;
            }

            if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(
                    (instance.isDev() ? instance.getLatestDevHash() : instance.getLatestVersion()))) {

                int ret = DialogManager.yesNoDialog().setTitle(Language.INSTANCE.localize("instance.updateavailable"))
                        .setContent(HTMLUtils.centerParagraph(
                                Language.INSTANCE.localizeWithReplace("instance.updatenow", "<br/><br/>")))
                        .addOption(Language.INSTANCE.localize("instance.dontremindmeagain")).setType(DialogManager.INFO)
                        .show();

                if (ret == 0) {
                    if (App.settings.getAccount() == null) {
                        DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                                .setContent(Language.INSTANCE.localize("instance.cantupdate"))
                                .setType(DialogManager.ERROR).show();
                    } else {
                        Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "UpdateFromPlay",
                                "Instance");
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
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                        .setContent(Language.INSTANCE.localize("instance.cantreinstall")).setType(DialogManager.ERROR)
                        .show();
            } else {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Reinstall", "Instance");
                new InstanceInstallerDialog(instance);
            }
        });
        this.updateButton.addActionListener(e -> {
            if (App.settings.getAccount() == null) {
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                        .setContent(Language.INSTANCE.localize("instance.cantupdate")).setType(DialogManager.ERROR)
                        .show();
            } else {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Update", "Instance");
                new InstanceInstallerDialog(instance, true, false, null, null, true);
            }
        });
        this.renameButton.addActionListener(e -> new RenameInstanceDialog(instance));
        this.backupButton.addActionListener(e -> {
            if (App.settings.isAdvancedBackupsEnabled()) {
                new BackupDialog(instance).setVisible(true);
            } else {
                if (instance.getSavesDirectory().exists()) {
                    int ret = DialogManager.yesNoDialog()
                            .setTitle(Language.INSTANCE.localize("backup.backingup", instance.getName()))
                            .setContent(HTMLUtils.centerParagraph(
                                    Language.INSTANCE.localizeWithReplace("backup.sure", "<br/><br/>")))
                            .setType(DialogManager.INFO).show();

                    if (ret == DialogManager.YES_OPTION) {
                        final JDialog dialog = new JDialog(App.settings.getParent(),
                                Language.INSTANCE.localizeWithReplace("backup.backingup", instance.getName()),
                                ModalityType.APPLICATION_MODAL);
                        dialog.setSize(300, 100);
                        dialog.setLocationRelativeTo(App.settings.getParent());
                        dialog.setResizable(false);

                        JPanel topPanel = new JPanel();
                        topPanel.setLayout(new BorderLayout());
                        JLabel doing = new JLabel(
                                Language.INSTANCE.localizeWithReplace("backup.backingup", instance.getName()));
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

                        Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Backup",
                                "Instance");

                        final Thread backupThread = new Thread(() -> {
                            Timestamp timestamp = new Timestamp(new Date().getTime());
                            String time = timestamp.toString().replaceAll("[^0-9]", "_");
                            String filename = instance.getSafeName() + "-" + time.substring(0, time.lastIndexOf("_"))
                                    + ".zip";
                            ZipUtil.pack(instance.getRootDirectory(), FileSystem.BACKUPS.resolve(filename).toFile(),
                                    ZipNameMapper.INSTANCE_BACKUP);
                            dialog.dispose();
                            App.TOASTER.pop(Language.INSTANCE.localizeWithReplace("backup.backupcomplete",
                                    " " + "" + filename));
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
                    DialogManager.okDialog().setTitle(Language.INSTANCE.localize("backup.nosavestitle"))
                            .setContent(Language.INSTANCE.localize("backup.nosaves")).setType(DialogManager.ERROR)
                            .show();
                }
            }
        });
        this.addButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "AddMods", "Instance");
            new AddModsDialog(instance);
        });
        this.editButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "EditMods", "Instance");
            new EditModsDialog(instance);
        });
        this.openButton.addActionListener(e -> OS.openFileExplorer(instance.getRootDirectory().toPath()));
        this.settingsButton.addActionListener(e -> {
            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Settings", "Instance");
            new InstanceSettingsDialog(instance);
        });
        this.cloneButton.addActionListener(e -> {
            String clonedName = JOptionPane.showInputDialog(App.settings.getParent(),
                    Language.INSTANCE.localize("instance.cloneenter"),
                    Language.INSTANCE.localize("instance.clonetitle"), JOptionPane.INFORMATION_MESSAGE);
            if (clonedName != null && clonedName.length() >= 1 && App.settings.getInstanceByName(clonedName) == null
                    && App.settings.getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
                    && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1) {

                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Clone", "Instance");

                final String newName = clonedName;
                final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("instance.clonetitle"), 0,
                        Language.INSTANCE.localize("instance.cloninginstance"), null);
                dialog.addThread(new Thread(() -> {
                    App.settings.cloneInstance(instance, newName);
                    dialog.close();
                    App.TOASTER.pop(
                            Language.INSTANCE.localizeWithReplace("instance.clonedsuccessfully", instance.getName()));
                }));
                dialog.start();
            } else if (clonedName == null || clonedName.equals("")) {
                LogManager.error("Error Occured While Cloning Instance! Dialog Closed/Cancelled!");
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                                .localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")))
                        .setType(DialogManager.ERROR).show();
            } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                LogManager.error("Error Occured While Cloning Instance! Invalid Name!");
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                                .localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")))
                        .setType(DialogManager.ERROR).show();
            } else {
                LogManager.error("Error Occured While Cloning Instance! Instance With That Name Already Exists!");
                DialogManager.okDialog().setTitle(Language.INSTANCE.localize("common.error"))
                        .setContent(HTMLUtils.centerParagraph(Language.INSTANCE
                                .localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")))
                        .setType(DialogManager.ERROR).show();
            }
        });
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog().setTitle(Language.INSTANCE.localize("instance.deleteinstance"))
                    .setContent(Language.INSTANCE.localize("instance.deletesure")).setType(DialogManager.ERROR).show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "Delete", "Instance");
                final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("instance.deletetitle"), 0,
                        Language.INSTANCE.localize("instance.deletinginstance"), null);
                dialog.addThread(new Thread(() -> {
                    App.settings.removeInstance(instance);
                    dialog.close();
                    App.TOASTER.pop(
                            Language.INSTANCE.localizeWithReplace("instance.deletedsuccessfully", instance.getName()));
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
                    if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {

                        int ret = DialogManager.yesNoDialog()
                                .setTitle(Language.INSTANCE.localize("instance.updateavailable"))
                                .setContent(HTMLUtils.centerParagraph(
                                        Language.INSTANCE.localizeWithReplace("instance.updatenow", "<br/><br/>")))
                                .addOption(Language.INSTANCE.localize("instance.dontremindmeagain"))
                                .setType(DialogManager.INFO).show();

                        if (ret == 0) {
                            if (App.settings.getAccount() == null) {
                                DialogManager.okDialog()
                                        .setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                                        .setContent(Language.INSTANCE.localize("instance.cantupdate"))
                                        .setType(DialogManager.ERROR).show();
                            } else {
                                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                        "UpdateFromPlay", "Instance");
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

                    JMenuItem changeImageItem = new JMenuItem(Language.INSTANCE.localize("instance.changeimage"));
                    rightClickMenu.add(changeImageItem);

                    JMenuItem shareCodeItem = new JMenuItem(Language.INSTANCE.localize("instance.sharecode"));
                    rightClickMenu.add(shareCodeItem);

                    JMenuItem updateItem = new JMenuItem(Language.INSTANCE.localize("common.update"));
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
                                Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                        "ChangeImage", "Instance");
                                try {
                                    Utils.safeCopy(img, new File(instance.getRootDirectory(), "instance.png"));
                                    image.setImage(instance.getImage().getImage());
                                    instance.save();
                                } catch (IOException ex) {
                                    LogManager.logStackTrace("Failed to set instance image", ex);
                                }
                            }
                        }
                    });

                    updateItem.addActionListener(e12 -> {
                        if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {
                            int ret = DialogManager.yesNoDialog()
                                    .setTitle(Language.INSTANCE.localize("instance.updateavailable"))
                                    .setContent(HTMLUtils.centerParagraph(
                                            Language.INSTANCE.localizeWithReplace("instance.updatenow", "<br/><br/>")))
                                    .addOption(Language.INSTANCE.localize("instance.dontremindmeagain"))
                                    .setType(DialogManager.INFO).show();

                            if (ret == 0) {
                                if (App.settings.getAccount() == null) {
                                    DialogManager.okDialog()
                                            .setTitle(Language.INSTANCE.localize("instance.noaccountselected"))
                                            .setContent(Language.INSTANCE.localize("instance.cantupdate"))
                                            .setType(DialogManager.ERROR).show();
                                } else {
                                    Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(),
                                            "Update", "Instance");
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

                    shareCodeItem.addActionListener(e1 -> {
                        if (!instance.getInstalledOptionalModNames().isEmpty()) {
                            Analytics.sendEvent(instance.getPackName() + " - " + instance.getVersion(), "MakeShareCode",
                                    "Instance");
                            try {
                                java.lang.reflect.Type type = new TypeToken<APIResponse<String>>() {
                                }.getType();

                                APIResponse<String> response = Gsons.DEFAULT
                                        .fromJson(Utils.sendAPICall(
                                                "pack/" + instance.getRealPack().getSafeName() + "/"
                                                        + instance.getVersion() + "/share-code",
                                                instance.getShareCodeData()), type);

                                if (response.wasError()) {
                                    App.TOASTER.pop(Language.INSTANCE.localize("instance.nooptionalmods"));
                                } else {
                                    StringSelection text = new StringSelection(response.getData());
                                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                    clipboard.setContents(text, null);

                                    App.TOASTER.pop(Language.INSTANCE.localize("instance.sharecodecopied"));
                                    LogManager.info("Share code copied to clipboard");
                                }
                            } catch (IOException ex) {
                                LogManager.logStackTrace("API call failed", ex);
                            }
                        } else {
                            App.TOASTER.pop(Language.INSTANCE.localize("instance.nooptionalmods"));
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
        this.playButton.setText(Language.INSTANCE.localize("common.play"));
        this.reinstallButton.setText(Language.INSTANCE.localize("common.reinstall"));
        this.updateButton.setText(Language.INSTANCE.localize("common.update"));
        this.renameButton.setText(Language.INSTANCE.localize("instance.rename"));
        this.backupButton.setText(Language.INSTANCE.localize("common.backup"));
        this.cloneButton.setText(Language.INSTANCE.localize("instance.clone"));
        this.deleteButton.setText(Language.INSTANCE.localize("common.delete"));
        this.addButton.setText(Language.INSTANCE.localize("common.addmods"));
        this.editButton.setText(Language.INSTANCE.localize("common.editmods"));
        this.openButton.setText(Language.INSTANCE.localize("common.openfolder"));
        this.settingsButton.setText(Language.INSTANCE.localize("tabs.settings"));
    }
}
