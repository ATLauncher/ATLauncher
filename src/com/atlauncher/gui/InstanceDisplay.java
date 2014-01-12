/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class for displaying instances in the Instance Tab
 * 
 * @author Ryan
 * 
 */
public class InstanceDisplay extends CollapsiblePanel {

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
    private JButton delete; // Delete button
    private JButton editMods; // Edit mods button
    private JButton openFolder; // Open Folder button
    private Pack pack; // The pack this instance is

    public InstanceDisplay(final Instance instance) {
        super(instance);
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

        play = new JButton(App.settings.getLocalizedString("common.play"));
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (instance.hasUpdate()
                        && !instance.hasUpdateBeenIgnored(instance.getLatestVersion())
                        && !instance.isDev()) {
                    String[] options = { App.settings.getLocalizedString("common.yes"),
                            App.settings.getLocalizedString("common.no"),
                            App.settings.getLocalizedString("instance.dontremindmeagain") };
                    int ret = JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("instance.updatenow",
                                            "<br/><br/>") + "</center></html>",
                            App.settings.getLocalizedString("instance.updateavailable"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                    if (ret == 0) {
                        if (App.settings.getAccount() == null) {
                            String[] optionss = { App.settings.getLocalizedString("common.ok") };
                            JOptionPane.showOptionDialog(App.settings.getParent(),
                                    App.settings.getLocalizedString("instance.cantupdate"),
                                    App.settings.getLocalizedString("instance.noaccountselected"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                    optionss, optionss[0]);
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

        reinstall = new JButton(App.settings.getLocalizedString("common.reinstall"));
        reinstall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.getAccount() == null) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.cantreinstall"),
                            App.settings.getLocalizedString("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    new InstanceInstallerDialog(instance);
                }
            }
        });

        // Rename Button

        rename = new JButton(App.settings.getLocalizedString("instance.rename"));
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
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.cantupdate"),
                            App.settings.getLocalizedString("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    new InstanceInstallerDialog(instance, true, false);
                }
            }
        });
        if (!instance.hasUpdate()) {
            update.setVisible(false);
        }

        // Backup Button

        backup = new JButton(App.settings.getLocalizedString("common.backup"));
        backup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new BackupDialog(instance).setVisible(true);
            }
        });

        // Delete Button

        delete = new JButton(App.settings.getLocalizedString("common.delete"));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("instance.deletesure"),
                        App.settings.getLocalizedString("instance.deleteinstance"),
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    App.settings.removeInstance(instance);
                }
            }
        });

        // Edit Mods Button

        editMods = new JButton(App.settings.getLocalizedString("common.editmods"));
        editMods.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EditModsDialog(instance);
            }
        });
        if (!instance.hasInstalledMods()) {
            editMods.setVisible(false);
        }

        // Open Folder Button

        openFolder = new JButton(App.settings.getLocalizedString("common.openfolder"));
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

        // Check if pack is a private pack and if the user can play it
        if (pack.isPrivate() && !App.settings.isInOfflineMode() && (!pack.isAllowedPlayer())) {
            if (!(instance.isDev() && pack.isTester())) {
                for (ActionListener al : play.getActionListeners()) {
                    play.removeActionListener(al);
                }
                play.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("instance.notauthorizedplay"),
                                App.settings.getLocalizedString("instance.notauthorized"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
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
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString(
                                            "instance.notauthorizedplaydev", "<br/><br/>")
                                    + "</center></html>", App.settings
                                    .getLocalizedString("instance.notauthorized"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
        }

        // Check is instance is playable and disable buttons if not

        if (!instance.isPlayable()) {
            for (ActionListener al : play.getActionListeners()) {
                play.removeActionListener(al);
            }
            play.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.corruptplay"),
                            App.settings.getLocalizedString("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
            for (ActionListener al : backup.getActionListeners()) {
                backup.removeActionListener(al);
            }
            backup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.corruptbackup"),
                            App.settings.getLocalizedString("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
        }

        if (App.settings.isInOfflineMode()) {
            for (ActionListener al : reinstall.getActionListeners()) {
                reinstall.removeActionListener(al);
            }
            reinstall.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.offlinereinstall"),
                            App.settings.getLocalizedString("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
            for (ActionListener al : update.getActionListeners()) {
                update.removeActionListener(al);
            }
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.offlineupdate"),
                            App.settings.getLocalizedString("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
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
}
