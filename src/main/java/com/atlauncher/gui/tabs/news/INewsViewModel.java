package com.atlauncher.gui.tabs.news;

import java.util.function.Consumer;

/**
 * 14 / 06 / 2022
 *
 * View model for NewsTab
 */
public interface INewsViewModel {

    /**
     * React to reload events
     * @param onReload function to be called
     */
    void addOnReloadListener(Consumer<String> onReload);

    /**
     * Reload news
     */
    void reload();
}
