/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.io.File;

import com.atlauncher.data.minecraft.loaders.LoaderVersion;

public interface Launchable {
    /**
     * Gets a File object for the directory where the assets for this version of
     * Minecraft are stored.
     *
     * @return File object for the assets directory used by Minecraft
     */
    public File getAssetsDir();

    /**
     * Gets a File object for the root directory of this Instance.
     *
     * @return File object for the root directory of this Instance
     */
    public File getRootDirectory();

    /**
     * Gets a File object for the jar mods directory of this Instance.
     *
     * @return File object for the jar mods directory of this Instance
     */
    public File getJarModsDirectory();

    /**
     * Gets a File object for the bin directory of this Instance.
     *
     * @return File object for the bin directory of this Instance
     */
    public File getBinDirectory();

    /**
     * Gets a File object for the natives directory of this Instance.
     *
     * @return File object for the natives directory of this Instance
     */
    public File getNativesDirectory();

    /**
     * Gets a File object for the minecraft.jar of this Instance.
     *
     * @return File object for the minecraft.jar of this Instance
     */
    public File getMinecraftJar();

    /**
     * Gets this instances name.
     *
     * @return the instances name
     */
    public String getName();

    /**
     * Gets the name of the Pack this Instance was created from. Pack's can be
     * deleted/removed in the future.
     *
     * @return the name of the Pack the Instance was created from.
     */
    public String getPackName();

    /**
     * Gets the version type that this Instance uses.
     *
     * @return the version type that this Instance uses
     */
    public String getVersion();

    public LoaderVersion getLoaderVersion();

    public InstanceSettings getSettings();

    /**
     * Gets the main class used to launch Minecraft.
     *
     * @return the main class used to launch Minecraft
     */
    public String getMainClass();

    /**
     * Gets the Minecraft Version that this Instance uses.
     *
     * @return the Minecraft Version that this Instance uses
     */
    public String getMinecraftVersion();

    /**
     * Gets the assets value which Minecraft uses to determine how to load assets in
     * the game.
     *
     * @return the assets value
     */
    public String getAssets();

    /**
     * Gets the version type that this Instance uses.
     *
     * @return the version type that this Instance uses
     */
    public String getVersionType();

    /**
     * Gets the minimum recommended RAM/memory for this Instance based off what the
     * Pack specifies. Defaults to 0 if there is none specified by the pack. Value
     * is in MB.
     *
     * @return the minimum RAM/memory recommended for this Instance in MB
     */
    public int getMemory();

    /**
     * Gets the minimum recommended PermGen/Metaspace size for this Instance based
     * off what the Pack specifies. Defaults to 0 if there is non specified by the
     * pack. Value is in MB.
     *
     * @return the minimum PermGen/Metaspace recommended for this Instance in MB
     */
    public int getPermGen();
}
