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
package com.atlauncher.gui.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.Type;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.FileTypeDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.Utils;

@SuppressWarnings("serial")
public class ModsJCheckBoxTransferHandler extends TransferHandler {
    private final EditModsDialog dialog;
    private final boolean disabled;

    public ModsJCheckBoxTransferHandler(EditModsDialog dialog, boolean disabled) {
        this.dialog = dialog;
        this.disabled = disabled;
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public boolean canImport(TransferSupport ts) {
        return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    public boolean importData(TransferSupport ts) {
        try {
            @SuppressWarnings("unchecked")
            final List<File> data = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (data.size() < 1) {
                return false;
            }

            Type type;
            File instanceFile;

            String[] modTypes = new String[] { "Mods Folder", "Resource Pack", "Shader Pack", "Inside Minecraft.jar" };

            FileTypeDialog fcd = new FileTypeDialog(GetText.tr("Add Mod"), GetText.tr("Adding Mods"), GetText.tr("Add"),
                    GetText.tr("Type"), modTypes);

            if (fcd.wasClosed()) {
                return false;
            }

            String typeTemp = fcd.getSelectorValue();

            if (typeTemp.equalsIgnoreCase("Inside Minecraft.jar")) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Add As Mod?"))
                        .setContent(new HTMLBuilder().text(GetText.tr(
                                "Adding as Inside Minecraft.jar is usually not what you want and will likely cause issues.<br/><br/>If you're adding mods this is usually not correct. Do you want to add this as a mod instead?"))
                                .build())
                        .setType(DialogManager.WARNING).show();

                if (ret != 0) {
                    type = Type.jar;
                    instanceFile = dialog.instance.ROOT.resolve("jarmods").toFile();
                } else {
                    type = Type.mods;
                    instanceFile = dialog.instance.ROOT.resolve("mods").toFile();
                }
            } else if (typeTemp.equalsIgnoreCase("CoreMods Mod")) {
                type = Type.coremods;
                instanceFile = dialog.instance.ROOT.resolve("coremods").toFile();
            } else if (typeTemp.equalsIgnoreCase("Texture Pack")) {
                type = Type.texturepack;
                instanceFile = dialog.instance.ROOT.resolve("texturepacks").toFile();
            } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                type = Type.resourcepack;
                instanceFile = dialog.instance.ROOT.resolve("resourcepacks").toFile();
            } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                type = Type.shaderpack;
                instanceFile = dialog.instance.ROOT.resolve("shaderpacks").toFile();
            } else {
                type = Type.mods;
                instanceFile = dialog.instance.ROOT.resolve("mods").toFile();
            }

            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Copying Mods"), 0,
                    GetText.tr("Copying Mods"), dialog);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> modsAdded = new ArrayList<>();

                for (Object item : data) {
                    File file = (File) item;
                    File copyTo = instanceFile;

                    if (!Utils.isAcceptedModFile(file)) {
                        DialogManager.okDialog().setTitle(GetText.tr("Invalid File")).setContent(GetText
                                .tr("Skipping file {0}. Only zip, jar and litemod files can be added.", file.getName()))
                                .setType(DialogManager.ERROR).show();
                        continue;
                    }

                    if (this.disabled) {
                        copyTo = dialog.instance.ROOT.resolve("disabledmods").toFile();
                    }

                    DisableableMod mod = new DisableableMod();
                    mod.disabled = this.disabled;
                    mod.userAdded = true;
                    mod.wasSelected = true;
                    mod.file = file.getName();
                    mod.type = type;
                    mod.optional = true;
                    mod.name = Optional.ofNullable(mod.getNameFromFile(file.toPath())).orElse(file.getName());
                    mod.version = Optional.ofNullable(mod.getVersionFromFile(file.toPath())).orElse("Unknown");
                    mod.description = Optional.ofNullable(mod.getDescriptionFromFile(file.toPath())).orElse(null);

                    if (!copyTo.exists()) {
                        copyTo.mkdirs();
                    }

                    if (Utils.copyFile(file, copyTo)) {
                        modsAdded.add(mod);
                    }
                }

