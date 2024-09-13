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
package com.atlauncher.gui.tabs.tools;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
 *
 * View model containing business logic for tools tabs
 */
public interface IToolsViewModel {

    // DebugModePanel

    /**
     * @return Self explanatory
     */
    boolean isDebugEnabled();

    /**
     * @return Should the UI button for launching in debug be clickable
     */
    boolean isLaunchInDebugEnabled();

    /**
     * Relaunches the launcher in debug mode
     */
    void launchInDebug();

    // DownloadClearerToolPanel

    /**
     * Clear out downloads from the launcher
     */
    void clearDownloads();

    // LibrariesDeleterToolPanel

    /**
     * Delete libraries used by Minecraft for launching the game
     */
    void deleteLibraries();

    // LogClearerToolPanel

    /**
     * Clear out launcher logs
     */
    void clearLogs();

    // NetworkCheckerToolPanel

    /**
     * Listen to if the network runner can be run
     * @param onChanged Function to call on ability to run has changed
     */
    void onCanRunNetworkCheckerChanged(Consumer<Boolean> onChanged);

    /**
     * @return Amount of hosts to be called
     */
    int hostsLength();

    /**
     * Run network checker
     * @param onTaskComplete A task has completed
     * @param onFail Failed to run
     * @param onSuccess Completed
     */
    void runNetworkChecker(Consumer<Void> onTaskComplete, Consumer<Void> onFail, Consumer<Void> onSuccess);

    // SkinUpdaterToolPanel

    /**
     * Listen to skin updater being enabled or not
     * @param onChanged function to be called on change
     */
    void onSkinUpdaterEnabledChanged(Consumer<Boolean> onChanged);

    /**
     * @return Accounts count
     */
    int accountCount();

    /**
     * Update all account skins
     * @param onTaskComplete Called when an account has been updated
     */
    void updateSkins(Consumer<Void> onTaskComplete);
}
