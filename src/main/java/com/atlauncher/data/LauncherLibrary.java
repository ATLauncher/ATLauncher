/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
