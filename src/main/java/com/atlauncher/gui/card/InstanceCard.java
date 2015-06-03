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

package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.annot.Subscribe;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.BackupDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.CompressionUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;

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
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.Date;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 *
 * @author Ryan
 */
public class InstanceCard extends CollapsiblePanel {
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
    private final JButton editButton = new JButton(Language.INSTANCE.localize("common.editmods"));
    private final JButton openButton = new JButton(Language.INSTANCE.localize("common.openfolder"));

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
        bottom.add(this.cloneButton);
        bottom.add(this.deleteButton);
        bottom.add(this.editButton);
        bottom.add(this.openButton);

        this.rightPanel.setLayout(new BorderLayout());
        this.rightPanel.setPreferredSize(new Dimension(this.rightPanel.getPreferredSize().width, 180));
        this.rightPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane
                .HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(this.splitter, BorderLayout.CENTER);

        EventHandler.EVENT_BUS.subscribe(this);

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
            playButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptplay"), Language.INSTANCE.localize("instance.corrupt"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : backupButton.getActionListeners()) {
                backupButton.removeActionListener(al);
            }
            backupButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptbackup"), Language.INSTANCE.localize("instance.corrupt"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : cloneButton.getActionListeners()) {
                cloneButton.removeActionListener(al);
            }
            cloneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptclone"), Language.INSTANCE.localize("instance.corrupt"), JOptionPane
                            .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
        }
    }

    private void addActionListeners() {
        this.playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored((instance.isDev() ? instance
                        .getLatestDevHash() : instance.getLatestVersion()))) {
                    String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize("common"
                            + ".no"), Language.INSTANCE.localize("instance.dontremindmeagain")};
                    int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                            (Language.INSTANCE.localizeWithReplace("instance" + "" +
                                    ".updatenow", "<br/><br/>")), Language.INSTANCE.localize("instance" + "" +
                                    ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[0]);
                    if (ret == 0) {
                        if (AccountManager.getActiveAccount() == null) {
                            String[] optionss = {Language.INSTANCE.localize("common.ok")};
                            JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize
                                    ("instance.cantupdate"), Language.INSTANCE.localize("instance.noaccountselected")
                                    , JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, optionss,
                                    optionss[0]);
                        } else {
                            new InstanceInstallerDialog(instance, true, false, null, null, true);
                        }
                    } else if (ret == 1 || ret == JOptionPane.CLOSED_OPTION) {
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
            }
        });
        this.reinstallButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (AccountManager.getActiveAccount() == null) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".cantreinstall"), Language.INSTANCE.localize("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new InstanceInstallerDialog(instance);
                }
            }
        });
        this.updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (AccountManager.getActiveAccount() == null) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".cantupdate"), Language.INSTANCE.localize("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new InstanceInstallerDialog(instance, true, false, null, null, true);
                }
            }
        });
        this.renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RenameInstanceDialog(instance);
            }
        });
        this.backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isAdvancedBackupsEnabled()) {
                    new BackupDialog(instance).setVisible(true);
                } else {
                    if (Files.exists(instance.getSavesDirectory())) {
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                                (Language.INSTANCE.localizeWithReplace("backup.sure", "<br/><br/>")), Language
                                .INSTANCE.localize("backup.backingup", instance.getName()), JOptionPane.YES_NO_OPTION);
                        if (ret == JOptionPane.YES_OPTION) {
                            final JDialog dialog = new JDialog(App.settings.getParent(), Language.INSTANCE
                                    .localizeWithReplace("backup.backingup", instance.getName()), ModalityType
                                    .APPLICATION_MODAL);
                            dialog.setSize(300, 100);
                            dialog.setLocationRelativeTo(App.settings.getParent());
                            dialog.setResizable(false);

                            JPanel topPanel = new JPanel();
                            topPanel.setLayout(new BorderLayout());
                            JLabel doing = new JLabel(Language.INSTANCE.localizeWithReplace("backup.backingup",
                                    instance.getName()));
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

                            final Thread backupThread = new Thread() {
                                public void run() {
                                    Timestamp timestamp = new Timestamp(new Date().getTime());
                                    String time = timestamp.toString().replaceAll("[^0-9]", "_");
                                    String filename = instance.getSafeName() + "-" + time.substring(0, time
                                            .lastIndexOf("_")) + ".zip";
                                    try {
                                        CompressionUtils.zip(instance.getSavesDirectory(), FileSystem.BACKUPS.resolve
                                                (filename));
                                    } catch (IOException e1) {
                                        LogManager.logStackTrace("Error backing up " + instance.getName(), e1);
                                    }
                                    dialog.dispose();
                                    App.TOASTER.pop(Language.INSTANCE.localizeWithReplace("backup.backupcomplete", " " +
                                            "" + filename));
                                }
                            };
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
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("backup" +
                                ".nosaves"), Language.INSTANCE.localize("backup.nosavestitle"), JOptionPane
                                .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    }
                }
            }
        });
        this.editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EditModsDialog(instance);
            }
        });
        this.openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openExplorer(instance.getRootDirectory());
            }
        });
        this.cloneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String clonedName = JOptionPane.showInputDialog(App.settings.getParent(), Language.INSTANCE.localize
                        ("instance.cloneenter"), Language.INSTANCE.localize("instance" + "" +
                        ".clonetitle"), JOptionPane.INFORMATION_MESSAGE);
                if (clonedName != null && clonedName.length() >= 1 && InstanceManager.getInstanceByName(clonedName)
                        == null && InstanceManager.getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", ""))
                        == null && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1) {

                    final String newName = clonedName;
                    final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("instance" + "" +
                            ".clonetitle"), 0, Language.INSTANCE.localize("instance.cloninginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            InstanceManager.cloneInstance(instance, newName);
                            dialog.close();
                            App.TOASTER.pop(Language.INSTANCE.localizeWithReplace("instance.clonedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                } else if (clonedName == null || clonedName.equals("")) {
                    LogManager.error("Error Occured While Cloning Instance! Dialog Closed/Cancelled!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                            .INSTANCE.localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")),
                            Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    LogManager.error("Error Occured While Cloning Instance! Invalid Name!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                            .INSTANCE.localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")),
                            Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    LogManager.error("Error Occured While Cloning Instance! Instance With That Name Already Exists!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), HTMLUtils.centerParagraph(Language
                            .INSTANCE.localizeWithReplace("instance.errorclone", instance.getName() + "<br/><br/>")),
                            Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        this.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(App.settings.getParent(), Language.INSTANCE.localize
                        ("instance.deletesure"), Language.INSTANCE.localize("instance" + "" +
                        ".deleteinstance"), JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    final ProgressDialog dialog = new ProgressDialog(Language.INSTANCE.localize("instance" + "" +
                            ".deletetitle"), 0, Language.INSTANCE.localize("instance.deletinginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            InstanceManager.removeInstance(instance);
                            dialog.close();
                            App.TOASTER.pop(Language.INSTANCE.localizeWithReplace("instance.deletedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                }
            }
        });
    }

    private void addMouseListeners() {
        this.image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {
                        String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize
                                ("common.no"), Language.INSTANCE.localize("instance" + "" +
                                ".dontremindmeagain")};
                        int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils.centerParagraph
                                (Language.INSTANCE.localizeWithReplace("instance" + "" +
                                        ".updatenow", "<br/><br/>")), Language.INSTANCE.localize("instance" + "" +
                                        ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                null, options, options[0]);
                        if (ret == 0) {
                            if (AccountManager.getActiveAccount() == null) {
                                String[] optionss = {Language.INSTANCE.localize("common.ok")};
                                JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize
                                        ("instance.cantupdate"), Language.INSTANCE.localize("instance" + "" +
                                                ".noaccountselected"), JOptionPane.DEFAULT_OPTION, JOptionPane
                                        .ERROR_MESSAGE, null, optionss, optionss[0]);
                            } else {
                                new InstanceInstallerDialog(instance, true, false, null, null, true);
                            }
                        } else if (ret == 1 || ret == JOptionPane.CLOSED_OPTION) {
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

                    changeImageItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            chooser.setAcceptAllFileFilterUsed(false);
                            chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                            int ret = chooser.showOpenDialog(App.settings.getParent());
                            if (ret == JFileChooser.APPROVE_OPTION) {
                                Path img = chooser.getSelectedFile().toPath();
                                if (img.endsWith(".png")) {
                                    FileUtils.copyFile(img, instance.getRootDirectory().resolve("instance.png"));
                                    image.setImage(instance.getImage().getImage());
                                    instance.save();
                                }
                            }
                        }
                    });

                    updateItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())) {
                                String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE
                                        .localize("common.no"), Language.INSTANCE.localize("instance" + "" +
                                        ".dontremindmeagain")};
                                int ret = JOptionPane.showOptionDialog(App.settings.getParent(), HTMLUtils
                                        .centerParagraph(Language.INSTANCE.localize("instance" + "" +
                                                ".updatenow", "<br/><br/>")), Language.INSTANCE.localize("instance" +
                                        ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                        null, options, options[0]);
                                if (ret == 0) {
                                    if (AccountManager.getActiveAccount() == null) {
                                        String[] optionss = {Language.INSTANCE.localize("common.ok")};
                                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE
                                                .localize("instance.cantupdate"), Language.INSTANCE.localize
                                                ("instance.noaccountselected"), JOptionPane.DEFAULT_OPTION,
                                                JOptionPane.ERROR_MESSAGE, null, optionss, optionss[0]);
                                    } else {
                                        new InstanceInstallerDialog(instance, true, false, null, null, true);
                                    }
                                } else if (ret == 1 || ret == JOptionPane.CLOSED_OPTION) {
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
                        }
                    });

                    shareCodeItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (instance.getInstalledOptionalModNames().size() > 0) {
                                try {
                                    APIResponse response = Gsons.DEFAULT.fromJson(Utils.sendAPICall("pack/" +
                                            instance.getRealPack().getSafeName() + "/" + instance.getVersion() +
                                            "/share-code", instance.getShareCodeData()), APIResponse.class);

                                    if (response.wasError()) {
                                        App.TOASTER.pop(Language.INSTANCE.localize("instance.nooptionalmods"));
                                    } else {
                                        StringSelection text = new StringSelection(response.getDataAsString());
                                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                        clipboard.setContents(text, null);

                                        App.TOASTER.pop(Language.INSTANCE.localize("instance.sharecodecopied"));
                                        LogManager.info("Share code copied to clipboard");
                                    }
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                App.TOASTER.pop(Language.INSTANCE.localize("instance.nooptionalmods"));
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

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.playButton.setText(Language.INSTANCE.localize("common.play"));
        this.reinstallButton.setText(Language.INSTANCE.localize("common.reinstall"));
        this.updateButton.setText(Language.INSTANCE.localize("common.update"));
        this.renameButton.setText(Language.INSTANCE.localize("instance.rename"));
        this.backupButton.setText(Language.INSTANCE.localize("common.backup"));
        this.cloneButton.setText(Language.INSTANCE.localize("instance.clone"));
        this.deleteButton.setText(Language.INSTANCE.localize("common.delete"));
        this.editButton.setText(Language.INSTANCE.localize("common.editmods"));
        this.openButton.setText(Language.INSTANCE.localize("common.openfolder"));
    }
}
