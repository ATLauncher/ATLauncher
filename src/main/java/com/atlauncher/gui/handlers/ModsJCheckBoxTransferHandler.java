/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Type;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.FileTypeDialog;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

@SuppressWarnings("serial")
public class ModsJCheckBoxTransferHandler extends TransferHandler {
    private EditModsDialog dialog;
    private boolean disabled;

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
            List<File> data = (List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (data.size() < 1) {
                return false;
            }

            for (Object item : data) {
                File file = (File) item;

                boolean usesCoreMods = false;
                try {
                    usesCoreMods = App.settings.getMinecraftVersion(dialog.instanceV2 != null ? dialog.instanceV2.id
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

                FileTypeDialog fcd = new FileTypeDialog(GetText.tr("Add Mod"),
                        GetText.tr("Adding Mod {0}", file.getName()), GetText.tr("Add"), GetText.tr("Type"), modTypes);
                String typeTemp = fcd.getSelectorValue();

                Type type;
                File instanceFile;

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
                    instanceFile = dialog.instanceV2 != null
                            ? dialog.instanceV2.getRoot().resolve("texturepacks").toFile()
                            : dialog.instance.getTexturePacksDirectory();
                } else if (typeTemp.equalsIgnoreCase("Resource Pack")) {
                    type = Type.resourcepack;
                    instanceFile = dialog.instanceV2 != null
                            ? dialog.instanceV2.getRoot().resolve("resourcepacks").toFile()
                            : dialog.instance.getResourcePacksDirectory();
                } else if (typeTemp.equalsIgnoreCase("Shader Pack")) {
                    type = Type.shaderpack;
                    instanceFile = dialog.instanceV2 != null
                            ? dialog.instanceV2.getRoot().resolve("shaderpacks").toFile()
                            : dialog.instance.getShaderPacksDirectory();
                } else {
                    type = Type.mods;
                    instanceFile = dialog.instanceV2 != null ? dialog.instanceV2.getRoot().resolve("mods").toFile()
                            : dialog.instance.getModsDirectory();
                }

                if (this.disabled) {
                    instanceFile = dialog.instanceV2 != null
                            ? dialog.instanceV2.getRoot().resolve("disabledmods").toFile()
                            : dialog.instance.getDisabledModsDirectory();
                }

                DisableableMod mod = null;

                MCMod mcMod = Utils.getMCModForFile(file);
                if (mcMod != null) {
                    mod = new DisableableMod(mcMod.name, mcMod.version, true, file.getName(), type, null,
                            mcMod.description, this.disabled, true);
                } else {
                    FabricMod fabricMod = Utils.getFabricModForFile(file);
                    if (fabricMod != null) {
                        mod = new DisableableMod(fabricMod.name, fabricMod.version, true, file.getName(), type, null,
                                fabricMod.description, this.disabled, true);
                    }
                }

                if (mod == null) {
                    mod = new DisableableMod(file.getName(), "Custom", true, file.getName(), type, null, null,
                            this.disabled, true);
                }

                if (Utils.copyFile(file, instanceFile)) {
                    if (dialog.instanceV2 != null) {
                        dialog.instanceV2.launcher.mods.add(mod);
                    } else {
                        dialog.instance.getInstalledMods().add(mod);
                    }
                }
            }

            dialog.reloadPanels();
            return true;

        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
