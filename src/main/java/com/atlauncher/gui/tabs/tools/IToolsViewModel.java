package com.atlauncher.gui.tabs.tools;

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
}
