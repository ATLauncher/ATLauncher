package com.atlauncher.gui.tabs.tools;

/**
 * 15 / 06 / 2022
 */
public interface IToolsViewModel {
    /**
     * @return Self explanatory
     */
    boolean isDebugEnabled();

    /**
     * @return Should the UI button for launching in debug be clickable
     */
    boolean isLaunchInDebugEnabled();

    void launchInDebug();
}
