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
package com.atlauncher.viewmodel.base

import com.atlauncher.data.minecraft.loaders.LoaderType
import com.atlauncher.data.minecraft.loaders.LoaderVersion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.awt.Font

/**
 * 25 / 06 / 2022
 */
interface IVanillaPacksViewModel {
    // UI settings

    val selectedLoaderType: StateFlow<LoaderType?>
    val font: Flow<Font>

    // Pack Options

    /**
     * Name of the new instance
     */
    val name: Flow<String?>

    /**
     * Set the name of the new instance
     */
    fun setName(name: String)

    /**
     * Description of the new instance
     */
    val description: Flow<String?>

    /**
     * Set the new description of the new instance
     */
    fun setDescription(description: String)

    // Release Options

    val showReleaseOption: Boolean
    val showExperimentOption: Boolean
    val showSnapshotOption: Boolean
    val showOldAlphaOption: Boolean
    val showOldBetaOption: Boolean

    val releaseSelected: Flow<Boolean>
    val releaseEnabled: Flow<Boolean>

    val experimentSelected: Flow<Boolean>
    val experimentEnabled: Flow<Boolean>

    val snapshotSelected: Flow<Boolean>
    val snapshotEnabled: Flow<Boolean>

    val oldAlphaSelected: Flow<Boolean>
    val oldAlphaEnabled: Flow<Boolean>

    val oldBetaSelected: Flow<Boolean>
    val oldBetaEnabled: Flow<Boolean>

    /**
     * Set [releaseSelected] or not
     *
     * Should reload [minecraftVersions]
     */
    fun setReleaseSelected(b: Boolean)

    /**
     * Set [experimentSelected] or not
     *
     * Should reload [minecraftVersions]
     */
    fun setExperimentSelected(b: Boolean)

    /**
     * Set [snapshotSelected] or not
     *
     * Should reload [minecraftVersions]
     */
    fun setSnapshotSelected(b: Boolean)

    /**
     * Set [oldAlphaSelected] or not
     *
     * Should reload [minecraftVersions]
     */
    fun setOldAlphaSelected(b: Boolean)

    /**
     * Set [oldBetaSelected] or not
     *
     * Should reload [minecraftVersions]
     */
    fun setOldBetaSelected(b: Boolean)

    // Version option
    class MCVersionRow(val id: String, val date: String, val type: String)

    /**
     * Minecraft versions to display in the version table
     */
    val minecraftVersions: Flow<Array<MCVersionRow>>

    /**
     * Set the selected minecraft version
     */
    fun setSelectedMinecraftVersion(valueAt: String?)

    // Mod loader option

    val isFabricVisible: Flow<Boolean>
    val isForgeVisible: Flow<Boolean>
    val isQuiltVisible: Flow<Boolean>

    /**
     * If fabric is enabled in config or not
     */
    val showFabricOption: Boolean

    /**
     * If forge is enabled in config or not
     */
    val showForgeOption: Boolean

    /**
     * If quilt is enabled in config or not
     */
    val showQuiltOption: Boolean

    /**
     * Has none been selected
     */
    val loaderTypeNoneSelected: Flow<Boolean>

    /**
     * Is the none button enabled or not
     */
    val loaderTypeNoneEnabled: Flow<Boolean>

    /**
     * Has fabric been selected
     */
    val loaderTypeFabricSelected: Flow<Boolean>

    /**
     * Is the fabric button enabled or not
     */
    val loaderTypeFabricEnabled: Flow<Boolean>

    /**
     * Has forge been selected
     */
    val loaderTypeForgeSelected: Flow<Boolean>

    /**
     * Is the forge button enabled or not
     */
    val loaderTypeForgeEnabled: Flow<Boolean>

    /**
     * Has quilt been selected
     */
    val loaderTypeQuiltSelected: Flow<Boolean>

    /**
     * Is the quilt button enabled or not
     */
    val loaderTypeQuiltEnabled: Flow<Boolean>

    /**
     * Set the loader type
     *
     * Will cause a reload of [loaderVersions]
     */
    fun setLoaderType(loader: LoaderType?)

    /**
     * Set the selected loader version
     */
    fun setLoaderVersion(loaderVersion: String)

    val loaderVersionsDropDownEnabled: Flow<Boolean>

    /**
     * Loader versions that the user can select for the loaderType
     */
    val loaderVersions: Flow<Array<LoaderVersion>?>

    /**
     * Is the loader loading.
     *
     * This value is used to show a little loading icon
     */
    val loaderLoading: Flow<Boolean>

    /**
     * Is the create server button enabled or not
     */
    val createServerEnabled: Flow<Boolean>

    /**
     * Is the create instance button enabled or not
     */
    val createInstanceEnabled: Flow<Boolean>

    /**
     * Create a server with the provided information
     */
    fun createServer()

    /**
     * Create an instance with the provided information
     */
    fun createInstance()

    /**
     * Warn the user about creating a server =/= playing the game themselves
     */
    val warnUserAboutServer: Boolean
}