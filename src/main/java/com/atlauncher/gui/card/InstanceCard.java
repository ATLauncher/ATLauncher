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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.BackupMode;
import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
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
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.OS;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 */
@SuppressWarnings("serial")
public class InstanceCard extends CollapsiblePanel implements RelocalizationListener {

    private final Instance instance;
    private final JTextArea descArea = new JTextArea();
    private final ImagePanel image;

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

    private final JPopupMenu settingsPopupMenu = new JPopupMenu();
    private final JMenuItem exportItem = new JMenuItem(GetText.tr("Export"));
    private final JMenuItem normalBackupMenuItem = new JMenuItem(GetText.tr("Normal Backup"));
    private final JMenuItem normalPlusModsBackupMenuItem = new JMenuItem(GetText.tr("Normal + Mods Backup"));
    private final JMenuItem fullBackupMenuItem = new JMenuItem(GetText.tr("Full Backup"));
    private final DropDownButton settingsButton = new DropDownButton(GetText.tr("Settings"), settingsPopupMenu, true,
            new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_settings", instance));
                    new InstanceSettingsDialog(instance);
                }
            });

    private final JPopupMenu modingPopupMenu = new JPopupMenu();
    private final JMenuItem addModsItem = new JMenuItem(GetText.tr("Add Mods"));
    private final JMenuItem editModsItem = new JMenuItem(GetText.tr("Edit Mods"));
    private final DropDownButton modingButton = new DropDownButton(GetText.tr("Moding"), modingPopupMenu);

    private final JPopupMenu morePopupMenu = new JPopupMenu();
    private final JMenuItem openFolderItem = new JMenuItem(GetText.tr("Open Folder"));
    private final JMenuItem openResourceMenuItem = new JMenuItem(GetText.tr("Open Resources"));
    private final JMenuItem discordLinkMenuItem = new JMenuItem(GetText.tr("Discord"));
    private final JMenuItem supportLinkMenuItem = new JMenuItem(GetText.tr("Support"));
    private final JMenuItem websiteLinkMenuItem = new JMenuItem(GetText.tr("Website"));
    private final JMenuItem wikiLinkMenuItem = new JMenuItem(GetText.tr("Wiki"));
    private final JMenuItem sourceLinkMenuItem = new JMenuItem(GetText.tr("Source"));
    private final JMenuItem updateItem = new JMenuItem(GetText.tr("Update"));
    private final JMenuItem serversItem = new JMenuItem(GetText.tr("Servers"));
    private final JMenuItem websiteItem = new JMenuItem(GetText.tr("Open Website"));
    private final DropDownButton moreButton = new DropDownButton("...", morePopupMenu);

    private final JPopupMenu editInstancePopupMenu = new JPopupMenu();
    private final JMenuItem reinstallMenuItem = new JMenuItem(GetText.tr("Reinstall"));
    private final JMenuItem cloneMenuItem = new JMenuItem(GetText.tr("Clone"));
    private final JMenuItem renameMenuItem = new JMenuItem(GetText.tr("Rename"));
    private final JMenuItem changeDescriptionMenuItem = new JMenuItem(GetText.tr("Change Description"));
    private final JMenuItem changeImageMenuItem = new JMenuItem(GetText.tr("Change Image"));
    private final JMenuItem deleteInstanceItem = new JMenuItem(GetText.tr("Delete"));

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

    private final boolean hasUpdate;

    public InstanceCard(Instance instance, boolean hasUpdate, String instanceTitleFormat) {
        super(instance, instanceTitleFormat);

        this.instance = instance;
        this.image = new ImagePanel(() -> instance.getImage().getImage());
        this.hasUpdate = hasUpdate;

        setupDescription();
        setupPlayPopupMenus();
        setupSettingsPopupMenu();
        setupModingPopupMenu();
        setupEditInstanceMenu();
        setupMorePopupMenu();

        // button grid setup block
        JPanel buttonGrid = new JPanel(new GridLayout(0, 2, 8, 6));
        buttonGrid.setBorder(new EmptyBorder(2, 10, 2, 10));
        buttonGrid.add(playButton);
        buttonGrid.add(modingButton);
        buttonGrid.add(settingsButton);
        buttonGrid.add(editInstanceButton);
        // button grid setup end block

        // card main forming
        JScrollPane desc = new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        desc.setPreferredSize(new Dimension(getPreferredSize().width, 32));

        JPanel upper = new JPanel();
        upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));

        JSplitPane headerSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainTitile, moreButton);
        headerSplitter.setResizeWeight(.85);
        upper.add(headerSplitter);
        upper.add(Box.createHorizontalGlue());
        upper.add(desc);

        JSplitPane subSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, buttonGrid);
        subSplitter.setEnabled(false);

        JSplitPane mainSpitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, image, subSplitter);
        mainSpitter.setEnabled(false);

        add(mainSpitter);

        RelocalizationManager.addListener(this);

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

    private void setupSettingsPopupMenu() {
        // export block
        exportItem.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_export", instance));
            new InstanceExportDialog(instance);
        });
        settingsPopupMenu.add(exportItem);
        exportItem.setEnabled(instance.canBeExported());
        // export end block

        settingsPopupMenu.addSeparator();
        // backup block
        normalBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.NORMAL));
        settingsPopupMenu.add(normalBackupMenuItem);

        normalPlusModsBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.NORMAL_PLUS_MODS));
        settingsPopupMenu.add(normalPlusModsBackupMenuItem);

        fullBackupMenuItem.addActionListener(e -> instance.backup(BackupMode.FULL));
        settingsPopupMenu.add(fullBackupMenuItem);
        // backup end block

    }

    private void setupModingPopupMenu() {
        // add mods block
        addModsItem.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_add_mods", instance));
            new AddModsDialog(instance);
            exportItem.setEnabled(instance.canBeExported());
        });
        modingPopupMenu.add(addModsItem);
        addModsItem.setEnabled(false);
        if (instance.launcher.enableCurseForgeIntegration
                && (ConfigManager.getConfigItem("platforms.curseforge.modsEnabled", true) == true
                        || (ConfigManager.getConfigItem("platforms.modrinth.modsEnabled", true) == true
                                && this.instance.launcher.loaderVersion != null))) {
            addModsItem.setEnabled(true);
        }
        // add mods end block

        // edit mod block
        editModsItem.addActionListener(e -> {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_edit_mods", instance));
            new EditModsDialog(instance);
            exportItem.setEnabled(instance.canBeExported());
        });
        modingPopupMenu.add(editModsItem);
        editModsItem.setEnabled(false);
        if (instance.launcher.enableEditingMods) {
            editModsItem.setEnabled(true);
        }
        // edit mod end block
    }

    private void setupMorePopupMenu() {
        moreButton.setBackground(null);
        moreButton.setBorder(new EmptyBorder(1, 1, 1, 1));

        // open folder block
        openFolderItem.addActionListener(e -> OS.openFileExplorer(instance.getRoot()));
        morePopupMenu.add(openFolderItem);

        openResourceMenuItem.addActionListener(e -> {
            DialogManager.okDialog().setTitle(GetText.tr("Reminder"))
                    .setContent(GetText.tr("You may not distribute ANY resources."))
                    .setType(DialogManager.WARNING).show();
            OS.openFileExplorer(instance.getMinecraftJarLibraryPath());
        });
        morePopupMenu.add(openResourceMenuItem);
        // open folder end block

        morePopupMenu.addSeparator();

        // update block
        updateItem.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot update pack as you have no account selected."))
                        .setType(DialogManager.ERROR).show();
                return;
            }

            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_update", instance));
            instance.update();
        });
        morePopupMenu.add(updateItem);
        if (!instance.isUpdatable()) {
            updateItem.setEnabled(instance.isUpdatable());
        }
        if (!hasUpdate) {
            updateItem.setEnabled(false);
        }
        // update end block

        // server item block
        serversItem.addActionListener(e -> OS.openWebBrowser(
                String.format("%s/%s?utm_source=launcher&utm_medium=button&utm_campaign=instance_v2_button",
                        Constants.SERVERS_LIST_PACK, instance.getSafePackName())));
        morePopupMenu.add(serversItem);
        if (instance.isExternalPack() || instance.launcher.vanillaInstance) {
            serversItem.setEnabled(false);
        }

        if (instance.getPack() != null && instance.getPack().system) {
            serversItem.setEnabled(false);
        }
        // server end block

        // website block
        websiteItem.addActionListener(e -> OS.openWebBrowser(instance.getWebsiteUrl()));
        morePopupMenu.add(websiteItem);
        websiteItem.setEnabled(instance.hasWebsite());
        // website end block

        // extra buttons in more popup menu
        if (instance.showGetHelpButton()) {
            morePopupMenu.addSeparator();
            if (instance.getDiscordInviteUrl() != null) {
                discordLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getDiscordInviteUrl()));
                morePopupMenu.add(discordLinkMenuItem);
            }

            if (instance.getSupportUrl() != null) {
                supportLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getSupportUrl()));
                morePopupMenu.add(supportLinkMenuItem);
            }

            if (instance.getWebsiteUrl() != null) {
                websiteLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getWebsiteUrl()));
                morePopupMenu.add(websiteLinkMenuItem);
            }

            if (instance.getWikiUrl() != null) {
                wikiLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getWikiUrl()));
                morePopupMenu.add(wikiLinkMenuItem);
            }

            if (instance.getSourceUrl() != null) {
                sourceLinkMenuItem.addActionListener(e -> OS.openWebBrowser(instance.getSourceUrl()));
                morePopupMenu.add(sourceLinkMenuItem);
            }
        }

    }

    void addLoaderItem(String key, JMenuItem add, JMenuItem change, JMenuItem remove, boolean enabled) {
        if (ConfigManager.getConfigItem("loaders." + key + "enabled", true) == enabled
                && !ConfigManager
                        .getConfigItem("loaders." + key + ".disabledMinecraftVersions", new ArrayList<String>())
                        .contains(instance.id)) {
            editInstancePopupMenu.add(add);
            editInstancePopupMenu.add(change);
        }
        editInstancePopupMenu.add(remove);
    }

    private void setupEditInstanceMenu() {

        reinstallMenuItem.addActionListener(e -> instance.startReinstall());
        editInstancePopupMenu.add(reinstallMenuItem);

        cloneMenuItem.addActionListener(e -> instance.startClone());
        editInstancePopupMenu.add(cloneMenuItem);

        renameMenuItem.addActionListener(e -> instance.startRename());
        editInstancePopupMenu.add(renameMenuItem);

        changeDescriptionMenuItem.addActionListener(e -> {
            instance.startChangeDescription();
            descArea.setText(instance.launcher.description);
        });

        editInstancePopupMenu.add(changeDescriptionMenuItem);
        changeImageMenuItem.addActionListener(e -> {
            instance.startChangeImage();
            image.setImage(instance.getImage().getImage());
        });
        editInstancePopupMenu.add(changeImageMenuItem);

        deleteInstanceItem.addActionListener(e -> {
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
        editInstancePopupMenu.add(deleteInstanceItem);

        editInstancePopupMenu.addSeparator();

        addLoaderItem("fabric", addFabricMenuItem, changeFabricVersionMenuItem, removeFabricMenuItem, true);
        addLoaderItem("forge", addForgeMenuItem, changeForgeVersionMenuItem, removeForgeMenuItem, true);
        addLoaderItem("legacyfabric", addLegacyFabricMenuItem, changeLegacyFabricVersionMenuItem,
                removeLegacyFabricMenuItem, true);
        addLoaderItem("neoforge", addNeoForgeMenuItem, changeNeoForgeVersionMenuItem, removeNeoForgeMenuItem, true);
        addLoaderItem("quilt", addQuiltMenuItem, changeQuiltVersionMenuItem, removeQuiltMenuItem, false);

        // loader things
        addFabricMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.FABRIC);
            setEditInstanceMenuItemVisibility();
        });
        addForgeMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.FORGE);
            setEditInstanceMenuItemVisibility();
        });
        addLegacyFabricMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.LEGACY_FABRIC);
            setEditInstanceMenuItemVisibility();
        });
        addNeoForgeMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.NEOFORGE);
            setEditInstanceMenuItemVisibility();
        });
        addQuiltMenuItem.addActionListener(e -> {
            instance.addLoader(LoaderType.QUILT);
            setEditInstanceMenuItemVisibility();
        });
        List.of(changeFabricVersionMenuItem,
                changeForgeVersionMenuItem,
                changeLegacyFabricVersionMenuItem,
                changeNeoForgeVersionMenuItem,
                changeQuiltVersionMenuItem)
                .forEach(item -> {
                    item.addActionListener(e -> {
                        instance.changeLoaderVersion();
                        setEditInstanceMenuItemVisibility();
                    });
                });
        List.of(removeFabricMenuItem,
                removeForgeMenuItem,
                removeLegacyFabricMenuItem,
                removeNeoForgeMenuItem,
                removeQuiltMenuItem)
                .forEach(item -> {
                    item.addActionListener(e -> {
                        instance.removeLoader();
                        setEditInstanceMenuItemVisibility();
                    });
                });
        setEditInstanceMenuItemVisibility();
    }

    private void setEditInstanceMenuItemVisibility() {
        reinstallMenuItem.setVisible(instance.isUpdatable());

        final boolean loaderVersionIsNull = instance.launcher.loaderVersion == null;
        final LoaderVersion loaderVersion = instance.launcher.loaderVersion;

        addFabricMenuItem.setVisible(loaderVersionIsNull);
        addForgeMenuItem.setVisible(loaderVersionIsNull);
        addLegacyFabricMenuItem.setVisible(loaderVersionIsNull);
        addNeoForgeMenuItem.setVisible(loaderVersionIsNull);
        addQuiltMenuItem.setVisible(loaderVersionIsNull);

        changeFabricVersionMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isFabric());
        changeForgeVersionMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isForge());
        changeLegacyFabricVersionMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isLegacyFabric());
        changeNeoForgeVersionMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isNeoForge());
        changeQuiltVersionMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isQuilt());

        removeFabricMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isFabric());
        removeForgeMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isForge());
        removeLegacyFabricMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isLegacyFabric());
        removeNeoForgeMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isNeoForge());
        removeQuiltMenuItem.setVisible(!loaderVersionIsNull && loaderVersion.isQuilt());
    }

    private void setupDescription() {
        descArea.setText(instance.getPackDescription());
        descArea.setEditable(false);
        descArea.setHighlighter(null);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setForeground(getBackground().brighter().brighter().brighter().brighter());
        if (instance.canChangeDescription()) {
            descArea.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        instance.startChangeDescription();
                        descArea.setText(instance.getPackDescription());
                    }
                }
            });
        }
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

        if (hasUpdate && !instance.hasLatestUpdateBeenIgnored()) {
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

    public Instance getInstance() {
        return instance;
    }

    @Override
    public void onRelocalization() {
        this.playButton.setText(GetText.tr("Play"));
        this.updateItem.setText(GetText.tr("Update"));
        this.deleteInstanceItem.setText(GetText.tr("Delete"));
        this.serversItem.setText(GetText.tr("Servers"));
        this.websiteItem.setText(GetText.tr("Open Website"));
        this.openFolderItem.setText(GetText.tr("Open Folder"));
        this.openResourceMenuItem.setText(GetText.tr("Open Resources"));
        this.settingsButton.setText(GetText.tr("Settings"));

        this.normalBackupMenuItem.setText(GetText.tr("Normal Backup"));
        this.normalPlusModsBackupMenuItem.setText(GetText.tr("Normal + Mods Backup"));
        this.fullBackupMenuItem.setText(GetText.tr("Full Backup"));

        this.modingButton.setText(GetText.tr("Moding"));
        this.addModsItem.setText(GetText.tr("Add Mods"));
        this.editModsItem.setText(GetText.tr("Edit Mods"));

        this.discordLinkMenuItem.setText(GetText.tr("Discord"));
        this.supportLinkMenuItem.setText(GetText.tr("Support"));
        this.websiteLinkMenuItem.setText(GetText.tr("Website"));
        this.wikiLinkMenuItem.setText(GetText.tr("Wiki"));
        this.sourceLinkMenuItem.setText(GetText.tr("Source"));
    }
}
