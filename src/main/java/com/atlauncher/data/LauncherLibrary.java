package com.atlauncher.data;

import java.net.URI;

/**
 * 09 / 04 / 2023
 * <p>
 * Represents a license of the launcher
 */
public class LauncherLibrary {
    /**
     * Name of the library
     */
    public final String name;

    /**
     * Link to the library
     */
    public final URI link;

    /**
     * @param name of the library
     * @param link to the library
     */
    public LauncherLibrary(String name, URI link) {
        this.name = name;
        this.link = link;
    }
}
