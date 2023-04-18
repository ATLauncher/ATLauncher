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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.Instance;

public class InformationSection extends SectionPanel {
    public InformationSection(Window parent, Instance instance) {
        super(parent, instance);

        setupComponents();
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();

        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JLabel instanceNameLabel = new JLabel(instance.launcher.name);
        instanceNameLabel.setFont(App.THEME.getNormalFont().deriveFont(20f));
        topPanel.add(instanceNameLabel);

        add(topPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(1.0);
        splitPane.setEnabled(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        if (instance.launcher.loaderVersion != null) {
            ((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 25, 0, 0, 0, 25, 0, 0, 25, 0, 0, 0, 0,
                    0, 0 };
        } else {
            ((GridBagLayout) mainPanel.getLayout()).rowHeights = new int[] { 0, 25, 0, 0, 0, 25, 0, 25, 0, 0, 0, 0, 0,
                    0 };
        }

        // Minecraft Version
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel minecraftVersionLabel = new JLabel(GetText.tr("Minecraft Version") + ": ");
        minecraftVersionLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(minecraftVersionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel minecraftVersion = new JLabel(instance.id);
        mainPanel.add(minecraftVersion, gbc);

        // Space
        gbc.gridy++;

        // Pack
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel packLabel = new JLabel(GetText.tr("Pack") + ": ");
        packLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(packLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel pack = new JLabel(instance.launcher.pack);
        mainPanel.add(pack, gbc);

        // Platform
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel platformLabel = new JLabel(GetText.tr("Platform") + ": ");
        platformLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(platformLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel platform = new JLabel(instance.getPlatformName());
        mainPanel.add(platform, gbc);

        // Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel versionLabel = new JLabel(GetText.tr("Version") + ": ");
        versionLabel.setFont(App.THEME.getBoldFont());
        mainPanel.add(versionLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel version = new JLabel(instance.launcher.version);
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
        JLabel loader = new JLabel(
                instance.launcher.loaderVersion == null ? GetText.tr("None") : instance.launcher.loaderVersion.type);
        mainPanel.add(loader, gbc);

        if (instance.launcher.loaderVersion != null) {
            // Loader Version
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = UIConstants.LABEL_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            JLabel loaderVersionLabel = new JLabel(
                    GetText.tr("{0} Version", instance.launcher.loaderVersion.getLoaderType()) + ": ");
            loaderVersionLabel.setFont(App.THEME.getBoldFont());
            mainPanel.add(loaderVersionLabel, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.FIELD_INSETS;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            JLabel loaderVersion = new JLabel(instance.launcher.loaderVersion.version);
            mainPanel.add(loaderVersion, gbc);
        }

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
        JLabel numberOfLaunches = new JLabel(instance.launcher.numPlays + "");
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
        JLabel totalTimePlayed = new JLabel(convertDurationToHumanString(instance.launcher.totalPlayTime));
        mainPanel.add(totalTimePlayed, gbc);

        SimpleDateFormat formatter = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

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
        JLabel lastPlayed = new JLabel(instance.launcher.lastPlayed == Instant.EPOCH ? GetText.tr("Never")
                : getTimeAgo(instance.launcher.lastPlayed));
        if (instance.launcher.lastPlayed != Instant.EPOCH) {
            lastPlayed.setToolTipText(formatter.format(new Date(instance.launcher.lastPlayed.toEpochMilli())));
        }
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
        JLabel created = new JLabel(instance.launcher.createdAt == Instant.EPOCH ? GetText.tr("Unknown")
                : getTimeAgo(instance.launcher.createdAt));
        if (instance.launcher.createdAt != Instant.EPOCH) {
            created.setToolTipText(formatter.format(new Date(instance.launcher.createdAt.toEpochMilli())));
        }
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
        JLabel lastUpdated = new JLabel(instance.launcher.updatedAt == Instant.EPOCH ? GetText.tr("Never")
                : getTimeAgo(instance.launcher.updatedAt));
        if (instance.launcher.updatedAt != Instant.EPOCH) {
            lastUpdated.setToolTipText(formatter.format(new Date(instance.launcher.updatedAt.toEpochMilli())));
        }
        mainPanel.add(lastUpdated, gbc);

        splitPane.setLeftComponent(mainPanel);

        JToolBar sideBar = new JToolBar();
        sideBar.setOrientation(SwingConstants.VERTICAL);
        sideBar.addSeparator();

        JButton changeMinecraftVersionButton = new JButton(GetText.tr("Change Minecraft Version"));
        sideBar.add(changeMinecraftVersionButton);

        JButton changePackVersionButton = new JButton(GetText.tr("Change Pack Version"));
        changePackVersionButton.setVisible(instance.isUpdatable());
        sideBar.add(changePackVersionButton);

        JButton changeLoaderVersionButton = new JButton();
        changeLoaderVersionButton.setVisible(instance.launcher.loaderVersion != null);
        if (instance.launcher.loaderVersion != null) {
            changeLoaderVersionButton
                    .setText(GetText.tr("Change {0} Version", instance.launcher.loaderVersion.getLoaderType()));
        }
        sideBar.add(changeLoaderVersionButton);
        sideBar.addSeparator();

        JButton removeInstallFabricButton = new JButton(
                instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isFabric()
                        ? GetText.tr("Remove {0}", "Fabric")
                        : GetText.tr("Install {0}", "Fabric"));
        removeInstallFabricButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isFabric());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isFabric()) {
            removeInstallFabricButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Fabric",
                    instance.launcher.loaderVersion.getLoaderType()));
        }
        sideBar.add(removeInstallFabricButton);

        JButton removeInstallForgeButton = new JButton(
                instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isForge()
                        ? GetText.tr("Remove {0}", "Forge")
                        : GetText.tr("Install {0}", "Forge"));
        removeInstallForgeButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isForge());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isForge()) {
            removeInstallForgeButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Forge",
                    instance.launcher.loaderVersion.getLoaderType()));
        }
        sideBar.add(removeInstallForgeButton);

        JButton removeInstallLegacyFabricButton = new JButton(
                instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isLegacyFabric()
                        ? GetText.tr("Remove {0}", "Legacy Fabric")
                        : GetText.tr("Install {0}", "Legacy Fabric"));
        removeInstallLegacyFabricButton.setEnabled(
                instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isLegacyFabric());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isLegacyFabric()) {
            removeInstallLegacyFabricButton.setToolTipText(
                    GetText.tr("You can only install 1 mod loader at a time. To install {0}, please remove {1}",
                            "Legacy Fabric", instance.launcher.loaderVersion.getLoaderType()));
        }
        sideBar.add(removeInstallLegacyFabricButton);

