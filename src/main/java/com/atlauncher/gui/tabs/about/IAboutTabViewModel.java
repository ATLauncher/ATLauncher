package com.atlauncher.gui.tabs.about;

import org.jetbrains.annotations.NotNull;

/**
 * 13 / 06 / 2022
 */
public interface IAboutTabViewModel {
    /**
     * @return List of authors
     */
    @NotNull
    String[] getAuthors();

    /**
     * @return Info about the launcher and its environment
     */
    @NotNull
    String getInfo();
}
