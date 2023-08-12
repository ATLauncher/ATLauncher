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
package com.atlauncher.gui.card;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.BackupMode;
import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.DropDownButton;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.AddModsDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceExportDialog;
import com.atlauncher.gui.dialogs.InstanceSettingsDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.reflect.TypeToken;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 */
@SuppressWarnings("serial")
public class InstanceCard extends CollapsiblePanel implements RelocalizationListener {
    private final Instance instance;
    private final JTextArea descArea = new JTextArea();
    private final ImagePanel image;
    private final JButton updateButton = new JButton(GetText.tr("Update"));
    private final JButton deleteButton = new JButton(GetText.tr("Delete"));
    private final JButton exportButton = new JButton(GetText.tr("Export"));
    private final JButton addButton = new JButton(GetText.tr("Add Mods"));
    private final JButton editButton = new JButton(GetText.tr("Edit Mods"));
    private final JButton serversButton = new JButton(GetText.tr("Servers"));
    private final JButton openWebsite = new JButton(GetText.tr("Open Website"));
    private final JButton settingsButton = new JButton(GetText.tr("Settings"));

    private final JPopupMenu openPopupMenu = new JPopupMenu();
    private final JMenuItem openResourceMenuItem = new JMenuItem(GetText.tr("Open Resources"));
    private final DropDownButton openButton = new DropDownButton(GetText.tr("Open Folder"), openPopupMenu, true,
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    OS.openFileExplorer(instance.getRoot());
                }
            });

    private final JPopupMenu playPopupMenu = new JPopupMenu();
    private final JMenuItem playOnlinePlayMenuItem = new JMenuItem(GetText.tr("Play Online"));
    private final JMenuItem playOfflinePlayMenuItem = new JMenuItem(GetText.tr("Play Offline"));
    private final DropDownButton playButton = new DropDownButton(GetText.tr("Play"), playPopupMenu, true,
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    play(false);
                }
            });

    private final JPopupMenu backupPopupMenu = new JPopupMenu();
    private final JMenuItem normalBackupMenuItem = new JMenuItem(GetText.tr("Normal Backup"));
    private final JMenuItem normalPlusModsBackupMenuItem = new JMenuItem(GetText.tr("Normal + Mods Backup"));
    private final JMenuItem fullBackupMenuItem = new JMenuItem(GetText.tr("Full Backup"));
    private final DropDownButton backupButton = new DropDownButton(GetText.tr("Backup"), backupPopupMenu);

    private final JPopupMenu getHelpPopupMenu = new JPopupMenu();
    private final JMenuItem discordLinkMenuItem = new JMenuItem(GetText.tr("Discord"));
    private final JMenuItem supportLinkMenuItem = new JMenuItem(GetText.tr("Support"));
    private final JMenuItem websiteLinkMenuItem = new JMenuItem(GetText.tr("Website"));
    private final JMenuItem wikiLinkMenuItem = new JMenuItem(GetText.tr("Wiki"));
    private final JMenuItem sourceLinkMenuItem = new JMenuItem(GetText.tr("Source"));
    private final DropDownButton getHelpButton = new DropDownButton(GetText.tr("Get Help"), getHelpPopupMenu);

    private final JPopupMenu editInstancePopupMenu = new JPopupMenu();
    private final JMenuItem reinstallMenuItem = new JMenuItem(GetText.tr("Reinstall"));
    private final JMenuItem cloneMenuItem = new JMenuItem(GetText.tr("Clone"));
    private final JMenuItem renameMenuItem = new JMenuItem(GetText.tr("Rename"));
    private final JMenuItem changeDescriptionMenuItem = new JMenuItem(GetText.tr("Change Description"));
    private final JMenuItem changeImageMenuItem = new JMenuItem(GetText.tr("Change Image"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem addFabricMenuItem = new JMenuItem(GetText.tr("Add {0}", "Fabric"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem changeFabricVersionMenuItem = new JMenuItem(GetText.tr("Change {0} Version", "Fabric"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem removeFabricMenuItem = new JMenuItem(GetText.tr("Remove {0}", "Fabric"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem addForgeMenuItem = new JMenuItem(GetText.tr("Add {0}", "Forge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem changeForgeVersionMenuItem = new JMenuItem(GetText.tr("Change {0} Version", "Forge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem removeForgeMenuItem = new JMenuItem(GetText.tr("Remove {0}", "Forge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem addLegacyFabricMenuItem = new JMenuItem(GetText.tr("Add {0}", "Legacy Fabric"));
    // #. {0} is the loader (Forge/LegacyFabric/Quilt)
    private final JMenuItem changeLegacyFabricVersionMenuItem = new JMenuItem(
            GetText.tr("Change {0} Version", "Legacy Fabric"));
    // #. {0} is the loader (Forge/LegacyFabric/Quilt)
    private final JMenuItem removeLegacyFabricMenuItem = new JMenuItem(GetText.tr("Remove {0}", "Legacy Fabric"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem addNeoForgeMenuItem = new JMenuItem(GetText.tr("Add {0}", "NeoForge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem changeNeoForgeVersionMenuItem = new JMenuItem(GetText.tr("Change {0} Version", "NeoForge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem removeNeoForgeMenuItem = new JMenuItem(GetText.tr("Remove {0}", "NeoForge"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem addQuiltMenuItem = new JMenuItem(GetText.tr("Add {0}", "Quilt"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem changeQuiltVersionMenuItem = new JMenuItem(GetText.tr("Change {0} Version", "Quilt"));
    // #. {0} is the loader (Forge/Fabric/Quilt)
    private final JMenuItem removeQuiltMenuItem = new JMenuItem(GetText.tr("Remove {0}", "Quilt"));
    private final DropDownButton editInstanceButton = new DropDownButton(GetText.tr("Edit Instance"),
            editInstancePopupMenu);

    public InstanceCard(Instance instance, String instanceTitleFormat) {
        super(instance, instanceTitleFormat);
        this.instance = instance;
        this.image = new ImagePanel(instance.getImage().getImage());
        JSplitPane splitter = new JSplitPane();
        splitter.setLeftComponent(this.image);
        JPanel rightPanel = new JPanel();
        splitter.setRightComponent(rightPanel);
        splitter.setEnabled(false);

        this.descArea.setText(instance.getPackDescription());
        this.descArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setLineWrap(true);
        this.descArea.setWrapStyleWord(true);
        this.descArea.setEditable(false);

        if (instance.canChangeDescription()) {
            this.descArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        instance.startChangeDescription();
                        descArea.setText(instance.launcher.description);
                    }
                }
            });
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        as.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        top.add(this.playButton);
        top.add(this.updateButton);
        top.add(this.editInstanceButton);
        top.add(this.backupButton);
        top.add(this.settingsButton);

        bottom.add(this.deleteButton);
        bottom.add(this.exportButton);
        bottom.add(this.getHelpButton);

        setupPlayPopupMenus();
        setupOpenPopupMenus();
        setupButtonPopupMenus();

        // check it can be exported
        this.exportButton.setVisible(instance.canBeExported());

        this.getHelpButton.setVisible(instance.showGetHelpButton());

        if (!instance.isUpdatable()) {
            this.updateButton.setVisible(instance.isUpdatable());
        }

        if (instance.isExternalPack() || instance.launcher.vanillaInstance) {
            this.serversButton.setVisible(false);
        }

        if (instance.getPack() != null && instance.getPack().system) {
            this.serversButton.setVisible(false);
        }

        this.openWebsite.setVisible(instance.hasWebsite());

        if (instance.launcher.enableCurseForgeIntegration
                && (ConfigManager.getConfigItem("platforms.curseforge.modsEnabled", true) == true
                        || (ConfigManager.getConfigItem("platforms.modrinth.modsEnabled", true) == true
                                && this.instance.launcher.loaderVersion != null))) {
            bottom.add(this.addButton);
        }

        if (instance.launcher.enableEditingMods) {
            bottom.add(this.editButton);
        }

        bottom.add(this.serversButton);
        bottom.add(this.openWebsite);
        bottom.add(this.openButton);

        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, 155));
        rightPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(splitter, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        if (!instance.hasUpdate()) {
            this.updateButton.setVisible(false);
        }

        this.addActionListeners();
        this.addMouseListeners();
    }

    private void setupPlayPopupMenus() {
        playOnlinePlayMenuItem.addActionListener(e -> {
            play(false);
        });
        playPopupMenu.add(playOnlinePlayMenuItem);

        playOfflinePlayMenuItem.addActionListener(e -> {
            play(true);
        });
        playPopupMenu.add(playOfflinePlayMenuItem);
    }

    private void setupOpenPopupMenus() {
        openResourceMenuItem.addActionListener(e -> {
            DialogManager.okDialog().setTitle(GetText.tr("Reminder"))
                    .setContent(GetText.tr("You may not distribute ANY resources."))
                    .setType(DialogManager.WARNING).show();
            OS.openFileExplorer(instance.getMinecraftJarLibraryPath());
        });
        openPopupMenu.add(openResourceMenuItem);
    }

    private void setupButtonPopupMenus() {
        if (instance.showGetHelpButton()) {
            if (instance.getDiscordInviteUrl() != null) {
                discordLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getDiscordInviteUrl()));
                getHelpPopupMenu.add(discordLinkMenuItem);
            }

            if (instance.getSupportUrl() != null) {
                supportLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getSupportUrl()));
                getHelpPopupMenu.add(supportLinkMenuItem);
            }

            if (instance.getWebsiteUrl() != null) {
                websiteLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getWebsiteUrl()));
                getHelpPopupMenu.add(websiteLinkMenuItem);
            }

            if (instance.getWikiUrl() != null) {
                wikiLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getWikiUrl()));
                getHelpPopupMenu.add(wikiLinkMenuItem);
            }

            if (instance.getSourceUrl() != null) {
                sourceLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getSourceUrl()));
                getHelpPopupMenu.add(sourceLinkMenuItem);
            }
        }

        normalBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.NORMAL));
        backupPopupMenu.add(normalBackupMenuItem);

        normalPlusModsBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.NORMAL_PLUS_MODS));
        backupPopupMenu.add(normalPlusModsBackupMenuItem);

        fullBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.FULL));
        backupPopupMenu.add(fullBackupMenuItem);

        setupEditInstanceButton();
    }

    private void setupEditInstanceButton() {
        editInstancePopupMenu.add(reinstallMenuItem);
        editInstancePopupMenu.add(cloneMenuItem);
        editInstancePopupMenu.add(renameMenuItem);
        editInstancePopupMenu.add(changeDescriptionMenuItem);
        editInstancePopupMenu.add(changeImageMenuItem);
        editInstancePopupMenu.addSeparator();

        if (ConfigManager.getConfigItem("loaders.fabric.enabled", true) == true
                && !ConfigManager.getConfigItem("loaders.fabric.disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(addFabricMenuItem);
            editInstancePopupMenu.add(changeFabricVersionMenuItem);
        }
        editInstancePopupMenu.add(removeFabricMenuItem);

        if (ConfigManager.getConfigItem("loaders.forge.enabled", true) == true
                && !ConfigManager.getConfigItem("loaders.forge.disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(addForgeMenuItem);
            editInstancePopupMenu.add(changeForgeVersionMenuItem);
        }
        editInstancePopupMenu.add(removeForgeMenuItem);

        if (ConfigManager.getConfigItem("loaders.legacyfabric.enabled", true) == true
                && !ConfigManager
                        .getConfigItem("loaders.legacyfabric.disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(addLegacyFabricMenuItem);
            editInstancePopupMenu.add(changeLegacyFabricVersionMenuItem);
        }
        editInstancePopupMenu.add(removeLegacyFabricMenuItem);

        if (ConfigManager.getConfigItem("loaders.neoforge.enabled", true) == true
                && !ConfigManager
                        .getConfigItem("loaders.neoforge.disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(addNeoForgeMenuItem);
            editInstancePopupMenu.add(changeNeoForgeVersionMenuItem);
        }
        editInstancePopupMenu.add(removeNeoForgeMenuItem);

        if (ConfigManager.getConfigItem("loaders.quilt.enabled", false) == true
                && !ConfigManager.getConfigItem("loaders.quilt.disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(addQuiltMenuItem);
            editInstancePopupMenu.add(changeQuiltVersionMenuItem);
        }
        editInstancePopupMenu.add(removeQuiltMenuItem);

        setEditInstanceMenuItemVisbility();

        reinstallMenuItem.addActionListener(e -> instance.startReinstall());
        cloneMenuItem.addActionListener(e -> instance.startClone());
        renameMenuItem.addActionListener(e -> instance.startRename());
        changeDescriptionMenuItem.addActionListener(e -> {
            instance.startChangeDescription();
            descArea.setText(instance.launcher.description);
        });
        changeImageMenuItem.addActionListener(e -> {
            instance.startChangeImage();
            image.setImage(instance.getImage().getImage());
        });

        // loader things
        addFabricMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.FABRIC);
            setEditInstanceMenuItemVisbility();
        });
        addForgeMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.FORGE);
            setEditInstanceMenuItemVisbility();
        });
        addLegacyFabricMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.LEGACY_FABRIC);
            setEditInstanceMenuItemVisbility();
        });
        addNeoForgeMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.NEOFORGE);
            setEditInstanceMenuItemVisbility();
        });
        addQuiltMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.QUILT);
            setEditInstanceMenuItemVisbility();
        });

        changeFabricVersionMenuItem.addActionListener(e -> {
            instance.changeLoaderVersion();
            setEditInstanceMenuItemVisbility();
        });
        changeForgeVersionMenuItem.addActionListener(e -> {
            instance.changeLoaderVersion();
            setEditInstanceMenuItemVisbility();
        });
        changeLegacyFabricVersionMenuItem.addActionListener(e -> {
            instance.changeLoaderVersion();
            setEditInstanceMenuItemVisbility();
        });
        changeNeoForgeVersionMenuItem.addActionListener(e -> {
            instance.changeLoaderVersion();
            setEditInstanceMenuItemVisbility();
        });
        changeQuiltVersionMenuItem.addActionListener(e -> {
            instance.changeLoaderVersion();
            setEditInstanceMenuItemVisbility();
        });

        removeFabricMenuItem.addActionListener(e -> {
            instance.removeLoader();
            setEditInstanceMenuItemVisbility();
        });
        removeForgeMenuItem.addActionListener(e -> {
            instance.removeLoader();
            setEditInstanceMenuItemVisbility();
        });
        removeLegacyFabricMenuItem.addActionListener(e -> {
            instance.removeLoader();
            setEditInstanceMenuItemVisbility();
        });
        removeNeoForgeMenuItem.addActionListener(e -> {
            instance.removeLoader();
            setEditInstanceMenuItemVisbility();
        });
        removeQuiltMenuItem.addActionListener(e -> {
            instance.removeLoader();
            setEditInstanceMenuItemVisbility();
        });
    }

    private void setEditInstanceMenuItemVisbility() {
        reinstallMenuItem.setVisible(instance.isUpdatable());

        addFabricMenuItem.setVisible(instance.launcher.loaderVersion == null);
        addForgeMenuItem.setVisible(instance.launcher.loaderVersion == null);
        addLegacyFabricMenuItem.setVisible(instance.launcher.loaderVersion == null);
        addNeoForgeMenuItem.setVisible(instance.launcher.loaderVersion == null);
        addQuiltMenuItem.setVisible(instance.launcher.loaderVersion == null);

        changeFabricVersionMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isFabric());
        changeForgeVersionMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isForge());
        changeLegacyFabricVersionMenuItem
                .setVisible(
                        instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isLegacyFabric());
        changeNeoForgeVersionMenuItem
                .setVisible(
                        instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isNeoForge());
        changeQuiltVersionMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isQuilt());

        removeFabricMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isFabric());
        removeForgeMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isForge());
        removeLegacyFabricMenuItem
                .setVisible(
                        instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isLegacyFabric());
        removeNeoForgeMenuItem
                .setVisible(
                        instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isNeoForge());
        removeQuiltMenuItem
                .setVisible(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isQuilt());
    }

    private void addActionListeners() {
        this.updateButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
                return;
            }

            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_update", instance));
            instance.update();
        });
        this.addButton.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_add_mods", instance));
            new AddModsDialog(instance);
            exportButton.setVisible(instance.canBeExported());
        });
        this.editButton.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_edit_mods", instance));
            new EditModsDialog(instance);
            exportButton.setVisible(instance.canBeExported());
        });
        this.serversButton.addActionListener(e -> OS.openWebBrowser(
                String.format("%s/%s?utm_source=launcher&utm_medium=button&utm_campaign=instance_v2_button",
                        Constants.SERVERS_LIST_PACK, instance.getSafePackName())));
        this.openWebsite.addActionListener(e -> OS.openWebBrowser(instance.getWebsiteUrl()));
        this.settingsButton.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_settings", instance));
            new InstanceSettingsDialog(instance);
        });
        this.deleteButton.addActionListener(e -> {
            int ret = DialogManager.yesNoDialog(false).setTitle(GetText.tr("Delete Instance"))
                    .setContent(
                            GetText.tr("Are you sure you want to delete the instance \"{0}\"?", instance.launcher.name))
                    .setType(DialogManager.ERROR).show();

            if (ret == DialogManager.YES_OPTION) {
                Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_delete", instance));
                final ProgressDialog dialog = new ProgressDialog(GetText.tr("Deleting Instance"), 0,
                        GetText.tr("Deleting Instance. Please wait..."), null, App.launcher.getParent());
                dialog.addThread(new Thread(() -> {
                    InstanceManager.removeInstance(instance);
                    dialog.close();
                    App.TOASTER.pop(GetText.tr("Deleted Instance Successfully"));
                }));
                dialog.start();
            }
        });
        this.exportButton.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_export", instance));
            new InstanceExportDialog(instance);
        });
    }

    private void play(boolean offline) {
        if (!instance.launcher.isPlayable) {
            DialogManager.okDialog().setTitle(GetText.tr("Instance Corrupt"))
                    .setContent(GetText
                            .tr("Cannot play instance as it's corrupted. Please reinstall, update or delete it."))
                    .setType(DialogManager.ERROR).show();
            return;
        }

        if (!App.settings.ignoreJavaOnInstanceLaunch && instance.shouldShowWrongJavaWarning()) {
            DialogManager.okDialog().setTitle(GetText.tr("Cannot launch instance due to your Java version"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "There was an issue launching this instance.<br/><br/>This version of the pack requires a Java version which you are not using.<br/><br/>Please install that version of Java and try again.<br/><br/>Java version needed: {0}",
                            instance.launcher.java.getVersionString())).build())
                    .setType(DialogManager.ERROR).show();
            return;
        }

        if (instance.hasUpdate() && !instance.hasLatestUpdateBeenIgnored()) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Update Available"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr(
                                    "An update is available for this instance.<br/><br/>Do you want to update now?"))
                            .build())
                    .addOption(GetText.tr("Ignore This Update"))
                    .addOption(GetText.tr("Don't Remind Me Again")).setType(DialogManager.INFO)
                    .show();

            if (ret == 0) {
                if (AccountManager.getSelectedAccount() == null) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                            .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                            .setType(DialogManager.ERROR).show();
                } else {
                    Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_update", instance));
                    instance.update();
                }
            } else if (ret == 1 || ret == DialogManager.CLOSED_OPTION || ret == 2 || ret == 3) {
                if (ret == 2) {
                    instance.ignoreUpdate();
                } else if (ret == 3) {
                    instance.ignoreAllUpdates();
                }

                if (!App.launcher.minecraftLaunched) {
                    if (instance.launch()) {
                        App.launcher.setMinecraftLaunched(true);
                    }
                }
            }
        } else {
            if (!App.launcher.minecraftLaunched) {
                if (instance.launch(offline)) {
                    App.launcher.setMinecraftLaunched(true);
                }
            }
        }
    }

    private void addMouseListeners() {
        this.image.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    play(false);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu rightClickMenu = new JPopupMenu();

                    JMenuItem playOnlineButton = new JMenuItem(GetText.tr("Play Online"));
                    playOnlineButton.addActionListener(l -> {
                        play(false);
                    });
                    rightClickMenu.add(playOnlineButton);

                    JMenuItem playOfflineButton = new JMenuItem(GetText.tr("Play Offline"));
                    playOfflineButton.addActionListener(l -> {
                        play(true);
                    });
                    rightClickMenu.add(playOnlineButton);

                    if (instance.isUpdatable()) {
                        rightClickMenu.addSeparator();
                    }

                    JMenuItem reinstallItem = new JMenuItem(GetText.tr("Reinstall"));
                    reinstallItem.addActionListener(l -> instance.startReinstall());
                    reinstallItem.setVisible(instance.isUpdatable());
                    rightClickMenu.add(reinstallItem);

                    JMenuItem updateItem = new JMenuItem(GetText.tr("Update"));
                    updateItem.addActionListener(l -> instance.update());
                    updateItem.setVisible(instance.isUpdatable());
                    updateItem.setEnabled(instance.hasUpdate() && instance.launcher.isPlayable);
                    rightClickMenu.add(updateItem);

                    rightClickMenu.addSeparator();

                    JMenuItem renameItem = new JMenuItem(GetText.tr("Rename"));
                    renameMenuItem.addActionListener(l -> instance.startRename());
                    rightClickMenu.add(renameItem);

                    JMenuItem changeDescriptionItem = new JMenuItem(GetText.tr("Change Description"));
                    changeDescriptionItem.addActionListener(l -> {
                        instance.startChangeDescription();
                        descArea.setText(instance.launcher.description);
                    });
                    changeDescriptionItem.setVisible(instance.canChangeDescription());
                    rightClickMenu.add(changeDescriptionItem);

                    JMenuItem changeImageItem = new JMenuItem(GetText.tr("Change Image"));
                    changeImageItem.addActionListener(l -> {
                        instance.startChangeImage();
                        image.setImage(instance.getImage().getImage());
                    });
                    rightClickMenu.add(changeImageItem);

                    JMenuItem cloneItem = new JMenuItem(GetText.tr("Clone"));
                    cloneItem.addActionListener(l -> {
                        instance.startClone();
                    });
                    rightClickMenu.add(cloneItem);

                    JMenuItem shareCodeItem = new JMenuItem(GetText.tr("Share Code"));
                    shareCodeItem.addActionListener(e1 -> {
                        Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_make_share_code", instance));
                        try {
                            java.lang.reflect.Type type = new TypeToken<APIResponse<String>>() {
                            }.getType();

                            APIResponse<String> response = Gsons.DEFAULT.fromJson(
                                    Utils.sendAPICall("pack/" + instance.getSafePackName() + "/"
                                            + instance.launcher.version + "/share-code", instance.getShareCodeData()),
                                    type);

                            if (response.wasError()) {
                                App.TOASTER.pop(GetText.tr("Error getting share code."));
                            } else {
                                StringSelection text = new StringSelection(response.getData());
                                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                                clipboard.setContents(text, null);

                                App.TOASTER.pop(GetText.tr("Share code copied to clipboard"));
                                LogManager.info("Share code copied to clipboard");
                            }
                        } catch (IOException ex) {
                            LogManager.logStackTrace("API call failed", ex);
                        }
                    });
                    shareCodeItem.setVisible((instance.getPack() != null && !instance.getPack().system)
                            && !instance.isExternalPack() && !instance.launcher.vanillaInstance
                            && instance.launcher.mods.stream().anyMatch(mod -> mod.optional));
                    rightClickMenu.add(shareCodeItem);

                    rightClickMenu.show(image, e.getX(), e.getY());
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

    public Instance getInstance() {
        return instance;
    }

    @Override
    public void onRelocalization() {
        this.playButton.setText(GetText.tr("Play"));
        this.updateButton.setText(GetText.tr("Update"));
        this.backupButton.setText(GetText.tr("Backup"));
        this.deleteButton.setText(GetText.tr("Delete"));
        this.addButton.setText(GetText.tr("Add Mods"));
        this.editButton.setText(GetText.tr("Edit Mods"));
        this.serversButton.setText(GetText.tr("Servers"));
        this.openWebsite.setText(GetText.tr("Open Website"));
        this.openButton.setText(GetText.tr("Open Folder"));
        this.openResourceMenuItem.setText(GetText.tr("Open Resources"));
        this.settingsButton.setText(GetText.tr("Settings"));

        this.normalBackupMenuItem.setText(GetText.tr("Normal Backup"));
        this.normalPlusModsBackupMenuItem.setText(GetText.tr("Normal + Mods Backup"));
        this.fullBackupMenuItem.setText(GetText.tr("Full Backup"));
        this.backupButton.setText(GetText.tr("Backup"));

        this.discordLinkMenuItem.setText(GetText.tr("Discord"));
        this.supportLinkMenuItem.setText(GetText.tr("Support"));
        this.websiteLinkMenuItem.setText(GetText.tr("Website"));
        this.wikiLinkMenuItem.setText(GetText.tr("Wiki"));
        this.sourceLinkMenuItem.setText(GetText.tr("Source"));
        this.getHelpButton.setText(GetText.tr("Get Help"));
    }
}
