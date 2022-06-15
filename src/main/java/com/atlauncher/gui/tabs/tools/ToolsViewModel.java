package com.atlauncher.gui.tabs.tools;

import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 15 / 06 / 2022
 */
public class ToolsViewModel implements IToolsViewModel {
    private static final Logger LOG = LogManager.getLogger(ToolsViewModel.class);

    @Override
    public boolean isDebugEnabled() {
        return LOG.isDebugEnabled();
    }

    @Override
    public boolean isLaunchInDebugEnabled() {
        return !OS.isUsingFlatpak() && !isDebugEnabled();
    }

    @Override
    public void launchInDebug() {
        Analytics.sendEvent("DebugMode", "Run", "Tool");

        OS.relaunchInDebugMode();
    }
}
