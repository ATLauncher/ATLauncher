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

import com.atlauncher.App;
import com.atlauncher.evnt.manager.SettingsManager;
import com.atlauncher.evnt.manager.SettingsValidityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public class CommandsSettingsViewModel implements ICommandsSettingsViewModel {
    private static final Logger LOG = LogManager.getLogger(CommandsSettingsViewModel.class);

    private String nullIfEmpty(String str) {
        if (str.isEmpty())
            return null;
        else
            return str;
    }

    private Consumer<Boolean> _addOnEnableCommandsChanged;
    private Consumer<String> _addOnPreLaunchCommandChanged;
    private Consumer<String> _addOnPostExitCommandChanged;
    private Consumer<String> _addOnWrapperCommandChanged;

    public CommandsSettingsViewModel() {
        SettingsManager.addListener(this);
    }

    @Override
    public void onSettingsSaved() {
        _addOnEnableCommandsChanged.accept(App.settings.enableCommands);
        _addOnPreLaunchCommandChanged.accept(App.settings.preLaunchCommand);
        _addOnPostExitCommandChanged.accept(App.settings.postExitCommand);
        _addOnWrapperCommandChanged.accept(App.settings.wrapperCommand);
    }

    @Override
    public void setEnableCommands(boolean b) {
        App.settings.enableCommands = b;
        SettingsManager.post();
    }

    @Override
    public void addOnEnableCommandsChanged(Consumer<Boolean> onChanged) {
        _addOnEnableCommandsChanged = onChanged;
        onChanged.accept(App.settings.enableCommands);
    }

    @Override
    public void setPreLaunchCommand(String text) {
        App.settings.preLaunchCommand = nullIfEmpty(text);
        SettingsValidityManager.post("preLaunchCommand", true);
        SettingsManager.post();
    }

    @Override
    public void setPreLaunchCommandPending() {
        SettingsValidityManager.post("preLaunchCommand", false);
    }

    @Override
    public void addOnPreLaunchCommandChanged(Consumer<String> setText) {
        _addOnPreLaunchCommandChanged = setText;
        setText.accept(App.settings.preLaunchCommand);
    }

    @Override
    public void setPostExitCommand(String text) {
        App.settings.postExitCommand = nullIfEmpty(text);
        SettingsValidityManager.post("setPostExitCommand", true);
        SettingsManager.post();
    }

    @Override
    public void setPostExitCommandPending() {
        SettingsValidityManager.post("setPostExitCommand", false);
    }

    @Override
    public void addOnPostExitCommandChanged(Consumer<String> setText) {
        _addOnPostExitCommandChanged = setText;
        setText.accept(App.settings.postExitCommand);
    }

    @Override
    public void setWrapperCommand(String text) {
        App.settings.wrapperCommand = nullIfEmpty(text);
        SettingsValidityManager.post("wrapperCommand", true);
        SettingsManager.post();
    }

    @Override
    public void setWrapperCommandPending() {
        SettingsValidityManager.post("wrapperCommand", false);
    }

    @Override
    public void addOnWrapperCommandChanged(Consumer<String> setText) {
        _addOnWrapperCommandChanged = setText;
        setText.accept(App.settings.wrapperCommand);
    }
}