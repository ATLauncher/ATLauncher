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
package com.atlauncher.gui.dialogs;

import javax.swing.SwingUtilities;

import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;

public class DialogException extends Exception {
    private DialogManager dialog;
    private String link;

    public DialogException(DialogManager dialog, String message) {
        this(dialog, message, null);
    }

    public DialogException(DialogManager dialog, String message, String link) {
        super(message);

        this.dialog = dialog;
        this.link = link;
    }

    public void showDialog() {
        SwingUtilities.invokeLater(() -> {
            dialog.show();

            if (link != null) {
                OS.openWebBrowser(link);
            }
        });
    }
}
