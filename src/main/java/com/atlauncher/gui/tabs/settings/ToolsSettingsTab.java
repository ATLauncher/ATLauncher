/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.gui.components.JLabelWithHover;
import com.atlauncher.utils.Utils;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class ToolsSettingsTab extends AbstractSettingsTab{
    private JLabelWithHover enableServerCheckerLabel;
    private JCheckBox enableServerChecker;

    private JLabelWithHover serverCheckerWaitLabel;
    private JTextField serverCheckerWait;

    public ToolsSettingsTab(){
        // Enable Server Checker
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        enableServerCheckerLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.serverchecker") + "?", HELP_ICON,
                "<html>"
                        + App.settings.getLocalizedString("settings.servercheckerhelp", "<br/>"
                        + "</html>"));
        add(enableServerCheckerLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        enableServerChecker = new JCheckBox();
        if(App.settings.enableServerChecker()){
            enableServerChecker.setSelected(true);
        }
        enableServerChecker.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e){
                if(!enableServerChecker.isSelected()){
                    serverCheckerWait.setEnabled(false);
                } else{
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
        serverCheckerWaitLabel = new JLabelWithHover(
                App.settings.getLocalizedString("settings.servercheckerwait") + ":", HELP_ICON,
                "<html>"
                        + Utils.splitMultilinedString(
                        App.settings.getLocalizedString("settings.servercheckerwaithelp"),
                        75, "<br/>") + "</html>");
        add(serverCheckerWaitLabel, gbc);

        gbc.gridx++;
        gbc.insets = FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        serverCheckerWait = new JTextField(4);
        serverCheckerWait.setText(App.settings.getServerCheckerWait() + "");
        if(!App.settings.enableServerChecker()){
            serverCheckerWait.setEnabled(false);
        }
        add(serverCheckerWait, gbc);
    }

    public boolean isValidServerCheckerWait(){
        if(Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")) < 1
                || Integer.parseInt(serverCheckerWait.getText().replaceAll("[^0-9]", "")) > 30){
            JOptionPane.showMessageDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("settings.servercheckerwaitinvalid"),
                    App.settings.getLocalizedString("settings.help"), JOptionPane.PLAIN_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean needToRestartServerChecker(){
        return ((enableServerChecker.isSelected() != App.settings.enableServerChecker()) || (App.settings
                .getServerCheckerWait() != Integer.parseInt(serverCheckerWait.getText().replaceAll(
                "[^0-9]", ""))));
    }

    public void save(){
        App.settings.setEnableServerChecker(enableServerChecker.isSelected());
        App.settings.setServerCheckerWait(Integer.parseInt(serverCheckerWait.getText().replaceAll(
                "[^0-9]", "")));
    }

    @Override
    public String getTitle(){
        return Language.INSTANCE.localize("tabs.tools");
    }
}