                if (!App.settings.dontCheckModsOnCurseForge) {
                    Map<Long, DisableableMod> murmurHashes = new HashMap<>();

                    modsAdded.stream()
                            .filter(dm -> dm.curseForgeProject == null && dm.curseForgeFile == null)
                            .filter(dm -> dm.getFile(dialog.instance.ROOT, dialog.instance.id) != null).forEach(dm -> {
                                try {
                                    long hash = Hashing
                                            .murmur(dm.getFile(dialog.instance.ROOT, dialog.instance.id).toPath());
                                    murmurHashes.put(hash, dm);
                                } catch (Throwable t) {
                                    LogManager.logStackTrace(t);
                                }
                            });

                    if (murmurHashes.size() != 0) {
                        CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                                .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

                        if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                            int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                                    .toArray();

                            if (projectIdsFound.length != 0) {
                                Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                                        .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    fingerprintResponse.exactMatches.stream()
                                            .filter(em -> em != null && em.file != null
                                                    && murmurHashes.containsKey(em.file.packageFingerprint))
                                            .forEach(foundMod -> {
                                                DisableableMod dm = murmurHashes
                                                        .get(foundMod.file.packageFingerprint);

                                                // add CurseForge information
                                                dm.curseForgeProjectId = foundMod.id;
                                                dm.curseForgeFile = foundMod.file;
                                                dm.curseForgeFileId = foundMod.file.id;

                                                CurseForgeProject curseForgeProject = foundProjects
                                                        .get(foundMod.id);

                                                if (curseForgeProject != null) {
                                                    dm.curseForgeProject = curseForgeProject;
                                                    dm.name = curseForgeProject.name;
                                                    dm.description = curseForgeProject.summary;
                                                }

                                                LogManager.debug("Found matching mod from CurseForge called "
                                                        + dm.curseForgeFile.displayName);
                                            });
                                }
                            }
                        }
                    }
                }

                if (!App.settings.dontCheckModsOnModrinth) {
                    Map<String, DisableableMod> sha1Hashes = new HashMap<>();

                    modsAdded.stream()
                            .filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
                            .filter(dm -> dm.getFile(dialog.instance.ROOT, dialog.instance.id) != null).forEach(dm -> {
                                try {
                                    sha1Hashes.put(Hashing
                                            .sha1(dm.disabled ? dm.getDisabledFile(dialog.instance).toPath()
                                                    : dm
                                                            .getFile(dialog.instance.ROOT, dialog.instance.id).toPath())
                                            .toString(), dm);
                                } catch (Throwable t) {
                                    LogManager.logStackTrace(t);
                                }
                            });

                    if (sha1Hashes.size() != 0) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                                .getVersionsFromSha1Hashes(keys.toArray(new String[keys.size()]));

                        if (modrinthVersions != null && modrinthVersions.size() != 0) {
                            String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                    .toArray(String[]::new);

                            if (projectIdsFound.length != 0) {
                                Map<String, ModrinthProject> foundProjects = ModrinthApi
                                        .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                        ModrinthVersion version = entry.getValue();
                                        ModrinthProject project = foundProjects.get(version.projectId);

                                        if (project != null) {
                                            DisableableMod dm = sha1Hashes.get(entry.getKey());

                                            // add Modrinth information
                                            dm.modrinthProject = project;
                                            dm.modrinthVersion = version;

                                            if (!dm.isFromCurseForge()
                                                    || App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                                                dm.name = project.title;
                                                dm.description = project.description;
                                            }

                                            LogManager
                                                    .debug(String.format(
                                                            "Found matching mod from Modrinth called %s with file %s",
                                                            project.title, version.name));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                dialog.instance.launcher.mods.addAll(modsAdded);

                progressDialog.close();
            }));
            progressDialog.start();

            dialog.reloadPanels();
            return true;

        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }
    }
}
