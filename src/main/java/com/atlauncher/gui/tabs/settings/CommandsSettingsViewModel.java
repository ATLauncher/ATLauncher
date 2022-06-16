package com.atlauncher.gui.tabs.settings;

import com.atlauncher.App;
import com.atlauncher.evnt.manager.SettingsManager;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public class CommandsSettingsViewModel implements ICommandsSettingsViewModel {
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
        SettingsManager.post();
    }

    @Override
    public void addOnPreLaunchCommandChanged(Consumer<String> setText) {
        _addOnPreLaunchCommandChanged = setText;
        setText.accept(App.settings.preLaunchCommand);
    }

    @Override
    public void setPostExitCommand(String text) {
        App.settings.postExitCommand = nullIfEmpty(text);
        SettingsManager.post();
    }

    @Override
    public void addOnPostExitCommandChanged(Consumer<String> setText) {
        _addOnPostExitCommandChanged = setText;
        setText.accept(App.settings.postExitCommand);
    }

    @Override
    public void setWrapperCommand(String text) {
        App.settings.wrapperCommand = nullIfEmpty(text);
        SettingsManager.post();
    }

    @Override
    public void addOnWrapperCommandChanged(Consumer<String> setText) {
        _addOnWrapperCommandChanged = setText;
        setText.accept(App.settings.wrapperCommand);
    }
}
