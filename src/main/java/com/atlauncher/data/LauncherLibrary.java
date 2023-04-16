package com.atlauncher.data;

import java.net.URI;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;

/**
 * 09 / 04 / 2023
 * <p>
 * Represents a license of the launcher
 */
public class LauncherLibrary {
    /**
     * Name of the library
     */
    @Nonnull
    public final String name;

    /**
     * Link to the library
     */
    @Nonnull
    public final URI link;

    /**
     * @param name of the library
     * @param link to the library
     */
    public LauncherLibrary(@NotNull String name, @NotNull URI link) {
        this.name = name;
        this.link = link;
    }
}
