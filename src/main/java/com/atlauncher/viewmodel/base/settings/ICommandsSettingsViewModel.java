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
package com.atlauncher.viewmodel.base.settings;

import com.atlauncher.evnt.listener.SettingsListener;
import com.atlauncher.gui.tabs.settings.CommandsSettingsTab;

import io.reactivex.rxjava3.core.Observable;

/**
 * @since 2022 / 06 / 16
 * <p>
 * View model for {@link CommandsSettingsTab}
 */
public interface ICommandsSettingsViewModel extends SettingsListener {

    /**
     * Listen to command enable state change
     */
    Observable<Boolean> getEnableCommands();

    /**
     * Set commands enabled or not
     *
     * @param b if commands are enabled
     */
    void setEnableCommands(boolean b);

    /**
     * Inform the settings that the pre-launch command has not been stored yet
     */
    void setPreLaunchCommandPending();

    /**
     * Listen to the pre-launch command being changed
     */
    Observable<String> getPreLaunchCommand();

    /**
     * Set the pre-launch command
     *
     * @param text pre-launch command
     */
    void setPreLaunchCommand(String text);

    /**
     * Inform the settings that the post-exit command has not been stored yet
     */
    void setPostExitCommandPending();

    /**
     * Listen to the post-exit command being changed
     */
    Observable<String> getPostExitCommand();

    /**
     * Set the post-exit command
     *
     * @param text post-exit command
     */
    void setPostExitCommand(String text);

    /**
     * Inform the settings that the wrapper command has not been stored yet
     */
    void setWrapperCommandPending();

    /**
     * Listen to the wrapper command being changed
     */
    Observable<String> getWrapperCommand();

    /**
     * Set the wrapper command
     *
     * @param text wrapper command
     */
    void setWrapperCommand(String text);
}