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
package com.atlauncher.gui.tabs;

import javax.swing.JTabbedPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.tabs.instancesettings.CommandsInstanceSettingsTab;
import com.atlauncher.gui.tabs.instancesettings.GeneralInstanceSettingsTab;
import com.atlauncher.gui.tabs.instancesettings.JavaInstanceSettingsTab;

public class InstanceSettingsTabbedPane extends JTabbedPane {
    private Instance instance;

    private GeneralInstanceSettingsTab generalInstanceSettingsTab;
    private JavaInstanceSettingsTab javaInstanceSettingsTab;
    private CommandsInstanceSettingsTab commandsInstanceSettingsTab;

    public InstanceSettingsTabbedPane(Instance instance) {
        super();
        this.instance = instance;

        setFont(App.THEME.getNormalFont().deriveFont(17.0F));
        setOpaque(true);

        resetSettings();
    }

    public void resetSettings() {
        int selectedTab = getSelectedIndex();

        while (getTabCount() != 0) {
            removeTabAt(0);
        }

        this.generalInstanceSettingsTab = new GeneralInstanceSettingsTab(instance);
        this.javaInstanceSettingsTab = new JavaInstanceSettingsTab(instance);
        this.commandsInstanceSettingsTab = new CommandsInstanceSettingsTab(instance);

        addTab(GetText.tr("General"), generalInstanceSettingsTab);
        addTab(GetText.tr("Java/Minecraft"), javaInstanceSettingsTab);
        addTab(GetText.tr("Commands"), commandsInstanceSettingsTab);

        setSelectedIndex(selectedTab == -1 ? 0 : selectedTab);
    }

    public boolean saveSettings() {
        if (!javaInstanceSettingsTab.isValidJavaPath()
                || !javaInstanceSettingsTab.isValidJavaParamaters()) {
            return false;
        }

        generalInstanceSettingsTab.saveSettings();
        javaInstanceSettingsTab.saveSettings();
        commandsInstanceSettingsTab.saveSettings();

        this.instance.save();
        return true;
    }
}
