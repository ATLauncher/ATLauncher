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
package com.atlauncher.gui.dialogs.editinstancedialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

public class InformationSection extends SectionPanel {
    private final SimpleDateFormat formatter = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

    private final EditInstanceDialog editInstanceDialog;

    private final SideBarButton update = new SideBarButton();
    private final SideBarButton reinstall = new SideBarButton();
    private final SideBarButton changeLoaderVersionButton = new SideBarButton();
    private final SideBarButton removeInstallFabricButton = new SideBarButton();
    private final SideBarButton removeInstallForgeButton = new SideBarButton();
    private final SideBarButton removeInstallLegacyFabricButton = new SideBarButton();
    private final SideBarButton removeInstallQuiltButton = new SideBarButton();
    private final JLabel version = new JLabel();
    private final JLabel minecraftVersion = new JLabel();
    private final JLabel loader = new JLabel();
    private final JPanel mainPanel = new JPanel();
    private final JLabel loaderVersionLabel = new JLabel();
    private final JLabel loaderVersion = new JLabel();
    private final JLabel lastUpdated = new JLabel();

    private JLabel minecraftVersionLabel = new JLabel(GetText.tr("Minecraft Version") + ": ");

    private JLabel packLabel = new JLabel(GetText.tr("Pack") + ": ");

    private JLabel pack = new JLabel(instance.launcher.pack);

    private JLabel platformLabel = new JLabel(GetText.tr("Platform") + ": ");

    private JLabel platform = new JLabel(instance.getPlatformName());

    private JLabel versionLabel = new JLabel(GetText.tr("Version") + ": ");

    private JLabel created = new JLabel();

    private JLabel lastPlayed = new JLabel();

    private JLabel totalTimePlayed = new JLabel();

    private JLabel numberOfLaunches = new JLabel();

