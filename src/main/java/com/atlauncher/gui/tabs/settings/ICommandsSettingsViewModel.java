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

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 * <p>
 * View model for {@link CommandsSettingsTab}
 */
public interface ICommandsSettingsViewModel extends IAbstractSettingsViewModel {

    /**
     * Set commands enabled or not
     *
     * @param b if commands are enabled
     */
    void setEnableCommands(boolean b);

    /**
     * Listen to command enable state change
     *
     * @param onChanged invoked when the command state is changed
     */
    void addOnEnableCommandsChanged(Consumer<Boolean> onChanged);


    /**
     * Set the pre-launch command
     *
     * @param text pre-launch command
     */
    void setPreLaunchCommand(String text);

    /**
     * Inform the settings that the pre-launch command has not been stored yet
     */
    void setPreLaunchCommandPending();

    /**
     * Listen to the pre-launch command being changed
     *
     * @param setText invoked when the pre-launch command is changed
     */
    void addOnPreLaunchCommandChanged(Consumer<String> setText);


    /**
     * Set the post-exit command
     *
     * @param text post-exit command
     */
    void setPostExitCommand(String text);

    /**
     * Inform the settings that the post-exit command has not been stored yet
     */
    void setPostExitCommandPending();

    /**
     * Listen to the post-exit command being changed
     *
     * @param setText invoked when the post-exit command is changed
     */
    void addOnPostExitCommandChanged(Consumer<String> setText);


    /**
     * Set the wrapper command
     *
     * @param text wrapper command
     */
    void setWrapperCommand(String text);

    /**
     * Inform the settings that the wrapper command has not been stored yet
     */
    void setWrapperCommandPending();

    /**
     * Listen to the wrapper command being changed
     *
     * @param setText invoked when the wrapper command is changed
     */
    void addOnWrapperCommandChanged(Consumer<String> setText);
}
