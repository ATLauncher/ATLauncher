/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class LoggingSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private JLabelWithHover forgeLoggingLevelLabel;
    private JComboBox<String> forgeLoggingLevel;

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
        forgeLoggingLevelLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.forgelogginglevel") +
                ":", HELP_ICON, "<html>" + App.settings.getLocalizedString("settings.forgelogginglevelhelp",
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

        // Enable Leaderboards

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableLeaderboardsLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.leaderboards") + "?",
                HELP_ICON, App.settings.getLocalizedString("settings.leaderboardshelp"));
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
        enableLoggingLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.logging") + "?",
                HELP_ICON, "<html>" + App.settings.getLocalizedString("settings.logginghelp", "<br/>" + "</html>"));
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
        enableOpenEyeReportingLabel = new JLabelWithHover(App.settings.getLocalizedString("settings.openeye") + "?",
                HELP_ICON, "<html>" + Utils.splitMultilinedString(App.settings.getLocalizedString("settings" + "" +
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
        this.forgeLoggingLevelLabel.setText(App.settings.getLocalizedString("settings" + ".forgelogginglevel") + ":");
        this.forgeLoggingLevelLabel.setToolTipText("<html>" + App.settings.getLocalizedString("settings" + "" +
                ".forgelogginglevelhelp", "<br/><br/>") + "</html>");

        this.enableLeaderboardsLabel.setText(App.settings.getLocalizedString("settings.leaderboards") + "?");
        this.enableLeaderboardsLabel.setToolTipText(App.settings.getLocalizedString("settings.leaderboardshelp"));

        this.enableLoggingLabel.setText(App.settings.getLocalizedString("settings.logging") + "?");
        this.enableLoggingLabel.setToolTipText("<html>" + App.settings.getLocalizedString("settings.logginghelp",
                "<br/>" + "</html>"));

        this.enableOpenEyeReportingLabel.setText(App.settings.getLocalizedString("settings.openeye") + "?");
        this.enableOpenEyeReportingLabel.setToolTipText("<html>" + Utils.splitMultilinedString(App.settings
                .getLocalizedString("settings.openeyehelp"), 80, "<br/>") + "</html>");
    }
}
