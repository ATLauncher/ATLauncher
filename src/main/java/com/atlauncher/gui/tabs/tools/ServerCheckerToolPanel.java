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
package com.atlauncher.gui.tabs.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.gui.dialogs.ServerListForCheckerDialog;
import com.atlauncher.network.Analytics;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ServerCheckerToolPanel extends AbstractToolPanel implements ActionListener, SettingsListener {

    public ServerCheckerToolPanel() {
        super(GetText.tr("Server Checker"));

        JLabel INFO_LABEL = new JLabel(new HTMLBuilder().center().split(70).text(GetText.tr(
            "This tool checks specified Minecraft servers to see if they are up or not and how many players are logged in. Settings can be configured in the Settings tab under the Tools sub tab."))
            .build());
        MIDDLE_PANEL.add(INFO_LABEL);
        BOTTOM_PANEL.add(LAUNCH_BUTTON);
        LAUNCH_BUTTON.addActionListener(this);
        SettingsManager.addListener(this);
        this.checkLaunchButtonEnabled();
    }

    private void checkLaunchButtonEnabled() {
        LAUNCH_BUTTON.setEnabled(App.settings.enableServerChecker);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == LAUNCH_BUTTON) {
            Analytics.sendEvent("ServerChecker", "Run", "Tool");
            new ServerListForCheckerDialog();
        }
    }

    @Override
    public void onSettingsSaved() {
        this.checkLaunchButtonEnabled();
    }
}
