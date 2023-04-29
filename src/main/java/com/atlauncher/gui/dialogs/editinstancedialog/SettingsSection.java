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
package com.atlauncher.gui.dialogs.editinstancedialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.tabs.InstanceSettingsTabbedPane;

public class SettingsSection extends SectionPanel {
    private InstanceSettingsTabbedPane tabbedPane = new InstanceSettingsTabbedPane(this.instance);

    public SettingsSection(EditInstanceDialog parent, Instance instance) {
        super(parent, instance);

        setupComponents();
    }

    private void setupComponents() {
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        JButton saveButton = new JButton(GetText.tr("Save"));
        saveButton.addActionListener(arg0 -> {
            if (tabbedPane.saveSettings()) {
                App.TOASTER.pop("Instance Settings Saved");
            }
        });
        bottomPanel.add(saveButton);

        JButton resetButton = new JButton(GetText.tr("Reset"));
        resetButton.addActionListener(arg0 -> {
            tabbedPane.resetSettings();
        });
        bottomPanel.add(resetButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }
}