    public InformationSection(EditInstanceDialog editInstanceDialog, Instance instance) {
        super(editInstanceDialog, instance);

        this.editInstanceDialog = editInstanceDialog;

        setupComponents();
        updateUIState();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();

        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JLabel instanceNameLabel = new JLabel(instance.launcher.name);
        instanceNameLabel.setFont(App.THEME.getNormalFont().deriveFont(20f));
        topPanel.add(instanceNameLabel);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        splitPane.setEnabled(false);

        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        // Minecraft Version
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        minecraftVersionLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(minecraftVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(minecraftVersion, gbc);

        // Space
        gbc.gridy++;

        // Pack
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        packLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(packLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(pack, gbc);

        // Platform
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        platformLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(platformLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(platform, gbc);

        // Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        versionLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(version, gbc);

        // Space
        gbc.gridy++;

        // Loader
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel loaderLabel = new JLabel(GetText.tr("Loader") + ": ");
        loaderLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(loaderLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(loader, gbc);

        // Loader Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        loaderVersionLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(loaderVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(loaderVersion, gbc);

        // Space
        gbc.gridy++;

        // Number Of Launches
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel numberOfLaunchesLabel = new JLabel(GetText.tr("Number Of Launches") + ": ");
        numberOfLaunchesLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(numberOfLaunchesLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(numberOfLaunches, gbc);

        // Total Time Played
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel totalTimePlayedLabel = new JLabel(GetText.tr("Total Time Played") + ": ");
        totalTimePlayedLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(totalTimePlayedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(totalTimePlayed, gbc);

        // Last Played
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel lastPlayedLabel = new JLabel(GetText.tr("Last Played") + ": ");
        lastPlayedLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(lastPlayedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(lastPlayed, gbc);

        // Created
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel createdLabel = new JLabel(GetText.tr("Created") + ": ");
        createdLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(createdLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        mainPanel.add(created, gbc);

        // Last Updated
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel lastUpdatedLabel = new JLabel(GetText.tr("Last Updated") + ": ");
        lastUpdatedLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(lastUpdatedLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gbc.weighty = 1;
        gbc.weightx = 1;
        mainPanel.add(lastUpdated, gbc);

        splitPane.setLeftComponent(mainPanel);

        JToolBar sideBar = new JToolBar();
        sideBar.setMinimumSize(new Dimension(160, 0));
        sideBar.setPreferredSize(new Dimension(160, 0));
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.setFloatable(false);

        sideBar.addSeparator();

        update.setText(GetText.tr("Update"));
        update.addActionListener(e -> {
            instance.update(parent);

            updateUIState();
        });
        sideBar.add(update);

        reinstall.setText(
                instance.isVanillaInstance() ? GetText.tr("Change Minecraft Version") : GetText.tr("Reinstall"));
        reinstall.addActionListener(e -> {
            // TODO: Java pass by value makes this shit and not work, fix it
            String mcVersion = instance.id;
            instance.startReinstall(parent);

            updateUIState();

            if (instance.isVanillaInstance() && !mcVersion.equals(instance.id)) {
                int ret = DialogManager.yesNoDialog().setType(DialogManager.WARNING)
                        .setTitle(GetText.tr("Minecraft Version Changed"))
                        .setContent(new HTMLBuilder().center().split(100).text(GetText.tr(
                                "You've changed the Minecraft version of this instance and your mods will likely no longer work.<br/><br/>Do you want to reinstall all mods now to make sure you have the correct versions?")))
                        .show();

                if (ret == 0) {
                    editInstanceDialog.tabbedPane.setSelectedIndex(1);

                    ModsSection modsSection = (ModsSection) editInstanceDialog.tabbedPane.getComponentAt(1);
                    modsSection.reinstallAllMods();
                }
            }
        });
        sideBar.add(reinstall);

        changeLoaderVersionButton.addActionListener(e -> {
            instance.changeLoaderVersion(parent);

            updateUIState();
        });
        sideBar.add(changeLoaderVersionButton);
        sideBar.addSeparator();

        removeInstallFabricButton.addActionListener(e -> {
            if (instance.launcher.loaderVersion == null) {
                instance.addLoader(parent, LoaderType.FABRIC);
            } else {
                instance.removeLoader(parent);
            }

            updateUIState();
        });
        sideBar.add(removeInstallFabricButton);

        removeInstallForgeButton.addActionListener(e -> {
            if (instance.launcher.loaderVersion == null) {
                instance.addLoader(parent, LoaderType.FORGE);
            } else {
                instance.removeLoader(parent);
            }

            updateUIState();
        });
        sideBar.add(removeInstallForgeButton);

        removeInstallLegacyFabricButton.addActionListener(e -> {
            if (instance.launcher.loaderVersion == null) {
                instance.addLoader(parent, LoaderType.LEGACY_FABRIC);
            } else {
                instance.removeLoader(parent);
            }

            updateUIState();
        });
        sideBar.add(removeInstallLegacyFabricButton);

        removeInstallQuiltButton.addActionListener(e -> {
            if (instance.launcher.loaderVersion == null) {
                instance.addLoader(parent, LoaderType.QUILT);
            } else {
                instance.removeLoader(parent);
            }

            updateUIState();
        });
        sideBar.add(removeInstallQuiltButton);
        sideBar.addSeparator();

        SideBarButton manageModsButton = new SideBarButton(GetText.tr("Manage Mods"));
        manageModsButton.addActionListener(e -> {
            editInstanceDialog.tabbedPane.setSelectedIndex(1);
        });
        sideBar.add(manageModsButton);

        SideBarButton manageResourcePacksButton = new SideBarButton(GetText.tr("Manage Resource Packs"));
        manageResourcePacksButton.addActionListener(e -> {
            editInstanceDialog.tabbedPane.setSelectedIndex(2);
        });
        sideBar.add(manageResourcePacksButton);

        SideBarButton manageShaderPacksButton = new SideBarButton(GetText.tr("Manage Shader Packs"));
        manageShaderPacksButton.addActionListener(e -> {
            editInstanceDialog.tabbedPane.setSelectedIndex(3);
        });
        sideBar.add(manageShaderPacksButton);
        sideBar.addSeparator();
        sideBar.add(Box.createVerticalGlue());
        sideBar.addSeparator();

        SideBarButton openFolderButton = new SideBarButton(GetText.tr("Open Folder"));
        openFolderButton.addActionListener(e -> {
            OS.openFileExplorer(instance.ROOT);
        });
        sideBar.add(openFolderButton);

        splitPane.setRightComponent(sideBar);
        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void updateUIState() {
        numberOfLaunches.setText(instance.launcher.numPlays + "");
        totalTimePlayed.setText(convertDurationToHumanString(instance.launcher.totalPlayTime));
        lastPlayed.setText(instance.launcher.lastPlayed == Instant.EPOCH ? GetText.tr("Never")
                : getTimeAgo(instance.launcher.lastPlayed));
        if (instance.launcher.lastPlayed != Instant.EPOCH) {
            lastPlayed.setToolTipText(formatter.format(new Date(instance.launcher.lastPlayed.toEpochMilli())));
        }
        created.setText(instance.launcher.createdAt == Instant.EPOCH ? GetText.tr("Unknown")
                : getTimeAgo(instance.launcher.createdAt));
        if (instance.launcher.createdAt != Instant.EPOCH) {
            created.setToolTipText(formatter.format(new Date(instance.launcher.createdAt.toEpochMilli())));
        }

        packLabel.setVisible(!instance.isVanillaInstance());
        pack.setVisible(!instance.isVanillaInstance());
        platformLabel.setVisible(!instance.isVanillaInstance());
        platform.setVisible(!instance.isVanillaInstance());
        versionLabel.setVisible(!instance.isVanillaInstance());
        version.setVisible(!instance.isVanillaInstance());
        update.setVisible(instance.isUpdatable() && instance.hasUpdate());
        reinstall.setVisible(instance.isUpdatable());

        if (instance.isVanillaInstance()) {
            ((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0, 25, 0, 0, 25, 0, 0, 0, 0,
                    0, 0 };
        } else if (instance.launcher.loaderVersion != null) {
            ((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 25, 0, 0, 0, 25, 0, 0, 25, 0, 0, 0, 0,
                    0, 0 };
        } else {
            ((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 25, 0, 0, 0, 25, 0, 25, 0, 0, 0, 0, 0,
                    0 };
        }

        minecraftVersion.setText(instance.id);
        version.setText(instance.launcher.version);
        loader.setText(
                instance.launcher.loaderVersion == null ? GetText.tr("None") : instance.launcher.loaderVersion.type);

        loaderVersionLabel.setVisible(instance.launcher.loaderVersion != null);
        loaderVersionLabel.setText(GetText.tr("{0} Version",
                instance.launcher.loaderVersion != null ? instance.launcher.loaderVersion.getLoaderType()
                        : GetText.tr("Loader"))
                + ": ");
        loaderVersion.setVisible(instance.launcher.loaderVersion != null);
        loaderVersion.setText(
                instance.launcher.loaderVersion != null ? instance.launcher.loaderVersion.version : GetText.tr("None"));

        changeLoaderVersionButton.setVisible(instance.launcher.loaderVersion != null);
        if (instance.launcher.loaderVersion != null) {
            changeLoaderVersionButton
                    .setText(GetText.tr("Change {0} Version", instance.launcher.loaderVersion.getLoaderType()));
        }

        lastUpdated.setText(instance.launcher.updatedAt == Instant.EPOCH ? GetText.tr("Never")
                : getTimeAgo(instance.launcher.updatedAt));
        if (instance.launcher.updatedAt != Instant.EPOCH) {
            lastUpdated.setToolTipText(formatter.format(new Date(instance.launcher.updatedAt.toEpochMilli())));
        } else {
            lastUpdated.setToolTipText(null);
        }

        removeInstallForgeButton
                .setText(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isForge()
                        ? GetText.tr("Remove {0}", "Forge")
                        : GetText.tr("Install {0}", "Forge"));
        removeInstallForgeButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isForge());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isForge()) {
            removeInstallForgeButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Forge",
                    instance.launcher.loaderVersion.getLoaderType()));
        } else {
            removeInstallForgeButton.setToolTipText(null);
        }

        removeInstallFabricButton
                .setText(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isFabric()
                        ? GetText.tr("Remove {0}", "Fabric")
                        : GetText.tr("Install {0}", "Fabric"));
        removeInstallFabricButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isFabric());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isFabric()) {
            removeInstallFabricButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Fabric",
                    instance.launcher.loaderVersion.getLoaderType()));
        } else {
            removeInstallFabricButton.setToolTipText(null);
        }

        removeInstallQuiltButton
                .setText(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isQuilt()
                        ? GetText.tr("Remove {0}", "Quilt")
                        : GetText.tr("Install {0}", "Quilt"));
        removeInstallQuiltButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isQuilt());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isQuilt()) {
            removeInstallQuiltButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Quilt",
                    instance.launcher.loaderVersion.getLoaderType()));
        } else {
            removeInstallQuiltButton.setToolTipText(null);
        }

        removeInstallLegacyFabricButton
                .setText(instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isLegacyFabric()
                        ? GetText.tr("Remove {0}", "Legacy Fabric")
                        : GetText.tr("Install {0}", "Legacy Fabric"));
        removeInstallLegacyFabricButton.setEnabled(
                instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isLegacyFabric());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isLegacyFabric()) {
            removeInstallLegacyFabricButton.setToolTipText(
                    GetText.tr("You can only install 1 mod loader at a time. To install {0}, please remove {1}",
                            "Legacy Fabric", instance.launcher.loaderVersion.getLoaderType()));
        } else {
            removeInstallLegacyFabricButton.setToolTipText(null);
        }

        update.setEnabled(!isLaunchingOrLaunched);
        reinstall.setEnabled(!isLaunchingOrLaunched);
        changeLoaderVersionButton.setEnabled(!isLaunchingOrLaunched);

        update.setToolTipText(
                !isLaunchingOrLaunched ? null : GetText.tr("This cannot be done while Minecraft is running"));
        reinstall.setToolTipText(
                !isLaunchingOrLaunched ? null : GetText.tr("This cannot be done while Minecraft is running"));
        changeLoaderVersionButton.setToolTipText(
                !isLaunchingOrLaunched ? null : GetText.tr("This cannot be done while Minecraft is running"));

        if (isLaunchingOrLaunched) {
            update.setEnabled(false);
            reinstall.setEnabled(false);
            changeLoaderVersionButton.setEnabled(false);
            removeInstallFabricButton.setEnabled(false);
            removeInstallForgeButton.setEnabled(false);
            removeInstallQuiltButton.setEnabled(false);
            removeInstallLegacyFabricButton.setEnabled(false);

            removeInstallFabricButton.setToolTipText(GetText.tr("This cannot be done while Minecraft is running"));
            removeInstallForgeButton.setToolTipText(GetText.tr("This cannot be done while Minecraft is running"));
            removeInstallQuiltButton.setToolTipText(GetText.tr("This cannot be done while Minecraft is running"));
            removeInstallLegacyFabricButton
                    .setToolTipText(GetText.tr("This cannot be done while Minecraft is running"));
        }
    }

