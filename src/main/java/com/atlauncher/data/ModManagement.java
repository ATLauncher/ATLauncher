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

import java.awt.Window;
import java.nio.file.Path;
import java.util.List;

import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;

import okhttp3.OkHttpClient;

/**
 * Interface for mod management. Used by Instances as well as Servers to manage the mods/plugins for them.
 */
public interface ModManagement {
    public abstract Path getRoot();

    public abstract String getName();

    public abstract String getVersion();

    public abstract String getMinecraftVersion();

    public abstract LoaderVersion getLoaderVersion();

    public abstract boolean supportsPlugins();

    public abstract boolean isForgeLikeAndHasInstalledSinytraConnector();

    public abstract List<DisableableMod> getMods();

    public abstract void addMod(DisableableMod mod);

    public abstract void addMods(List<DisableableMod> modsToAdd);

    public abstract void removeMod(DisableableMod mod);

    public abstract void addFileFromCurseForge(CurseForgeProject mod, CurseForgeFile file, ProgressDialog<Void> dialog);

    public abstract void addFileFromModrinth(ModrinthProject project, ModrinthVersion version, ModrinthFile file,
        ProgressDialog<Void> dialog);

    public abstract void scanMissingMods(Window parent);

    public abstract void setNotes(String notes);

    public abstract String getNotes();

    public abstract void setShouldWrapNotes(boolean shouldWrapNotes);

    public abstract boolean shouldWrapNotes();

    public abstract String getPackName();

    public abstract String getPlatformName();

    public abstract boolean isVanillaInstance();

    public abstract void save();

    public abstract List<Path> getLogPathsFromFilesystem(List<Path> logPaths);

    public abstract List<Path> getModPathsFromFilesystem(List<Path> modPaths);

    public abstract DisableableMod reinstallModFromModrinth(DisableableMod mod, ModrinthProject project,
        ModrinthVersion version,
        OkHttpClient progressClient);

    public abstract DisableableMod reinstallModFromCurseForge(DisableableMod mod, CurseForgeProject project,
        CurseForgeFile version, OkHttpClient progressClient);
}
