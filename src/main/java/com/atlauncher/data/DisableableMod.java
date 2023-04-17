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

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.format.ISODateTimeFormat;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.CurseForgeProjectFileSelectorDialog;
import com.atlauncher.gui.dialogs.ModrinthVersionSelectorDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.InternalModMetadataUtils;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.moandjiezana.toml.Toml;

@SuppressWarnings("serial")
public class DisableableMod implements Serializable {
    public String name;
    public String version;
    public boolean optional;
    public String file;
    public String path;
    public Type type;
    public Color colour;
    public String description;
    public boolean disabled;
    public boolean userAdded = false; // Default to not being user added
    public boolean wasSelected = true; // Default to it being selected on install
    public boolean skipped = false; // For browser download mods if they were skipped or not

    public Map<String, String> internalModMetadata = new HashMap<>();

    @SerializedName(value = "curseForgeProjectId", alternate = { "curseModId" })
    public Integer curseForgeProjectId;

    @SerializedName(value = "curseForgeFileId", alternate = { "curseFileId" })
    public Integer curseForgeFileId;

    @SerializedName(value = "curseForgeProject", alternate = { "curseMod" })
    public CurseForgeProject curseForgeProject;

    @SerializedName(value = "curseForgeFile", alternate = { "curseFile" })
    public CurseForgeFile curseForgeFile;

    @SerializedName(value = "modrinthProject", alternate = { "modrinthMod" })
    public ModrinthProject modrinthProject;
    public ModrinthVersion modrinthVersion;

    public DisableableMod(String name, String version, boolean optional, String file, String path, Type type,
            Color colour, String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            Integer curseForgeModId, Integer curseForgeFileId, CurseForgeProject curseForgeProject,
            CurseForgeFile curseForgeFile, ModrinthProject modrinthProject, ModrinthVersion modrinthVersion) {
        this.name = name;
        this.version = version;
        this.optional = optional;
        this.file = file;
        this.path = path;
        this.type = type;
        this.colour = colour;
        this.description = description;
        this.disabled = disabled;
        this.userAdded = userAdded;
        this.wasSelected = wasSelected;
        this.skipped = skipped;
        this.curseForgeProjectId = curseForgeModId;
        this.curseForgeFileId = curseForgeFileId;
        this.curseForgeProject = curseForgeProject;
        this.curseForgeFile = curseForgeFile;
        this.modrinthProject = modrinthProject;
        this.modrinthVersion = modrinthVersion;
    }

