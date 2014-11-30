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

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;
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
public class LoggingSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
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
        RelocalizationManager.addListener(this);
        // Forge Logging Level
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        forgeLoggingLevelLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.forgelogginglevel") + ":",
                HELP_ICON, "<html>" + Language.INSTANCE.localizeWithReplace("settings.forgelogginglevelhelp",
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
        forgeLoggingLevel.setSelectedItem(App.settings.getForgeLoggingLevel());
        add(forgeLoggingLevel, gbc);

        // Days of logs to keep

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        daysOfLogsToKeepLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.daysoflogstokeep") + ":",
                HELP_ICON, Language.INSTANCE.localize("settings.daysoflogstokeephelp"));
        add(daysOfLogsToKeepLabel, gbc);

        daysOfLogsToKeepModel = new SpinnerNumberModel(App.settings.getDaysOfLogsToKeep(), 1, 30, 1);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        daysOfLogsToKeep = new JSpinner(daysOfLogsToKeepModel);
        daysOfLogsToKeep.setValue(App.settings.getDaysOfLogsToKeep());
        add(daysOfLogsToKeep, gbc);

        // Enable Leaderboards

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.leaderboards") + "?",
                HELP_ICON, Language.INSTANCE.localize("settings.leaderboardshelp"));
        add(enableLeaderboardsLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLeaderboards = new JCheckBox();
        if (App.settings.enableLeaderboards()) {
            enableLeaderboards.setSelected(true);
        }
        if (!App.settings.enableLogs()) {
            enableLeaderboards.setEnabled(false);
        }
        add(enableLeaderboards, gbc);

        // Enable Logging

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLoggingLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.logging") + "?", HELP_ICON,
                "<html>" + Language.INSTANCE.localizeWithReplace("settings.logginghelp", "<br/>" + "</html>"));
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
        if (App.settings.enableLogs()) {
            enableLogs.setSelected(true);
        }
        add(enableLogs, gbc);

        // Enable OpenEye Reporting

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableOpenEyeReportingLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.openeye") + "?",
                HELP_ICON, "<html>" + Utils.splitMultilinedString(Language.INSTANCE.localize("settings" + "" +
                ".openeyehelp"), 80, "<br/>") + "</html>");
        add(enableOpenEyeReportingLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableOpenEyeReporting = new JCheckBox();
        if (!App.settings.enableLogs()) {
            enableOpenEyeReporting.setEnabled(false);
        }
        if (App.settings.enableOpenEyeReporting()) {
            enableOpenEyeReporting.setSelected(true);
        }
        add(enableOpenEyeReporting, gbc);
    }

    public void save() {
        App.settings.setForgeLoggingLevel((String) forgeLoggingLevel.getSelectedItem());
        App.settings.setDaysOfLogsToKeep((Integer) daysOfLogsToKeep.getValue());
        App.settings.setEnableLeaderboards(enableLeaderboards.isSelected());
        App.settings.setEnableLogs(enableLogs.isSelected());
        App.settings.setEnableOpenEyeReporting(enableOpenEyeReporting.isSelected());
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("settings.loggingtab");
    }

    @Override
    public void onRelocalization() {
        this.forgeLoggingLevelLabel.setText(Language.INSTANCE.localize("settings" + ".forgelogginglevel") + ":");
        this.forgeLoggingLevelLabel.setToolTipText("<html>" + Language.INSTANCE.localizeWithReplace("settings" + "" +
                ".forgelogginglevelhelp", "<br/><br/>") + "</html>");

        this.daysOfLogsToKeepLabel.setText(Language.INSTANCE.localize("settings.daysoflogstokeep") + "?");
        this.daysOfLogsToKeepLabel.setToolTipText(Language.INSTANCE.localize("settings.daysoflogstokeephelp"));

        this.enableLeaderboardsLabel.setText(Language.INSTANCE.localize("settings.leaderboards") + "?");
        this.enableLeaderboardsLabel.setToolTipText(Language.INSTANCE.localize("settings.leaderboardshelp"));

        this.enableLoggingLabel.setText(Language.INSTANCE.localize("settings.logging") + "?");
        this.enableLoggingLabel.setToolTipText("<html>" + Language.INSTANCE.localizeWithReplace("settings" + "" +
                ".logginghelp", "<br/>" + "</html>"));

        this.enableOpenEyeReportingLabel.setText(Language.INSTANCE.localize("settings.openeye") + "?");
        this.enableOpenEyeReportingLabel.setToolTipText("<html>" + Utils.splitMultilinedString(Language.INSTANCE
                .localize("settings.openeyehelp"), 80, "<br/>") + "</html>");
    }
}
