package com.atlauncher.domain.repository.base;

import com.atlauncher.data.Pack;
import com.atlauncher.domain.repository.impl.DesktopFileRepository;

/**
 * Source of truth of desktop files.
 * <p>
 * Used to add desktop files for mod-packs to the application menu or list.
 *
 * @since 31 / 05 / 2023
 */
public abstract class IDesktopFileRepository {

    private static IDesktopFileRepository impl = null;

    /**
     * TODO replace with dependency injection
     *
     * @return Implementation of an IDesktopFileRepository
     */
    public static IDesktopFileRepository getInstance() {
        if (impl == null) {
            impl = new DesktopFileRepository();
        }
        return impl;
    }

    /**
     * Add a desktop file
     *
     * @return true if file was added successfully, false otherwise
     */
    public abstract boolean addDesktopFile(Pack pack);

    /**
     * Remove a desktop file for a given mod-pack.
     * <p>
     * Does nothing if the mod-pack has no desktop file
     *
     * @return true if file was removed, false otherwise
     */
    public abstract boolean removeDesktopFile(Pack pack);

    /**
     * Check if a mod-pack has a desktop file or not.
     *
     * @return true if the mod-pack has a desktop file, false otherwise.
     */
    public abstract boolean hasDesktopFile(Pack pack);
}
