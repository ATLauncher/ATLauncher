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
package com.atlauncher.viewmodel.base;

import java.awt.Font;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.antlr.v4.runtime.misc.NotNull;

import com.atlauncher.data.MCVersionRow;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;

import io.reactivex.rxjava3.core.Observable;

public interface ICreatePackViewModel {
    // UI settings

    /**
     * Controls what font to use
     */
    @NotNull
    Observable<Font> font();

    // Pack Options

    /**
     * Name of the new instance
     */
    @NotNull
    Observable<Optional<String>> name();

    /**
     * Set the name of the new instance
     */
    void setName(@NotNull String name);

    /**
     * Description of the new instance
     */
    @NotNull
    Observable<Optional<String>> description();

    /**
     * Set the new description of the new instance
     */
    void setDescription(@NotNull String description);

    // Release Options

    /**
     * If the release checkbox should be visible
     */
    @NotNull
    Boolean showReleaseOption();

    /**
     * If the experiment checkbox should be visible
     */
    @NotNull
    Boolean showExperimentOption();

    /**
     * If the snapshot checkbox should be visible
     */
    @NotNull
    Boolean showSnapshotOption();

    /**
     * If the old alpha option should be visible
     */
    @NotNull
    Boolean showOldAlphaOption();

    /**
     * If the old beta option should be visible
     */
    @NotNull
    Boolean showOldBetaOption();

    /**
     * Is the release checkbox selected
     */
    @NotNull
    Observable<Boolean> releaseSelected();

    /**
     * Is the release checkbox enabled
     */
    @NotNull
    Observable<Boolean> releaseEnabled();

    /**
     * Is the experiment checkbox selected
     */
    @NotNull
    Observable<Boolean> experimentSelected();

    /**
     * Is the experiment checkbox enabled
     */
    @NotNull
    Observable<Boolean> experimentEnabled();

    /**
     * Is the snapshot checkbox selected
     */
    @NotNull
    Observable<Boolean> snapshotSelected();

    /**
     * Is the snapshot checkbox enabled
     */
    @NotNull
    Observable<Boolean> snapshotEnabled();

    /**
     * Is the alpha checkbox selected
     */
    @NotNull
    Observable<Boolean> oldAlphaSelected();

    /**
     * Is the alpha checkbox enabled
     */
    @NotNull
    Observable<Boolean> oldAlphaEnabled();

    /**
     * Is the beta checkbox selected
     */
    @NotNull
    Observable<Boolean> oldBetaSelected();

    /**
     * Is the beta checkbox enabled
     */
    @NotNull
    Observable<Boolean> oldBetaEnabled();

    /**
     * Set [releaseSelected] or not
     * <p>
     * Should reload [minecraftVersions]
     */
    void setReleaseSelected(@NotNull Boolean b);

    /**
     * Set [experimentSelected] or not
     * <p>
     * Should reload [minecraftVersions]
     */
    void setExperimentSelected(@NotNull Boolean b);

    /**
     * Set [snapshotSelected] or not
     * <p>
     * Should reload [minecraftVersions]
     */
    void setSnapshotSelected(@NotNull Boolean b);

    /**
     * Set [oldAlphaSelected] or not
     * <p>
     * Should reload [minecraftVersions]
     */
    void setOldAlphaSelected(@NotNull Boolean b);

    /**
     * Set [oldBetaSelected] or not
     * <p>
     * Should reload [minecraftVersions]
     */
    void setOldBetaSelected(@NotNull Boolean b);

    /**
     * Minecraft versions to display in the version table
     */
    @NotNull
    Observable<List<MCVersionRow>> minecraftVersions();

    /**
     * Which index is currently selected
     */
    @NotNull
    Observable<Integer> selectedMinecraftVersionIndex();

    /**
     * Set the selected minecraft version
     */
    void setSelectedMinecraftVersion(@Nullable String valueAt);

    // Mod loader option

    /**
     * Is fabric an option currently
     */
    @NotNull
    Observable<Boolean> isFabricVisible();

    /**
     * Is forge an option currently
     */
    @NotNull
    Observable<Boolean> isForgeVisible();

    /**
     * Is legacy fabric an option currently
     */
    @NotNull
    Observable<Boolean> isLegacyFabricVisible();

    /**
     * Is neoforge an option currently
     */
    @NotNull
    Observable<Boolean> isNeoForgeVisible();

