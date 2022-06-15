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

import com.atlauncher.interfaces.NetworkProgressable;

import java.util.function.Consumer;

/**
 * 15 / 06 / 2022
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

    void launchInDebug();

    // DownloadClearerToolPanel

    void clearDownloads();

    void clearLogs();

    // NetworkCheckerToolPanel

    void onCanRunNetworkCheckerChanged(Consumer<Boolean> onChanged);

    int hostsLength();

    /**
     * @param onTaskComplete
     * @param onFail
     */
    void runNetworkChecker(Consumer<Void> onTaskComplete, Consumer<Void> onFail, Consumer<Void> onSuccess);

    // RuntimeDownloaderToolPanel

    void removeRuntime(
        Consumer<Void> onFail,
        Consumer<Void> onSuccess
    );

    boolean downloadRuntime(
        NetworkProgressable progressable,
        Consumer<Void> onTaskComplete,
        Consumer<String> newLabel,
        Consumer<Void> clearDownloadedBytes
    );

    // SkinUpdaterToolPanel

    void onSkinUpdaterEnabledChanged(Consumer<Boolean> onChanged);

    int accountCount();
    void updateSkins(Consumer<Void> onTaskComplete);
}
