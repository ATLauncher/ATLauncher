/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.JLabelWithHover;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ToolsSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private final JLabelWithHover enableServerCheckerLabel;
    private final JCheckBox enableServerChecker;

    private final JLabelWithHover serverCheckerWaitLabel;
    private JSpinner serverCheckerWait;

    public ToolsSettingsTab() {
        RelocalizationManager.addListener(this);
        // Enable Server Checker
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableServerCheckerLabel = new JLabelWithHover(GetText.tr("Enable Server Checker") + "?", HELP_ICON, GetText
                .tr("This setting enables or disables the checking of added servers in the Server Checker Tool."));
        add(enableServerCheckerLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.CHECKBOX_FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableServerChecker = new JCheckBox();
        if (App.settings.enableServerChecker) {
            enableServerChecker.setSelected(true);
        }
        enableServerChecker.addActionListener(e -> {
            if (!enableServerChecker.isSelected()) {
                serverCheckerWait.setEnabled(false);
            } else {
                serverCheckerWait.setEnabled(true);
            }
        });
        add(enableServerChecker, gbc);

        // Server Checker Wait Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        serverCheckerWaitLabel = new JLabelWithHover(GetText.tr("Time Between Checks") + ":", HELP_ICON,
                new HTMLBuilder().center().split(100).text(GetText.tr(
                        "This option controls how long the launcher should wait between checking servers in the server checker. This value is in minutes and should be between 1 and 30, with the default being 5."))
                        .build());
        add(serverCheckerWaitLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        SpinnerNumberModel serverCheckerWaitModel = new SpinnerNumberModel(App.settings.serverCheckerWait, 1, 30, 1);

        serverCheckerWait = new JSpinner(serverCheckerWaitModel);
        if (!App.settings.enableServerChecker) {
            serverCheckerWait.setEnabled(false);
        }
        add(serverCheckerWait, gbc);
    }

    public boolean needToRestartServerChecker() {
        return ((enableServerChecker.isSelected() != App.settings.enableServerChecker)
                || (App.settings.serverCheckerWait != (Integer) serverCheckerWait.getValue()));
    }

    public void save() {
        App.settings.enableServerChecker = enableServerChecker.isSelected();
        App.settings.serverCheckerWait = (Integer) serverCheckerWait.getValue();
    }

    @Override
    public String getTitle() {
        return GetText.tr("Tools");
    }

    @Override
    public void onRelocalization() {
        this.enableServerCheckerLabel.setText(GetText.tr("Enable Server Checker") + "?");
        this.enableServerCheckerLabel.setToolTipText(GetText
                .tr("This setting enables or disables the checking of added servers in the Server Checker Tool."));

        this.serverCheckerWaitLabel.setText(GetText.tr("Time Between Checks") + ":");
        this.serverCheckerWaitLabel.setToolTipText(new HTMLBuilder().center().split(100).text(GetText.tr(
                "This option controls how long the launcher should wait between checking servers in the server checker. This value is in minutes and should be between 1 and 30, with the default being 5."))
                .build());
    }
}
