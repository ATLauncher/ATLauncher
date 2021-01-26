/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import com.atlauncher.App;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthMod;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.dialogs.CurseForgeProjectFileSelectorDialog;
import com.atlauncher.gui.dialogs.ModrinthVersionSelectorDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Utils;
import com.google.gson.annotations.SerializedName;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class DisableableMod implements Serializable {
    public String name;
    public String version;
    public boolean optional;
    public String file;
    public Type type;
    public Color colour;
    public String description;
    public boolean disabled;
    public boolean userAdded = false; // Default to not being user added
    public boolean wasSelected = true; // Default to it being selected on install

    @SerializedName(value = "curseForgeProjectId", alternate = { "curseModId" })
    public Integer curseForgeProjectId;

    @SerializedName(value = "curseForgeFileId", alternate = { "curseFileId" })
    public Integer curseForgeFileId;

    @SerializedName(value = "curseForgeProject", alternate = { "curseMod" })
    public CurseForgeProject curseForgeProject;

    @SerializedName(value = "curseForgeFile", alternate = { "curseFile" })
    public CurseForgeFile curseForgeFile;

    public ModrinthMod modrinthMod;
    public ModrinthVersion modrinthVersion;

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, Integer curseForgeModId,
            Integer curseForgeFileId, CurseForgeProject curseForgeProject, CurseForgeFile curseForgeFile,
            ModrinthMod modrinthMod, ModrinthVersion modrinthVersion) {
        this.name = name;
        this.version = version;
        this.optional = optional;
        this.file = file;
        this.type = type;
        this.colour = colour;
        this.description = description;
        this.disabled = disabled;
        this.userAdded = userAdded;
        this.wasSelected = wasSelected;
        this.curseForgeProjectId = curseForgeModId;
        this.curseForgeFileId = curseForgeFileId;
        this.curseForgeProject = curseForgeProject;
        this.curseForgeFile = curseForgeFile;
        this.modrinthMod = modrinthMod;
        this.modrinthVersion = modrinthVersion;
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, Integer curseForgeModId,
            Integer curseForgeFileId, CurseForgeProject curseForgeProject, CurseForgeFile curseForgeFile) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected,
                curseForgeModId, curseForgeFileId, curseForgeProject, curseForgeFile, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected,
            CurseForgeProject curseForgeProject, CurseForgeFile curseForgeFile) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected,
                curseForgeProject.id, curseForgeFile.id, curseForgeProject, curseForgeFile);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, ModrinthMod modrinthMod,
            ModrinthVersion modrinthVersion) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected, null, null,
                null, null, modrinthMod, modrinthVersion);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, Integer curseForgeModId,
            Integer curseForgeFileId) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected,
                curseForgeModId, curseForgeFileId, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, wasSelected, null, null,
                null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded) {
        this(name, version, optional, file, type, colour, description, disabled, userAdded, true, null, null, null,
                null);
    }

    public DisableableMod() {
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public boolean hasColour() {
        return this.colour != null;
    }

    public Color getColour() {
        return this.colour;
    }

    public String getDescription() {
        if (this.description == null) {
            return "";
        }
        return this.description;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean wasSelected() {
        return this.wasSelected;
    }

    public void setWasSelected(boolean wasSelected) {
        this.wasSelected = wasSelected;
    }

    public boolean isUserAdded() {
        return this.userAdded;
    }

    public boolean isUpdatable() {
        return this.isFromCurseForge() || this.isFromModrinth();
    }

    public boolean isFromCurseForge() {
        return this.curseForgeProjectId != null && this.curseForgeFileId != null;
    }

    public boolean hasFullCurseForgeInformation() {
        return this.curseForgeProject != null && this.curseForgeFile != null;
    }

    public Integer getCurseForgeModId() {
        return this.curseForgeProjectId;
    }

    public Integer getCurseForgeFileId() {
        return this.curseForgeFileId;
    }

    public boolean isFromModrinth() {
        return this.modrinthMod != null && this.modrinthVersion != null;
    }

    public String getFilename() {
        return this.file;
    }

    public boolean enable(Instance instance) {
        if (this.disabled) {
            if (!getFile(instance).getParentFile().exists()) {
                getFile(instance).getParentFile().mkdir();
            }
            if (Utils.moveFile(getDisabledFile(instance), getFile(instance), true)) {
                this.disabled = false;
            }
        }
        return false;
    }

    public boolean disable(Instance instance) {
        if (!this.disabled) {
            if (Utils.moveFile(getFile(instance), getDisabledFile(instance), true)) {
                this.disabled = true;
                return true;
            }
        }
        return false;
    }

    public boolean doesFileExist(Instance instance) {
        if (isDisabled()) {
            return getDisabledFile(instance).exists();
        }

        return getFile(instance).exists();
    }

    public File getDisabledFile(Instance instance) {
        return instance.getRoot().resolve("disabledmods/" + this.file).toFile();
    }

    public File getFile(Instance instance) {
        return getFile(instance.getRoot(), null);
    }

    public File getFile(Instance instance, Path base) {
        return getFile(base, null);
    }

    public File getFile(Path base) {
        return getFile(base, null);
    }

    public File getFile(Path base, String mcVersion) {
        File dir = null;
        switch (type) {
            case jar:
            case forge:
            case mcpc:
                dir = base.resolve("jarmods").toFile();
                break;
            case texturepack:
                dir = base.resolve("texturepacks").toFile();
                break;
            case resourcepack:
                dir = base.resolve("resourcepacks").toFile();
                break;
            case mods:
                dir = base.resolve("mods").toFile();
                break;
            case ic2lib:
                dir = base.resolve("mods/ic2").toFile();
                break;
            case denlib:
                dir = base.resolve("mods/denlib").toFile();
                break;
            case coremods:
                dir = base.resolve("coremods").toFile();
                break;
            case shaderpack:
                dir = base.resolve("shaderpacks").toFile();
                break;
            case dependency:
                if (mcVersion != null) {
                    dir = base.resolve("mods/" + mcVersion).toFile();
                }
                break;
            default:
                LogManager.warn("Unsupported mod for enabling/disabling " + this.name);
                break;
        }
        if (dir == null) {
            return null;
        }
        return new File(dir, file);
    }

    public Type getType() {
        return this.type;
    }

    public boolean checkForUpdate(Window parent, Instance instance) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "UpdateMods", "Instance");

        if (isFromCurseForge()) {
            List<CurseForgeFile> curseForgeFiles = CurseForgeApi.getFilesForProject(curseForgeProjectId);

            Stream<CurseForgeFile> curseForgeFilesStream = curseForgeFiles.stream()
                    .sorted(Comparator.comparingInt((CurseForgeFile file) -> file.id).reversed());

            if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                curseForgeFilesStream = curseForgeFilesStream.filter(file -> file.gameVersion.contains(instance.id));
            }

            if (curseForgeFilesStream.noneMatch(file -> file.id > curseForgeFileId)) {
                return false;
            }

            ProgressDialog<CurseForgeProject> dialog = new ProgressDialog<>(
                    GetText.tr("Checking For Update On CurseForge"), 0, GetText.tr("Checking For Update On CurseForge"),
                    "Cancelled checking for update on CurseForge");
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(CurseForgeApi.getProjectById(curseForgeProjectId));
                dialog.close();
            }));
            dialog.start();

            new CurseForgeProjectFileSelectorDialog(parent, dialog.getReturnValue(), instance, curseForgeFileId);
        } else if (isFromModrinth()) {
            ProgressDialog<List<Object>> dialog = new ProgressDialog<>(GetText.tr("Checking For Update On Modrinth"), 0,
                    GetText.tr("Checking For Update On Modrinth"), "Cancelled checking for update on Modrinth");
            dialog.addThread(new Thread(() -> {
                ModrinthMod mod = ModrinthApi.getMod(modrinthMod.id);
                List<ModrinthVersion> versions = ModrinthApi.getVersions(mod.versions);

                Stream<ModrinthVersion> versionsStream = versions.stream()
                        .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed());

                if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                    versionsStream = versionsStream.filter(
                            v -> v.gameVersions.contains(instance.id));
                }

                if (versionsStream.noneMatch(v -> Date.from(Instant.parse(v.datePublished))
                        .after(Date.from(Instant.parse(modrinthVersion.datePublished))))) {
                    dialog.setReturnValue(null);
                    dialog.close();
                    return;
                }

                List<Object> returns = new ArrayList<>();
                returns.add(mod);
                returns.add(versions);

                dialog.setReturnValue(returns);
                dialog.close();
            }));
            dialog.start();

            if (dialog.getReturnValue() == null) {
                return false;
            }

            List<Object> returns = dialog.getReturnValue();

            new ModrinthVersionSelectorDialog(parent, (ModrinthMod) returns.get(0),
                    (List<ModrinthVersion>) returns.get(1), instance, modrinthVersion.id);
        }

        return true;
    }

    public boolean reinstall(Window parent, Instance instance) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "ReinstallMods", "Instance");

        if (isFromCurseForge()) {
            ProgressDialog<CurseForgeProject> dialog = new ProgressDialog<>(GetText.tr("Getting Files From CurseForge"),
                    0, GetText.tr("Getting Files From CurseForge"), "Cancelled getting files from CurseForge");
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(CurseForgeApi.getProjectById(curseForgeProjectId));
                dialog.close();
            }));
            dialog.start();

            new CurseForgeProjectFileSelectorDialog(parent, dialog.getReturnValue(), instance, curseForgeFileId);
        } else if (isFromModrinth()) {
            ProgressDialog<ModrinthMod> dialog = new ProgressDialog<>(GetText.tr("Getting Files From Modrinth"), 0,
                    GetText.tr("Getting Files From Modrinth"), "Cancelled getting files from Modrinth");
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(ModrinthApi.getMod(modrinthMod.id));
                dialog.close();
            }));
            dialog.start();

            new ModrinthVersionSelectorDialog(parent, dialog.getReturnValue(), instance, modrinthVersion.id);
        }

        return true;
    }
}