    public DisableableMod(String name, String version, boolean optional, String file, String path, Type type,
            Color colour, String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            Integer curseForgeModId, Integer curseForgeFileId, CurseForgeProject curseForgeProject,
            CurseForgeFile curseForgeFile) {
        this(name, version, optional, file, path, type, colour, description, disabled, userAdded, wasSelected, skipped,
                curseForgeModId, curseForgeFileId, curseForgeProject, curseForgeFile, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            Integer curseForgeModId,
            Integer curseForgeFileId, CurseForgeProject curseForgeProject, CurseForgeFile curseForgeFile) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, wasSelected, skipped,
                curseForgeModId, curseForgeFileId, curseForgeProject, curseForgeFile, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            CurseForgeProject curseForgeProject, CurseForgeFile curseForgeFile) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, wasSelected, skipped,
                curseForgeProject.id, curseForgeFile.id, curseForgeProject, curseForgeFile);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            ModrinthProject modrinthProject,
            ModrinthVersion modrinthVersion) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, wasSelected, skipped,
                null,
                null, null, null, modrinthProject, modrinthVersion);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped,
            Integer curseForgeModId,
            Integer curseForgeFileId) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, wasSelected, skipped,
                curseForgeModId, curseForgeFileId, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded, boolean wasSelected, boolean skipped) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, wasSelected, skipped,
                null,
                null, null, null);
    }

    public DisableableMod(String name, String version, boolean optional, String file, Type type, Color colour,
            String description, boolean disabled, boolean userAdded) {
        this(name, version, optional, file, null, type, colour, description, disabled, userAdded, true, false, null,
                null,
                null, null);
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
        return this.modrinthProject != null && this.modrinthVersion != null;
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
        return getFile(instance.getRoot(), instance.id);
    }

    public File getFile(Instance instance, Path base) {
        return getFile(base, instance.id);
    }

    public File getFile(Path base) {
        return getFile(base, null);
    }

    public File getFile(Path base, String mcVersion) {
        File dir = null;
        if (path != null) {
            dir = base.resolve(path).toFile();
        } else {
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
        }
        if (dir == null) {
            LogManager.warn("null path returned for mod " + this.name);
            return null;
        }
        return new File(dir, file);
    }

    public Type getType() {
        return this.type;
    }

    public boolean checkForUpdate(Window parent, Instance instance) {
        return checkForUpdate(parent, instance, null);
    }

    public boolean checkForUpdate(Window parent, Instance instance, ModPlatform platform) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "UpdateMods", "Instance");

        if (platform == ModPlatform.CURSEFORGE || (platform == null && isFromCurseForge()
                && (!isFromModrinth() || App.settings.defaultModPlatform == ModPlatform.CURSEFORGE))) {
            ProgressDialog<Object> dialog = new ProgressDialog<>(
                    // #. {0} is the platform were checking for updates (e.g. CurseForge/Modrinth)
                    GetText.tr("Checking For Update On {0}", "CurseForge"), 0,
                    // #. {0} is the platform were checking for updates (e.g. CurseForge/Modrinth)
                    GetText.tr("Checking For Update On {0}", "CurseForge"),
                    "Cancelled checking for update on CurseForge", parent);
            dialog.addThread(new Thread(() -> {
                List<CurseForgeFile> curseForgeFiles = CurseForgeApi.getFilesForProject(curseForgeProjectId);

                if (curseForgeFiles == null) {
                    dialog.setReturnValue(false);
                    dialog.close();
                    return;
                }

                Stream<CurseForgeFile> curseForgeFilesStream = curseForgeFiles.stream()
                        .sorted(Comparator.comparingInt((CurseForgeFile file) -> file.id).reversed());

                if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                    curseForgeFilesStream = curseForgeFilesStream
                            .filter(file -> file.gameVersions.contains(instance.id));
                }

                if (App.settings.addModRestriction == AddModRestriction.LAX) {
                    try {
                        List<String> minecraftVersionsToSearch = MinecraftManager.getMajorMinecraftVersions(instance.id)
                                .stream().map(mv -> mv.id).collect(Collectors.toList());

                        curseForgeFilesStream = curseForgeFilesStream.filter(
                                file -> file.gameVersions.stream()
                                        .anyMatch(gv -> minecraftVersionsToSearch.contains(gv)));
                    } catch (InvalidMinecraftVersion e) {
                        LogManager.logStackTrace(e);
                    }
                }

                // filter out files not for our loader
                curseForgeFilesStream = curseForgeFilesStream.filter(cf -> {
                    if (cf.gameVersions.contains("Fabric") && instance.launcher.loaderVersion != null
                            && (instance.launcher.loaderVersion.isFabric()
                                    || instance.launcher.loaderVersion.isLegacyFabric()
                                    || instance.launcher.loaderVersion.isQuilt())) {
                        return true;
                    }

                    if (cf.gameVersions.contains("Forge") && instance.launcher.loaderVersion != null
                            && instance.launcher.loaderVersion.isForge()) {
                        return true;
                    }

                    if (cf.gameVersions.contains("Quilt") && instance.launcher.loaderVersion != null
                            && instance.launcher.loaderVersion.isQuilt()) {
                        return true;
                    }

                    // if there's no loaders, assume the mod is untagged so we should show it
                    if (!cf.gameVersions.contains("Fabric") && !cf.gameVersions.contains("Forge")
                            && !cf.gameVersions.contains("Quilt")) {
                        return true;
                    }

                    return false;
                });

                if (curseForgeFilesStream.noneMatch(file -> file.id > curseForgeFileId)) {
                    dialog.setReturnValue(false);
                    dialog.close();
                    return;
                }

                dialog.setReturnValue(CurseForgeApi.getProjectById(curseForgeProjectId));
                dialog.close();
            }));
            dialog.start();

            if (dialog.getReturnValue() instanceof Boolean) {
                return ((Boolean) dialog.getReturnValue()) == true;
            }

            if (dialog.getReturnValue() == null) {
                return false;
            }

            new CurseForgeProjectFileSelectorDialog(parent, (CurseForgeProject) dialog.getReturnValue(), instance,
                    curseForgeFileId);
        } else if (platform == ModPlatform.MODRINTH || platform == null && isFromModrinth()
                && (!isFromCurseForge() || App.settings.defaultModPlatform == ModPlatform.MODRINTH)) {
            ProgressDialog<Pair<ModrinthProject, List<ModrinthVersion>>> dialog = new ProgressDialog<>(
                    // #. {0} is the platform were checking for updates (e.g. CurseForge/Modrinth)
                    GetText.tr("Checking For Update On {0}", "Modrinth"), 0,
                    // #. {0} is the platform were checking for updates (e.g. CurseForge/Modrinth)
                    GetText.tr("Checking For Update On {0}", "Modrinth"), "Cancelled checking for update on Modrinth",
                    parent);
            dialog.addThread(new Thread(() -> {
                ModrinthProject mod = ModrinthApi.getProject(modrinthProject.id);
                List<ModrinthVersion> versions = ModrinthApi.getVersions(modrinthProject.id, instance.id,
                        instance.launcher.loaderVersion);

                if (versions == null) {
                    dialog.setReturnValue(null);
                    dialog.close();
                    return;
                }

                Stream<ModrinthVersion> versionsStream = versions.stream()
                        .sorted(Comparator.comparing((ModrinthVersion version) -> version.datePublished).reversed());

                if (App.settings.addModRestriction == AddModRestriction.STRICT) {
                    versionsStream = versionsStream.filter(v -> v.gameVersions.contains(instance.id));
                }

                if (versionsStream.noneMatch(v -> ISODateTimeFormat.dateTimeParser().parseDateTime(v.datePublished)
                        .minusSeconds(1)
                        .isAfter(ISODateTimeFormat.dateTimeParser().parseDateTime(modrinthVersion.datePublished)))) {
                    dialog.setReturnValue(null);
                    dialog.close();
                    return;
                }

                dialog.setReturnValue(new Pair<ModrinthProject, List<ModrinthVersion>>(mod, versions));
                dialog.close();
            }));
            dialog.start();

            if (dialog.getReturnValue() == null) {
                return false;
            }

            Pair<ModrinthProject, List<ModrinthVersion>> pair = dialog.getReturnValue();

            new ModrinthVersionSelectorDialog(parent, pair.left(), pair.right(), instance, modrinthVersion.id);
        }

        return true;
    }

    public boolean reinstall(Window parent, Instance instance) {
        return reinstall(parent, instance, null);
    }

    public boolean reinstall(Window parent, Instance instance, ModPlatform platform) {
        Analytics.sendEvent(instance.launcher.pack + " - " + instance.launcher.version, "ReinstallMods", "Instance");

        if (platform == ModPlatform.CURSEFORGE || (platform == null && isFromCurseForge()
                && (!isFromModrinth() || App.settings.defaultModPlatform == ModPlatform.CURSEFORGE))) {
            ProgressDialog<CurseForgeProject> dialog = new ProgressDialog<>(
                    // #. {0} is the platform were getting files from (e.g. CurseForge/Modrinth)
                    GetText.tr("Getting Files From {0}", "CurseForge"),
                    // #. {0} is the platform were getting files from (e.g. CurseForge/Modrinth)
                    0, GetText.tr("Getting Files From {0}", "CurseForge"), "Cancelled getting files from CurseForge",
                    parent);
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(CurseForgeApi.getProjectById(curseForgeProjectId));
                dialog.close();
            }));
            dialog.start();

            new CurseForgeProjectFileSelectorDialog(parent, dialog.getReturnValue(), instance, curseForgeFileId, false);
        } else if (platform == ModPlatform.MODRINTH || (platform == null && isFromModrinth()
                && (!isFromCurseForge() || App.settings.defaultModPlatform == ModPlatform.MODRINTH))) {
            ProgressDialog<ModrinthProject> dialog = new ProgressDialog<>(
                    // #. {0} is the platform were getting files from (e.g. CurseForge/Modrinth)
                    GetText.tr("Getting Files From {0}", "Modrinth"), 0,
                    // #. {0} is the platform were getting files from (e.g. CurseForge/Modrinth)
                    GetText.tr("Getting Files From {0}", "Modrinth"), "Cancelled getting files from Modrinth", parent);
            dialog.addThread(new Thread(() -> {
                dialog.setReturnValue(ModrinthApi.getProject(modrinthProject.id));
                dialog.close();
            }));
            dialog.start();

            new ModrinthVersionSelectorDialog(parent, dialog.getReturnValue(), instance, modrinthVersion.id, false);
        }

        return true;
    }

    public static DisableableMod generateMod(File file, com.atlauncher.data.Type type, boolean enabled) {
        DisableableMod mod = new DisableableMod();
        mod.disabled = !enabled;
        mod.userAdded = true;
        mod.wasSelected = true;
        mod.file = file.getName();
        mod.type = type;
        mod.optional = true;
        mod.name = file.getName();
        mod.version = "Unknown";
        mod.description = null;

        return mod;
    }

    private JsonObject getMcModInfoFile(Path path) {
        String mcModInfoString;

        if (internalModMetadata.containsKey("mcMod")) {
            mcModInfoString = internalModMetadata.get("mcMod");
        } else {
            mcModInfoString = InternalModMetadataUtils.getRawInternalModMetadata(path.toFile(), "mcmod.info");
            internalModMetadata.put("mcMod", mcModInfoString);
        }

        return InternalModMetadataUtils.parseMcModInfoFile(mcModInfoString);
    }

    private JsonObject getFabricModFile(Path path) {
        String fabricModString;

        if (internalModMetadata.containsKey("fabricMod")) {
            fabricModString = internalModMetadata.get("fabricMod");
        } else {
            fabricModString = InternalModMetadataUtils.getRawInternalModMetadata(path.toFile(), "fabric.mod.json");
            internalModMetadata.put("fabricMod", fabricModString);
        }

        return InternalModMetadataUtils.parseFabricModFile(fabricModString);
    }

    private JsonObject getQuiltModFile(Path path) {
        String quiltModString;

        if (internalModMetadata.containsKey("quiltMod")) {
            quiltModString = internalModMetadata.get("quiltMod");
        } else {
            quiltModString = InternalModMetadataUtils.getRawInternalModMetadata(path.toFile(), "quilt.mod.json");
            internalModMetadata.put("quiltMod", quiltModString);
        }

        return InternalModMetadataUtils.parseQuiltModFile(quiltModString);
    }

    private Toml getModsTomlFile(Path path) {
        String modsTomlString;

        if (internalModMetadata.containsKey("modsToml")) {
            modsTomlString = internalModMetadata.get("modsToml");
        } else {
            modsTomlString = InternalModMetadataUtils.getRawInternalModMetadata(path.toFile(), "META-INF/mods.toml");
            internalModMetadata.put("modsToml", modsTomlString);
        }

        return InternalModMetadataUtils.parseModsTomlFile(modsTomlString);
    }

    private Properties getManifestMfFile(Path path) {
        String manifestMfString;

        if (internalModMetadata.containsKey("manifestMf")) {
            manifestMfString = internalModMetadata.get("manifestMf");
        } else {
            manifestMfString = InternalModMetadataUtils.getRawInternalModMetadata(path.toFile(),
                    "META-INF/MANIFEST.MF");
            internalModMetadata.put("manifestMf", manifestMfString);
        }

        return InternalModMetadataUtils.parseManifestMfFile(manifestMfString);
    }

    public void scanInternalModMetadata(Path path) {
        PerformanceManager.start(String.format("scanInternalModMetadata::%s", path.getFileName().toString()));
        getMcModInfoFile(path);
        getFabricModFile(path);
        getQuiltModFile(path);
        getModsTomlFile(path);
        getManifestMfFile(path);
        PerformanceManager.end(String.format("scanInternalModMetadata::%s", path.getFileName().toString()));
    }

    public String getNameFromFile(Path path) {
        return getNameFromFile(null, path);
    }

    public String getNameFromFile(Instance instance, Path path) {
        if (instance != null && instance.getLoaderVersion() != null) {
            if (instance.getLoaderVersion().isFabric() || instance.getLoaderVersion().isLegacyFabric()) {
                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("name") ? fabricMod.get("name").getAsString() : name;
                }
            } else if (instance.getLoaderVersion().isQuilt()) {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("name") ? quiltMod.get("name").getAsString() : name;
                }

                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("name") ? fabricMod.get("name").getAsString() : name;
                }
            } else if (instance.getLoaderVersion().isForge()) {
                JsonObject mcMod = getMcModInfoFile(path);
                if (mcMod != null) {
                    return mcMod.has("name") ? mcMod.get("name").getAsString() : name;
                }

                Toml modsToml = getModsTomlFile(path);
                if (modsToml != null) {
                    return modsToml.contains("mods[0].displayName") ? modsToml.getString("mods[0].displayName")
                            : name;
                }
            }
        }

        JsonObject mcMod = getMcModInfoFile(path);
        if (mcMod != null) {
            return mcMod.has("name") ? mcMod.get("name").getAsString() : name;
        } else {
            JsonObject fabricMod = getFabricModFile(path);
            if (fabricMod != null) {
                return fabricMod.has("name") ? fabricMod.get("name").getAsString() : name;
            } else {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("name") ? quiltMod.get("name").getAsString() : name;
                } else {
                    Toml modsToml = getModsTomlFile(path);
                    if (modsToml != null) {
                        return modsToml.contains("mods[0].displayName") ? modsToml.getString("mods[0].displayName")
                                : name;
                    }
                }
            }
        }

        return name;
    }

    public String getVersionFromFile(Path path) {
        return getVersionFromFile(null, path);
    }

    public String getVersionFromFile(Instance instance, Path path) {
        if (instance != null && instance.getLoaderVersion() != null) {
            if (instance.getLoaderVersion().isFabric() || instance.getLoaderVersion().isLegacyFabric()) {
                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("version") ? fabricMod.get("version").getAsString() : version;
                }
            } else if (instance.getLoaderVersion().isQuilt()) {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("version") ? quiltMod.get("version").getAsString() : version;
                }

                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("version") ? fabricMod.get("version").getAsString() : version;
                }
            } else if (instance.getLoaderVersion().isForge()) {
                JsonObject mcMod = getMcModInfoFile(path);
                if (mcMod != null) {
                    return mcMod.has("version") ? mcMod.get("version").getAsString() : version;
                }

                Toml modsToml = getModsTomlFile(path);
                if (modsToml != null) {
                    String parsedVersion = modsToml.contains("mods[0].version")
                            ? modsToml.getString("mods[0].version")
                            : version;

                    if (parsedVersion.equals("${file.jarVersion}")) {
                        Properties manifestMf = getManifestMfFile(path);
                        parsedVersion = manifestMf.getProperty("Implementation-Version");
                    }

                    return parsedVersion;
                }
            }
        }

        JsonObject mcMod = getMcModInfoFile(path);
        if (mcMod != null) {
            return mcMod.has("version") ? mcMod.get("version").getAsString() : version;
        } else {
            JsonObject fabricMod = getFabricModFile(path);
            if (fabricMod != null) {
                return fabricMod.has("version") ? fabricMod.get("version").getAsString() : version;
            } else {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("version") ? quiltMod.get("version").getAsString() : version;
                } else {
                    Toml modsToml = getModsTomlFile(path);
                    if (modsToml != null) {
                        String parsedVersion = modsToml.contains("mods[0].version")
                                ? modsToml.getString("mods[0].version")
                                : version;

                        if (parsedVersion.equals("${file.jarVersion}")) {
                            Properties manifestMf = getManifestMfFile(path);
                            parsedVersion = manifestMf.getProperty("Implementation-Version");
                        }

                        return parsedVersion;
                    }
                }
            }
        }

        return version;
    }

    public String getDescriptionFromFile(Path path) {
        return getDescriptionFromFile(null, path);
    }

    public String getDescriptionFromFile(Instance instance, Path path) {
        if (instance != null && instance.getLoaderVersion() != null) {
            if (instance.getLoaderVersion().isFabric() || instance.getLoaderVersion().isLegacyFabric()) {
                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("description") ? fabricMod.get("description").getAsString() : description;
                }
            } else if (instance.getLoaderVersion().isQuilt()) {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("description") ? quiltMod.get("description").getAsString() : description;
                }

                JsonObject fabricMod = getFabricModFile(path);
                if (fabricMod != null) {
                    return fabricMod.has("description") ? fabricMod.get("description").getAsString() : description;
                }
            } else if (instance.getLoaderVersion().isForge()) {
                JsonObject mcMod = getMcModInfoFile(path);
                if (mcMod != null) {
                    return mcMod.has("description") ? mcMod.get("description").getAsString() : description;
                }

                Toml modsToml = getModsTomlFile(path);
                if (modsToml != null) {
                    return modsToml.contains("mods[0].description") ? modsToml.getString("mods[0].description")
                            : description;
                }
            }
        }

        JsonObject mcMod = getMcModInfoFile(path);
        if (mcMod != null) {
            return mcMod.has("description") ? mcMod.get("description").getAsString() : description;
        } else {
            JsonObject fabricMod = getFabricModFile(path);
            if (fabricMod != null) {
                return fabricMod.has("description") ? fabricMod.get("description").getAsString() : description;
            } else {
                JsonObject quiltMod = getQuiltModFile(path);
                if (quiltMod != null) {
                    return quiltMod.has("description") ? quiltMod.get("description").getAsString() : description;
                } else {
                    Toml modsToml = getModsTomlFile(path);
                    if (modsToml != null) {
                        return modsToml.contains("mods[0].description") ? modsToml.getString("mods[0].description")
                                : description;
                    }
                }
            }
        }

        return description;
    }
}
