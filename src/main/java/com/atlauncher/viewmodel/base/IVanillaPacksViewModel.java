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

import com.atlauncher.data.minecraft.loaders.LoaderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * 25 / 06 / 2022
 */
public interface IVanillaPacksViewModel {

    void addOnFontChanged(@NotNull Consumer<Font> consumer);

    void setName(@NotNull String name);
    void addOnNameChanged(@NotNull Consumer<String> name);

    void setDescription(@NotNull String description);
    void addOnDescriptionChanged(@NotNull Consumer<String> name);

    boolean showReleaseOption();

    boolean showExperimentOption();

    boolean showSnapshotOption();

    boolean showOldAlphaOption();

    boolean showOldBetaOption();

    void setReleaseSelected(boolean b);

    void addOnReleaseEnabledChanged(@NotNull Consumer<Boolean> onChanged);

    void setExperimentSelected(boolean b);

    void addOnExperimentEnabledChanged(@NotNull Consumer<Boolean> onChanged);

    void setSnapshotSelected(boolean b);

    void addOnSnapshotEnabledChanged(@NotNull Consumer<Boolean> onChanged);

    void setOldAlphaSelected(boolean b);

    void addOnOldAlphaEnabledChanged(@NotNull Consumer<Boolean> onChanged);

    void setOldBetaSelected(boolean b);

    void addOnOldBetaEnabledChanged(@NotNull Consumer<Boolean> onChanged);

    class MCVersionRow {
        public final String id, date, type;

        public MCVersionRow(String id, String date, String type) {
            this.id = id;
            this.date = date;
            this.type = type;
        }
    }

    void setMinecraftVersion(String valueAt);

    void addOnMinecraftVersionsChanged(@NotNull Consumer<MCVersionRow[]> onChanged);

    boolean showFabricOption();
    void addOnFabricVisibleChanged(@NotNull Consumer<Boolean> onChanged);

    boolean showForgeOption();
    void addOnForgeVisibleChanged(@NotNull Consumer<Boolean> onChanged);

    boolean showQuiltOption();
    void addOnQuiltVisibleChanged(@NotNull Consumer<Boolean> onChanged);

    void setLoaderType(@Nullable LoaderType loader);

    void setLoaderVersion(String loaderVersion);
    void addOnLoaderVersionsChanged(@NotNull Consumer<String[]> consumer);

    void addOnLoaderLoadingListener(@NotNull Consumer<Boolean> consumer);
    void addOnLoaderGroupEnabledListener(@NotNull Consumer<Boolean> consumer);

    List<String> getFabricDisabledMCVersions();

    List<String> getForgeDisabledMCVersions();

    List<String> getQuiltDisabledMCVersions();


    void addOnCreateServerVisibleChanged(@NotNull Consumer<Boolean> consumer);
    void createServer();

    void createInstance();
}
