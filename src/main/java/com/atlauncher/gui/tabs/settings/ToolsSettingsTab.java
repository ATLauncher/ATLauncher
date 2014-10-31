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
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ToolsSettingsTab extends AbstractSettingsTab implements RelocalizationListener {
    private JLabelWithHover enableServerCheckerLabel;
    private JCheckBox enableServerChecker;

    private JLabelWithHover serverCheckerWaitLabel;
    private JTextField serverCheckerWait;

    public ToolsSettingsTab() {
        RelocalizationManager.addListener(this);
        // Enable Server Checker
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableServerCheckerLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.serverchecker") + "?",
                HELP_ICON, "<html>" + Language.INSTANCE.localizeWithReplace("settings.servercheckerhelp",
                "<br/>" + "</html>"));
        add(enableServerCheckerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableServerChecker = new JCheckBox();
        if (App.settings.enableServerChecker()) {
            enableServerChecker.setSelected(true);
        }
        enableServerChecker.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!enableServerChecker.isSelected()) {
                    serverCheckerWait.setEnabled(false);
                } else {
                    serverCheckerWait.setEnabled(true);
                }
            }
        });
        add(enableServerChecker, gbc);

        // Server Checker Wait Settings
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        serverCheckerWaitLabel = new JLabelWithHover(Language.INSTANCE.localize("settings.servercheckerwait") + ":",
                HELP_ICON, "<html>" + Utils.splitMultilinedString(Language.INSTANCE.localize("settings" + "" +
                ".servercheckerwaithelp"), 75, "<br/>") + "</html>");
        add(serverCheckerWaitLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverCheckerWait = new JTextField(4);
        serverCheckerWait.setText(App.settings.getServerCheckerWait() + "");
        if (!App.settings.enableServerChecker()) {
            serverCheckerWait.setEnabled(false);
        }
        add(serverCheckerWait, gbc);
    }

    public boolean isValidServerCheckerWait() {
        if (Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]",
                "")) < 1 || Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")) > 30) {
            JOptionPane.showMessageDialog(App.settings.getParent(), Language.INSTANCE.localize("settings" + "" +
                            ".servercheckerwaitinvalid"), Language.INSTANCE.localize("settings.help"),
                    JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean needToRestartServerChecker() {
        return ((enableServerChecker.isSelected() != App.settings.enableServerChecker()) || (App.settings
                .getServerCheckerWait() != Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", ""))));
    }

    public void save() {
        App.settings.setEnableServerChecker(enableServerChecker.isSelected());
        App.settings.setServerCheckerWait(Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")));
    }

    @Override
    public String getTitle() {
        return Language.INSTANCE.localize("tabs.tools");
    }

    @Override
    public void onRelocalization() {
        this.enableServerCheckerLabel.setText(Language.INSTANCE.localize("settings.serverchecker") + "?");
        this.enableServerCheckerLabel.setToolTipText("<html>" + Language.INSTANCE.localizeWithReplace("settings" + "" +
                ".servercheckerhelp", "<br/>" + "</html>"));

        this.serverCheckerWaitLabel.setText(Language.INSTANCE.localize("settings.servercheckerwait") + ":");
        this.serverCheckerWaitLabel.setToolTipText("<html>" + Utils.splitMultilinedString(Language.INSTANCE.localize
                ("settings.servercheckerwaithelp"), 75, "<br/>") + "</html>");
    }
}
