/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.dialogs.BackupDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * TODO: Rewrite along with CollapsiblePanel
 * <p/>
 * Class for displaying instances in the Instance Tab
 *
 * @author Ryan
 */
public class InstanceDisplay extends CollapsiblePanel implements RelocalizationListener {
    private final Instance instance;
    private JPanel leftPanel; // Left panel with image
    private JPanel rightPanel; // Right panel with description and actions
    private JSplitPane splitPane; // The split pane
    private JLabel instanceImage; // The image for the instance
    private JTextArea instanceDescription; // Description of the instance
    private JSplitPane instanceActions; // All the actions that can be performed on the instance
    private JPanel instanceActionsTop; // All the actions that can be performed on the instance
    private JPanel instanceActionsBottom; // All the actions that can be performed on the instance
    private JButton play; // Play button
    private JButton reinstall; // Reinstall button
    private JButton rename; // Rename button
    private JButton update; // Update button
    private JButton backup; // Backup button
    private JButton clone; // Clone button
    private JButton delete; // Delete button
    private JButton editMods; // Edit mods button
    private JButton openFolder; // Open Folder button
    private Pack pack; // The pack this instance is

    public InstanceDisplay(final Instance instance) {
        super(instance);
        this.instance = instance;
        RelocalizationManager.addListener(this);
        pack = instance.getRealPack();
        JPanel panel = super.getContentPane();
        panel.setLayout(new BorderLayout());

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setEnabled(false);

        instanceImage = new JLabel(instance.getImage());
        instanceImage.setPreferredSize(new Dimension(300, 150));
        instanceImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion()) &&
                            !instance.isDev()) {
                        String[] options = {Language.INSTANCE.localize("common.yes"),
                                Language.INSTANCE.localize("common.no"), Language.INSTANCE.localize("instance" + "" +
                                ".dontremindmeagain")};
                        int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                                "<html><p align=\"center\">" + Language.INSTANCE.localize("instance.updatenow",
                                        "<br/><br/>") + "</p></html>", Language.INSTANCE.localize("instance" + "" +
                                        ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                null, options, options[0]);
                        if (ret == 0) {
                            if (App.settings.getAccount() == null) {
                                String[] optionss = {Language.INSTANCE.localize("common.ok")};
                                JOptionPane.showOptionDialog(App.settings.getParent(),
                                        App.settings.getLocalizedString("instance.cantupdate"),
                                        App.settings.getLocalizedString("instance.noaccountselected"),
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, optionss,
                                        optionss[0]);
                            } else {
                                new InstanceInstallerDialog(instance, true, false);
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
                    JFileChooser chooser = new JFileChooser();
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                    int ret = chooser.showOpenDialog(App.settings.getParent());
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        File img = chooser.getSelectedFile();
                        if (img.getAbsolutePath().endsWith(".png")) {
                            try {
                                Utils.safeCopy(img, new File(instance.getRootDirectory(), "instance.png"));
                                instanceImage.setIcon(instance.getImage());
                                instance.save();
                            } catch (IOException e1) {
                                e1.printStackTrace(System.err);
                            }
                        }
                    }
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

        instanceDescription = new JTextArea();
        instanceDescription.setBorder(BorderFactory.createEmptyBorder());
        instanceDescription.setEditable(false);
        instanceDescription.setHighlighter(null);
        instanceDescription.setLineWrap(true);
        instanceDescription.setWrapStyleWord(true);
        instanceDescription.setText(instance.getPackDescription());
        JScrollPane instanceDescriptionScoller = new JScrollPane(instanceDescription,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        instanceDescriptionScoller.getVerticalScrollBar().setUnitIncrement(16);

        instanceActions = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        instanceActions.setEnabled(false);
        instanceActions.setDividerSize(0);

        instanceActionsTop = new JPanel();
        instanceActionsTop.setLayout(new FlowLayout());
        instanceActionsBottom = new JPanel();
        instanceActionsBottom.setLayout(new FlowLayout());
        instanceActions.setLeftComponent(instanceActionsTop);
        instanceActions.setRightComponent(instanceActionsBottom);

        // Play Button

        play = new JButton(Language.INSTANCE.localize("common.play"));
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion()) && !instance
                        .isDev()) {
                    String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize("common"
                            + ".no"), Language.INSTANCE.localize("instance.dontremindmeagain")};
                    int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                            "<html><p align=\"center\">" + App.settings.getLocalizedString("instance.updatenow",
                                    "<br/><br/>") + "</p></html>", Language.INSTANCE.localize("instance" + "" +
                                    ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[0]);
                    if (ret == 0) {
                        if (App.settings.getAccount() == null) {
                            String[] optionss = {Language.INSTANCE.localize("common.ok")};
                            JOptionPane.showOptionDialog(App.settings.getParent(),
                                    Language.INSTANCE.localize("instance.cantupdate"),
                                    Language.INSTANCE.localize("instance.noaccountselected"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, optionss, optionss[0]);
                        } else {
                            new InstanceInstallerDialog(instance, true, false);
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

        // Reinstall Button

        reinstall = new JButton(Language.INSTANCE.localize("common.reinstall"));
        reinstall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.getAccount() == null) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".cantreinstall"), Language.INSTANCE.localize("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new InstanceInstallerDialog(instance);
                }
            }
        });

        // Rename Button

        rename = new JButton(Language.INSTANCE.localize("instance.rename"));
        rename.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new RenameInstanceDialog(instance);
            }
        });

        // Update Button

        update = new JButton("Update");
        update.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.getAccount() == null) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".cantupdate"), Language.INSTANCE.localize("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new InstanceInstallerDialog(instance, true, false);
                }
            }
        });
        if (!instance.hasUpdate()) {
            update.setVisible(false);
        }

        // Backup Button

        backup = new JButton(Language.INSTANCE.localize("common.backup"));
        backup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isAdvancedBackupsEnabled()) {
                    new BackupDialog(instance).setVisible(true);
                } else {
                    if (instance.getSavesDirectory().exists()) {
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(),
                                "<html><p align=\"center\">" + App.settings.getLocalizedString("backup.sure",
                                        "<br/><br/>") + "</p></html>", App.settings.getLocalizedString("backup" + "" +
                                        ".backingup", instance.getName()), JOptionPane.YES_NO_OPTION);
                        if (ret == JOptionPane.YES_OPTION) {
                            final JDialog dialog = new JDialog(App.settings.getParent(),
                                    App.settings.getLocalizedString("backup.backingup", instance.getName()),
                                    ModalityType.APPLICATION_MODAL);
                            dialog.setSize(300, 100);
                            dialog.setLocationRelativeTo(App.settings.getParent());
                            dialog.setResizable(false);

                            JPanel topPanel = new JPanel();
                            topPanel.setLayout(new BorderLayout());
                            JLabel doing = new JLabel(App.settings.getLocalizedString("backup.backingup",
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
                                    String filename = instance.getSafeName() + "-" + time.substring(0,
                                            time.lastIndexOf("_")) + ".zip";
                                    Utils.zip(instance.getSavesDirectory(), new File(App.settings.getBackupsDir(),
                                            filename));
                                    dialog.dispose();
                                    App.TOASTER.pop(App.settings.getLocalizedString("backup.backupcomplete",
                                            " " + filename));
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
                                ".nosaves"), Language.INSTANCE.localize("backup.nosavestitle"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    }
                }
            }
        });

        // Clone Button

        clone = new JButton(Language.INSTANCE.localize("instance.clone"));
        clone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String clonedName = JOptionPane.showInputDialog(App.settings.getParent(),
                        Language.INSTANCE.localize("instance.cloneenter"), Language.INSTANCE.localize("instance" + "" +
                                ".clonetitle"), JOptionPane.INFORMATION_MESSAGE);
                if (clonedName != null && clonedName.length() >= 1 && App.settings.getInstanceByName(clonedName) ==
                        null && App.settings.getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]",
                        "")) == null && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1) {

                    final String newName = clonedName;
                    final ProgressDialog dialog = new ProgressDialog(App.settings.getLocalizedString("instance" + "" +
                            ".clonetitle"), 0, App.settings.getLocalizedString("instance.cloninginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            App.settings.cloneInstance(instance, newName);
                            dialog.close();
                            App.TOASTER.pop(App.settings.getLocalizedString("instance.clonedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                } else if (clonedName == null || clonedName.equals("")) {
                    LogManager.error("Error Occured While Cloning Instance! Dialog Closed/Cancelled!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    LogManager.error("Error Occured While Cloning Instance! Invalid Name!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    LogManager.error("Error Occured While Cloning Instance! Instance With That Name Already Exists!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Delete Button

        delete = new JButton(Language.INSTANCE.localize("common.delete"));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(App.settings.getParent(),
                        Language.INSTANCE.localize("instance.deletesure"), Language.INSTANCE.localize("instance" + "" +
                                ".deleteinstance"), JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    final ProgressDialog dialog = new ProgressDialog(App.settings.getLocalizedString("instance" + "" +
                            ".deletetitle"), 0, App.settings.getLocalizedString("instance.deletinginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            App.settings.removeInstance(instance);
                            dialog.close();
                            App.TOASTER.pop(App.settings.getLocalizedString("instance.deletedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                }
            }
        });

        // Edit Mods Button

        editMods = new JButton(Language.INSTANCE.localize("common.editmods"));
        editMods.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EditModsDialog(instance);
            }
        });

        // Open Folder Button

        openFolder = new JButton(Language.INSTANCE.localize("common.openfolder"));
        openFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openExplorer(instance.getRootDirectory());
            }
        });

        // Check if pack can be installed and remove buttons if not

        if (!instance.canInstall()) {
            reinstall.setVisible(false);
            update.setVisible(false);
        }

        if (this.pack != null) {
            // Check if pack is a private pack and if the user can play it
            if (pack.isPrivate() && !App.settings.isInOfflineMode() && (!pack.isAllowedPlayer())) {
                if (!(instance.isDev() && pack.isTester())) {
                    for (ActionListener al : play.getActionListeners()) {
                        play.removeActionListener(al);
                    }
                    play.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            String[] options = {Language.INSTANCE.localize("common.ok")};
                            JOptionPane.showOptionDialog(App.settings.getParent(),
                                    Language.INSTANCE.localize("instance.notauthorizedplay"),
                                    Language.INSTANCE.localize("instance.notauthorized"), JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                        }
                    });
                }
            }
            // Check if instance is a dev version and if the user still has access

            if (instance.isDev() && !App.settings.isInOfflineMode() && !pack.isTester()) {
                for (ActionListener al : play.getActionListeners()) {
                    play.removeActionListener(al);
                }
                play.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                                .settings.getLocalizedString("instance.notauthorizedplaydev",
                                        "<br/><br/>") + "</p></html>", App.settings.getLocalizedString("instance" + "" +
                                ".notauthorized"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    }
                });
            }
        }

        // Check is instance is playable and disable buttons if not

        if (!instance.isPlayable()) {
            for (ActionListener al : play.getActionListeners()) {
                play.removeActionListener(al);
            }
            play.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptplay"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : backup.getActionListeners()) {
                backup.removeActionListener(al);
            }
            backup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptbackup"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : clone.getActionListeners()) {
                clone.removeActionListener(al);
            }
            clone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".corruptclone"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
        }

        if (App.settings.isInOfflineMode()) {
            for (ActionListener al : reinstall.getActionListeners()) {
                reinstall.removeActionListener(al);
            }
            reinstall.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".offlinereinstall"), Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : update.getActionListeners()) {
                update.removeActionListener(al);
            }
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                            ".offlineupdate"), Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
        }

        if (instance.isDev()) {
            update.setVisible(false);
        }

        // Add buttons to panels

        instanceActionsTop.add(play);
        instanceActionsTop.add(reinstall);
        instanceActionsTop.add(rename);
        instanceActionsTop.add(update);

        instanceActionsBottom.add(backup);
        instanceActionsBottom.add(clone);
        instanceActionsBottom.add(delete);
        instanceActionsBottom.add(editMods);
        instanceActionsBottom.add(openFolder);

        // Add panels to other panels

        leftPanel.add(instanceImage, BorderLayout.CENTER);
        rightPanel.add(instanceDescriptionScoller, BorderLayout.CENTER);
        rightPanel.add(instanceActions, BorderLayout.SOUTH);

        panel.add(splitPane, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, 180));
    }

    @Override
    public void onRelocalization() {
        this.play.setText(Language.INSTANCE.localize("common.play"));
        this.reinstall.setText(Language.INSTANCE.localize("common.reinstall"));
        this.rename.setText(Language.INSTANCE.localize("instance.rename"));
        this.backup.setText(Language.INSTANCE.localize("common.backup"));
        this.clone.setText(Language.INSTANCE.localize("instance.clone"));
        this.delete.setText(Language.INSTANCE.localize("common.delete"));
        this.editMods.setText(Language.INSTANCE.localize("common.editmods"));
        this.openFolder.setText(Language.INSTANCE.localize("common.openfolder"));
    }
}
