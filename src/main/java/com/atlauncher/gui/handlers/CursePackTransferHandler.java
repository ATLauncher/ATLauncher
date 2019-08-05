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

import com.atlauncher.utils.CursePackUtils;

@SuppressWarnings("serial")
public class CursePackTransferHandler extends TransferHandler {
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public boolean canImport(TransferSupport ts) {
        return ts.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                || ts.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @SuppressWarnings("unchecked")
    public boolean importData(TransferSupport ts) {
        try {
            if (ts.getTransferable().isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return CursePackUtils.loadFromUrl((String) ts.getTransferable().getTransferData(DataFlavor.stringFlavor));
            } else if (ts.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return CursePackUtils.loadFromFile(
                        ((List<File>) ts.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)).get(0));
            }
        } catch (UnsupportedFlavorException | IOException e) {
            return false;
        }

        return false;
    }
}