        JButton removeInstallQuiltButton = new JButton(
                instance.launcher.loaderVersion != null && instance.launcher.loaderVersion.isQuilt()
                        ? GetText.tr("Remove {0}", "Quilt")
                        : GetText.tr("Install {0}", "Quilt"));
        removeInstallQuiltButton
                .setEnabled(instance.launcher.loaderVersion == null || instance.launcher.loaderVersion.isQuilt());
        if (instance.launcher.loaderVersion != null && !instance.launcher.loaderVersion.isQuilt()) {
            removeInstallQuiltButton.setToolTipText(GetText.tr(
                    "You can only install 1 mod loader at a time. To install {0}, please remove {1}", "Quilt",
                    instance.launcher.loaderVersion.getLoaderType()));
        }
        sideBar.add(removeInstallQuiltButton);
        sideBar.addSeparator();

        JButton manageModsButton = new JButton(GetText.tr("Manage Mods"));
        sideBar.add(manageModsButton);

        JButton manageResourcePacksButton = new JButton(GetText.tr("Manage Resource Packs"));
        sideBar.add(manageResourcePacksButton);

        JButton manageShaderPacksButton = new JButton(GetText.tr("Manage Shader Packs"));
        sideBar.add(manageShaderPacksButton);
        sideBar.addSeparator();
        sideBar.add(Box.createVerticalGlue());
        sideBar.addSeparator();

        JButton openFolderButton = new JButton(GetText.tr("Open Folder"));
        sideBar.add(openFolderButton);

        splitPane.setRightComponent(sideBar);
        add(splitPane, BorderLayout.CENTER);
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

        if (sb.charAt(sb.length() - 2) == ',') {
            sb.deleteCharAt(sb.length() - 2);
        }

        if (showAgo) {
            sb.append(GetText.tr("ago"));
        }

        return sb.toString();
    }
}
