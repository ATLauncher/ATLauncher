package com.atlauncher.gui.tabs.settings;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 */
public interface ICommandsSettingsViewModel extends IAbstractSettingsViewModel {
    void setEnableCommands(boolean b);

    void addOnEnableCommandsChanged(Consumer<Boolean> onChanged);

    void setPreLaunchCommand(String text);

    void addOnPreLaunchCommandChanged(Consumer<String> setText);

    void setPostExitCommand(String text);

    void addOnPostExitCommandChanged(Consumer<String> setText);

    void setWrapperCommand(String text);

    void addOnWrapperCommandChanged(Consumer<String> setText);
}
