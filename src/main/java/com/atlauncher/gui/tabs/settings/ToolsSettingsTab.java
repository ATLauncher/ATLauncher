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
import com.atlauncher.annot.Subscribe;
import com.atlauncher.evnt.EventHandler;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.Utils;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class ToolsSettingsTab extends AbstractSettingsTab {
    private JLabelWithHover enableServerCheckerLabel;
    private JCheckBox enableServerChecker;

    private JLabelWithHover serverCheckerWaitLabel;
    private JTextField serverCheckerWait;

    public ToolsSettingsTab() {
        EventHandler.EVENT_BUS.subscribe(this);
        // Enable Server Checker
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableServerCheckerLabel = new JLabelWithHover(LanguageManager.localize("settings.serverchecker") + "?",
                HELP_ICON, "<html>" + LanguageManager.localizeWithReplace("settings.servercheckerhelp", "<br/>" +
                "</html>"));
        add(enableServerCheckerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableServerChecker = new JCheckBox();
        if (SettingsManager.enableServerChecker()) {
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
        serverCheckerWaitLabel = new JLabelWithHover(LanguageManager.localize("settings.servercheckerwait") + ":",
                HELP_ICON, "<html>" + Utils.splitMultilinedString(LanguageManager.localize("settings" + "" +
                ".servercheckerwaithelp"), 75, "<br/>") + "</html>");
        add(serverCheckerWaitLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverCheckerWait = new JTextField(4);
        serverCheckerWait.setText(SettingsManager.getServerCheckerWait() + "");
        if (!SettingsManager.enableServerChecker()) {
            serverCheckerWait.setEnabled(false);
        }
        add(serverCheckerWait, gbc);
    }

    public boolean isValidServerCheckerWait() {
        if (Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")) < 1 || Integer.parseInt
                (serverCheckerWait.getText().replaceAll("[^0-9]", "")) > 30) {
            JOptionPane.showMessageDialog(App.settings.getParent(), LanguageManager.localize("settings" + "" +
                    ".servercheckerwaitinvalid"), LanguageManager.localize("settings.help"), JOptionPane
                    .PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean needToRestartServerChecker() {
        return ((enableServerChecker.isSelected() != SettingsManager.enableServerChecker()) || (SettingsManager
                .getServerCheckerWait() != Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", ""))));
    }

    public void save() {
        SettingsManager.setEnableServerChecker(enableServerChecker.isSelected());
        SettingsManager.setServerCheckerWait(Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")));
    }

    @Override
    public String getTitle() {
        return LanguageManager.localize("tabs.tools");
    }

    @Subscribe
    public void onRelocalization(EventHandler.RelocalizationEvent e) {
        this.enableServerCheckerLabel.setText(LanguageManager.localize("settings.serverchecker") + "?");
        this.enableServerCheckerLabel.setToolTipText("<html>" + LanguageManager.localizeWithReplace("settings" + "" +
                ".servercheckerhelp", "<br/>" + "</html>"));

        this.serverCheckerWaitLabel.setText(LanguageManager.localize("settings.servercheckerwait") + ":");
        this.serverCheckerWaitLabel.setToolTipText("<html>" + Utils.splitMultilinedString(LanguageManager.localize
                ("settings.servercheckerwaithelp"), 75, "<br/>") + "</html>");
    }
}
