/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
package com.atlauncher.gui.tabs.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class DebugModePanel extends AbstractToolPanel implements ActionListener {
    private final JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText.tr(
            "Use this to relaunch ATLauncher in debug mode. This can be used to get more debug logs in order to help diagnose issues with ATLauncher."))
            .build());

    public DebugModePanel() {
        super(GetText.tr("Debug Mode"));

        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        LAUNCH_BUTTON.setEnabled(!LogManager.showDebug);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            Analytics.sendEvent("DebugMode", "Run", "Tool");
            OS.relaunchInDebugMode();
        }
    }
}
