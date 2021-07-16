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

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.SettingsListener;

import javax.swing.*;
import java.awt.*;

public class CommandSettingsTab extends AbstractSettingsTab
{
    private final JTextArea command;

    public CommandSettingsTab()
    {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BELOW_BASELINE_TRAILING;

        JLabel commandLabel = new JLabel("Pre launch command");
        add(commandLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;

        command = new JTextArea("something");
    }

    @Override
    public String getTitle()
    {
        return "Commands";
    }

    public void save()
    {
        App.settings.commandPath = command.getText();
    }
}