    public String convertDurationToHumanString(Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds == 0) {
            return GetText.tr("None");
        }

        long years = seconds / 31536000;
        seconds -= years * 31536000;

        long months = seconds / 2592000;
        seconds -= months * 2592000;

        long weeks = seconds / 604800;
        seconds -= weeks * 604800;

        long days = seconds / 86400;
        seconds -= days * 86400;

        long hours = seconds / 3600;
        seconds -= hours * 3600;

        long minutes = seconds / 60;
        seconds -= minutes * 60;

        return toHumanString((int) years, (int) months, (int) weeks, (int) days, (int) hours, (int) minutes,
                (int) seconds, false);
    }

    public String getTimeAgo(Instant instant) {
        Period period = new Period(new DateTime(instant.toEpochMilli()), DateTime.now());

        if (instant.compareTo(Instant.now()) == 0) {
            return GetText.tr("Now");
        }

        int years = period.getYears();
        int months = period.getMonths();
        int weeks = period.getWeeks();
        int days = period.getDays();
        int hours = period.getHours();
        int minutes = period.getMinutes();
        int seconds = period.getSeconds();

        return toHumanString(years, months, weeks, days, hours, minutes, seconds, true);
    }

    private String toHumanString(int years, int months, int weeks, int days, int hours, int minutes,
            int seconds, boolean showAgo) {
        StringBuilder sb = new StringBuilder("");
        if (years > 1) {
            sb.append(GetText.tr("{0} years,", years) + " ");
        } else if (years == 1) {
            sb.append(GetText.tr("1 year,") + " ");
        }

        if (months > 1) {
            sb.append(GetText.tr("{0} months,", months) + " ");
        } else if (months == 1) {
            sb.append(GetText.tr("1 month,") + " ");
        }

        if (weeks > 1) {
            sb.append(GetText.tr("{0} weeks,", weeks) + " ");
        } else if (weeks == 1) {
            sb.append(GetText.tr("1 week,") + " ");
        }

        if (days > 1) {
            sb.append(GetText.tr("{0} days,", days) + " ");
        } else if (days == 1) {
            sb.append(GetText.tr("1 day,") + " ");
        }

        if (hours > 1) {
            sb.append(GetText.tr("{0} hours,", hours) + " ");
        } else if (hours == 1) {
            sb.append(GetText.tr("1 hour,") + " ");
        }

        if (sb.length() == 0) {
            if (minutes > 1) {
                sb.append(GetText.tr("{0} minutes,", minutes) + " ");
            } else if (minutes == 1) {
                sb.append(GetText.tr("1 minute,") + " ");
            }

            if (seconds > 1) {
                sb.append(GetText.tr("{0} seconds", seconds) + " ");
            } else if (seconds == 1) {
                sb.append(GetText.tr("{0} second", seconds) + " ");
            }
        }

        if (sb.charAt(sb.length() - 2) == ',') {
            sb.deleteCharAt(sb.length() - 2);
        }

        if (showAgo) {
            sb.append(GetText.tr("ago"));
        }

        return sb.toString();
    }
}
