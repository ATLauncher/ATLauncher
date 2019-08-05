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

import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.curse.pack.CurseManifest;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;

import org.zeroturnaround.zip.ZipUtil;

@SuppressWarnings("serial")
public class CursePackTransferHandler extends TransferHandler {
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
            if (data.size() != 1) {
                return false;
            }

            File file = data.get(0);

            if (!file.getName().endsWith(".zip")) {
                LogManager.error("Cannot install as the file was not a zip file");
                return false;
            }

            try {
                CurseManifest manifest = Gsons.MINECRAFT
                        .fromJson(new String(ZipUtil.unpackEntry(file, "manifest.json")), CurseManifest.class);

                if (!manifest.manifestType.equals("minecraftModpack")) {
                    LogManager.error("Cannot install as the manifest is not a Minecraft Modpack");
                    return false;
                }

                if (manifest.manifestVersion != 1) {
                    LogManager.error("Cannot install as the manifest is version " + manifest.manifestVersion
                            + " which I cannot install");
                    return false;
                }

                new InstanceInstallerDialog(manifest, file);
            } catch (Exception e) {
                LogManager.logStackTrace("Failed to install Curse pack from drag and drop", e);
                return false;
            }

            return true;

        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
