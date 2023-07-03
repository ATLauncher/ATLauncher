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
package com.atlauncher.gui.tabs.settings;

import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.gui.components.JLabelWithHover;

@SuppressWarnings("serial")
public class LoggingSettingsTab extends AbstractSettingsTab {
    private final JComboBox<String> forgeLoggingLevel;

    private final JCheckBox enableLogs;

    private final JCheckBox enableAnalytics;

    public LoggingSettingsTab() {
        // Forge Logging Level
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover forgeLoggingLevelLabel = new JLabelWithHover(GetText.tr("Forge Logging Level") + ":", HELP_ICON,
                "<html>" + GetText.tr("This determines the type of logging that Forge should report back to you.")
                        + "</html>");
        add(forgeLoggingLevelLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        forgeLoggingLevel = new JComboBox<>();
        forgeLoggingLevel.addItem("SEVERE");
        forgeLoggingLevel.addItem("WARNING");
        forgeLoggingLevel.addItem("INFO");
        forgeLoggingLevel.addItem("CONFIG");
        forgeLoggingLevel.addItem("FINE");
        forgeLoggingLevel.addItem("FINER");
        forgeLoggingLevel.addItem("FINEST");
        forgeLoggingLevel.setSelectedItem(App.settings.forgeLoggingLevel);
        add(forgeLoggingLevel, gbc);

        // Enable Logging

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableLoggingLabel = new JLabelWithHover(GetText.tr("Enable Logging") + "?", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "The Launcher sends back anonymous usage and error logs to our servers in order to make the Launcher and Packs better. If you don't want this to happen then simply disable this option."))
                        .build());
        add(enableLoggingLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableLogs = new JCheckBox();
        if (App.settings.enableLogs) {
            enableLogs.setSelected(true);
        }
        add(enableLogs, gbc);

        // Enable Analytics

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabelWithHover enableAnalyticsLabel = new JLabelWithHover(GetText.tr("Enable Anonymous Analytics") + "?",
                HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "The Launcher sends back anonymous analytics to our own servers in a non identifying way in order to track what people do and don't use in the launcher. This helps determine what new features we implement in the future. All analytics are anonymous and contain no user/instance information in it at all. If you don't want to send anonymous analytics, you can disable this option."))
                        .build());
        add(enableAnalyticsLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableAnalytics = new JCheckBox();
        if (App.settings.enableAnalytics) {
            enableAnalytics.setSelected(true);
        }
        add(enableAnalytics, gbc);
    }

    public void save() {
        App.settings.forgeLoggingLevel = (String) forgeLoggingLevel.getSelectedItem();
        App.settings.enableLogs = enableLogs.isSelected();
        App.settings.enableAnalytics = enableAnalytics.isSelected();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Logging");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Logging";
    }
}
