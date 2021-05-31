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
package com.atlauncher.gui.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.atlauncher.App;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Type;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeFingerprintedMod;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.FileTypeDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

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

            String[] modTypes = new String[] { "Mods Folder", "Inside Minecraft.jar", "Resource Pack", "Shader Pack" };

            FileTypeDialog fcd = new FileTypeDialog(GetText.tr("Add Mod"), GetText.tr("Adding Mods"), GetText.tr("Add"),
                    GetText.tr("Type"), modTypes);

            if (fcd.wasClosed()) {
                return false;
            }

            String typeTemp = fcd.getSelectorValue();

            if (typeTemp.equalsIgnoreCase("Inside Minecraft.jar")) {
                type = Type.jar;
                instanceFile = dialog.instance.getRoot().resolve("jarmods").toFile();
            } else if (typeTemp.equalsIgnoreCase("CoreMods Mod")) {
                type = Type.coremods;
                instanceFile = dialog.instance.getRoot().resolve("coremods").toFile();
            } else if (typeTemp.equalsIgnoreCase("Texture Pack")) {
                type = Type.texturepack;
                instanceFile = dialog.instance.getRoot().resolve("texturepacks").toFile();
            } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                type = Type.resourcepack;
                instanceFile = dialog.instance.getRoot().resolve("resourcepacks").toFile();
            } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                type = Type.shaderpack;
                instanceFile = dialog.instance.getRoot().resolve("shaderpacks").toFile();
            } else {
                type = Type.mods;
                instanceFile = dialog.instance.getRoot().resolve("mods").toFile();
            }

            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Copying Mods"), 0,
                    GetText.tr("Copying Mods"), dialog);

            progressDialog.addThread(new Thread(() -> {
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
                        copyTo = dialog.instance.getRoot().resolve("disabledmods").toFile();
                    }

                    DisableableMod mod = new DisableableMod();
                    mod.disabled = this.disabled;
                    mod.userAdded = true;
                    mod.wasSelected = true;
                    mod.file = file.getName();
                    mod.type = type;
                    mod.optional = true;
                    mod.name = file.getName();
                    mod.version = "Unknown";
                    mod.description = null;

                    MCMod mcMod = Utils.getMCModForFile(file);
                    if (mcMod != null) {
                        mod.name = Optional.ofNullable(mcMod.name).orElse(file.getName());
                        mod.version = Optional.ofNullable(mcMod.version).orElse("Unknown");
                        mod.description = Optional.ofNullable(mcMod.description).orElse(null);
                    } else {
                        FabricMod fabricMod = Utils.getFabricModForFile(file);
                        if (fabricMod != null) {
                            mod.name = Optional.ofNullable(fabricMod.name).orElse(file.getName());
                            mod.version = Optional.ofNullable(fabricMod.version).orElse("Unknown");
                            mod.description = Optional.ofNullable(fabricMod.description).orElse(null);
                        }
                    }

                    if (!App.settings.dontCheckModsOnCurseForge) {
                        try {
                            long murmurHash = Hashing.murmur(file.toPath());

                            LogManager.debug("File " + file.getName() + " has murmur hash of " + murmurHash);

                            CurseForgeFingerprint fingerprintResponse = CurseForgeApi.checkFingerprint(murmurHash);

                            if (fingerprintResponse.exactMatches.size() == 1) {
                                CurseForgeFingerprintedMod foundMod = fingerprintResponse.exactMatches.get(0);

                                // add CurseForge information
                                mod.curseForgeProject = CurseForgeApi.getProjectById(foundMod.id);
                                mod.curseForgeProjectId = foundMod.id;
                                mod.curseForgeFile = foundMod.file;
                                mod.curseForgeFileId = foundMod.file.id;

                                mod.name = mod.curseForgeProject.name;
                                mod.description = mod.curseForgeProject.summary;

                                LogManager
                                        .debug("Found matching mod from CurseForge called " + mod.curseForgeProject.name
                                                + " with file named " + mod.curseForgeFile.displayName);
                            }
                        } catch (IOException e1) {
                            LogManager.logStackTrace(e1);
                        }
                    }

                    if (!copyTo.exists()) {
                        copyTo.mkdirs();
                    }

                    if (Utils.copyFile(file, copyTo)) {
                        dialog.instance.launcher.mods.add(mod);
                    }
                }
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