    /**
     * Is PaperMC an option currently
     */
    @NotNull
    Observable<Boolean> isPaperMCVisible();

    /**
     * Is quilt an option currently
     */
    @NotNull
    Observable<Boolean> isQuiltVisible();

    /**
     * If fabric is enabled in config or not
     */
    @NotNull
    Boolean showFabricOption();

    /**
     * If forge is enabled in config or not
     */
    @NotNull
    Boolean showForgeOption();

    /**
     * If legacy forge should be enabled or not
     */
    @NotNull
    Boolean showLegacyFabricOption();

    /**
     * If neoforge should be enabled or not
     */
    @NotNull
    Boolean showNeoForgeOption();

    /**
     * If PaperMC should be enabled or not
     */
    @NotNull
    Boolean showPaperMCOption();

    /**
     * If quilt is enabled in config or not
     */
    @NotNull
    Boolean showQuiltOption();

    /**
     * Has none been selected
     */
    @NotNull
    Observable<Boolean> loaderTypeNoneSelected();

    /**
     * Is the none button enabled or not
     */
    @NotNull
    Observable<Boolean> loaderTypeNoneEnabled();

    /**
     * Has fabric been selected
     */
    @NotNull
    Observable<Boolean> loaderTypeFabricSelected();

    /**
     * Is the fabric button enabled or not
     */
    @NotNull
    Observable<Boolean> loaderTypeFabricEnabled();

    /**
     * Has forge been selected
     */
    @NotNull
    Observable<Boolean> loaderTypeForgeSelected();

    /**
     * Is the forge button enabled or not
     */
    @NotNull
    Observable<Boolean> loaderTypeForgeEnabled();

    /**
     * Is legacy fabric selected
     */
    @NotNull
    Observable<Boolean> loaderTypeLegacyFabricSelected();

    /**
     * Is legacy fabric enabled
     */
    @NotNull
    Observable<Boolean> loaderTypeLegacyFabricEnabled();

    /**
     * Is neoforge selected
     */
    @NotNull
    Observable<Boolean> loaderTypeNeoForgeSelected();

    /**
     * Is neoforge enabled
     */
    @NotNull
    Observable<Boolean> loaderTypeNeoForgeEnabled();

    /**
     * Is PaperMC selected
     */
    @NotNull
    Observable<Boolean> loaderTypePaperMCSelected();

    /**
     * Is PaperMC enabled
     */
    @NotNull
    Observable<Boolean> loaderTypePaperMCEnabled();

    /**
     * Has quilt been selected
     */
    @NotNull
    Observable<Boolean> loaderTypeQuiltSelected();

    /**
     * Is the quilt button enabled or not
     */
    @NotNull
    Observable<Boolean> loaderTypeQuiltEnabled();

    /**
     * Set the loader type
     * <p>
     * Will cause a reload of [loaderVersions]
     */
    void setLoaderType(@Nullable LoaderType loader);

    /**
     * Set the selected loader version
     */
    void setLoaderVersion(@NotNull LoaderVersion loaderVersion);

    /**
     * Is the loader versions drop down enabled
     */
    @NotNull
    Observable<Boolean> loaderVersionsDropDownEnabled();

    /**
     * Loader versions that the user can select for the loaderType
     */
    @NotNull
    Observable<Optional<List<LoaderVersion>>> loaderVersions();

    /**
     * The selected mod loader version
     */
    @NotNull
    Observable<Integer> selectedLoaderVersionIndex();

    /**
     * Is the loader loading.
     * <p>
     * This public void ue is used to show a little loading icon
     */
    @NotNull
    Observable<Boolean> loaderLoading();

    /**
     * Is the create server button enabled or not
     */
    @NotNull
    Observable<Boolean> createServerEnabled();

    /**
     * Is the create instance button enabled or not
     */
    @NotNull
    Observable<Boolean> createInstanceEnabled();

    /**
     * The text to display when the create instance button is not enabled
     */
    @NotNull
    Observable<Optional<String>> createInstanceDisabledReason();

    /**
     * Create a server with the provided information
     */
    void createServer();

    /**
     * Create an instance with the provided information
     */
    void createInstance();

    /**
     * Warn the user about creating a server =/= playing the game themselves
     */
    @NotNull
    Boolean warnUserAboutServer();
}