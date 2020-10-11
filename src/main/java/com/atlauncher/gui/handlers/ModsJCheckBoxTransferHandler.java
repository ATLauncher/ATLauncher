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
package com.atlauncher.gui.handlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Type;
import com.atlauncher.data.curse.CurseFingerprint;
import com.atlauncher.data.curse.CurseFingerprintedMod;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.FileTypeDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.utils.CurseApi;
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

            boolean usesCoreMods = false;
            try {
                usesCoreMods = MinecraftManager.getMinecraftVersion(dialog.instanceV2 != null ? dialog.instanceV2.id
                        : dialog.instance.getMinecraftVersion()).coremods;
            } catch (InvalidMinecraftVersion e1) {
                LogManager.logStackTrace(e1);
            }
            String[] modTypes;
            if (usesCoreMods) {
                modTypes = new String[] { "Mods Folder", "Inside Minecraft.jar", "CoreMods Mod", "Texture Pack",
                        "Shader Pack" };
            } else {
                modTypes = new String[] { "Mods Folder", "Inside Minecraft.jar", "Resource Pack", "Shader Pack" };
            }

            FileTypeDialog fcd = new FileTypeDialog(GetText.tr("Add Mod"), GetText.tr("Adding Mods"), GetText.tr("Add"),
                    GetText.tr("Type"), modTypes);

            if (fcd.wasClosed()) {
                return false;
            }

            String typeTemp = fcd.getSelectorValue();

            if (typeTemp.equalsIgnoreCase("Inside Minecraft.jar")) {
                type = Type.jar;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("jarmods").toFile()
                        : dialog.instance.getJarModsDirectory();
            } else if (typeTemp.equalsIgnoreCase("CoreMods Mod")) {
                type = Type.coremods;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("coremods").toFile()
                        : dialog.instance.getCoreModsDirectory();
            } else if (typeTemp.equalsIgnoreCase("Texture Pack")) {
                type = Type.texturepack;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("texturepacks").toFile()
                        : dialog.instance.getTexturePacksDirectory();
            } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                type = Type.resourcepack;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("resourcepacks").toFile()
                        : dialog.instance.getResourcePacksDirectory();
            } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                type = Type.shaderpack;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("shaderpacks").toFile()
                        : dialog.instance.getShaderPacksDirectory();
            } else {
                type = Type.mods;
                instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("mods").toFile()
                        : dialog.instance.getModsDirectory();
            }

            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Copying Mods"), 0,
                    GetText.tr("Copying Mods"));

            progressDialog.addThread(new Thread(() -> {
                for (Object item : data) {
                    File file = (File) item;
                    File copyTo = instanceFile;

                    if (!file.getName().endsWith(".jar") && !file.getName().endsWith(".litemod")
                            && !file.getName().endsWith(".zip")) {
                        DialogManager.okDialog().setTitle(GetText.tr("Invalid File")).setContent(GetText
                                .tr("Skipping file {0}. Only zip, jar and litemod files can be added.", file.getName()))
                                .setType(DialogManager.ERROR).show();
                        continue;
                    }

                    if (this.disabled) {
                        copyTo = dialog.instanceV2 != null
                                ? dialog.instanceV2.getRoot().resolve("disabledmods").toFile()
                                : dialog.instance.getDisabledModsDirectory();
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

                    try {
                        long murmurHash = Hashing.murmur(file.toPath());

                        LogManager.debug("File " + file.getName() + " has murmur hash of " + murmurHash);

                        CurseFingerprint fingerprintResponse = CurseApi.checkFingerprint(murmurHash);

                        if (fingerprintResponse.exactMatches.size() == 1) {
                            CurseFingerprintedMod foundMod = fingerprintResponse.exactMatches.get(0);

                            // add Curse information
                            mod.curseMod = CurseApi.getModById(foundMod.id);
                            mod.curseModId = foundMod.id;
                            mod.curseFile = foundMod.file;
                            mod.curseFileId = foundMod.file.id;

                            mod.name = mod.curseMod.name;
                            mod.description = mod.curseMod.summary;

                            LogManager.debug("Found matching mod from CurseForge called " + mod.curseMod.name
                                    + " with file named " + mod.curseFile.displayName);
                        }
                    } catch (IOException e1) {
                        LogManager.logStackTrace(e1);
                    }

                    if (!copyTo.exists()) {
                        copyTo.mkdirs();
                    }

                    if (Utils.copyFile(file, copyTo)) {
                        if (dialog.instanceV2 != null) {
                            dialog.instanceV2.launcher.mods.add(mod);
                        } else {
                            dialog.instance.getInstalledMods().add(mod);
                        }
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
