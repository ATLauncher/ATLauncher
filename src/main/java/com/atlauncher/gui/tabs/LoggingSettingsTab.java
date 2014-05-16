/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import javax.swing.border.Border;

import com.atlauncher.App;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class LoggingSettingsTab extends AbstractSettingsTab {

    private JLabel forgeLoggingLevelLabel;
    private JComboBox<String> forgeLoggingLevel;

    private JLabel enableLeaderboardsLabel;
    private JCheckBox enableLeaderboards;

    private JLabel enableLoggingLabel;
    private JCheckBox enableLogs;

    private JLabel enableOpenEyeReportingLabel;
    private JCheckBox enableOpenEyeReporting;

    public LoggingSettingsTab() {
        // Forge Logging Level
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        forgeLoggingLevelLabel = new JLabel(
                App.settings.getLocalizedString("settings.forgelogginglevel") + ":") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        forgeLoggingLevelLabel.setIcon(helpIcon);
        forgeLoggingLevelLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.forgelogginglevelhelp", "<br/><br/>")
                + "</center></html>");
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
        enableLeaderboardsLabel = new JLabel(
                App.settings.getLocalizedString("settings.leaderboards") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableLeaderboardsLabel.setIcon(helpIcon);
        enableLeaderboardsLabel.setToolTipText(App.settings
                .getLocalizedString("settings.leaderboardshelp"));
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
        enableLoggingLabel = new JLabel(App.settings.getLocalizedString("settings.logging") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableLoggingLabel.setIcon(helpIcon);
        enableLoggingLabel.setToolTipText("<html><center>"
                + App.settings.getLocalizedString("settings.logginghelp", "<br/>")
                + "</center></html>");
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
        enableOpenEyeReportingLabel = new JLabel(
                App.settings.getLocalizedString("settings.openeye") + "?") {
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColour(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        enableOpenEyeReportingLabel.setIcon(helpIcon);
        enableOpenEyeReportingLabel.setToolTipText("<html><center>"
                + Utils.splitMultilinedString(
                        App.settings.getLocalizedString("settings.openeyehelp"), 80, "<br/>")
                + "</center></html>");
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
}
