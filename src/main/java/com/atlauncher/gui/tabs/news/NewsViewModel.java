package com.atlauncher.gui.tabs.news;

import com.atlauncher.managers.NewsManager;

import java.util.function.Consumer;

/**
 * 14 / 06 / 2022
 */
public class NewsViewModel implements INewsViewModel {
    private Consumer<String> _onReload;

    @Override
    public void addOnReloadListener(Consumer<String> onReload) {
        _onReload = onReload;
    }

    @Override
    public void reload() {
        _onReload.accept(NewsManager.getNewsHTML());
    }
}
