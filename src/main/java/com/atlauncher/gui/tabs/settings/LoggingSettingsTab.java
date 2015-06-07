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
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class LoggingSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover forgeLoggingLevelLabel;
    private JComboBox<String> forgeLoggingLevel;

    private JLabelWithHover daysOfLogsToKeepLabel;
    private SpinnerModel daysOfLogsToKeepModel;
    private JSpinner daysOfLogsToKeep;

    private JLabelWithHover enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JLabelWithHover enableLoggingLabel;
    private JCheckBox enableLogs;

    private JLabelWithHover enableOpenEyeReportingLabel;
    private JCheckBox enableOpenEyeReporting;

    public LoggingSettingsTab() {
        EventHandler.EVENT_BUS.subscribe(this);
        // Forge Logging Level
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        forgeLoggingLevelLabel = new JLabelWithHover(LanguageManager.localize("settings.forgelogginglevel") + ":",
                HELP_ICON, "<html>" + LanguageManager.localizeWithReplace("settings.forgelogginglevelhelp",
                "<br/><br/>") + "</html>");
        add(forgeLoggingLevelLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        forgeLoggingLevel = new JComboBox<String>();
        forgeLoggingLevel.addItem("SEVERE");
        forgeLoggingLevel.addItem("WARNING");
        forgeLoggingLevel.addItem("INFO");
        forgeLoggingLevel.addItem("CONFIG");
        forgeLoggingLevel.addItem("FINE");
        forgeLoggingLevel.addItem("FINER");
        forgeLoggingLevel.addItem("FINEST");
        forgeLoggingLevel.setSelectedItem(SettingsManager.getForgeLoggingLevel());
        add(forgeLoggingLevel, gbc);

        // Days of logs to keep

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        daysOfLogsToKeepLabel = new JLabelWithHover(LanguageManager.localize("settings.daysoflogstokeep") + ":",
                HELP_ICON, LanguageManager.localize("settings.daysoflogstokeephelp"));
        add(daysOfLogsToKeepLabel, gbc);

        daysOfLogsToKeepModel = new SpinnerNumberModel(SettingsManager.getDaysOfLogsToKeep(), 1, 30, 1);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        daysOfLogsToKeep = new JSpinner(daysOfLogsToKeepModel);
        daysOfLogsToKeep.setValue(SettingsManager.getDaysOfLogsToKeep());
        add(daysOfLogsToKeep, gbc);

        // Enable Leaderboards

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabelWithHover(LanguageManager.localize("settings.leaderboards") + "?",
                HELP_ICON, LanguageManager.localize("settings.leaderboardshelp"));
        add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        if (SettingsManager.enableLeaderboards()) {
            enableLeaderboards.setSelected(true);
        }
        if (!SettingsManager.enableLogs()) {
            enableLeaderboards.setEnabled(false);
        }
        add(enableLeaderboards, gbc);

        // Enable Logging

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLoggingLabel = new JLabelWithHover(LanguageManager.localize("settings.logging") + "?", HELP_ICON,
                "<html>" + LanguageManager.localizeWithReplace("settings.logginghelp", "<br/>" + "</html>"));
        add(enableLoggingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLogs = new JCheckBox();
        enableLogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!enableLogs.isSelected()) {
                    enableOpenEyeReporting.setSelected(false);
                    enableOpenEyeReporting.setEnabled(false);
                    enableLeaderboards.setSelected(false);
                    enableLeaderboards.setEnabled(false);
                } else {
                    enableOpenEyeReporting.setSelected(true);
                    enableOpenEyeReporting.setEnabled(true);
                    enableLeaderboards.setSelected(true);
                    enableLeaderboards.setEnabled(true);
                }
            }
        });
        if (SettingsManager.enableLogs()) {
            enableLogs.setSelected(true);
        }
        add(enableLogs, gbc);

        // Enable OpenEye Reporting

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableOpenEyeReportingLabel = new JLabelWithHover(LanguageManager.localize("settings.openeye") + "?",
                HELP_ICON, "<html>" + Utils.splitMultilinedString(LanguageManager.localize("settings" + "" +
                ".openeyehelp"), 80, "<br/>") + "</html>");
        add(enableOpenEyeReportingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableOpenEyeReporting = new JCheckBox();
        if (!SettingsManager.enableLogs()) {
            enableOpenEyeReporting.setEnabled(false);
        }
        if (SettingsManager.enableOpenEyeReporting()) {
            enableOpenEyeReporting.setSelected(true);
        }
        add(enableOpenEyeReporting, gbc);
    }

    public void save() {
        SettingsManager.setForgeLoggingLevel((String) forgeLoggingLevel.getSelectedItem());
        SettingsManager.setDaysOfLogsToKeep((Integer) daysOfLogsToKeep.getValue());
        SettingsManager.setEnableLeaderboards(enableLeaderboards.isSelected());
        SettingsManager.setEnableLogs(enableLogs.isSelected());
        SettingsManager.setEnableOpenEyeReporting(enableOpenEyeReporting.isSelected());
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("settings.loggingtab");
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.forgeLoggingLevelLabel.setText(LanguageManager.localize("settings" + ".forgelogginglevel") + ":");
        this.forgeLoggingLevelLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" + "" +
                ".forgelogginglevelhelp", "<br/><br/>") + "</html>");

        this.daysOfLogsToKeepLabel.setText(LanguageManager.localize("settings.daysoflogstokeep") + "?");
        this.daysOfLogsToKeepLabel.setToolTipText(LanguageManager.localize("settings.daysoflogstokeephelp"));

        this.enableLeaderboardsLabel.setText(LanguageManager.localize("settings.leaderboards") + "?");
        this.enableLeaderboardsLabel.setToolTipText(LanguageManager.localize("settings.leaderboardshelp"));

        this.enableLoggingLabel.setText(LanguageManager.localize("settings.logging") + "?");
        this.enableLoggingLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" + "" +
                ".logginghelp", "<br/>" + "</html>"));

        this.enableOpenEyeReportingLabel.setText(LanguageManager.localize("settings.openeye") + "?");
        this.enableOpenEyeReportingLabel.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager
                .localize("settings.openeyehelp"), 80, "<br/>") + "</html>");
    }
}
