package com.atlauncher.gui.tabs.tools;

import com.atlauncher.FileSystem;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

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

    @Override
    public void clearDownloads() {
        Analytics.sendEvent("DownloadClearer", "Run", "Tool");

        for (File file : FileSystem.DOWNLOADS.toFile().listFiles()) {
            if (!file.equals(FileSystem.TECHNIC_DOWNLOADS.toFile())) {
                Utils.delete(file);
            }
        }

        for (File file : FileSystem.TECHNIC_DOWNLOADS.toFile().listFiles()) {
            Utils.delete(file);
        }

        for (File file : FileSystem.FAILED_DOWNLOADS.toFile().listFiles()) {
            Utils.delete(file);
        }
    }
}
